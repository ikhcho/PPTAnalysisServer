package kr.co.ppt.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.ppt.server.service.StockService;
import kr.co.ppt.stock.CompanyVO;

@Controller
@RequestMapping("/stock")
public class StockController {
	
	@Autowired
	StockService sService;
	
	@RequestMapping("/selectCompanyList.do")
	@ResponseBody
	public String selectCompanyList() {
		List<CompanyVO> list = sService.selectComList();
		JSONArray result = new JSONArray();
		for(CompanyVO companyVO : list){
			Map<String, String> map = new HashMap<>();
			map.put("comName", companyVO.getName());
			map.put("comCode", companyVO.getCode());
			JSONObject jsonObj = new JSONObject(map);
			result.add(jsonObj);			
		}
		return result.toJSONString();
	}
	
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
