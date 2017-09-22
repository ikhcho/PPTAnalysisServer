package kr.co.ppt.server.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import kr.co.ppt.analysis.Analysis;
import kr.co.ppt.analysis.FilteredAnalysis;
import kr.co.ppt.analysis.MergeAnalysis;
import kr.co.ppt.analysis.OpiAnalysis;
import kr.co.ppt.analysis.OpiAnalysis2;
import kr.co.ppt.analysis.ProAnalysis;
import kr.co.ppt.analysis.RTAVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.server.dao.AnalysisDAO;
import kr.co.ppt.stock.CompanyVO;

@Service
public class AnalysisService {
	@Autowired
	StockService sService;
	
	@Autowired
	DictionaryService dService;
	
	@Autowired
	DtreeService dTreeService;
	
	@Autowired
	AnalysisDAO aDAO;
	
	public static Map<String,Double> fit = new HashMap<>();
	public static Map<String,Double> meg = new HashMap<>();
	public static Map<String,Map<String,String[]>> threshold = new HashMap<>();
	private static final Resource RESOURCE = new ClassPathResource("/");
	private static final String[] newsCodes = {"culture","digital"};
	static{
		for(String newsCode : newsCodes){
			try {
				FileReader fr = new FileReader(RESOURCE.getURI().getPath().substring(1)+newsCode+"_tfidf.csv");
				BufferedReader br = new BufferedReader(fr);
				String data = "";
				String text = br.readLine();
				Map<String,String[]> map = new HashMap<>();
				while ((text = br.readLine()) != null) {
					map.put(text.split(",")[0], text.split(","));
				}
				threshold.put(newsCode, map);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private Map<String,Double> getThreshold(String comName, String newsCode, String anaCode){
		int from = 1;
		int to = 2;
		switch(anaCode){
		case "fit2":
			from += 2;
			to += 2;
			break;
		case "meg1":
			from += 4;
			to += 4;
			break;
		case "meg2":
			from += 6;
			to += 6;
			break;
		}
		return dService.selectTFIDFMongo(newsCode,
				Double.parseDouble(threshold.get(newsCode).get(comName)[from]),
				Double.parseDouble(threshold.get(newsCode).get(comName)[to])
				);
	}
	
	public String trainAnalyze(String comName, String newsCode, String anaCode, String[] dateRange, boolean make){
		long start = System.currentTimeMillis();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		JSONArray stockArr = sService.selectStock(comName);
		List<String> csv = new ArrayList<String>();
		switch(anaCode){
			case "opi1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis(posJson,negJson,stockArr);
				csv.add("opi1Inc,opi1Dec,result");
				break;
			case "opi2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis2(posJson,negJson,stockArr );
				csv.add("opi2Inc,opi2Dec,result");
				break;
			case "pro1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new ProAnalysis(prodicArr,stockArr);
				csv.add("pro1Inc,pro1Dec,pro1Equ,result");
				break;
			case "pro2":
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				analysis = new ProAnalysis(prodicArr,stockArr);
				csv.add("pro2Inc,pro2Dec,pro1Equ,result");
				break;
			case "fit1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new FilteredAnalysis(prodicArr,stockArr,fit);
				csv.add("fit1Inc,fit1Dec,fit1Equ,result");
				break;
			case "fit2":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new FilteredAnalysis(prodicArr,stockArr,fit);
				csv.add("fit2Inc,fit2Dec,fit2Equ,result");
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,meg);
				csv.add("meg1Inc,meg1Dec,meg1Equ,result");
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,meg);
				csv.add("meg2Inc,meg2Dec,meg2Equ,result");
				break;
		}
		
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			String predict = analysis.trainAnalyze(morpVO);
			if(!predict.equals(""))
				csv.add(predict);
		}
		if(make)
			makeCSV(comName,newsCode,anaCode,csv);
		long end = System.currentTimeMillis();
		System.out.println("MongDB - " + anaCode + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		
		return String.valueOf(analysis.getSuccess()*100 / analysis.getPredictCnt());
	}
	
	public JSONArray realtimeAnalyze(String predicDate, String newsCode){
		JSONArray array = new JSONArray();
		String[] anaCodes = {"opi1","opi2","pro1","pro2","fit1","fit2","meg1","meg2"};
		List<CompanyVO> list = sService.selectComList();
		for(CompanyVO companyVO : list){
			Map<String,String> map = new HashMap<>();
			String comName = companyVO.getName();
			Analysis analysis = null;
			JSONObject posJson = dService.selectOpiDicMongo(comName, "pos", "economic");;
			JSONObject negJson = dService.selectOpiDicMongo(comName, "neg", "economic");
			JSONArray prodicArr = dService.selectProDicMongo(comName, "economic");
			for(String anaCode : anaCodes){
				switch(anaCode){
					case "opi1":
						analysis = new OpiAnalysis(posJson,negJson);
						break;
					case "opi2":
						analysis = new OpiAnalysis2(posJson,negJson);
						break;
					case "pro1":
						analysis = new ProAnalysis(prodicArr);
						break;
					case "pro2":
						analysis = new ProAnalysis(prodicArr);
						break;
					case "fit1":
						analysis = new FilteredAnalysis(prodicArr,getThreshold(comName,newsCode,anaCode));
						break;
					case "fit2":
						analysis = new FilteredAnalysis(prodicArr,getThreshold(comName,newsCode,anaCode));
						break;
					case "meg1":
						analysis = new MergeAnalysis(posJson,negJson,prodicArr,getThreshold(comName,newsCode,anaCode));
						break;
					case "meg2":
						analysis = new MergeAnalysis(posJson,negJson,prodicArr,getThreshold(comName,newsCode,anaCode));
						break;
				}
				
				//JSONArray treeArr = dTreeService.selectDtree(comName, "economic", anaCode);
				//analysis.setTreeArr(treeArr);
				NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+predicDate+".json");
				map.put("comNo", String.valueOf(companyVO.getNo()));
				map.put("comName", comName);
				map.put("anaCode", anaCode);
				map.put("newsCode", newsCode);
				map.put("todayFluc", analysis.todayAnalyze(morpVO));
				map.put("tomorrowFluc", analysis.tomorrowAnalyze(morpVO));
				JSONObject obj = new JSONObject(map);
				array.add(obj);
				System.out.println(map.toString());
			}
		}
		System.out.println("RTA 끝");
		return array;
	}
	
