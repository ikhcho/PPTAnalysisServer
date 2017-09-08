package kr.co.ppt.analysis;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import kr.co.ppt.dictionary.TfidfVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.stock.StockVO;
import kr.co.ppt.util.Tool;

public class MergeAnalysis implements Analysis{
	private List<OpiDicVO> posList;
	private List<OpiDicVO> negList;
	private List<ProDicVO> proDicList;
	private JSONObject posJson;
	private JSONObject negJson;
	JSONArray prodicArr;
	private List<StockVO> stockList;
	private List<TfidfVO> tfidfList;
	private double incScore=0;
	private double decScore=0;
	private double equScore=0;
	private int success = 0;
	private int predictCnt=0;
	private List<String> csv = new ArrayList<String>();
	private int wordCnt=0;
	
	public MergeAnalysis(List<OpiDicVO> posList, List<OpiDicVO> negList, List<ProDicVO> proDicList,
			List<StockVO> stockList, List<TfidfVO> tfidfList) {
		super();
		this.posList = posList;
		this.negList = negList;
		this.proDicList = proDicList;
		this.stockList = stockList;
		this.tfidfList = tfidfList;
	}

	public MergeAnalysis(JSONObject posJson, JSONObject negJson, JSONArray prodicArr, 
			List<StockVO> stockList, List<TfidfVO> tfidfList) {
		this.posJson = posJson;
		this.negJson = negJson;
		this.prodicArr = prodicArr;
		this.stockList = stockList;
		this.tfidfList = tfidfList;
	}
	
	@Override
	public void trainAnalyze(NewsMorpVO morpVO) {
		incScore=0;
		decScore=0;
		equScore=0;
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO);
			for(NewsMorpVO morp: morpList){
				Set<String> opinionKey = new HashSet<String>();
				Map<String,Integer> map = morp.getBegin();
				map.putAll(morp.getAppend());
				map.putAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev());
				Iterator<String> iter = map.keySet().iterator();

				while(iter.hasNext()){
					String key = iter.next();
					for(OpiDicVO pos :posList){
						if(pos.getTerm().equals(key)){
							opinionKey.add(key);
							break;
						}
					}
					for(OpiDicVO neg :negList){
						if(neg.getTerm().equals(key)){
							opinionKey.add(key);
							break;
						}
					}
				}
				
				Iterator<String> opiKeyIter = opinionKey.iterator();
				while(opiKeyIter.hasNext()){
					String key = opiKeyIter.next();
					for(ProDicVO prodic : proDicList){
						if(prodic.getTerm().equals(key)){
							for(TfidfVO tfidfVO : tfidfList){
								if(tfidfVO.getTerm().equals(key)){
									incScore += (prodic.getInc() * tfidfVO.getTfidf() / 10);
									decScore += (prodic.getDec() * tfidfVO.getTfidf() / 10);
									equScore += (prodic.getEqu() * tfidfVO.getTfidf() / 10);
									wordCnt++;
									break;
								}
							}
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
		String predicDate = Tool.getDate(morpVO.getNewsDate(), 1);
		if(Tool.isOpen(predicDate)){
			List<NewsMorpVO> morpList = Tool.mergeVO(morpVO);
			for(NewsMorpVO morp: morpList){
				Set<String> opinionKey = new HashSet<String>();
				Map<String,Integer> map = morp.getBegin();
				map.putAll(morp.getAppend());
				map.putAll(new NewsMorpVO("D:\\PPT\\mining\\"+morp.getCategory()+Tool.getDate(morp.getNewsDate(), 1)+".json").getPrev());
				Iterator<String> iter = map.keySet().iterator();

				while(iter.hasNext()){
					String key = iter.next();
					if(posJson.containsKey(key))
						opinionKey.add(key);
					else if(negJson.containsKey(key))
						opinionKey.add(key);
				}
				
				Iterator<String> opiKeyIter = opinionKey.iterator();
				while(opiKeyIter.hasNext()){
					String key = opiKeyIter.next();
					for(int i=0; i<prodicArr.size(); i++){
						JSONObject prodic = (JSONObject)prodicArr.get(i);
						if(prodic.get("word").equals(key)){
							for(TfidfVO tfidfVO : tfidfList){
								if(tfidfVO.getTerm().equals(key)){
									incScore += (Double.parseDouble((String)prodic.get("inc")) * tfidfVO.getTfidf() / 10);
									decScore += (Double.parseDouble((String)prodic.get("dec")) * tfidfVO.getTfidf() / 10);
									equScore += (Double.parseDouble((String)prodic.get("equ")) * tfidfVO.getTfidf() / 10);
									wordCnt++;
									break;
								}
							}
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
		String path = "D:\\PPT\\analysis\\meg1.csv";
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			fos.write("meg1Inc,meg1Dec,meg1Equ,result\n".getBytes("utf-8"));
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
