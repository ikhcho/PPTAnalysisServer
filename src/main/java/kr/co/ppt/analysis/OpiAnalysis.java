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
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.util.Tool;

public class OpiAnalysis implements Analysis{
	//MongoDB
	private JSONObject posJson;
	private JSONObject negJson;
	private JSONArray stockArr;
	private JSONArray treeArr = null;
	private int success = 0;
	private int posScore=0;
	private int negScore=0;
	private int predictCnt=0;
	
	public OpiAnalysis(JSONObject posJson, JSONObject negJson) {
		this.posJson = posJson;
		this.negJson = negJson;
		
	}
	
	public OpiAnalysis(JSONObject posJson, JSONObject negJson, JSONArray stockArr) {
		this.posJson = posJson;
		this.negJson = negJson;
		this.stockArr = stockArr;
	}
	
	private void analyze(Iterator<String> iter){
		while (iter.hasNext()) {
			String key = iter.next();
			if (posJson.containsKey(key))
				posScore++;
			else if (negJson.containsKey(key))
				negScore++;
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
			Iterator<String> iter = NewsMorpSet.iterator();
			analyze(iter);
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
		Iterator<String> iter = NewsMorpSet.iterator();
		analyze(iter);
		
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
		Iterator<String> iter = NewsMorpSet.iterator();
		analyze(iter);
		
		return predict();
	}
	
	@Override
	public String userReqAnalyze(MorpVO morpVO) {
		posScore=0;
		negScore=0;
		Iterator<String> iter = morpVO.getMorp().keySet().iterator();
		analyze(iter);
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
