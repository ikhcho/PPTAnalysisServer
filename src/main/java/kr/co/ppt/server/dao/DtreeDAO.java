package kr.co.ppt.server.dao;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import kr.co.ppt.mongo.JSONReader;

@Repository
public class DtreeDAO {
	@Autowired
	JSONReader jsonReader;
	private MongoCollection<Document> collection=null;
	
	public void insertDtree(String comName, String newsCode, String anaCode, JSONArray dTree){
		collection = jsonReader.DB.getCollection("DTREE");
		try {
			Document document = new Document();
			document.append("comName", comName);
			document.append("newsCode", newsCode);
			document.append("anaCode", anaCode);
			document.append("dTree", dTree);
			collection.insertOne(document);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Document selectDtree( Bson query){
		collection = jsonReader.DB.getCollection("DTREE"); 
		return collection.find(query).first();
	}
	
	public void deleteDtree(Bson query){
		collection = jsonReader.DB.getCollection("DTREE"); 
		collection.deleteOne(query);
	}
}
