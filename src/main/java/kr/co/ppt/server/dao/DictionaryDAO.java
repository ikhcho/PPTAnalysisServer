package kr.co.ppt.server.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
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
	
	//=======================Connect to MongoDB================================//
	private MongoClient mongo = new MongoClient("222.106.22.63:30000");
	private MongoDatabase db = mongo.getDatabase("ppt");
	private JSONReader jsonReader = new JSONReader();
	private MongoCollection<Document> collection=null;
	
	public void insertAllDictionary(String colName, List<CompanyVO> list, String[] path, int type){
		collection = db.getCollection(colName);
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
		collection = db.getCollection(colName); 
		return collection.find(query).first();
	}
	
	//=======================Connect to Oracle================================//
	public void makeTFIDF(Map<Object,Object> map){
		template.insert("dictionary.makeTFIDF", map);
	}
	
	public List<TfidfVO> selectTFIDF(Map<Object,Object> map){
		return template.selectList("dictionary.selectTFIDF", map);
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
