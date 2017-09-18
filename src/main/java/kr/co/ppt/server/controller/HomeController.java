package kr.co.ppt.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.ppt.R.Dtree;
import kr.co.ppt.server.service.StockService;
import kr.co.ppt.stock.KospiVO;

@Controller
public class HomeController {
	@Autowired
	StockService sService;
	
	@RequestMapping("/home.do")
	public String home(Model model){
		model.addAttribute("comList", sService.selectComList());
		
		return "index";
	}
	
	@RequestMapping(value = "/chart.do", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String chart(){
		
		String data = "{\"name\": \"flare\",\"children\": [{\"name\": \"analytics\",\"children\": [{\"name\": \"cluster\",\"children\": [{\"name\": \"AgglomerativeCluster\", \"size\": 3938},{\"name\": \"CommunityStructure\", \"size\": 3812},{\"name\": \"HierarchicalCluster\", \"size\": 6714},{\"name\": \"MergeEdge\", \"size\": 743}]},{\"name\": \"graph\",\"children\": [{\"name\": \"BetweennessCentrality\", \"size\": 3534},{\"name\": \"LinkDistance\", \"size\": 5731},{\"name\": \"MaxFlowMinCut\", \"size\": 7840},{\"name\": \"ShortestPaths\", \"size\": 5914},{\"name\": \"SpanningTree\", \"size\": 3416}]},{\"name\": \"optimization\",\"children\": [{\"name\": \"AspectRatioBanker\", \"size\": 7074}]}]}]}";
		return data;
	}
	
}
