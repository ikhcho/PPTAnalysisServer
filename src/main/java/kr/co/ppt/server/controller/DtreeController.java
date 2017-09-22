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
	public String insertDtree(String newsCode, String anaCode){
		dService.insertDtree(newsCode, anaCode);
		return "";
	}
	
	@RequestMapping("/selectDtree.do")
	@ResponseBody
	public String selectDtree(String comName, String newsCode, String anaCode){
		return dService.selectDtree(comName, newsCode, anaCode).toJSONString();
	}
	
	@RequestMapping("/updateDtree.do")
	@ResponseBody
	public String updateDtree(String comName, String newsCode, String anaCode){
		return dService.updateDtree(comName,newsCode, anaCode).toJSONString();
	}
}
