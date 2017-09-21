package kr.co.ppt.server.dao;

import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
}
