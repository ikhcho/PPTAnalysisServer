package kr.co.ppt.server.dao;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
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
	
	private MongoCollection<Document> collection=null;
	
	public Document selectLastNews(Bson query){
		collection = jsonReader.DB.getCollection("NEWS");
		return collection.find(query).first();
	}
	
}
