package kr.co.ppt.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class KospiVO {
	private static Document KOSPIDOM;
	private static List<Map<String,String>> symbols;
	private static List<String> comNames;
	private static List<String> comCodes;
	
	static{
		KOSPIDOM=null;
		try {
			KOSPIDOM = Jsoup.connect("http://www.klca.or.kr/sub/info/member_search.asp?rLink=COM").get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		symbols = new ArrayList<Map<String,String>>();
		comNames = new ArrayList<String>();
		comCodes = new ArrayList<String>();
		setSymbol();
	}
	
	private static void setSymbol(){
		Elements comList = KOSPIDOM.select(".list_ysc").select("li");
		for (Element e : comList) {
			String comName = e.text().replace(e.select("span").text(), "").trim();
			String comCode="";
			if(e.select("span").text().equals("준회원")){
				continue;
			}else{
				comCode = e.select("span").text().trim()+".ks";
				Map<String,String> symbol = new HashMap<String, String>();
				symbol.put(comName, comCode);
				symbols.add(symbol);
				comNames.add(comName);
				comCodes.add(comCode);
			}
		}
	}
	
	public List<Map<String,String>> getSymbols(){
		return symbols;
	}
	
	public List<String> getComNames(){
		return comNames;
	}

	public List<String> getComCodes(){
		return comCodes;
	}
	
	public String getSymbolCode(String key){
		for(Map<String,String> m : symbols){
			if(m.containsKey(key)){
				return m.get(key);
			}
		}
		return "정보를 찾을 수 없습니다.";
	}
	
	public List<String> getSymbolCode(String[] keys){
		List<String> res = new ArrayList<String>();
		for(String key : keys){
			for(Map<String,String> m : symbols){
				if(m.containsKey(key)){
					res.add(m.get(key));
				}
			}
		}
		return res;
	}
}
