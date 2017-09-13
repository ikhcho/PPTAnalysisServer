package kr.co.ppt.dictionary;

import java.util.HashMap;
import java.util.Map;

public class TfidfVO {
	
	private String term;
	private double f;
	private double tf;
	private double df;
	private double idf;
	private double tfidf;
	
	
	public String getTerm() {
		return term;
	}


	public void setTerm(String term) {
		this.term = term;
	}


	public double getF() {
		return f;
	}


	public void setF(double f) {
		this.f = f;
	}


	public double getTf() {
		return tf;
	}


	public void setTf(double tf) {
		this.tf = tf;
	}


	public double getDf() {
		return df;
	}


	public void setDf(double df) {
		this.df = df;
	}


	public double getIdf() {
		return idf;
	}


	public void setIdf(double idf) {
		this.idf = idf;
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
