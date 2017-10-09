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
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.util.Tool;

public class ProAnalysis implements Analysis{
	// MongoDB
	private JSONArray prodicArr;
	private JSONArray stockArr;
	private JSONArray treeArr = null;
	private double incScore=0;
	private double decScore=0;
	private double equScore=0;
	private int success = 0;
	private int predictCnt=0;
	private JSONArray userDic = null;
	
	public ProAnalysis(JSONArray prodicArr) {
		this.prodicArr = prodicArr;
	}
	
	public ProAnalysis(JSONArray prodicArr, JSONArray stockArr) {
		this.prodicArr = prodicArr;
		this.stockArr = stockArr;
		this.treeArr = treeArr;
	}

	private void analyze(Set<String> NewsMorpSet){
		if(userDic != null){
			Set<String> tmpSet = new HashSet<String>();
			for(int i=0; i<userDic.size(); i++){
				JSONObject userTerm = (JSONObject) userDic.get(i);
				String key = (String) userTerm.get("term");
				if (NewsMorpSet.contains(key)) {
					tmpSet.add(key);
				}
			}
			NewsMorpSet = tmpSet;
		}
		for (int i = 0; i < prodicArr.size(); i++) {
			JSONObject prodic = (JSONObject) prodicArr.get(i);
			String key = (String) prodic.get("word");
			if (NewsMorpSet.contains(key)) {
				incScore += Double.parseDouble((String) prodic.get("inc"));
				decScore += Double.parseDouble((String) prodic.get("dec"));
				equScore += Double.parseDouble((String) prodic.get("equ"));
			}
		}
	}
	
	@Override
	public String trainAnalyze(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO,1,true);
			Set<String> NewsMorpSet = new HashSet<String>();
			for(NewsMorpVO morp: morpList){
				NewsMorpSet.addAll(morp.getBegin().keySet());
				NewsMorpSet.addAll(morp.getAppend().keySet());
				NewsMorpSet.addAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev().keySet());
			}
			analyze(NewsMorpSet);
			String flucState="";
			for (int i = 0; i < stockArr.size(); i++) {
				JSONObject stock = (JSONObject) stockArr.get(i);
				if(stock.get("date").equals(predicDate)){
					flucState = ((String)stock.get("raise")).substring(0,1);
					break;
				}
			}
			double total = incScore + decScore + equScore;
			if(flucState.equals(predict()))
				success++;
			predictCnt++;
			
			String result = String.valueOf(incScore / total) 
							+ "," + String.valueOf(decScore / total)
							+ "," + String.valueOf(equScore / total)
							+ ","+flucState;
			return result;
		}else{
			return "";
		}
	}
	
	@Override
	public String todayAnalyze(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		List<NewsMorpVO> morpList = Tool.mergeVO(morpVO,-1,false);
		Set<String> NewsMorpSet = new HashSet<String>();
		for (NewsMorpVO morp : morpList) {
			NewsMorpSet.addAll(morp.getBegin().keySet());
			NewsMorpSet.addAll(morp.getPrev().keySet());
			NewsMorpSet.addAll(new NewsMorpVO(
					"D:\\PPT\\mining\\" + morp.getCategory() + Tool.getDate(morp.getNewsDate(), -1) + ".json")
							.getAppend().keySet());
		}
		analyze(NewsMorpSet);
		return predict();
	}
	
	@Override
	public String tomorrowAnalyze(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		Set<String> NewsMorpSet = new HashSet<String>();
		NewsMorpSet.addAll(morpVO.getBegin().keySet());
		if(NewsMorpSet.isEmpty()){
			NewsMorpSet.addAll(morpVO.getPrev().keySet());
		}
		analyze(NewsMorpSet);
		
		return predict();
	}

	@Override
	public String userReqAnalyze(MorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		analyze(morpVO.getMorp().keySet());
		return predict();
	}
	
	@Override
	public String predict(){
		String flucState="";
		double total = incScore + decScore + equScore;
		if(total == 0)
			return "x";
		if(treeArr == null){
			if (incScore > decScore)
				flucState = "p";
			else if(incScore < decScore)
				flucState = "m";
		}else{
			Dtree dTree = new Dtree();
			dTree.setDtree(treeArr);
			flucState = dTree.getDecision(incScore / total, decScore / total, equScore / total);
		}
		return flucState;
	}
	
	@Override
	public double getInc() {
		double total = incScore + decScore + equScore;
		if(total==0)
			return 0;
		else
			return incScore/total;
	}

	@Override
	public double getDec() {
		double total = incScore + decScore + equScore;
		if(total==0)
			return 0;
		else
			return decScore/total;
	}

	@Override
	public double getEqu() {
		double total = incScore + decScore + equScore;
		if(total==0)
			return 0;
		else
			return equScore/total;
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
	
	@Override
	public void setUserDic(JSONArray userDic) {
		this.userDic = userDic;
	}
}
