package kr.co.ppt.server.dao;

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

import kr.co.ppt.mongo.JSONReader;
import kr.co.ppt.stock.CompanyVO;
import kr.co.ppt.stock.StockVO;


@Repository
public class StockDAO {
	@Autowired
	SqlSessionTemplate template;
	
	@Autowired
	JSONReader jsonReader;
	//=======================Connect to MongoDB================================//
	private MongoCollection<Document> collection=null;
	
	public void insertStock(List<CompanyVO> list){
		collection = jsonReader.DB.getCollection("STOCKS");
		//collection.drop();
		for(CompanyVO companyVO : list){
			String path = "D:\\PPT\\stock\\" + companyVO.getName() + ".json";
			try{
				Document document = jsonReader.setDocument(path, jsonReader.STOCK);
				collection.insertOne(document);
				System.out.println(companyVO.getName()+" 성공");
			}catch(Exception e){
				System.out.println(companyVO.getName() +" 에러");
			}
		}
	}
	
	public Document selectStock(Bson query){
		collection = jsonReader.DB.getCollection("STOCKS"); 
		return collection.find(query).first();
	}
	
	//=======================Connect to Oracle================================//
	public void insertCompany(Map<Object,Object> map){
		template.insert("stock.insertComList", map);
	}
	
	public List<CompanyVO> selectComList(){
		return template.selectList("stock.selectComList");
	}
	
	public void insertStock(StockVO stockVO){
		template.insert("stock.insertStock", stockVO);
	}
	
	public List<StockVO> selectStockList(String comName){
		return template.selectList("stock.selectStockList",comName);
	}
}
