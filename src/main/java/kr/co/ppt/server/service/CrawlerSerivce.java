package kr.co.ppt.server.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.ppt.crawler.CrawlerThread;
import kr.co.ppt.crawler.DaumNewsDom;
import kr.co.ppt.crawler.NewsCategoryVO;
import kr.co.ppt.server.dao.CrawlerDAO;

@Service
public class CrawlerSerivce {

	@Autowired
	CrawlerDAO cDAO;
	@Autowired
	AnalysisService aService;
	
	public void batch(String newsCode){
		NewsCategoryVO.getTabMap();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		while(true){
			String today = sdf.format(new Date());
			CrawlerThread economic = new CrawlerThread(NewsCategoryVO.getTabMap().get(newsCode), today,1);
			economic.start();
			try {
				economic.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//aService.realtimeAnalyze(today);
		}
	}
	
	public JSONArray recentNews(){
		JSONArray arr = new JSONArray();
		try{
			Map<String, String> newsCodeMap = NewsCategoryVO.getTabMap();
			Iterator<String> newsCodeIter = newsCodeMap.keySet().iterator();
			while(newsCodeIter.hasNext()){
				String newsCode = newsCodeIter.next();
				DaumNewsDom daum = new DaumNewsDom();
				daum.setDom(Jsoup.connect(newsCodeMap.get(newsCode)).get());
				List<String> hrefList = daum.getHref();
				DaumNewsDom news = new DaumNewsDom();
				news.setDom(Jsoup.connect(hrefList.get(0)).get());
				Map<String,String> map = new HashMap<>();
				map.put("newsCode", newsCode);
				map.put("title", news.getTitle());
				map.put("link", hrefList.get(0));
				JSONObject obj = new JSONObject(map);
				arr.add(obj);
			}
			DaumNewsDom daum = new DaumNewsDom();
			daum.setDom(Jsoup.connect("http://media.daum.net/").get());
			Map<String,String> map = new HashMap<>();
			map.put("newsCode", "main");
			map.put("title", daum.getHeadTitle());
			map.put("link", daum.getHeadHref().get(0));
			JSONObject obj = new JSONObject(map);
			arr.add(obj);
		}catch(Exception e){
			e.printStackTrace();
		}
		return arr;
	}
	
	public JSONArray recentNews(String newsCode, int num){
		JSONArray arr = new JSONArray();
		String url = newsCode.equals("main")?"http://media.daum.net/":NewsCategoryVO.getTabMap().get(newsCode);
		try{
			DaumNewsDom daum = new DaumNewsDom();
			daum.setDom(Jsoup.connect(url).get());
			List<String> hrefList = newsCode.equals("main")?daum.getHeadHref():daum.getHref();
			for(int i=0; i<num; i++){
				DaumNewsDom news = new DaumNewsDom();
				news.setDom(Jsoup.connect(hrefList.get(i)).get());
				Map<String,String> map = new HashMap<>();
				map.put("newsCode", newsCode);
				map.put("title", news.getTitle());
				map.put("link", hrefList.get(i));
				JSONObject obj = new JSONObject(map);
				arr.add(obj);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return arr;
	}
}
