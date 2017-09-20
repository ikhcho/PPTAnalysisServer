package kr.co.ppt.server.service;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.ppt.analysis.Analysis;
import kr.co.ppt.analysis.FilteredAnalysis;
import kr.co.ppt.analysis.MergeAnalysis;
import kr.co.ppt.analysis.OpiAnalysis;
import kr.co.ppt.analysis.OpiAnalysis2;
import kr.co.ppt.analysis.ProAnalysis;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.stock.CompanyVO;

@Service
public class AnalysisService {
	@Autowired
	StockService sService;
	
	@Autowired
	DictionaryService dService;
	
	@Autowired
	DtreeService dTreeService;
	
	public static Map<String,Double> fit = new HashMap<>();
	public static Map<String,Double> meg = new HashMap<>();
	
	public String trainAnalyze(String comName, String newsCode, String function, String[] dateRange, boolean make){
		long start = System.currentTimeMillis();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		JSONArray stockArr = sService.selectStock(comName);
		List<String> csv = new ArrayList<String>();
		switch(function){
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
			makeCSV(comName,function,csv);
		long end = System.currentTimeMillis();
		System.out.println("MongDB - " + function + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		
		return String.valueOf(analysis.getSuccess()*100 / analysis.getPredictCnt());
	}
	
	public JSONArray realtimeAnalyze(String predicDate, String newsCode){
		JSONArray array = new JSONArray();
		String[] functions = {"opi1","opi2","pro1","pro2","fit1","fit2","meg1","meg2"};
		List<CompanyVO> list = sService.selectComList();
		for(CompanyVO companyVO : list){
			Map<String,String> map = new HashMap<>();
			String comName = companyVO.getName();
			Analysis analysis = null;
			JSONObject posJson = dService.selectOpiDicMongo(comName, "pos", "economic");;
			JSONObject negJson = dService.selectOpiDicMongo(comName, "neg", "economic");
			JSONArray prodicArr = dService.selectProDicMongo(comName, "economic");
			for(String function : functions){
				JSONArray treeArr = dTreeService.selectDtree(comName, "economic", function);
				switch(function){
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
						analysis = new FilteredAnalysis(prodicArr,fit);
						break;
					case "fit2":
						analysis = new FilteredAnalysis(prodicArr,fit);
						break;
					case "meg1":
						analysis = new MergeAnalysis(posJson,negJson,prodicArr,meg);
						break;
					case "meg2":
						analysis = new MergeAnalysis(posJson,negJson,prodicArr,meg);
						break;
				}
				
				analysis.setTreeArr(treeArr);
				NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+predicDate+".json");
				map.put("comNo", String.valueOf(companyVO.getNo()));
				map.put("comName", comName);
				map.put("anaCode", function);
				map.put("newsCode", newsCode);
				map.put("todayFluc", analysis.todayAnalyze(morpVO));
				map.put("tomorrowFluc", analysis.tomorrowAnalyze(morpVO));
				JSONObject obj = new JSONObject(map);
				array.add(obj);
				System.out.println(obj.toJSONString());
			}
			break;
		}
		return array;
	}
	
	public String dTreeAnalyze(String comName, String newsCode, String function, String[] dateRange){
		long start = System.currentTimeMillis();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		Map<String,Double> tfidfMap = null;
		JSONArray stockArr = sService.selectStock(comName);
		JSONArray treeArr = dTreeService.selectDtree(comName, newsCode, function);
		List<String> csv = new ArrayList<String>();
		switch(function){
			case "opi1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis(posJson,negJson,stockArr);
				csv.add("opi1Inc,opi1Dec,result");
				break;
			case "opi2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis2(posJson,negJson,stockArr);
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
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3.9, 6.1);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				csv.add("fit1Inc,fit1Dec,fit1Equ,result");
				break;
			case "fit2":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3.9, 6.1);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				csv.add("fit2Inc,fit2Dec,fit2Equ,result");
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 4.1, 6.5);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				csv.add("meg1Inc,meg1Dec,meg1Equ,result");
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 4.1, 6.5);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				csv.add("meg2Inc,meg2Dec,meg2Equ,result");
				break;
		}
		analysis.setTreeArr(treeArr);
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			String predict = analysis.trainAnalyze(morpVO);
			if(!predict.equals(""))
				csv.add(predict);
		}
		long end = System.currentTimeMillis();
		System.out.println("MongDB - " + function + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		
		return String.valueOf(analysis.getSuccess()*100 / analysis.getPredictCnt());
	}
	
	public String analyze(MorpVO morpVO, String comName, String newsCode, String function){
		long start = System.currentTimeMillis();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		Map<String,Double> tfidfMap = null;
		JSONArray stockArr = sService.selectStock(comName);
		switch(function){
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
		System.out.println(function + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		return comName + "의 " + function+ "analysis 수행 시간 : "+(end-start)/1000 + "s" + " : " + analysis.userReqAnalyze(morpVO);
	}
	
	public void makeCSV(String comName, String function, List<String> csv){
		String path = "D:\\PPT\\analysis\\"+comName+"_"+function+".csv";
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
	
}
