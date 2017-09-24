package kr.co.ppt.crawler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class NewsCategoryVO {
	
	public static Map<String,String> tabMap = new HashMap<String,String>();
	static{
		tabMap.put("사회", "http://media.daum.net/breakingnews/society?");
		tabMap.put("정치", "http://media.daum.net/breakingnews/politics?");
		tabMap.put("경제", "http://media.daum.net/breakingnews/economic?");
		tabMap.put("국제", "http://media.daum.net/breakingnews/foreign?");
		tabMap.put("문화", "http://media.daum.net/breakingnews/culture?");
		tabMap.put("연예", "http://media.daum.net/breakingnews/entertain?");
		tabMap.put("IT", "http://media.daum.net/breakingnews/digital?");
	}
	
	public static Map<String, String> getTabMap() {
		return tabMap;
	}
	
	public static String getURL(String key){
		
		return tabMap.get(key);
	}
	
}
