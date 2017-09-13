package kr.co.ppt.crawler;

import org.jsoup.nodes.Document;

public interface JsoupDom {

	public void setDom(Document dom);
	public String getContent();
}
