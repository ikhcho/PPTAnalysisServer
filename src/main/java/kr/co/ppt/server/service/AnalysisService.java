package kr.co.ppt.server.service;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.conversions.Bson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.model.Filters;

import kr.co.ppt.analysis.Analysis;
import kr.co.ppt.analysis.FilteredAnalysis;
import kr.co.ppt.analysis.MergeAnalysis;
import kr.co.ppt.analysis.OpiAnalysis;
import kr.co.ppt.analysis.OpiAnalysis2;
import kr.co.ppt.analysis.ProAnalysis;
import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.dictionary.TfidfVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.stock.StockVO;


@Service
public class AnalysisService {
	@Autowired
	StockService sService;
	
	@Autowired
	DictionaryService dService;
	
	
	public List<String> trainAnalyze(String comName, String newsCode, String function, String[] dateRange){
		long start = System.currentTimeMillis();
		List<String> list = new ArrayList<String>();
		Analysis analysis = null;
		List<OpiDicVO> posList = null;
		List<OpiDicVO> negList = null;
		List<ProDicVO> proDicList = null;
		JSONArray stockArr = sService.selectStock(comName);
		List<TfidfVO> tfidfList = null;
		switch(function){
			case "opi1":
				posList = dService.selectOpiDic("OPI_POS_DIC", comName, newsCode);
				negList = dService.selectOpiDic("OPI_NEG_DIC", comName, newsCode);
				analysis = new OpiAnalysis(posList,negList,stockArr);
				break;
			case "opi2":
				posList = dService.selectOpiDic("OPI_POS_DIC", comName, newsCode);
				negList = dService.selectOpiDic("OPI_NEG_DIC", comName, newsCode);
				analysis = new OpiAnalysis2(posList,negList,stockArr);
				break;
			case "pro1":
				proDicList = dService.selectProDic(comName, newsCode);
				analysis = new ProAnalysis(proDicList,stockArr);
				break;
			case "pro2":
				proDicList = dService.selectProDic2(comName, newsCode);
				analysis = new ProAnalysis(proDicList,stockArr);
				break;
			case "fit1":
				proDicList = dService.selectProDic(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 5);
				analysis = new FilteredAnalysis(proDicList,stockArr,tfidfList);
				break;
			case "fit2":
				proDicList = dService.selectProDic2(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 5);
				analysis = new FilteredAnalysis(proDicList,stockArr,tfidfList);
				break;
			case "meg1":
				posList = dService.selectOpiDic("OPI_POS_DIC", comName, newsCode);
				negList = dService.selectOpiDic("OPI_NEG_DIC", comName, newsCode);
				proDicList = dService.selectProDic(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 7);
				analysis = new MergeAnalysis(posList,negList,proDicList,stockArr,tfidfList);
				break;
			case "meg2":
				posList = dService.selectOpiDic("OPI_POS_DIC", comName, newsCode);
				negList = dService.selectOpiDic("OPI_NEG_DIC", comName, newsCode);
				proDicList = dService.selectProDic(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 7);
				analysis = new MergeAnalysis(posList,negList,proDicList,stockArr,tfidfList);
				break;
		}
		
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			analysis.trainAnalyze(morpVO);
		}
		
		list.add("예측확률 : " + analysis.getPredictCnt() + "중 " + analysis.getSuccess() + "건 맞음 : "
				+ (analysis.getSuccess()*100 / analysis.getPredictCnt()) + "%");
		long end = System.currentTimeMillis();
		System.out.println("ORACLE DB - " + function + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		return list;
	}
	
	public List<String> trainAnalyzeWithMongo(String comName, String newsCode, String function, String[] dateRange){
		long start = System.currentTimeMillis();
		List<String> list = new ArrayList<String>();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		Map<String,Double> tfidfMap = null;
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
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3, 5);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				csv.add("fit1Inc,fit1Dec,fit1Equ,result");
				break;
			case "fit2":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3, 5);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				csv.add("fit2Inc,fit2Dec,fit2Equ,result");
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3, 7);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				csv.add("meg1Inc,meg1Dec,meg1Equ,result");
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3, 7);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				csv.add("meg2Inc,meg2Dec,meg2Equ,result");
				break;
		}
		
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			String predict = analysis.trainAnalyzeWithMongo(morpVO);
			if(!predict.equals(""))
				csv.add(predict);
		}
		
		makeCSV(comName,function,csv);
		list.add("예측확률 : " + analysis.getPredictCnt() + "중 " + analysis.getSuccess() + "건 맞음 : "
				+ (analysis.getSuccess()*100 / analysis.getPredictCnt()) + "%");
		long end = System.currentTimeMillis();
		System.out.println("MongDB - " + function + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		return list;
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
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3, 5);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				break;
			case "fit2":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3, 5);
				analysis = new FilteredAnalysis(prodicArr,stockArr,tfidfMap);
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3, 7);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfMap = dService.selectTFIDFMongo(newsCode, 3, 7);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockArr,tfidfMap);
				break;
		}
		long end = System.currentTimeMillis();
		System.out.println(function + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		return comName + "의 " + function+ "analysis 수행 시간 : "+(end-start)/1000 + "s" + " : " + analysis.analyze(morpVO);
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
