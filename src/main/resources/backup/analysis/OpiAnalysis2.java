package kr.co.ppt.analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import kr.co.ppt.dictionary.OpiDic;
import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.util.Tool;
import kr.co.ppt.yahoofinance.StockVO;

/* 
 * Time Series Analysis : 시계열 분석 기법
 * 1개 기업의 StockVO 와 1개의 카테고리에 해당하는 news 분석 MorpVO List
 * 
 * 
 * 
*/
public class OpiAnalysis2 implements Analysis{
	private OpiDic opiDic;
	private int success = 0;
	private int posScore=0;
	private int negScore=0;
	private List<Map<String,String>> list;
	private List<NewsMorpVO> morpList;// 공휴일 및 주말과 같은 장이 시작되지 않은 날은 이전 자료를 가져온다.
	private int predictCnt=0;
	private String prediction;
	
	public OpiAnalysis2(String comName) {
		opiDic = new OpiDic(comName);
	}
	
	public OpiAnalysis2(StockVO stockVO) {
		opiDic = new OpiDic(stockVO.getName());
		this.list = stockVO.getQuote();
	}

	@Override
	public void analyze(NewsMorpVO morpVO) {
		prediction="";
		if(Tool.isOpen(Tool.getDate(morpVO.getNewsDate(), 1))){
			morpList = Tool.mergeVO(morpVO);
			for(NewsMorpVO morp: morpList){
				OpiDicVO posDic = opiDic.getPosDic();
				OpiDicVO negDic = opiDic.getNegDic();
				
				Map<String,Integer> map = morp.getBegin();
				map.putAll(morp.getAppend());
				map.putAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev());
				Iterator<String> iter = map.keySet().iterator();
				
				while(iter.hasNext()){
					String key = iter.next();
					if(posDic.getDicionary().containsKey(key)){
						posScore ++;
					}else if(negDic.getDicionary().containsKey(key)){
						negScore ++;
					}
				}
			}
			predict(Tool.getDate(morpVO.getNewsDate(), 1));
		}else{
			return;
		}
	}
	
	@Override
	public void predict(String predicDate){
		for (Map<String,String> quote : list) {
			if(quote.get("date").equals(predicDate)){
				prediction = quote.get("raise");
				break;
			}
		}
		/*System.out.print("Opinion Analysis =>> ");
		System.out.print(predicDate + " 예측 : ");
		System.out.print("긍정치 : " + posScore);
		System.out.print(", 부정치 : " + negScore);
		System.out.print(", 주가 : " + predict);*/
		if ((posScore > negScore && prediction.contains("p")) || (posScore < negScore && prediction.contains("m"))) {
			//System.out.println("예측성공");
			success++;
		} else{
			//System.out.println("예측실패");
		}
		double total = posScore + negScore;
		prediction = String.valueOf((double)posScore/total) + "," + String.valueOf((double)negScore/total) + ","+prediction.substring(0,1);
		predictCnt++;
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
	public String getPrediction() {
		return prediction;
	}
}
