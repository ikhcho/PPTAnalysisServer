package kr.co.ppt.server.dao;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import kr.co.ppt.mongo.JSONReader;
import kr.co.ppt.stock.CompanyVO;

@Repository
public class CrawlerDAO {
	@Autowired
	JSONReader jsonReader;
	@Autowired
	SqlSessionTemplate template;
	
	private MongoCollection<Document> collection=null;
	
	public Document selectLastNews(Bson query){
		collection = jsonReader.DB.getCollection("NEWS");
		return collection.find(query).first();
	}
	
	public void insertTotalNewsCnt(Map<String, Object> map){
		template.insert("crawler.insertTotalNewsCnt",map);
	}
	
	public long selectTotalNewsCnt(String newsCode){
		return (long)template.selectOne("crawler.selectTotalNewsCnt",newsCode);
	}
	
	public void updateNewsCnt(Map<String, Object> map){
		template.insert("crawler.updateNewsCnt",map);
	}
	
	public void updateTotalNewsCnt(Map<String, Object> map){
		template.insert("crawler.updateTotalNewsCnt",map);
	}
	
}
