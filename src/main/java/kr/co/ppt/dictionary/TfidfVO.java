package kr.co.ppt.dictionary;

import java.util.HashMap;
import java.util.Map;

public class TfidfVO {
	
	private String term;
	private double tfidf;
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public double getTfidf() {
		return tfidf;
	}
	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\"");
		builder.append(term);
		builder.append("\" : ");
		builder.append(tfidf);
		return builder.toString();
	}
	
	
}
