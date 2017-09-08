package kr.co.ppt.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.ppt.dictionary.OpiDic;
import kr.co.ppt.dictionary.ProDic;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.dictionary.TFIDF;
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
public class FilteredAnalysis implements Analysis{
	private ProDic proDic;
	private double incScore=0;
	private double decScore=0;
	private double equScore=0;
	private int cnt=0;
	private int predictCnt=0;
	private List<Map<String,String>> list;
	private TFIDF tfidf;
	private int success = 0;
	private List<NewsMorpVO> morpList;// 공휴일 및 주말과 같은 장이 시작되지 않은 날은 이전 자료를 가져온다.
	private String prediction;
	
	public FilteredAnalysis(StockVO stockVO, TFIDF tfidf) {
		proDic = new ProDic(stockVO.getName());
		this.list = stockVO.getQuote();
		this.tfidf = tfidf;
	}
	
	public FilteredAnalysis(String comName, TFIDF tfidf) {
		proDic = new ProDic(comName);
	}
	
	@Override
	public void analyze(NewsMorpVO morpVO) {
		prediction="";
		if(Tool.isOpen(Tool.getDate(morpVO.getNewsDate(), 1))){
			morpList = Tool.mergeVO(morpVO);
			for(NewsMorpVO morp: morpList){
				Map<String, ProDicVO>dictionary = new HashMap<String, ProDicVO>();
				for(ProDicVO proDicVO : proDic.getDictionary()){
					dictionary.put(proDicVO.getWord(), proDicVO);
				}
				Map<String,Integer> map = morp.getBegin();
				map.putAll(morp.getAppend());
				map.putAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev());
				Iterator<String> iter = map.keySet().iterator();
				
				while(iter.hasNext()){
					String key = iter.next();
					if(dictionary.containsKey(key) && tfidf.getTfidf().containsKey(key)){
						ProDicVO prodicVO = dictionary.get(key);//단어의 확률 모임
						incScore += (prodicVO.getInc() * tfidf.getTfidf().get(key) / 10);
						decScore += (prodicVO.getDec() * tfidf.getTfidf().get(key) / 10);
						equScore += (prodicVO.getEqu() * tfidf.getTfidf().get(key) / 10);
						cnt++;
						//System.out.println(key + " : " + "상승 -" + prodicVO.getInc() + ", 하락-" + prodicVO.getDec() + ", tfidf-" + tfidf.getTfidf().get(key));
					}else{
						 continue;
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
		/*System.out.print("Filtered Analysis =>> ");
		System.out.print(predicDate + " 예측 : ");
		System.out.print("상승확률 : " + incScore / cnt + "%");
		System.out.print(", 하락확률 : " + decScore / cnt + "%");
		System.out.print(", 동결확률 : " + equScore / cnt + "%");
		System.out.print(", 주가 : " + predict);*/
		if ((incScore / cnt > decScore / cnt && prediction.contains("p"))
				|| (incScore / cnt < decScore / cnt && prediction.contains("m"))) {
			//System.out.println("예측성공");
			success++;
		} else{
			//System.out.println("예측실패");
		}
		/*if (((decScore / cnt) < 91.2175 && prediction.contains("p")) || ((decScore / cnt) > 91.2175 && prediction.contains("m"))) {
			//System.out.println("예측성공");
			success++;
		} else{
			//System.out.println("예측실패");
		}*/
		prediction = String.valueOf(incScore / cnt) + "," + String.valueOf(decScore / cnt) + ","
				+ String.valueOf(equScore / cnt) + "," + prediction.substring(0, 1);
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
