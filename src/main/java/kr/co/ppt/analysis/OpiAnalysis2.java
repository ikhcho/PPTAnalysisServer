package kr.co.ppt.analysis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.util.Tool;

public class OpiAnalysis2 implements Analysis{
	//OracleDB
	private List<OpiDicVO> posList;
	private List<OpiDicVO> negList;
	// MongoDB
	private JSONObject posJson;
	private JSONObject negJson;
	private JSONArray stockArr;
	private int success = 0;
	private int posScore=0;
	private int negScore=0;
	private int predictCnt=0;
	
	public OpiAnalysis2(List<OpiDicVO> posList, List<OpiDicVO> negList, JSONArray stockArr) {
		super();
		this.posList = posList;
		this.negList = negList;
		this.stockArr = stockArr;
	}

	public OpiAnalysis2(JSONObject posJson, JSONObject negJson, JSONArray stockArr) {
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
				for (OpiDicVO pos : posList) {
					if (pos.getTerm().equals(key)) {
						posScore += pos.getWeight();
						break;
					}
				}
				for (OpiDicVO neg : negList) {
					if (neg.getTerm().equals(key)) {
						negScore += neg.getWeight();
						break;
					}
				}
			}
			return predict(predicDate);
		}else{
			return "";
		}
	}
	
	@Override
	public String trainAnalyzeWithMongo(NewsMorpVO morpVO) {
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
				if(posJson.containsKey(key))
					posScore += Float.parseFloat((String)posJson.get(key));
				else if(negJson.containsKey(key))
					negScore += Float.parseFloat((String)negJson.get(key));
			}
			return predict(predicDate);
		}else{
			return "";
		}
	}
	
	@Override
	public String analyze(MorpVO morpVO) {
		posScore=0;
		negScore=0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String predicDate = Tool.getDate(sdf.format(new Date()), 1);
		Iterator<String> iter = morpVO.getMorp().keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if(posJson.containsKey(key))
				posScore += Float.parseFloat((String)posJson.get(key));
			else if(negJson.containsKey(key))
				negScore += Float.parseFloat((String)negJson.get(key));
		}
		double total = posScore + negScore;
		String result = predicDate + " 예측 : " 
				+ String.valueOf(posScore/total) + "," 
				+ String.valueOf(negScore/total);
		return result;
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
		
		if ((posScore > negScore && flucState.equals("p")) || (posScore < negScore &&flucState.equals("m"))) {
			success++;
		}
		
		double total = posScore + negScore;
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

}
