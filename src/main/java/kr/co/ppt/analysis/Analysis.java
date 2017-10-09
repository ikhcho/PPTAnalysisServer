package kr.co.ppt.analysis;

import org.json.simple.JSONArray;

import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;

public interface Analysis {

	public String trainAnalyze(NewsMorpVO morpVO);
	public String todayAnalyze(NewsMorpVO morpVO);
	public String tomorrowAnalyze(NewsMorpVO morpVO);
	public String userReqAnalyze(MorpVO morpVO);
	public String predict();
	public double getInc();
	public double getDec();
	public double getEqu();
	public int getSuccess();
	public int getPredictCnt();
	public void setTreeArr(JSONArray dtree);
	public void setUserDic(JSONArray userDic);
}

