package kr.co.ppt.analysis;

import org.json.simple.JSONArray;

import kr.co.ppt.morp.FileMorpVO;
import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;

public interface Analysis {

	public String trainAnalyze(NewsMorpVO morpVO);
	public String realtimeAnalyze(FileMorpVO morpVO);
	public String userReqAnalyze(MorpVO morpVO);
	public String predict(String predicDate);
	public int getSuccess();
	public int getPredictCnt();
	public void setTreeArr(JSONArray dtree);
}

