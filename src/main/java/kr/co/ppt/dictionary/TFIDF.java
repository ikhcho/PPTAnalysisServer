package kr.co.ppt.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.util.Tool;

public class TFIDF {
	private List<NewsMorpVO> morpList = new ArrayList<NewsMorpVO>();
	private static Map<String,Integer> dictionary = new HashMap<String,Integer>();
	private double D; //문서크기 - IDF 변수
	private Map<String, Double> tfidf = new HashMap<String, Double>(); //TF-IDF 결과값
	public List<String> termList = new ArrayList<>();	
	public List<Double> fList = new ArrayList<>();	
	public List<Double> tfList = new ArrayList<>();	
	public List<Double> dfList = new ArrayList<>();	
	public List<Double> idfList = new ArrayList<>();	
	public List<Double> tfidfList = new ArrayList<>();	
	
	public TFIDF(String category){
		String[] dateRange = Tool.dateRange("20160101","20170630");
		for(int i=0; i<dateRange.length; i++){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+category+dateRange[i]+".json");
			morpList.add(morpVO);
		}
		this.D = morpList.size()*3;
		setDictionary();
	}
	
	public TFIDF(List<NewsMorpVO> morpList){
		this.morpList = morpList;
		this.D = morpList.size()*3;
		setDictionary();
	}
	
	private void setDictionary(){
		List<Map<String,Integer>> list = new ArrayList();
		System.out.println("Merge morpVO ...");
		for(NewsMorpVO morpVO : morpList){
			list.add(morpVO.getPrev());
			list.add(morpVO.getBegin());
			list.add(morpVO.getAppend());
		}
		dictionary = Tool.mergeMap(list);
		System.out.println("Total word count : "+dictionary.size());
	}
	
	public Map<String, Double> getTfidf() {
		return tfidf;
	}
	
	public void setTFIDF(){
		System.out.println("Calcurating TF-IDF....");
		String maxKey = Tool.countSort(dictionary).get(0);
		int max = dictionary.get(maxKey);
		Iterator<String> iter = dictionary.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			double f = (double)dictionary.get(key);
			double df = (double)getDF(key);
			double idf = Math.log(1 + (D / df));
			double tf = Math.log(1 + f);
			termList.add(key);
			fList.add(f);
			tfList.add(tf);
			dfList.add(df);
			idfList.add(idf);
			tfidfList.add(tf * idf);
			tfidf.put(key, tf * idf);
		}
		System.out.println("Filtered Word : " + tfidf.size());
	}
	
	//Document Frequency
	private int getDF(String term){ 
		int count = 0;
		for(NewsMorpVO morpVO : morpList){
			if(morpVO.getPrev().containsKey(term))
				count++;
			if(morpVO.getBegin().containsKey(term))
				count++;
			if(morpVO.getAppend().containsKey(term))
				count++;
		}
		return count;
	}

}
