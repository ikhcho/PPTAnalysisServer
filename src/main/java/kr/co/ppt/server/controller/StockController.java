package kr.co.ppt.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.ppt.server.service.StockService;

@Controller
@RequestMapping("/stock")
public class StockController {
	
	@Autowired
	StockService sService;
	
	//사전 데이터 저장용
	@RequestMapping("/mongo/insertStock.do")
	@ResponseBody
	public String insertStockMongo() {
		sService.insertStockMongo();
		return "성공";
	}
		
	@RequestMapping("/insertCompany.do")
	@ResponseBody
	public String insertCompany(String comName, String comCode, String type){
		sService.insertCompany(comName, comCode, type);
		return comName + ", " + comCode + " 등록 되었습니다.";
	}
	
	//사전 데이터 저장용
	@RequestMapping("/makeComList.do")
	@ResponseBody
	public String insertComList(){
		int insertCnt = sService.insertComList();
		return "총 " + insertCnt + "개의 회사가 등록 되었습니다.";
	}
	
	//사전 데이터 저장용
	@RequestMapping("/insertStock.do")
	@ResponseBody
	public String insertStock() {
		int insertCnt = sService.insertStock();
		return "총 " + insertCnt + "개의 주가 정보가 등록 되었습니다.";
	}
}
