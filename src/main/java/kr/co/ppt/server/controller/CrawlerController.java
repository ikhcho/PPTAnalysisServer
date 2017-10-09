package kr.co.ppt.server.controller;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;

import kr.co.ppt.server.service.CrawlerSerivce;

@Controller
@RequestMapping("/crawler")
public class CrawlerController {
	@Autowired
	CrawlerSerivce cService;
	
	@RequestMapping("batch.do")
	@ResponseBody
	public String batch(String newsCode){
		cService.batch(newsCode);
		return "끝";
	}
	
	@RequestMapping("insertTotalNewsCnt.do")
	@ResponseBody
	public String insertTotalNewsCnt(String newsCode){
		cService.insertTotalNewsCnt(newsCode);
		return "끝";
	}
	
	@RequestMapping("recentNews.do")
	@ResponseBody
	public String recentNews(String newsCode, String num, String callback){
		if(newsCode == null && num == null){
			if(callback == null)
				return cService.recentNews().toJSONString();
			else
				return  callback+"("+cService.recentNews().toJSONString()+")";
		}else{
			if(callback == null)
				return cService.recentNews(newsCode,Integer.parseInt(num)).toJSONString();
			else
				return  callback+"("+cService.recentNews(newsCode,Integer.parseInt(num)).toJSONString()+")";
		}
	}
}
