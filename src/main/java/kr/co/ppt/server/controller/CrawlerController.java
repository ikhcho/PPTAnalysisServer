package kr.co.ppt.server.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.ppt.crawler.NewsCategoryVO;
import kr.co.ppt.server.service.CrawlerSerivce;

@Controller
@RequestMapping("/crawler")
public class CrawlerController {
	@Autowired
	CrawlerSerivce cService;
	
	@RequestMapping("craw.do")
	public void craw(){
		Map<String,String> recentNews = new HashMap<>();
		cService.craw();
	}
	
	@RequestMapping("recentNews.do")
	@ResponseBody
	public String recentNews(String newsCode, String num){
		if(newsCode == null && num == null){
			return cService.recentNews();
		}else{
			return cService.recentNews(newsCode,Integer.parseInt(num));
		}
	}
}
