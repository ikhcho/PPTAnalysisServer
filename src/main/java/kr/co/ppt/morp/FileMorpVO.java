package kr.co.ppt.morp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FileMorpVO {
	private String category;
	private String newsDate;
	private Map<String, Integer> prev;
	private Map<String, Integer> begin;
	private Map<String, Integer> append;
	
	//FileReader을 이용한 객체생성
	public FileMorpVO(String fileName){
		try{
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String text = "";
			String data = "";
			data="";
			while((text = br.readLine()) != null){
				data+= text;
			}
			br.close();
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
			setCategory((String)jsonObject.get("category"));
			setNewsDate((String)jsonObject.get("newsDate"));
			setWordMap(jsonObject,"prev");
			setWordMap(jsonObject,"begin");
			setWordMap(jsonObject,"append");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
	public void setWordMap(JSONObject parentObject,String key) {
		JSONArray childArray = (JSONArray) parentObject.get(key);
		JSONObject childObject = (JSONObject) childArray.get(0);
		if(key.equals("prev")){
			prev = mapCasting(childObject);
		}else if(key.equals("begin")){
			begin = mapCasting(childObject);
		}else{
			append = mapCasting(childObject);
		}
	}
	
	private Map<String,Integer> mapCasting(JSONObject obj){
		Map<String,Integer> map = new HashMap<String,Integer>();
		Iterator<String> iter = obj.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			Integer value = Integer.parseInt((String)obj.get(key));
			map.put(key, value);
		}
		return map;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getNewsDate() {
		return newsDate;
	}
	public void setNewsDate(String newsDate) {
		this.newsDate = newsDate;
	}
	public Map<String, Integer> getPrev() {
		return prev;
	}
	public void setPrev(Map<String, Integer> prev) {
		this.prev = prev;
	}
	public Map<String, Integer> getBegin() {
		return begin;
	}
	public void setBegin(Map<String, Integer> begin) {
		this.begin = begin;
	}
	public Map<String, Integer> getAppend() {
		return append;
	}
	public void setAppend(Map<String, Integer> append) {
		this.append = append;
	}

	@Override
	public String toString() {
		return "MorpVO [category=" + category + ", newsDate=" + newsDate + ", prev=" + prev + ", begin=" + begin
				+ ", append=" + append + "]";
	}
	
}
