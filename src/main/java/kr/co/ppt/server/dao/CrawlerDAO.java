package kr.co.ppt.server.dao;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;

import kr.co.ppt.mongo.JSONReader;
import kr.co.ppt.stock.CompanyVO;

@Repository
public class CrawlerDAO {
	@Autowired
	JSONReader jsonReader;
	
	private MongoCollection<Document> collection=null;
	
	public void insertAllDictionary(String colName, List<CompanyVO> list, String[] path, int type){
		collection = jsonReader.DB.getCollection(colName);
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
}
