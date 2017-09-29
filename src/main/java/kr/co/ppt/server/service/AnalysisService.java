package kr.co.ppt.server.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
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
	
	public static Map<String,Map<String,String[]>> threshold = new HashMap<>();
	private static final Resource RESOURCE = new ClassPathResource("/");
	private static final String[] newsCodes = {"culture","digital","economic","entertain","foreign","politics","society"};
	private Map<String,Map<String,Double>> tfidfMap = new HashMap<>();
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
	
	private Map<String,Double> getTfidfMap(String comName, String newsCode, String anaCode){
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
		String key = newsCode+threshold.get(newsCode).get(comName)[from]+threshold.get(newsCode).get(comName)[to];
		
		if(!tfidfMap.containsKey(key)){
			Map map = dService.selectTFIDFMongo(newsCode,
					Double.parseDouble(threshold.get(newsCode).get(comName)[from]),
					Double.parseDouble(threshold.get(newsCode).get(comName)[to])
					);
			tfidfMap.put(key, map);
			System.out.println("tfidfMap 추가 : "+comName + " - " + anaCode);
			return map;
			
		}else{
			return tfidfMap.get(key);
		}
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
				analysis = new FilteredAnalysis(prodicArr,stockArr,getTfidfMap(comName,newsCode,anaCode));
				csv.add("fit1Inc,fit1Dec,fit1Equ,result");
				break;
			case "fit2":
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				analysis = new FilteredAnalysis(prodicArr,stockArr,getTfidfMap(comName,newsCode,anaCode));
				csv.add("fit2Inc,fit2Dec,fit2Equ,result");
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,getTfidfMap(comName,newsCode,anaCode));
				csv.add("meg1Inc,meg1Dec,meg1Equ,result");
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,getTfidfMap(comName,newsCode,anaCode));
				csv.add("meg2Inc,meg2Dec,meg2Equ,result");
				break;
		}
		
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			String predict = analysis.trainAnalyze(morpVO);
			if(!predict.equals("")&&!predict.contains("NaN"))
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
		NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+predicDate+".json");
		for(CompanyVO companyVO : list){
			String comName = companyVO.getName();
			Analysis analysis = null;
			JSONObject posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);;
			JSONObject negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
			JSONArray prodicArr = dService.selectProDicMongo(comName, newsCode);
			JSONArray pro2dicArr = dService.selectPro2DicMongo(comName, newsCode);
			for(String anaCode : anaCodes){
				Map<String,String> map = new HashMap<>();
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
						analysis = new ProAnalysis(pro2dicArr);
						break;
					case "fit1":
						analysis = new FilteredAnalysis(prodicArr,getTfidfMap(comName,newsCode,anaCode));
						break;
					case "fit2":
						analysis = new FilteredAnalysis(pro2dicArr,getTfidfMap(comName,newsCode,anaCode));
						break;
					case "meg1":
						analysis = new MergeAnalysis(posJson,negJson,prodicArr,getTfidfMap(comName,newsCode,anaCode));
						break;
					case "meg2":
						analysis = new MergeAnalysis(posJson,negJson,pro2dicArr,getTfidfMap(comName,newsCode,anaCode));
						break;
				}
				
				JSONArray treeArr = dTreeService.selectDtree(comName, newsCode, anaCode);
				analysis.setTreeArr(treeArr);
				
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
	
	public JSONArray realtimeAnalyzeWithFile(String predicDate, String newsCode){
		JSONArray array = new JSONArray();
		String[] anaCodes = {"opi1","opi2","pro1","pro2","fit1","fit2","meg1","meg2"};
		List<CompanyVO> list = sService.selectComList();
		NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+predicDate+".json");
		for(CompanyVO companyVO : list){
			String comName = companyVO.getName();
			Analysis analysis = null;
			JSONObject posJson = new OpiDicVO(newsCode, comName, "pos").getOpiDic();
			JSONObject negJson = new OpiDicVO(newsCode, comName, "neg").getOpiDic();
			JSONArray prodicArr = new ProDicVO(newsCode, comName).getProdicArr();
			JSONArray pro2dicArr = new ProDicVO(newsCode, comName+"2").getProdicArr();
			for(String anaCode : anaCodes){
				Map<String,String> map = new HashMap<>();
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
						analysis = new ProAnalysis(pro2dicArr);
						break;
					case "fit1":
						analysis = new FilteredAnalysis(prodicArr,getTfidfMap(comName,newsCode,anaCode));
						break;
					case "fit2":
						analysis = new FilteredAnalysis(pro2dicArr,getTfidfMap(comName,newsCode,anaCode));
						break;
					case "meg1":
						analysis = new MergeAnalysis(posJson,negJson,prodicArr,getTfidfMap(comName,newsCode,anaCode));
						break;
					case "meg2":
						analysis = new MergeAnalysis(posJson,negJson,pro2dicArr,getTfidfMap(comName,newsCode,anaCode));
						break;
				}
				
				JSONArray treeArr = dTreeService.selectDtree(comName, newsCode, anaCode);
				analysis.setTreeArr(treeArr);
				
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
				analysis = new FilteredAnalysis(prodicArr,stockArr,getTfidfMap(comName,newsCode,anaCode));
				break;
			case "fit2":
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				analysis = new FilteredAnalysis(prodicArr,stockArr,getTfidfMap(comName,newsCode,anaCode));
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,getTfidfMap(comName,newsCode,anaCode));
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,getTfidfMap(comName,newsCode,anaCode));
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
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
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
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 4.1, 6.5);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				break;
		}
		long end = System.currentTimeMillis();
		System.out.println(anaCode + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		return comName + "의 " + anaCode+ "analysis 수행 시간 : "+(end-start)/1000 + "s" + " : " + analysis.userReqAnalyze(morpVO);
	}
	
	public String getTfidfThreshold(String comName, String newsCode, String[] dateRange){
		long start = System.currentTimeMillis();
		Analysis analysis = null;
		JSONObject posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
		JSONObject negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
		JSONArray prodicArr = dService.selectProDicMongo(comName, newsCode);
		JSONArray pro2dicArr = dService.selectPro2DicMongo(comName, newsCode);
		JSONArray stockArr = sService.selectStock(comName);
		
		double[] row = new double[12];
		
		double[][] thres = {
				{2.5,4.5},{3,5},{3.5,5.5},{4,6},{4.5,6.5}, //2
				{2.5,5.5},{3,6},{3.5,6.5},{4,7},{4.5,7.5}, //3
				{2.5,6.5},{3,7},{3.5,7.5},{4,8},{4.5,8.5}, //4
				{2.5,7.5},{3,8},{3.5,8.5},{4,9},{4.5,9.5}, //5
				};
		for (int i = 0; i < thres.length; i++) {
			Map<String, Double> tfidfMap = dService.selectTFIDFMongo(newsCode, thres[i][0], thres[i][1]);
			analysis = new FilteredAnalysis(prodicArr, stockArr, tfidfMap);
			int value = getPredicValue(analysis,dateRange,newsCode);
			if(value>row[0]){
				row[0] = value;
				row[1] = thres[i][0];
				row[2] = thres[i][1];
			}
			
			analysis = new FilteredAnalysis(pro2dicArr, stockArr, tfidfMap);
			value = getPredicValue(analysis,dateRange,newsCode);
			if(value>row[3]){
				row[3] = value;
				row[4] = thres[i][0];
				row[5] = thres[i][1];
			}
			
			analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
			value = getPredicValue(analysis,dateRange,newsCode);
			if(value>row[6]){
				row[6] = value;
				row[7] = thres[i][0];
				row[8] = thres[i][1];
			}
			
			analysis = new MergeAnalysis(posJson,negJson,pro2dicArr,stockArr,tfidfMap);
			value = getPredicValue(analysis,dateRange,newsCode);
			if(value>row[9]){
				row[9] = value;
				row[10] = thres[i][0];
				row[11] = thres[i][1];
			}
		}
		
		long end = System.currentTimeMillis();
		System.out.println("MongDB - " + comName + " 수행 시간 : "+(end-start)/1000 + "s");
		String result="";
		for(int i=0; i<4; i++){
			result += "," + row[i*3+1] + "," + row[i*3+2];
		}
		System.out.println(comName+result);
		return result+"\n";
	}
	
	public int getPredicValue(Analysis analysis, String[] dateRange, String newsCode){
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			String predict = analysis.trainAnalyze(morpVO);
		}
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
