package kr.co.ppt.analysis;

import kr.co.ppt.morp.NewsMorpVO;

public interface Analysis {

	public void trainAnalyze(NewsMorpVO morpVO);
	public void trainAnalyzeWithMongo(NewsMorpVO morpVO);
	public void analyze();
	public String predict(String predicDate);
	public int getSuccess();
	public int getPredictCnt();
	public String makeCSV();
}

