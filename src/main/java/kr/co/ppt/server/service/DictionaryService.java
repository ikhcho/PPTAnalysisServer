package kr.co.ppt.server.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.dictionary.TFIDF;
import kr.co.ppt.dictionary.TfidfVO;
import kr.co.ppt.mongo.JSONReader;
import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.server.dao.DictionaryDAO;
import kr.co.ppt.stock.CompanyVO;
import kr.co.ppt.stock.KospiVO;
import kr.co.ppt.stock.StockVO;
import kr.co.ppt.util.Tool;

@Service
public class DictionaryService {
	@Autowired
	DictionaryDAO dDAO;
	@Autowired
	StockService sService;
	
	//=======================Connect to MongoDB================================//
	public void insertAllDictionary(String collectionName, String newsCode){
		List<CompanyVO> list = sService.selectComList();
		String[] path = new String[2];
		int type = 0;
		switch(collectionName){
			case "opi":
				collectionName = "OPI_DIC";
				type = JSONReader.OPI_DIC_JSON;
				path = new String[4];
				path[0] = "D:\\PPT\\opidic\\"+newsCode+"\\";
				path[1] = "_pos.json";
				path[2] = "_neg.json";
				path[3] = "_neu.json";
				break;
			case "pro":
				collectionName = "PRO_DIC";
				type = JSONReader.PRO_DIC_JSON;
				path[0] = "D:\\PPT\\prodic\\"+newsCode+"\\";
				path[1] = ".json";
				break;
			case "pro2":
				collectionName = "PRO2_DIC";
				type = JSONReader.PRO_DIC_JSON;
				path[0] = "D:\\PPT\\prodic\\"+newsCode+"\\";
				path[1] = "2.json";
				break;
		}
		dDAO.insertAllDictionary(collectionName, list, path, type);
	}
	
