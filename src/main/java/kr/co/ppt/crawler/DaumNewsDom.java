package kr.co.ppt.crawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DaumNewsDom implements JsoupDom{

	private Document dom;

	//Getter, Setter
	public Document getDom() {
		return dom;
	}
	
	public void setDom(Document dom){
		this.dom = dom;
	}
	
	//마지막 페이지는 li(컨텐츠)가 없다.
	public Boolean hasContent(){
		if(dom.select(".box_etc").select("ul").select("li").size()==0){
			return true;
		}else{
			return false;
		}
	}
	
	//페이지 내에 모든 URL크롤링
	public List<String> getHref() {
		List<String> list = new ArrayList<String>();
		
		Elements els = dom.select(".list_news2").select("a");
		for (Element e : els) {
			list.add(e.attr("href"));
		}
		return new ArrayList<String>(new HashSet<String>(list));
	}
	
	public String getWriteDate(){
		Elements els = dom.select("span[class=info_view]").select("span");
		String res="";
		if(els.size()==0){
			els = dom.select("span[class=info_data]").select("span");
			for (Element e : els) {
				if(e.is("span[class=data]")){
					res+=e.text();
					res+=" ";
				}
				if(e.is("span[class^=num]")){
					res+=e.text();
				}
			}
		}else{
			for (Element e : els) {
				if(e.text().contains("입력")){
					res=e.text();
				}
			}
		}
		return res;
	}
	
	public String getContent(){
		Elements els = dom.select("#harmonyContainer");
		String res="";
		String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
		for (Element e : els) {
			res=e.text().replaceAll(match, "");
		}
		return res;
	}

}
