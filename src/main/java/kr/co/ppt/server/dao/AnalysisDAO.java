package kr.co.ppt.server.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.co.ppt.analysis.RTAVO;

@Repository
public class AnalysisDAO {

	@Autowired
	SqlSessionTemplate template;
	
	public void insertRTA(Map<Object,Object> map){
		template.insert("analysis.insertRTA", map);
	}
	
	public void updateRTA(Map<Object,Object> map){
		template.update("analysis.updateRTA", map);
	}
	
	public List<RTAVO> selectOneRTA(String comName){
		return template.selectList("analysis.selectOneRTA", comName);
	}
	
	public List<RTAVO> selectAllRTA(){
		return template.selectList("analysis.selectAllRTA");
	}
	
	public RTAVO selectRTA(String newsCode){
		return template.selectOne("analysis.selectRTA",newsCode);
	}
	public List<RTAVO> selectAllRTA(String newsCode){
		return template.selectList("analysis.selectTodayRTA",newsCode);
	}
	
	public void updateYesterdayRTA(RTAVO rtaVO){
		template.update("analysis.updateYesterdayRTA", rtaVO);
	}
	
	public void insertMyAnalysis(Map<Object,Object> map){
		template.insert("analysis.insertMyAnalysis", map);
	}
	public void updateMyAnalysis(Map<Object,Object> map){
		template.update("analysis.updateMyAnalysis", map);
	}
	
	public List<RTAVO> selectAllMyAnalysis(String newsCode){
		return template.selectList("analysis.selectAllMyAnalysis",newsCode);
	}
	
	public RTAVO selectOneMyAnalysis(Map<String,Object> map){
		return template.selectOne("analysis.selectOneMyAnalysis",map);
	}
	
	public void updateYesterdayMyAnalysis(RTAVO rtaVO){
		template.update("analysis.updateYesterdayMyAnalysis", rtaVO);
	}
	
	public void insertReliability(Map<Object,Object> map){
		template.insert("analysis.insertReliability", map);
	}
}
