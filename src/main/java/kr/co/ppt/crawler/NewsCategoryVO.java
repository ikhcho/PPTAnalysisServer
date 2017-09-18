package kr.co.ppt.crawler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class NewsCategoryVO {
	
	public static Map<String,String> tabMap = new HashMap<String,String>();
	static{
		tabMap.put("main", "http://media.daum.net/");
		tabMap.put("society", "http://media.daum.net/breakingnews/society");
		tabMap.put("politics", "http://media.daum.net/breakingnews/politics");
		tabMap.put("economic", "http://media.daum.net/breakingnews/economic");
		tabMap.put("foreign", "http://media.daum.net/breakingnews/foreign");
		tabMap.put("culture", "http://media.daum.net/breakingnews/culture");
		tabMap.put("entertain", "http://media.daum.net/breakingnews/entertain");
		tabMap.put("digital", "http://media.daum.net/breakingnews/digital");
	}
	
	public static Map<String, String> getTabMap() {
		return tabMap;
	}
	
	public static String getURL(String key){
		
		return tabMap.get(key);
	}
	
}
