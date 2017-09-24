package kr.co.ppt.server.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.ppt.server.dao.StockDAO;
import kr.co.ppt.stock.CompanyVO;
import kr.co.ppt.stock.KospiVO;
import kr.co.ppt.stock.StockVO;

@Service
public class StockService {
	@Autowired
	StockDAO sDAO;
	
	public void insertCompany(String comName, String comCode, String type){
		Map<Object, Object> dataMap = new HashMap<Object, Object>();
		dataMap.put("comName", comName);
		dataMap.put("comCode", comCode);
		dataMap.put("stockType", type);
		sDAO.insertCompany(dataMap);
	}
	
	public List<CompanyVO> selectComList(){
		return sDAO.selectComList();
	}
	
	public List<StockVO> selectStockList(String comName){
		return sDAO.selectStockList(comName);
	}
	
	
	//사전 데이터 저장용
	public int insertComList() {
		KospiVO kospiVO = new KospiVO();
		int count = 0;
		List<String> comNameList = kospiVO.getComNames();
		List<String> comCodeList = kospiVO.getComCodes();

		for (int i = 0; i < comNameList.size(); i++) {
			Map<Object, Object> dataMap = new HashMap<Object, Object>();
			dataMap.put("name", comNameList.get(i));
			dataMap.put("code", comCodeList.get(i));
			dataMap.put("stockType", "KOSPI");
			sDAO.insertCompany(dataMap);
			count++;
		}
		return count;
	}

	//사전 데이터 저장용
	public int insertStock(){
		int count = 0;
		// 파일형태의 주가데이터
		List<CompanyVO> list = selectComList();
		BufferedReader br;
		for (CompanyVO companyVO : list) {
			try {
				br = new BufferedReader(new FileReader("D:\\PPT\\stock\\" + companyVO.getName() + ".json"));
				String data = "";
				String text = "";
				while ((text = br.readLine()) != null) {
					data += text;
				}
				br.close();
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
				JSONArray childArray = (JSONArray) jsonObject.get("quote");
				for (int i = 0; i < childArray.size(); i++) {
					JSONObject childObject = (JSONObject) childArray.get(i);
					StockVO stockVO = new StockVO();
					stockVO.setComNo(companyVO.getNo());
					int year = Integer.parseInt(((String)childObject.get("date")).substring(0, 4));
					int month = Integer.parseInt(((String)childObject.get("date")).substring(4,6));
					int day = Integer.parseInt(((String)childObject.get("date")).substring(6));
					stockVO.setOpenDate(new Date(year,month,day));
					stockVO.setOpen(Integer.parseInt((String)childObject.get("open")));
					stockVO.setClose(Integer.parseInt((String)childObject.get("close")));
					stockVO.setHigh(Integer.parseInt((String)childObject.get("high")));
					stockVO.setLow(Integer.parseInt((String)childObject.get("low")));
					stockVO.setVolume(Integer.parseInt((String)childObject.get("volume")));
					stockVO.setFlucState(((String) childObject.get("raise")).substring(0, 1));
					if(((String) childObject.get("raise")).substring(0, 1).equals("-")){
						stockVO.setRaise(0);
						stockVO.setRate(0);
					}else{
						stockVO.setRaise(Integer.parseInt(((String)childObject.get("raise")).substring(1)));
						stockVO.setRate(Integer.parseInt(((String)childObject.get("rate")).substring(1)));
					}
					sDAO.insertStock(stockVO);
					count++;
				}
				System.out.println(companyVO.getName());
				System.out.println("현재 추가 row 수 : " + count);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(companyVO.getName() + "없음");
			}
		}
		return count;
	}
	
}
