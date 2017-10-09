package kr.co.ppt.analysis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import kr.co.ppt.R.Dtree;
import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.util.Tool;

public class OpiAnalysis2 implements Analysis{
	// MongoDB
	private JSONObject posJson;
	private JSONObject negJson;
	private JSONArray stockArr;
	private JSONArray treeArr = null;
	private int success = 0;
	private int posScore=0;
	private int negScore=0;
	private int predictCnt=0;
	private JSONArray userDic = null;
	
	public OpiAnalysis2(JSONObject posJson, JSONObject negJson) {
		this.posJson = posJson;
		this.negJson = negJson;
		
	}
	
	public OpiAnalysis2(JSONObject posJson, JSONObject negJson, JSONArray stockArr) {
		this.posJson = posJson;
		this.negJson = negJson;
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
		Iterator<String> iter = NewsMorpSet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if(posJson.containsKey(key))
				posScore += Float.parseFloat((String)posJson.get(key));
			else if(negJson.containsKey(key))
				negScore += Float.parseFloat((String)negJson.get(key));
		}
	}
	
	@Override
	public String trainAnalyze(NewsMorpVO morpVO) {
		posScore=0;
		negScore=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			//공휴일 및 주말과 같은 장이 시작되지 않은 날은 이전 자료를 가져온다.
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO,1,true);
			Set<String> NewsMorpSet = new HashSet<String>();
			for(NewsMorpVO morp: morpList){
				//D+1예측은 D_begin+D_append + (D+1)_prev 
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
			
			double total = posScore + negScore;
			if(flucState.equals(predict()))
				success++;
			predictCnt++;
			
			String result = String.valueOf(posScore/total) + "," + String.valueOf(negScore/total) + ","+flucState;
			return result;
		}else{
			return "";
		}
	}
	
	@Override
	public String todayAnalyze(NewsMorpVO morpVO) {
		posScore=0;
		negScore=0;
		// 공휴일 및 주말과 같은 장이 시작되지 않은 날은 이전 자료를 가져온다.
		List<NewsMorpVO> morpList = Tool.mergeVO(morpVO,-1,false);
		Set<String> NewsMorpSet = new HashSet<String>();
		for (NewsMorpVO morp : morpList) {
			// D+1예측은 D_begin+D_append + (D+1)_prev
			NewsMorpSet.addAll(morp.getBegin().keySet());
			NewsMorpSet.addAll(morp.getPrev().keySet());
			NewsMorpSet.addAll(new NewsMorpVO(
					"D:\\PPT\\mining\\" + morp.getCategory() + Tool.getDate(morp.getNewsDate(), -1) + ".json").getAppend()
							.keySet());
		}
		analyze(NewsMorpSet);
		
		return predict();
	}
	@Override
	public String tomorrowAnalyze(NewsMorpVO morpVO) {
		posScore=0;
		negScore=0;
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
		posScore=0;
		negScore=0;
		analyze(morpVO.getMorp().keySet());
		return predict();
	}
	
	@Override
	public String predict(){
		String flucState="";
		double total = posScore + negScore;
		if(total == 0)
			return "x";
		if(treeArr == null){
			if (posScore > negScore)
				flucState="p";
			else if(posScore < negScore)
				flucState="m";
		}else{
			Dtree dTree = new Dtree();
			dTree.setDtree(treeArr);
			flucState = dTree.getDecision(posScore / total, negScore / total, 0);
		}
		return flucState;
	}
	
	@Override
	public double getInc() {
		double total = posScore + negScore;
		if(total==0)
			return 0;
		else
			return posScore/total;
	}

	@Override
	public double getDec() {
		double total = posScore + negScore;
		if(total==0)
			return 0;
		else
			return negScore/total;
	}

	@Override
	public double getEqu() {
		return 0;
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
