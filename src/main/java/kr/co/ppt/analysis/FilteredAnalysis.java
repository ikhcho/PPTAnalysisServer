package kr.co.ppt.analysis;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.dictionary.TfidfVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.stock.StockVO;
import kr.co.ppt.util.Tool;

public class FilteredAnalysis implements Analysis{
	private List<ProDicVO> proDicList;
	JSONArray prodicArr;
	private List<StockVO> stockList;
	private List<TfidfVO> tfidfList;
	Map<String,Double> tfidfMap;
	private double incScore=0;
	private double decScore=0;
	private double equScore=0;
	private int success = 0;
	private int predictCnt=0;
	private int wordCnt=0;
	
	public FilteredAnalysis(List<ProDicVO> proDicList, List<StockVO> stockList, List<TfidfVO> tfidfList) {
		super();
		this.proDicList = proDicList;
		this.stockList = stockList;
		this.tfidfList = tfidfList;
	}
	
	public FilteredAnalysis(JSONArray prodicArr, List<StockVO> stockList, Map<String,Double> tfidfMap) {
		this.prodicArr = prodicArr;
		this.stockList = stockList;
		this.tfidfMap = tfidfMap;
	}
	@Override
	public String trainAnalyze(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		wordCnt=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO);
			Set<String> NewsMorpSet = new HashSet<String>();
			for(NewsMorpVO morp: morpList){
				NewsMorpSet.addAll(morp.getBegin().keySet());
				NewsMorpSet.addAll(morp.getAppend().keySet());
				NewsMorpSet.addAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev().keySet());
			}
			Map<String, Double> equalTerm = new HashMap<String, Double>();
			for (TfidfVO tfidfVO : tfidfList) {
				if (NewsMorpSet.contains(tfidfVO.getTerm()))
					equalTerm.put(tfidfVO.getTerm(), tfidfVO.getTfidf());
			}
			for (ProDicVO prodic : proDicList) {
				String key = (String) prodic.getTerm();
				if (equalTerm.containsKey(key)) {
					incScore += (prodic.getInc() * equalTerm.get(key) / 10);
					decScore += (prodic.getDec() * equalTerm.get(key) / 10);
					equScore += (prodic.getEqu() * equalTerm.get(key) / 10);
					wordCnt++;
				}
			}
			return predict(predicDate);
		}else{
			return "";
		}
	}
	
	
	@Override
	public String trainAnalyzeWithMongo(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		wordCnt=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO);
			Set<String> NewsMorpSet = new HashSet<String>();
			for(NewsMorpVO morp: morpList){
				NewsMorpSet.addAll(morp.getBegin().keySet());
				NewsMorpSet.addAll(morp.getAppend().keySet());
				NewsMorpSet.addAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev().keySet());
			}
			Map<String, Double> equalTerm = new HashMap<String, Double>();
			Iterator<String> iter = NewsMorpSet.iterator();
			while(iter.hasNext()){
				String key = iter.next();
				if(tfidfMap.containsKey(key))
					equalTerm.put(key,tfidfMap.get(key));
			}
			for (int i = 0; i < prodicArr.size(); i++) {
				JSONObject prodic = (JSONObject) prodicArr.get(i);
				String key = (String) prodic.get("word");
				if (equalTerm.containsKey(key)) {
					incScore += (Double.parseDouble((String) prodic.get("inc")) * equalTerm.get(key) / 10);
					decScore += (Double.parseDouble((String) prodic.get("dec")) * equalTerm.get(key) / 10);
					equScore += (Double.parseDouble((String) prodic.get("equ")) * equalTerm.get(key) / 10);
					wordCnt++;
				}
			}
			return predict(predicDate);
		}else{
			return "";
		}
		
	}

	@Override
	public String analyze(MorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		wordCnt=0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String predicDate = Tool.getDate(sdf.format(new Date()), 1);
		Map<String, Double> equalTerm = new HashMap<String, Double>();
		Iterator<String> iter = morpVO.getMorp().keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			if(tfidfMap.containsKey(key))
				equalTerm.put(key,tfidfMap.get(key));
		}
		for (int i = 0; i < prodicArr.size(); i++) {
			JSONObject prodic = (JSONObject) prodicArr.get(i);
			String key = (String) prodic.get("word");
			if (equalTerm.containsKey(key)) {
				incScore += (Double.parseDouble((String) prodic.get("inc")) * equalTerm.get(key) / 10);
				decScore += (Double.parseDouble((String) prodic.get("dec")) * equalTerm.get(key) / 10);
				equScore += (Double.parseDouble((String) prodic.get("equ")) * equalTerm.get(key) / 10);
				wordCnt++;
			}
		}
		String result = predicDate + " 예측 : " 
				+ String.valueOf(incScore / wordCnt) 
				+ "," + String.valueOf(decScore / wordCnt)
				+ "," + String.valueOf(equScore / wordCnt);
		return result;
	}
	
	@Override
	public String predict(String predicDate){
		String flucState="";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		for(StockVO stockVO: stockList){
			if(sdf.format(stockVO.getOpenDate()).equals(predicDate)){
				flucState = stockVO.getFlucState();
				break;
			}
		}
		if ((incScore / wordCnt > decScore / wordCnt && flucState.equals("p"))
				|| (incScore / wordCnt < decScore / wordCnt && flucState.equals("m"))) {
			success++;
		}
		predictCnt++;
		
		String result = String.valueOf(incScore / wordCnt) 
						+ "," + String.valueOf(decScore / wordCnt)
						+ "," + String.valueOf(equScore / wordCnt)
						+ ","+flucState;
		return result;
	}
	
	@Override
	public int getSuccess() {
		return success;
	}

	@Override
	public int getPredictCnt() {
		return predictCnt;
	}

}
