package kr.co.ppt.analysis;

import kr.co.ppt.morp.NewsMorpVO;

public interface Analysis {

	public void analyze(NewsMorpVO morpVO);
	public void predict(String predicDate);
	public int getSuccess();
	public int getPredictCnt();
	public String getPrediction();
}

