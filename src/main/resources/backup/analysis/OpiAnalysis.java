package kr.co.ppt.analysis;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class OpiAnalysis implements Analysis{
	private OpiDic opiDic;
	private int success = 0;
	private float posScore=0F;
	private float negScore=0F;
	private List<Map<String,String>> list;
	private List<NewsMorpVO> morpList;// 공휴일 및 주말과 같은 장이 시작되지 않은 날은 이전 자료를 가져온다.
	private int predictCnt=0;
	private String prediction;
	
	public OpiAnalysis(String comName) {
		opiDic = new OpiDic(comName);
	}
	
	public OpiAnalysis(StockVO stockVO) {
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
						posScore += posDic.getDicionary().get(key);
					}else if(negDic.getDicionary().containsKey(key)){
						negScore += negDic.getDicionary().get(key);
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
		if ((posScore > negScore && prediction.contains("p")) || (posScore < negScore && prediction.contains("m"))) {
			success++;
		}
		
		double total = posScore + negScore;
		prediction = predicDate + "," + String.valueOf(posScore/total) + "," + String.valueOf(negScore/total) + ","+prediction.substring(0,1);
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
