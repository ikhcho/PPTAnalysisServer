package kr.co.ppt.analysis;

import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.morp.NewsMorpVO;

public interface Analysis {

	public String trainAnalyze(NewsMorpVO morpVO);
	public String trainAnalyzeWithMongo(NewsMorpVO morpVO);
	public String analyze(MorpVO morpVO);
	public String predict(String predicDate);
	public int getSuccess();
	public int getPredictCnt();
}