	public String dTreeAnalyze(String comName, String newsCode, String anaCode, String[] dateRange){
		long start = System.currentTimeMillis();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		Map<String,Double> tfidfMap = null;
		JSONArray stockArr = sService.selectStock(comName);
		JSONArray treeArr = dTreeService.selectDtree(comName, newsCode, anaCode);
		switch(anaCode){
			case "opi1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis(posJson,negJson,stockArr);
				break;
			case "opi2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis2(posJson,negJson,stockArr);
				break;
			case "pro1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new ProAnalysis(prodicArr,stockArr);
				break;
			case "pro2":
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				analysis = new ProAnalysis(prodicArr,stockArr);
				break;
			case "fit1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new FilteredAnalysis(prodicArr,stockArr,fit);
				break;
			case "fit2":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new FilteredAnalysis(prodicArr,stockArr,fit);
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,meg);
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,meg);
				break;
		}
		analysis.setTreeArr(treeArr);
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			String predict = analysis.trainAnalyze(morpVO);
		}
		long end = System.currentTimeMillis();
		System.out.println("MongDB - " + anaCode + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		
		return String.valueOf(analysis.getSuccess()*100 / analysis.getPredictCnt());
	}
	
	public String analyze(MorpVO morpVO, String comName, String newsCode, String anaCode){
		long start = System.currentTimeMillis();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		Map<String,Double> tfidfMap = null;
		JSONArray stockArr = sService.selectStock(comName);
		switch(anaCode){
			case "opi1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis(posJson,negJson,stockArr );
				break;
			case "opi2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis2(posJson,negJson,stockArr );
				break;
			case "pro1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new ProAnalysis(prodicArr,stockArr);
				break;
			case "pro2":
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				analysis = new ProAnalysis(prodicArr,stockArr);
				break;
			case "fit1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3.9, 6.1);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				break;
			case "fit2":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3.9, 6.1);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 4.1, 6.5);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 4.1, 6.5);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				break;
		}
		long end = System.currentTimeMillis();
		System.out.println(anaCode + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		return comName + "의 " + anaCode+ "analysis 수행 시간 : "+(end-start)/1000 + "s" + " : " + analysis.userReqAnalyze(morpVO);
	}
	
	public int getTfidfThreshold(String comName, String newsCode, String anaCode, String[] dateRange, double from, double to){
		long start = System.currentTimeMillis();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		Map<String,Double> tfidfMap = null;
		JSONArray stockArr = sService.selectStock(comName);
		switch(anaCode){
			case "fit1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, from, to);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				break;
			case "fit2":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, from, to);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, from, to);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, from, to);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				break;
		}
		
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			String predict = analysis.trainAnalyze(morpVO);
		}
		long end = System.currentTimeMillis();
		System.out.println("MongDB - " + anaCode + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		
		return analysis.getSuccess()*100 / analysis.getPredictCnt();
	}
	public void makeCSV(String comName, String newsCode, String anaCode, List<String> csv){
		String path = "D:\\PPT\\analysis\\"+newsCode+"\\"+comName+"_"+anaCode+".csv";
		FileOutputStream fos;
		try {
			System.out.println("시작");
			fos = new FileOutputStream(path);
			for(String text : csv){
				fos.write((text+"\n").getBytes("utf-8"));
			}
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void insertRTA(JSONArray rtArr){
		for(int i=0; i<rtArr.size(); i++){
			JSONObject obj = (JSONObject)rtArr.get(i);
			Map<Object,Object> map = new HashMap<>();
			map.put("comNo", obj.get("comNo"));
			map.put("comName", obj.get("comName"));
			map.put("anaCode", obj.get("anaCode"));
			map.put("newsCode", obj.get("newsCode"));
			map.put("todayFluc", obj.get("todayFluc"));
			map.put("tomorrowFluc", obj.get("tomorrowFluc"));
			aDAO.insertRTA(map);
		}
	}
	
	public void updateRTA(JSONArray rtArr){
		for(int i=0; i<rtArr.size(); i++){
			JSONObject obj = (JSONObject)rtArr.get(i);
			Map<Object,Object> map = new HashMap<>();
			map.put("comNo", obj.get("comNo"));
			map.put("comName", obj.get("comName"));
			map.put("anaCode", obj.get("anaCode"));
			map.put("newsCode", obj.get("newsCode"));
			map.put("todayFluc", obj.get("todayFluc"));
			map.put("tomorrowFluc", obj.get("tomorrowFluc"));
			aDAO.updateRTA(map);
		}
	}
	
	public JSONArray selectOneRTA(String comName){
		JSONArray arr = new JSONArray();
		for(RTAVO rta : aDAO.selectOneRTA(comName)){
			Map<Object,Object> map = new HashMap<>();
			map.put("no", rta.getNo());
			map.put("comName", rta.getComName());
			map.put("anaCode", rta.getAnaCode());
			map.put("newsCode", rta.getNewsCode());
			map.put("todayFluc", rta.getTodayFluc());
			map.put("tomorrowFluc", rta.getTomorrowFluc());
			map.put("regDate", rta.getRegDate());
			JSONObject obj = new JSONObject(map);
			arr.add(obj);
		}
		return arr;
	}
	
	public JSONArray selectAllRTA(){
		JSONArray arr = new JSONArray();
		for(RTAVO rta : aDAO.selectAllRTA()){
			Map<Object,Object> map = new HashMap<>();
			map.put("no", rta.getNo());
			map.put("comName", rta.getComName());
			map.put("anaCode", rta.getAnaCode());
			map.put("newsCode", rta.getNewsCode());
			map.put("todayFluc", rta.getTodayFluc());
			map.put("tomorrowFluc", rta.getTomorrowFluc());
			map.put("regDate", rta.getRegDate());
			JSONObject obj = new JSONObject(map);
			arr.add(obj);
		}
		return arr;
	}
}
