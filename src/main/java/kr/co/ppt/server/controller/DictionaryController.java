package kr.co.ppt.server.controller;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.dictionary.TfidfVO;
import kr.co.ppt.mongo.JSONReader;
import kr.co.ppt.server.service.DictionaryService;
import kr.co.ppt.stock.KospiVO;

@Controller
@RequestMapping("/dictionary")
public class DictionaryController {
	@Autowired
	DictionaryService dService;
	
	//=======================Connect to MongoDB================================//
	@RequestMapping("/mongo/insertDictionary.do")
	@ResponseBody
	public String insertAllDictionary(String collectionName){
		dService.insertAllDictionary(collectionName);
		return "";
	}
	
	@RequestMapping("/mongo/selectOpiDic.do")
	@ResponseBody
	public String selectOpiDicMongo(String comName,String opinion, String newsCode){
		return  dService.selectOpiDicMongo(comName, opinion, newsCode).toJSONString();
	}
	
	@RequestMapping("/mongo/test.do")
	@ResponseBody
	public String selectMongoProdic(String comName){
		MongoClient mongo = new MongoClient("222.106.22.63:30000");
		MongoDatabase db = mongo.getDatabase("ppt");
		JSONReader jsonReader = new JSONReader();
		MongoCollection<Document> collection = db.getCollection("PRO_DIC"); 
		Bson query = Filters.and(Filters.eq("comName",comName), Filters.eq("newsCode","economic"));
		Document find = collection.find().first();
		
		return find.toJson();
	}
	
	@RequestMapping("/mongo/insertTFIDF.do")
	@ResponseBody
	public String insertTFIDF(String newsCode){
		dService.insertTFIDF(newsCode);
		return "";
	}
	
	@RequestMapping("/mongo/selectTFIDF.do")
	@ResponseBody
	public String selectTFIDFMongo(String newsCode,double from, double to){
		dService.selectTFIDFMongo(newsCode,from,to);
		return "";
	}
	//=======================Connect to ORACLE================================//
	@RequestMapping("/oracle/selectTFIDF.do")
	@ResponseBody
	public String selectTFIDF(String newsCode, double from, double to){
		System.out.println(from + "< idf < " + to + "범위 TF-IDF사전 호출 시도");
		List<TfidfVO> list = dService.selectTFIDF(newsCode, from, to);
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for(int i=0; i<list.size()-1; i++){
			builder.append(list.get(i).toString());
			builder.append(", ");
		}
		builder.append(list.get(list.size()-1).toString());
		builder.append("}");
		return builder.toString();
	}
	
	@RequestMapping("/oracle/selectOpiDic.do")
	@ResponseBody
	public String selectOpiDic(String table, String comName, String newsCode){
		List<OpiDicVO> list = dService.selectOpiDic(table, comName, newsCode);
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for(int i=0; i<list.size()-1; i++){
			builder.append(list.get(i).toString());
			builder.append(", ");
		}
		builder.append(list.get(list.size()-1).toString());
		builder.append("}");
		return builder.toString();
	}
	
	@RequestMapping("/oracle/selectProDic.do")
	@ResponseBody
	public String selectProDic(String comName, String newsCode){
		List<ProDicVO> list = dService.selectProDic(comName, newsCode);
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(int i=0; i<list.size()-1; i++){
			builder.append(list.get(i).toString());
			builder.append(", ");
		}
		builder.append(list.get(list.size()-1).toString());
		builder.append("]");
		return builder.toString();
	}
	
	@RequestMapping("/oracle/insertAllOpiDic.do")
	@ResponseBody
	public String makeAllOpiDic(String newsCode){
		int insertCnt = dService.makeAllOpiDic(newsCode);
		return "총 " + insertCnt + "개의 단어가 등록 되었습니다.";
	}
	
	@RequestMapping("/oracle/insertAllProDic.do")
	@ResponseBody
	public String makeAllProDic(String newsCode){
		int insertCnt = dService.makeAllProDic(newsCode);
		return "총 " + insertCnt + "개의 단어가 등록 되었습니다.";
	}
	
	/*
	@RequestMapping("/makeTFIDF.do")
	@ResponseBody
	public String makeTFIDF(String newsCode){
		int insertCnt = dService.makeTFIDF(newsCode);
		return "총 " + insertCnt + "개의 단어가 등록 되었습니다.";
	}
	*/
	
	
}
