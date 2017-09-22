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
}
