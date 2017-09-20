package kr.co.ppt.server.dao;

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
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import kr.co.ppt.dictionary.OpiDicVO;
import kr.co.ppt.dictionary.ProDicVO;
import kr.co.ppt.dictionary.TfidfVO;
import kr.co.ppt.mongo.JSONReader;
import kr.co.ppt.stock.CompanyVO;


@Repository
public class DictionaryDAO {
	@Autowired
	SqlSessionTemplate template;
	
	@Autowired
	JSONReader jsonReader;
	//=======================Connect to MongoDB================================//
	private MongoCollection<Document> collection=null;
	
	public void insertAllDictionary(String colName, List<CompanyVO> list, String[] path, int type){
		collection = jsonReader.DB.getCollection(colName);
		//collection.drop();
		for(CompanyVO companyVO : list){
			String comName = companyVO.getName();
			try{
				for(int i=1; i<path.length; i++){
					Document document = jsonReader.setDocument(path[0]+comName+path[i], type);
					collection.insertOne(document);
					System.out.println(comName+path[i]+" 성공");
				}
			}catch(Exception e){
				System.out.println(comName +" 에러");
			}
		}
	}
	
	public Document selectDictionary(String colName, Bson query){
		collection = jsonReader.DB.getCollection(colName); 
		return collection.find(query).first();
	}
	
	public void insertTFIDF(List<TfidfVO> list, String newsCode){
		collection = jsonReader.DB.getCollection("TFIDF");
		for(TfidfVO tfidfVO : list){
			try {
				Document document = new Document();
				document.append("newsCode", newsCode);
				document.append("word",  tfidfVO.getTerm());
				document.append("f", tfidfVO.getF());
				document.append("tf", tfidfVO.getTf());
				document.append("df", tfidfVO.getDf());
				document.append("idf", tfidfVO.getIdf());
				document.append("tfidf", tfidfVO.getTfidf());
				collection.insertOne(document);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(collection.find().first().toJson());
	}
	
	public Map<String,Double> selectTFIDF(Bson query){
		Map<String,Double> map = new HashMap<String,Double>();
		collection = jsonReader.DB.getCollection("TFIDF");
		MongoCursor<Document> cursor = collection.find(query).iterator();
		JSONParser parser = new JSONParser();
		while(cursor.hasNext()){
			try {
				JSONObject obj = (JSONObject)parser.parse(cursor.next().toJson());
				map.put((String)obj.get("word"), (Double)obj.get("tfidf"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return map;
	}
	
	//=======================Connect to Oracle================================//
	public void makeTFIDF(Map<Object,Object> map){
		template.insert("dictionary.makeTFIDF", map);
	}
	
	public List<TfidfVO> selectTFIDF(Map<Object,Object> map){
		return template.selectList("dictionary.selectTFIDF", map);
	}
	
	public List<TfidfVO> selectTFIDF(String newsCode){
		return template.selectList("dictionary.selectAllTFIDF", newsCode);
	}
	
	public List<OpiDicVO> selectOpiDic(Map<Object,Object> map){
		return template.selectList("dictionary.selectOpiDic", map);
	}
	
	public List<ProDicVO> selectProDic(Map<Object,Object> map){
		return template.selectList("dictionary.selectProDic", map);
	}
	
	public List<ProDicVO> selectProDic2(Map<Object,Object> map){
		return template.selectList("dictionary.selectProDic2", map);
	}
	
	public void makeOpiDic(Map<Object,Object> map){
		template.insert("dictionary.makeOpiDic", map);
	}
	
	public void makeProDic(Map<Object,Object> map){
		template.insert("dictionary.makeProDic", map);
	}
}
