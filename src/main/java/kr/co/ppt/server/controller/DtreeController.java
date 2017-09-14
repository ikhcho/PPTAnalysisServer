package kr.co.ppt.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.ppt.server.service.DtreeService;

@Controller
@RequestMapping("/dtree")
public class DtreeController {

	@Autowired
	DtreeService dService;
	
	@RequestMapping("/insertDtree.do")
	@ResponseBody
	public String insertDtree(String newsCode, String function){
		dService.insertDtree(newsCode, function);
		return "";
	}
	
	@RequestMapping("/selectDtree.do")
	@ResponseBody
	public String selectDtree(String comName, String newsCode, String function){
		return dService.selectDtree(comName, newsCode, function).toJSONString();
	}
}