	public JSONObject selectOpiDicMongo(String comName,String opinion, String newsCode){
		Bson query = Filters.and(Filters.eq("comName",comName), Filters.eq("opinion",opinion), Filters.eq("newsCode",newsCode));
		String data = dDAO.selectDictionary("OPI_DIC",query).toJson();
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject)((JSONArray)((JSONObject) parser.parse(data)).get("dictionary")).get(0);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	
	public JSONArray selectProDicMongo(String comName, String newsCode){
		Bson query = Filters.and(Filters.eq("comName",comName), Filters.eq("newsCode",newsCode));
		String data = dDAO.selectDictionary("PRO_DIC",query).toJson();
		JSONParser parser = new JSONParser();
		JSONArray arr = null;
		try {
			arr = (JSONArray)((JSONObject) parser.parse(data)).get("dictionary");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arr;
	}
	
	public JSONArray selectPro2DicMongo(String comName, String newsCode){
		Bson query = Filters.and(Filters.eq("comName",comName), Filters.eq("newsCode",newsCode));
		String data = dDAO.selectDictionary("PRO2_DIC",query).toJson();
		JSONParser parser = new JSONParser();
		JSONArray arr = null;
		try {
			arr = (JSONArray)((JSONObject) parser.parse(data)).get("dictionary");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arr;
	}
	
	public void insertTFIDF(String newsCode){
		List<NewsMorpVO> morpList = new ArrayList<NewsMorpVO>();
		String[] dateRange = Tool.dateRange("20160101","20161231");

		for (int i = 0; i < dateRange.length; i++) {
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\" + newsCode + dateRange[i] + ".json");
			morpList.add(morpVO);
		}
		
		TFIDF tfidf = new TFIDF(morpList);
		tfidf.setTFIDF();
		List<TfidfVO> list = new ArrayList<>();
		for (int i = 0; i < tfidf.fList.size(); i++) {
			TfidfVO tfidfVO = new TfidfVO();
			tfidfVO.setTerm(tfidf.termList.get(i));
			tfidfVO.setF(tfidf.fList.get(i));
			tfidfVO.setTf(tfidf.tfList.get(i));
			tfidfVO.setDf(tfidf.dfList.get(i));
			tfidfVO.setIdf(tfidf.idfList.get(i));
			tfidfVO.setTfidf(tfidf.tfidfList.get(i));
			list.add(tfidfVO);
			if (i % 10000 == 0)
				System.out.println(i);
		}
		dDAO.insertTFIDF(list, newsCode);
	}
	
	public Map<String,Double> selectTFIDFMongo(String newsCode, double from, double to){
		Bson query = Filters.and(Filters.eq("newsCode",newsCode),Filters.gt("idf",from), Filters.lt("idf",to));
		Map<String,Double> map =dDAO.selectTFIDF(query);
		System.out.println("MongoDB - " + from + "< idf < " + to + "범위 TF-IDF사전 " + map.size() + "건 호출 성공");
		return map;
	}
	
	public Map<String,Double> selectTFIDFMongo(String newsCode){
		Bson query = Filters.eq("newsCode",newsCode);
		Map<String,Double> map = dDAO.selectTFIDF(query);
		System.out.println("MongoDB - TF-IDF사전 " + map.size() + "건 호출 성공");
		return map;
	}
	
	public List<JSONObject> selectMyDic(String newsCode){
		Bson query = Filters.eq("newsCode",newsCode);
		return dDAO.selectMyDic(query);
	}
	//=======================Connect to Oracle================================//
	public List<TfidfVO> selectTFIDF(String newsCode, double from, double to){
		Map<Object,Object> dataMap = new HashMap<Object,Object>();
		dataMap.put("newsCode", newsCode);
		dataMap.put("from", from);
		dataMap.put("to", to);
		List<TfidfVO> list = dDAO.selectTFIDF(dataMap);
		System.out.println("Oracle DB - " + from + "< idf < " + to + "범위 TF-IDF사전 " + list.size() + "건 호출 성공");
		return list;
	}
	
	public List<TfidfVO> selectTFIDF(String newsCode){
		List<TfidfVO> list = dDAO.selectTFIDF(newsCode);
		TfidfVO tfidfVO = new TfidfVO();
		return list;
	}
	
	public List<OpiDicVO> selectOpiDic(String table, String comName, String newsCode){
		Map<Object,Object> dataMap = new HashMap<Object,Object>();
		dataMap.put("table", table);
		dataMap.put("comName", comName);
		dataMap.put("newsCode", newsCode);
		List<OpiDicVO> list = dDAO.selectOpiDic(dataMap);
		return list;
	}
	
	public List<ProDicVO> selectProDic(String comName, String newsCode){
		Map<Object,Object> dataMap = new HashMap<Object,Object>();
		dataMap.put("comName", comName);
		dataMap.put("newsCode", newsCode);
		List<ProDicVO> list = dDAO.selectProDic(dataMap);
		return list;
	}
	
	public List<ProDicVO> selectProDic2(String comName, String newsCode){
		Map<Object,Object> dataMap = new HashMap<Object,Object>();
		dataMap.put("comName", comName);
		dataMap.put("newsCode", newsCode);
		List<ProDicVO> list = dDAO.selectProDic(dataMap);
		return list;
	}
	
	//전처리 데이터 용
	public int makeAllOpiDic(String newsCode){
		int count = 0;
		String[] file = {"_pos.json","_neg.json","_neu.json"};
		String[] table = {"OPI_POS_DIC", "OPI_NEG_DIC", "OPI_NEU_DIC"};
		String[] sequence = {"OPI_POS_DIC_SEQ.nextVal", "OPI_NEG_DIC_SEQ.nextVal", "OPI_NEU_DIC_SEQ.nextVal"};
		// 파일형태의 주가데이터
		List<CompanyVO> list = sService.selectComList();
		BufferedReader br;
		for(CompanyVO companyVO : list){
			try{
				for(int i=0; i<3; i++){
					br = new BufferedReader(new FileReader("D:\\PPT\\opidic\\" + companyVO.getName() + file[i]));// 긍정 사전
					String data="";
					String text="";
					while((text = br.readLine()) != null){
						data+=text;
					}
					br.close();
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
					JSONArray childArray = (JSONArray) jsonObject.get("dictionary");
					JSONObject childObject = (JSONObject) childArray.get(0);
					Iterator<String> iter = childObject.keySet().iterator();
					while(iter.hasNext()){
						String key = iter.next();
						Float value = Float.parseFloat((String)childObject.get(key));
						Map<Object, Object> dataMap = new HashMap<Object, Object>();
						dataMap.put("table", table[i]);
						dataMap.put("sequence", sequence[i]);
						dataMap.put("comNo", companyVO.getNo());
						dataMap.put("newsCode", newsCode);
						dataMap.put("term", key);
						dataMap.put("weight", value);
						dDAO.makeOpiDic(dataMap);
						count++;
					}
					System.out.println(companyVO.getName() + table[i] );
					System.out.println("현재 추가 row 수 : " + count );
				}
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(companyVO.getName() + "없음");
			}
		}
		return count;
	}
	//전처리 데이터 용
	public int makeAllProDic(String newsCode) {
		int count = 0;
		String[] file = {".json", "2.json"};
		String[] table = {"PRO_DIC", "PRO2_DIC"};
		String[] sequence = {"PRO_DIC_SEQ.nextVal", "PRO2_DIC_SEQ.nextVal"};
		// 파일형태의 주가데이터
		List<CompanyVO> list = sService.selectComList();
		BufferedReader br;
		for (CompanyVO companyVO : list) {
			try {
				for (int i = 0; i < 2; i++) {
					br = new BufferedReader(new FileReader("D:\\PPT\\prodic\\" + companyVO.getName() + file[i]));// 긍정 사전
					String data = "";
					String text = "";
					while ((text = br.readLine()) != null) {
						data += text;
					}
					br.close();
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
					JSONArray childArray = (JSONArray) jsonObject.get("dictionary");
					for(int j=0; j< childArray.size(); j++){
						JSONObject childObject = (JSONObject) childArray.get(j);
						Map<Object, Object> dataMap = new HashMap<Object, Object>();
						dataMap.put("table", table[i]);
						dataMap.put("sequence", sequence[i]);
						dataMap.put("comNo", companyVO.getNo());
						dataMap.put("newsCode", newsCode);
						dataMap.put("term", childObject.get("word"));
						dataMap.put("inc", childObject.get("inc"));
						dataMap.put("dec", childObject.get("dec"));
						dataMap.put("equ", childObject.get("equ"));
						dDAO.makeProDic(dataMap);
						count++;
					}
					System.out.println(companyVO.getName() + table[i] );
					System.out.println("현재 추가 row 수 : " + count );
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(companyVO.getName() + "없음");
			}
		}
		return count;
	}
	
	/*
	@Autowired
	KospiVO kospiVO;
	//File형태의 형태소 데이터
	private static List<NewsMorpVO> morpList = new ArrayList<NewsMorpVO>();
	private static String[] dateRange = Tool.dateRange("20160101","20170630");
	
	static{
		for(int i=0; i<dateRange.length; i++){
			NewsMorpVO morpVO = new NewsMorpVO("D:\\PPT\\mining\\economic"+dateRange[i]+".json");
			morpList.add(morpVO);
		}
	}
	
	public int makeTFIDF(String newsCode){
		int count=0;
		TFIDF tfidf = new TFIDF(morpList);
		tfidf.setTFIDF();
		
		for(int i=0; i<tfidf.fList.size(); i++){
			Map<Object,Object> dataMap = new HashMap<Object,Object>();
			dataMap.put("newsCode", newsCode);
			dataMap.put("term", tfidf.termList.get(i));
			dataMap.put("f", tfidf.fList.get(i));
			dataMap.put("tf", tfidf.tfList.get(i));
			dataMap.put("df", tfidf.dfList.get(i));
			dataMap.put("idf", tfidf.idfList.get(i));
			dataMap.put("tfidf", tfidf.tfidfList.get(i));
			dDAO.makeTFIDF(dataMap);
			if(i%10000==0)
				System.out.println(i);
			count++;
		}
		
		return count;
	}
	*/
}
