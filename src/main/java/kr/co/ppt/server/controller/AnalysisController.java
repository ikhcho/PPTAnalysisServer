package kr.co.ppt.server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.ppt.server.service.AnalysisService;
import kr.co.ppt.util.Tool;

@Controller
@RequestMapping("/analysis")
public class AnalysisController {

	@Autowired
	AnalysisService aService;
	
	@RequestMapping("/trainAnalyze.do")
	public String trainAnalyze(Model model, String comName, String newsCode, String function, String from, String to){
		System.out.println(comName+"의 주가 예측 요청");
		String[] dateRange = Tool.dateRange(from, to);
		List<String> list = aService.trainAnalyze(comName,newsCode,function,dateRange);
		list.addAll(aService.trainAnalyzeWithMongo(comName,newsCode,function,dateRange));
		model.addAttribute("list", list);
		
		return "trainAnalyze";
	}
	
	
}
