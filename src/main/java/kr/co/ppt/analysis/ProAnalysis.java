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

import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.stock.StockVO;
import kr.co.ppt.util.Tool;

public class ProAnalysis implements Analysis{
	private List<ProDicVO> proDicList;
	JSONArray prodicArr;
	private List<StockVO> stockList;
	private double incScore=0;
	private double decScore=0;
	private double equScore=0;
	private int success = 0;
	private int predictCnt=0;
	private int wordCnt=0;
	
	public ProAnalysis(List<ProDicVO> proDicList, List<StockVO> stockList) {
		this.proDicList = proDicList;
		this.stockList = stockList;
	}
	
	public ProAnalysis(JSONArray prodicArr, List<StockVO> stockList) {
		this.prodicArr = prodicArr;
		this.stockList = stockList;
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
			Iterator<String> iter = NewsMorpSet.iterator();

			while (iter.hasNext()) {
				String key = iter.next();
				for (ProDicVO prodic : proDicList) {
					if (prodic.getTerm().equals(key)) {
						incScore += prodic.getInc();
						decScore += prodic.getDec();
						equScore += prodic.getEqu();
						wordCnt++;
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
			for (int i = 0; i < prodicArr.size(); i++) {
				JSONObject prodic = (JSONObject) prodicArr.get(i);
				String key = (String) prodic.get("word");
				if (NewsMorpSet.contains(key)) {
					incScore += Double.parseDouble((String) prodic.get("inc"));
					decScore += Double.parseDouble((String) prodic.get("dec"));
					equScore += Double.parseDouble((String) prodic.get("equ"));
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

		for (int i = 0; i < prodicArr.size(); i++) {
			JSONObject prodic = (JSONObject) prodicArr.get(i);
			String key = (String) prodic.get("word");
			if (morpVO.getMorp().containsKey(key)) {
				incScore += Double.parseDouble((String) prodic.get("inc"));
				decScore += Double.parseDouble((String) prodic.get("dec"));
				equScore += Double.parseDouble((String) prodic.get("equ"));
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
