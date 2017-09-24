package kr.co.ppt.server.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.co.ppt.stock.CompanyVO;
import kr.co.ppt.stock.StockVO;


@Repository
public class StockDAO {
	@Autowired
	SqlSessionTemplate template;
	
	public void insertCompany(Map<Object,Object> map){
		template.insert("stock.insertComList", map);
	}
	
	public List<CompanyVO> selectComList(){
		return template.selectList("stock.selectComList");
	}
	
	public void insertStock(StockVO stockVO){
		template.insert("stock.insertStock", stockVO);
	}
	
	public List<StockVO> selectStockList(String comName){
		return template.selectList("stock.selectStockList",comName);
	}
}
