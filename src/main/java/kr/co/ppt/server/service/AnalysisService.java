package kr.co.ppt.server.service;

import java.util.ArrayList;
import java.util.List;

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
import kr.co.ppt.analysis.FilteredAnalysis2;
import kr.co.ppt.analysis.MergeAnalysis;
import kr.co.ppt.analysis.MergeAnalysis2;
import kr.co.ppt.analysis.OpiAnalysis;
import kr.co.ppt.analysis.OpiAnalysis2;
import kr.co.ppt.analysis.ProAnalysis;
import kr.co.ppt.analysis.ProAnalysis2;
import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.dictionary.TfidfVO;
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
		List<StockVO> stockList = sService.selectStockList(comName);
		List<TfidfVO> tfidfList = null;
		switch(function){
			case "opi1":
				posList = dService.selectOpiDic("OPI_POS_DIC", comName, newsCode);
				negList = dService.selectOpiDic("OPI_NEG_DIC", comName, newsCode);
				analysis = new OpiAnalysis(posList,negList,stockList);
				break;
			case "opi2":
				posList = dService.selectOpiDic("OPI_POS_DIC", comName, newsCode);
				negList = dService.selectOpiDic("OPI_NEG_DIC", comName, newsCode);
				analysis = new OpiAnalysis2(posList,negList,stockList);
				break;
			case "pro1":
				proDicList = dService.selectProDic(comName, newsCode);
				analysis = new ProAnalysis(proDicList,stockList);
				break;
			case "pro2":
				proDicList = dService.selectProDic2(comName, newsCode);
				analysis = new ProAnalysis2(proDicList,stockList);
				break;
			case "fit1":
				proDicList = dService.selectProDic(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 5);
				analysis = new FilteredAnalysis(proDicList,stockList,tfidfList);
				break;
			case "fit2":
				proDicList = dService.selectProDic2(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 5);
				analysis = new FilteredAnalysis2(proDicList,stockList,tfidfList);
				break;
			case "meg1":
				posList = dService.selectOpiDic("OPI_POS_DIC", comName, newsCode);
				negList = dService.selectOpiDic("OPI_NEG_DIC", comName, newsCode);
				proDicList = dService.selectProDic(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 7);
				analysis = new MergeAnalysis(posList,negList,proDicList,stockList,tfidfList);
				break;
			case "meg2":
				posList = dService.selectOpiDic("OPI_POS_DIC", comName, newsCode);
				negList = dService.selectOpiDic("OPI_NEG_DIC", comName, newsCode);
				proDicList = dService.selectProDic(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 7);
				analysis = new MergeAnalysis2(posList,negList,proDicList,stockList,tfidfList);
				break;
		}
		
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			analysis.trainAnalyze(morpVO);
		}
		
		list.add(analysis.makeCSV());
		list.add("예측확률 : " + analysis.getPredictCnt() + "중 " + analysis.getSuccess() + "건 맞음 : "
				+ (analysis.getSuccess()*100 / analysis.getPredictCnt()) + "%");
		long end = System.currentTimeMillis();
		System.out.println(function + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		return list;
	}
	
	public List<String> trainAnalyzeWithMongo(String comName, String newsCode, String function, String[] dateRange){
		long start = System.currentTimeMillis();
		List<String> list = new ArrayList<String>();
		Analysis analysis = null;
		JSONObject posJson = null;
		JSONObject negJson = null;
		JSONArray prodicArr = null;
		List<TfidfVO> tfidfList = null;
		List<StockVO> stockList = sService.selectStockList(comName);
		switch(function){
			case "opi1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis(posJson,negJson,stockList );
				break;
			case "opi2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				analysis = new OpiAnalysis2(posJson,negJson,stockList );
				break;
			case "pro1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				analysis = new ProAnalysis(prodicArr,stockList);
				break;
			case "pro2":
				prodicArr = dService.selectPro2DicMongo(comName, newsCode);
				analysis = new ProAnalysis2(prodicArr,stockList);
				break;
			case "fit1":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 5);
				analysis = new FilteredAnalysis(prodicArr,stockList,tfidfList);
				break;
			case "fit2":
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 5);
				analysis = new FilteredAnalysis2(prodicArr,stockList,tfidfList);
				break;
			case "meg1":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 7);
				analysis = new MergeAnalysis(posJson,negJson,prodicArr,stockList,tfidfList);
				break;
			case "meg2":
				posJson = dService.selectOpiDicMongo(comName, "pos", newsCode);
				negJson = dService.selectOpiDicMongo(comName, "neg", newsCode);
				prodicArr = dService.selectProDicMongo(comName, newsCode);
				tfidfList = dService.selectTFIDF(newsCode, 3, 7);
				analysis = new MergeAnalysis2(posJson,negJson,prodicArr,stockList,tfidfList);
				break;
		}
		
		for(String date : dateRange){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\"+newsCode+date+".json");
			analysis.trainAnalyzeWithMongo(morpVO);
		}
		
		list.add(analysis.makeCSV());
		list.add("예측확률 : " + analysis.getPredictCnt() + "중 " + analysis.getSuccess() + "건 맞음 : "
				+ (analysis.getSuccess()*100 / analysis.getPredictCnt()) + "%");
		long end = System.currentTimeMillis();
		System.out.println(function + "analysis 수행 시간 : "+(end-start)/1000 + "s");
		return list;
	}
}
