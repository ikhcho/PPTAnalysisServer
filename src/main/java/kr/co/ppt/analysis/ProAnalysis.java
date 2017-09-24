package kr.co.ppt.analysis;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
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
	private List<String> csv = new ArrayList<String>();
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
	public void trainAnalyze(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		wordCnt=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO);
			for(NewsMorpVO morp: morpList){
				Map<String,Integer> map = morp.getBegin();
				map.putAll(morp.getAppend());
				map.putAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev());
				Iterator<String> iter = map.keySet().iterator();
				
				while(iter.hasNext()){
					String key = iter.next();
					for(ProDicVO prodic : proDicList){
						if(prodic.getTerm().equals(key)){
							incScore += prodic.getInc();
							decScore += prodic.getDec();
							equScore += prodic.getEqu();
							wordCnt++;
							break;
						}
					}
				}
			}
			predict(predicDate);
		}else{
			return;
		}
	}
	
	@Override
	public void trainAnalyzeWithMongo(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		wordCnt=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO);
			for(NewsMorpVO morp: morpList){
				Map<String,Integer> map = morp.getBegin();
				map.putAll(morp.getAppend());
				map.putAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev());
				Iterator<String> iter = map.keySet().iterator();
				
				//확률사전 
				Map<String, ProDicVO>dictionary = new HashMap<String, ProDicVO>();
				
				while(iter.hasNext()){
					String key = iter.next();
					for(int i=0; i<prodicArr.size(); i++){
						JSONObject prodic = (JSONObject)prodicArr.get(i);
						if(prodic.get("word").equals(key)){
							incScore += Double.parseDouble((String)prodic.get("inc"));
							decScore += Double.parseDouble((String)prodic.get("dec"));
							equScore += Double.parseDouble((String)prodic.get("equ"));
							wordCnt++;
							break;
						}
					}
				}
			}
			predict(predicDate);
		}else{
			return;
		}
	}

	@Override
	public void analyze() {
		// TODO Auto-generated method stub
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
		csv.add(result);
		return predicDate + "," + result;
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
	public String makeCSV(){
		String path = "D:\\PPT\\analysis\\pro1.csv";
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			fos.write("pro1Inc,pro1Dec,pro1Equ,result\n".getBytes("utf-8"));
			for(String text : csv){
				fos.write((text+"\n").getBytes("utf-8"));
			}
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
}
