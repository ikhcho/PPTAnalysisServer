package kr.co.ppt.server.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.ppt.crawler.CrawlerThread;
import kr.co.ppt.crawler.DaumNewsDom;
import kr.co.ppt.crawler.NewsCategoryVO;
import kr.co.ppt.server.dao.CrawlerDAO;
import kr.co.ppt.util.Tool;

@Service
public class CrawlerSerivce {
	@Autowired
	CrawlerDAO cDAO;
	@Autowired
	AnalysisService aService;
	@Autowired
	DictionaryService dService;
	
	public void batch(String newsCode){
		NewsCategoryVO.getTabMap();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		boolean ended = false;
		String yesterday = sdf.format(new Date());
		while(true){
			String today = sdf.format(new Date());
			LocalTime currentTime = LocalTime.now();
			
			//날짜변경
			if(!yesterday.equals(today)){
				try {
					Thread.sleep(1000*60*60);//1시간 대기
					//오늘 예측 결과를 어제 예측결과로 저장
					aService.updateYesterdayRTA(newsCode);
					// 나만의 분석 예측
					aService.updateYesterdayMyAnalysis(newsCode);
					
					//뉴스 카운트 변경
					long count = 0;
					count = getNewsCnt(newsCode,Tool.getDate(today, -1)) + cDAO.selectTotalNewsCnt(newsCode);
					Map<String, Object> map = new HashMap<>();
					map = new HashMap<>();
					map.put("newsCode", newsCode);
					map.put("total", count);
					cDAO.updateTotalNewsCnt(map);
					
					yesterday = today;
					ended = false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				CrawlerThread crawler = new CrawlerThread(NewsCategoryVO.getTabMap().get(newsCode), today,1);
				crawler.start();
				crawler.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//크롤링 뉴스 개수
			Map<String, Object> map = new HashMap<>();
			map.put("newsCode", newsCode);
			map.put("today", getNewsCnt(newsCode,today));
			cDAO.updateNewsCnt(map);
			
			if (!ended) {
				// 실시간 예측정보 업데이트
				aService.updateRTA(aService.realtimeAnalyzeWithFile(today, newsCode));
				// 나만의 분석 예측
				List<JSONObject> list = dService.selectMyDic(newsCode);
				for(JSONObject obj : list){
					int userNo = (int)obj.get("userNo");
					String dicName = (String)obj.get("dicName");
					String comName = (String)obj.get("comName");
					String anaCode = (String)obj.get("anaCode");
					JSONArray userDic = (JSONArray)obj.get("userDic");
					JSONObject myObj = aService.myAnalyzeWithFile(today, comName, newsCode, anaCode, userDic);
					myObj.put("userNo", userNo);
					myObj.put("dicName", dicName);
					if(aService.selectOneMyAnalysis(userNo,dicName) == null)
						aService.insertMyAnalysis(myObj);
					else
						aService.updateMyAnalysis(myObj);
				}
				//newsCode로 
				if ((currentTime.getHour() > 16) || (currentTime.getHour() == 15 && currentTime.getMinute() > 30)) {
					ended = true;// 장마감
				}
			}
			
		}
	}
	
	public long getNewsCnt(String newsCode, String date){
		long count = 0;
		try {
			FileReader fr = new FileReader("D:\\PPT\\news\\" + newsCode + date + ".json");
			BufferedReader br = new BufferedReader(fr);
			String text;
			String data = "{\"" + newsCode + "\":[";
			while ((text = br.readLine()) != null) {
				data += text;
			}
			data += "]}";
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
			JSONArray daily = (JSONArray) jsonObject.get(newsCode);
			count += daily.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(newsCode + " : " + count + "개");
		return count;
	}
	
	public void insertTotalNewsCnt(String newsCode){
		Map<String, Object> map = new HashMap<>();
		map.put("newsCode", newsCode);
		map.put("total", getTotalNewsCnt(newsCode));
		cDAO.insertTotalNewsCnt(map);
	}
	
	public long getTotalNewsCnt(String newsCode){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String today = sdf.format(new Date());
		String[] dateRange = Tool.dateRange("20160101", Tool.getDate(today, -1));
		long count = 0;
		label: for (int i = 0; i < dateRange.length; i++) {
			try {
				FileReader fr = new FileReader("D:\\PPT\\news\\" + newsCode + dateRange[i] + ".json");
				BufferedReader br = new BufferedReader(fr);
				String text;
				String data = "{\"" + newsCode + "\":[";
				while ((text = br.readLine()) != null) {
					data += text;
				}
				data += "]}";
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
				JSONArray daily = (JSONArray) jsonObject.get(newsCode);
				count += daily.size();
			} catch (Exception e) {
				e.printStackTrace();
				break label;
			}
		}
		System.out.println(newsCode + " : " + count + "개");
		return count;
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
			Set<String> titleSet = new HashSet<>();
			for(int i=0; i<hrefList.size(); i++){
				DaumNewsDom news = new DaumNewsDom();
				news.setDom(Jsoup.connect(hrefList.get(i)).get());
				Map<String,String> map = new HashMap<>();
				if(titleSet.isEmpty()){
					titleSet.add(news.getTitle());
				}else if(titleSet.contains(news.getTitle())){
					System.out.println("중복 : " + news.getTitle());
					continue;
				}else{
					System.out.println(titleSet.toString());
					map.put("newsCode", newsCode);
					map.put("title", news.getTitle());
					map.put("link", hrefList.get(i));
					JSONObject obj = new JSONObject(map);
					arr.add(obj);
					titleSet.add(news.getTitle());
					System.out.println("추가 : " + news.getTitle());
				}
				if(arr.size() == num)
					break;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return arr;
	}
}
