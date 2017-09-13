package kr.co.ppt.analysis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.util.Tool;

public class ProAnalysis implements Analysis{
	//OracleDB
	private List<ProDicVO> proDicList;
	// MongoDB
	private JSONArray prodicArr;
	private JSONArray stockArr;
	private double incScore=0;
	private double decScore=0;
	private double equScore=0;
	private int success = 0;
	private int predictCnt=0;
	
	public ProAnalysis(List<ProDicVO> proDicList, JSONArray stockArr) {
		this.proDicList = proDicList;
		this.stockArr = stockArr;
	}
	
	public ProAnalysis(JSONArray prodicArr, JSONArray stockArr) {
		this.prodicArr = prodicArr;
		this.stockArr = stockArr;
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
			Iterator<String> iter = NewsMorpSet.iterator();

			while (iter.hasNext()) {
				String key = iter.next();
				for (ProDicVO prodic : proDicList) {
					if (prodic.getTerm().equals(key)) {
						incScore += prodic.getInc();
						decScore += prodic.getDec();
						equScore += prodic.getEqu();
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
			for (int i = 0; i < prodicArr.size(); i++) {
				JSONObject prodic = (JSONObject) prodicArr.get(i);
				String key = (String) prodic.get("word");
				if (NewsMorpSet.contains(key)) {
					incScore += Double.parseDouble((String) prodic.get("inc"));
					decScore += Double.parseDouble((String) prodic.get("dec"));
					equScore += Double.parseDouble((String) prodic.get("equ"));
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String predicDate = Tool.getDate(sdf.format(new Date()), 1);

		for (int i = 0; i < prodicArr.size(); i++) {
			JSONObject prodic = (JSONObject) prodicArr.get(i);
			String key = (String) prodic.get("word");
			if (morpVO.getMorp().containsKey(key)) {
				incScore += Double.parseDouble((String) prodic.get("inc"));
				decScore += Double.parseDouble((String) prodic.get("dec"));
				equScore += Double.parseDouble((String) prodic.get("equ"));
			}
		}
		double total = incScore + decScore + equScore;
		String result = predicDate + " 예측 : " 
				+ String.valueOf(incScore / total) 
				+ "," + String.valueOf(decScore / total)
				+ "," + String.valueOf(equScore / total);
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
		double total = incScore + decScore + equScore;
		if ((incScore > decScore  && flucState.equals("p"))
				|| (incScore < decScore && flucState.equals("m"))) {
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

}
