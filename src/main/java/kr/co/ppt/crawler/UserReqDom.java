package kr.co.ppt.crawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UserReqDom implements JsoupDom{

	private Document dom;
	
	public UserReqDom(){
		
	}

	public UserReqDom(String url){
		try{
			dom = Jsoup.connect(url).get();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//Getter, Setter
	public Document getDom() {
		return dom;
	}
	
	public void setDom(Document dom){
		this.dom = dom;
	}
	
	public String getContent(){
		Elements els = dom.select("body");
		String res="";
		for (Element e : els) {
			res=e.text();
		}
		return res;
	}

}
