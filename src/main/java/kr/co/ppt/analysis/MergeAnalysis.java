package kr.co.ppt.analysis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import kr.co.ppt.R.Dtree;
import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.dictionary.TfidfVO;
import kr.co.ppt.morp.FileMorpVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.util.Tool;

public class MergeAnalysis implements Analysis{
	// MongoDB
	private JSONObject posJson;
	private JSONObject negJson;
	private JSONArray prodicArr;
	private JSONArray stockArr;
	private JSONArray treeArr = null;
	private Map<String,Double> tfidfMap;
	private double incScore=0;
	private double decScore=0;
	private double equScore=0;
	private int success = 0;
	private int predictCnt=0;

	public MergeAnalysis(JSONObject posJson, JSONObject negJson, JSONArray prodicArr, Map<String,Double> tfidfMap) {
		this.posJson = posJson;
		this.negJson = negJson;
		this.prodicArr = prodicArr;
		this.tfidfMap = tfidfMap;
	}
	
	public MergeAnalysis(JSONObject posJson, JSONObject negJson, JSONArray prodicArr, 
			JSONArray stockArr, Map<String,Double> tfidfMap) {
		this.posJson = posJson;
		this.negJson = negJson;
		this.prodicArr = prodicArr;
		this.stockArr = stockArr;
		this.tfidfMap = tfidfMap;
	}
	
	@Override
	public String trainAnalyze(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
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
				if(posJson.containsKey(key) && tfidfMap.containsKey(key))
					equalTerm.put(key,tfidfMap.get(key));
				else if(negJson.containsKey(key) && tfidfMap.containsKey(key))
					equalTerm.put(key,tfidfMap.get(key));
			}
			
			for (int i = 0; i < prodicArr.size(); i++) {
				JSONObject prodic = (JSONObject) prodicArr.get(i);
				String key = (String) prodic.get("word");
				if (equalTerm.containsKey(key)) {
					incScore += (Double.parseDouble((String) prodic.get("inc")) * equalTerm.get(key) / 10);
					decScore += (Double.parseDouble((String) prodic.get("dec")) * equalTerm.get(key) / 10);
					equScore += (Double.parseDouble((String) prodic.get("equ")) * equalTerm.get(key) / 10);
				}
			}
			return predict(predicDate);
		}else{
			return "";
		}
	}
	@Override
	public String realtimeAnalyze(FileMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		List<FileMorpVO> morpList = Tool.mergeVO(morpVO);
		Set<String> NewsMorpSet = new HashSet<String>();
		for (FileMorpVO morp : morpList) {
			NewsMorpSet.addAll(morp.getBegin().keySet());
			NewsMorpSet.addAll(morp.getPrev().keySet());
			NewsMorpSet.addAll(new NewsMorpVO(
					"D:\\PPT\\mining\\" + morp.getCategory() + Tool.getDate(morp.getNewsDate(), -1) + ".json")
							.getAppend().keySet());
		}
		Map<String, Double> equalTerm = new HashMap<String, Double>();
		Iterator<String> iter = NewsMorpSet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (posJson.containsKey(key) && tfidfMap.containsKey(key))
				equalTerm.put(key, tfidfMap.get(key));
			else if (negJson.containsKey(key) && tfidfMap.containsKey(key))
				equalTerm.put(key, tfidfMap.get(key));
		}

		for (int i = 0; i < prodicArr.size(); i++) {
			JSONObject prodic = (JSONObject) prodicArr.get(i);
			String key = (String) prodic.get("word");
			if (equalTerm.containsKey(key)) {
				incScore += (Double.parseDouble((String) prodic.get("inc")) * equalTerm.get(key) / 10);
				decScore += (Double.parseDouble((String) prodic.get("dec")) * equalTerm.get(key) / 10);
				equScore += (Double.parseDouble((String) prodic.get("equ")) * equalTerm.get(key) / 10);
			}
		}
		String flucState="";
		double total = incScore + decScore + equScore;
		if(treeArr == null){
			if (incScore > decScore )
				flucState="p";
			else if(incScore < decScore)
				flucState="m";
		}else{
			Dtree dTree = new Dtree();
			dTree.setDtree(treeArr);
			flucState = dTree.getDecision(incScore / total, decScore / total, equScore / total);
		}
		return flucState;
	}
	
	@Override
	public String userReqAnalyze(MorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String predicDate = Tool.getDate(sdf.format(new Date()), 1);
		Set<String> opinionKey = new HashSet<String>();
		Iterator<String> iter = morpVO.getMorp().keySet().iterator();
		Map<String, Double> equalTerm = new HashMap<String, Double>();
		while(iter.hasNext()){
			String key = iter.next();
			if(posJson.containsKey(key) && tfidfMap.containsKey(key))
				equalTerm.put(key,tfidfMap.get(key));
			else if(negJson.containsKey(key) && tfidfMap.containsKey(key))
				equalTerm.put(key,tfidfMap.get(key));
		}
		
		for (int i = 0; i < prodicArr.size(); i++) {
			JSONObject prodic = (JSONObject) prodicArr.get(i);
			String key = (String) prodic.get("word");
			if (equalTerm.containsKey(key)) {
				incScore += (Double.parseDouble((String) prodic.get("inc")) * equalTerm.get(key) / 10);
				decScore += (Double.parseDouble((String) prodic.get("dec")) * equalTerm.get(key) / 10);
				equScore += (Double.parseDouble((String) prodic.get("equ")) * equalTerm.get(key) / 10);
			}
		}
		String flucState="";
		double total = incScore + decScore + equScore;
		if(treeArr == null){
			if (incScore > decScore )
				flucState="p";
			else if(incScore < decScore)
				flucState="m";
		}else{
			Dtree dTree = new Dtree();
			dTree.setDtree(treeArr);
			flucState = dTree.getDecision(incScore / total, decScore / total, equScore / total);
		}
		return flucState;
	}
	
	@Override
	public String predict(String predicDate){
		String flucState="";
		for (int i = 0; i < stockArr.size(); i++) {
			JSONObject stock = (JSONObject) stockArr.get(i);
			if(stock.get("date").equals(predicDate)){
				flucState = ((String)stock.get("raise")).substring(0,1);
				break;
			}
		}
		
		double total = incScore + decScore + equScore;
		if(treeArr == null){
			if ((incScore > decScore && flucState.equals("p"))
					|| (incScore < decScore && flucState.equals("m"))) {
				success++;
			}
		}else{
			Dtree dTree = new Dtree();
			dTree.setDtree(treeArr);
			if(flucState.equals(dTree.getDecision(incScore / total, decScore / total, equScore / total)))
				success++;
		}
		predictCnt++;
		String result = String.valueOf(incScore / total) 
						+ "," + String.valueOf(decScore / total)
						+ "," + String.valueOf(equScore / total)
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

	@Override
	public void setTreeArr(JSONArray treeArr) {
		this.treeArr = treeArr;
	}
}
