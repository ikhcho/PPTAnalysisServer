package kr.co.ppt.server.service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.ppt.crawler.DaumNewsDom;
import kr.co.ppt.crawler.NewsCategoryVO;
import kr.co.ppt.server.dao.CrawlerDAO;

@Service
public class CrawlerSerivce {

	@Autowired
	CrawlerDAO cDAO;
	
	public static Map<String,String> recentNews = new HashMap<String,String>();
	
	public void craw(){
		Map<String, String> newsCodeMap = NewsCategoryVO.getTabMap();
		Iterator<String> newsCodeIter = newsCodeMap.keySet().iterator();
		while(newsCodeIter.hasNext()){
			String newsCode = newsCodeIter.next();
			//daum news page document
			int page=1;
			try{
				String content="";
				label:
				while(true){
					//origin news document
					DaumNewsDom daum = new DaumNewsDom();
					daum.setDom(Jsoup.connect(newsCodeMap.get(newsCode)+"?page="+page).get());
					if(daum.hasContent()){//기사 마지막 체크
						break;
					}else{
						List<String> hrefList = daum.getHref();
						for (String href : hrefList) {
							try {
								DaumNewsDom news = new DaumNewsDom();
								news.setDom(Jsoup.connect(href).get());
								content="";
								if(news.getTitle().equals(recentNews.get(newsCode))){
									recentNews.replace(newsCode, news.getTitle());//최근뉴스로 교체
									break label;
								}
								//DB저장
								if(news.getContent().equals("")){
									continue;
								}else{
									content += "{\"newsCode\" : \"" + newsCode 
											+ "\", \"newsDate\" : \"" + news.getWriteDate().split(" ")[1].replaceAll("\\.", "")
											+ "\", \"time\" : \"" + news.getWriteDate().split(" ")[2]
											+ "\", \"link\" : \"" + href
											+ "\", \"title\" : \"" + news.getTitle()
											+ "\", \"content\" : \"" + news.getContent() + "\"}";
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								System.out.println("=====에러URL=====");
								System.out.println(href);
								System.out.println(e1.getMessage());
								continue;
							}
						}
						page++;
					}
				}
			}catch(Exception e2){
				e2.printStackTrace();
			}
		}
		//
	}
	
	public String recentNews(){
		String result ="{[";
		try{
			
			Map<String, String> newsCodeMap = NewsCategoryVO.getTabMap();
			Iterator<String> newsCodeIter = newsCodeMap.keySet().iterator();
			while(newsCodeIter.hasNext()){
				String newsCode = newsCodeIter.next();
				DaumNewsDom daum = new DaumNewsDom();
				daum.setDom(Jsoup.connect(newsCodeMap.get(newsCode)).get());
				if(newsCode.equals("main")){
					result += "{\"newsCode\" : \"" + newsCode + "\"";
					result += ", \"title\" : \"" + daum.getHeadTitle() + "\"";
					result += ", \"link\" : \"" + daum.getHeadHref().get(0) + "\"},";
				}else{
					List<String> hrefList = daum.getHref();
					DaumNewsDom news = new DaumNewsDom();
					news.setDom(Jsoup.connect(hrefList.get(0)).get());
					result += "{\"newsCode\" : \"" + newsCode + "\"";
					result += ", \"title\" : \"" + news.getTitle() + "\"";
					result += ", \"link\" : \"" + hrefList.get(0) + "\"},";
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		result = result.substring(0, result.length()-1);
		result += "]}";
		return result;
	}
	
	public String recentNews(String newsCode, int num){
		String url = NewsCategoryVO.getTabMap().get(newsCode);
		
		String result ="{[";
		try{
			DaumNewsDom daum = new DaumNewsDom();
			daum.setDom(Jsoup.connect(url).get());
			List<String> hrefList = newsCode.equals("main")?daum.getHeadHref():daum.getHref();
			for(int i=0; i<num; i++){
				DaumNewsDom news = new DaumNewsDom();
				news.setDom(Jsoup.connect(hrefList.get(i)).get());
				result += "{\"newsCode\" : \"" + newsCode + "\"";
				result += ", \"title\" : \"" + news.getTitle() + "\"";
				result += ", \"link\" : \"" + hrefList.get(i) + "\"},";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		result = result.substring(0, result.length()-1);
		result += "]}";
		return result;
	}
}
