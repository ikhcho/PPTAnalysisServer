package kr.co.ppt.server.service;

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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import kr.co.ppt.R.Dtree;
import kr.co.ppt.mongo.JSONReader;
import kr.co.ppt.server.dao.DtreeDAO;
import kr.co.ppt.stock.CompanyVO;

@Service
public class DtreeService {
	@Autowired
	DtreeDAO dDAO;
	@Autowired
	StockService sService;
	
	public void insertDtree(String newsCode, String function){
		JSONReader jsonReader = new JSONReader();
		MongoCollection<Document> collection = jsonReader.DB.getCollection("DTREE");
		//collection.drop();
		List<CompanyVO> comList =sService.selectComList();
		String errorCom = "";
		for(CompanyVO companyVO: comList){
			try{
				String comName = companyVO.getName();
				Dtree dTree = new Dtree();
				dTree.setDtree(comName, function);
				dTree.getDtree();
				dDAO.insertDtree(comName, newsCode, function, dTree.getDtree());
				Thread.sleep(1000);
			}catch(Exception e){
				System.out.println(companyVO.getName());
				errorCom+=companyVO.getName()+"\n";
				e.printStackTrace();
				continue;
			}
		}
		System.out.println("=========");
		System.out.println(errorCom);
	}
	
	public JSONArray selectDtree(String comName, String newsCode, String function){
		Bson query = Filters.and(Filters.eq("comName",comName), Filters.eq("newsCode",newsCode), Filters.eq("function",function));
		String data = dDAO.selectDtree(query).toJson();
		JSONParser parser = new JSONParser();
		JSONArray arr = null;
		try {
			arr = (JSONArray)((JSONObject) parser.parse(data)).get("dTree");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arr;
	}
	
}
