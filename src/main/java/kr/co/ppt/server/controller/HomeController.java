package kr.co.ppt.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.co.ppt.R.DecisionTree;
import kr.co.ppt.stock.KospiVO;

@Controller
public class HomeController {
	@Autowired
	KospiVO kospiVO;
	
	@RequestMapping("/home.do")
	public String home(Model model){
		model.addAttribute("comList", kospiVO.getComNames());
		DecisionTree dTree = new DecisionTree();
		dTree.d();
		return "index";
	}
	
}
