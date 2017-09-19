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
import kr.co.ppt.morp.FileMorpVO;
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

	@Override
	public String trainAnalyze(NewsMorpVO morpVO) {
		posScore=0;
		negScore=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			//공휴일 및 주말과 같은 장이 시작되지 않은 날은 이전 자료를 가져온다.
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO);
			Set<String> NewsMorpSet = new HashSet<String>();
			for(NewsMorpVO morp: morpList){
				//D+1예측은 D_begin+D_append + (D+1)_prev 
				NewsMorpSet.addAll(morp.getBegin().keySet());
				NewsMorpSet.addAll(morp.getAppend().keySet());
				NewsMorpSet.addAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev().keySet());
			}
			Iterator<String> iter = NewsMorpSet.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				if (posJson.containsKey(key))
					posScore++;
				else if (negJson.containsKey(key))
					negScore++;
			}
			return predict(predicDate);
		}else{
			return "";
		}
	}
	
	@Override
	public String realtimeAnalyze(FileMorpVO morpVO) {
		posScore=0;
		negScore=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		// 공휴일 및 주말과 같은 장이 시작되지 않은 날은 이전 자료를 가져온다.
		List<FileMorpVO> morpList = Tool.mergeVO(morpVO);
		Set<String> NewsMorpSet = new HashSet<String>();
		for (FileMorpVO morp : morpList) {
			// D+1예측은 D_begin+D_append + (D+1)_prev
			NewsMorpSet.addAll(morp.getBegin().keySet());
			NewsMorpSet.addAll(morp.getAppend().keySet());
			NewsMorpSet.addAll(new NewsMorpVO(
					"D:\\PPT\\mining\\" + morp.getCategory() + Tool.getDate(morp.getNewsDate(), 1) + ".json").getPrev()
							.keySet());
		}
		Iterator<String> iter = NewsMorpSet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (posJson.containsKey(key))
				posScore++;
			else if (negJson.containsKey(key))
				negScore++;
		}
		String flucState="";
		double total = posScore + negScore;
		
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
	public String userReqAnalyze(MorpVO morpVO) {
		posScore=0;
		negScore=0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String predicDate = Tool.getDate(sdf.format(new Date()), 1);
		Iterator<String> iter = morpVO.getMorp().keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (posJson.containsKey(key))
				posScore++;
			else if (negJson.containsKey(key))
				negScore++;
		}
		String flucState="";
		double total = posScore + negScore;
		
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
	public String predict(String predicDate){
		String flucState="";
		for (int i = 0; i < stockArr.size(); i++) {
			JSONObject stock = (JSONObject) stockArr.get(i);
			if(stock.get("date").equals(predicDate)){
				flucState = ((String)stock.get("raise")).substring(0,1);
				break;
			}
		}
		
		double total = posScore + negScore;
		
		if(treeArr == null){
			if ((posScore > negScore && flucState.equals("p")) || (posScore < negScore &&flucState.equals("m"))) {
				success++;
			}
		}else{
			Dtree dTree = new Dtree();
			dTree.setDtree(treeArr);
			if(flucState.equals(dTree.getDecision(posScore / total, negScore / total, 0)))
				success++;
		}
		predictCnt++;
		
		String result = String.valueOf(posScore/total) + "," + String.valueOf(negScore/total) + ","+flucState;
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
