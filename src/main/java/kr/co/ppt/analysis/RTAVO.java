package kr.co.ppt.analysis;

import java.sql.Date;

public class RTAVO {
	private int no;
	private String comName;
	private String anaCode;
	private String newsCode;
	private String todayFluc;
	private String tomorrowFluc;
	private Date regDate;
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public String getComName() {
		return comName;
	}
	public void setComName(String comName) {
		this.comName = comName;
	}
	public String getAnaCode() {
		return anaCode;
	}
	public void setAnaCode(String anaCode) {
		this.anaCode = anaCode;
	}
	public String getNewsCode() {
		return newsCode;
	}
	public void setNewsCode(String newsCode) {
		this.newsCode = newsCode;
	}
	public String getTodayFluc() {
		return todayFluc;
	}
	public void setTodayFluc(String todayFluc) {
		this.todayFluc = todayFluc;
	}
	public String getTomorrowFluc() {
		return tomorrowFluc;
	}
	public void setTomorrowFluc(String tomorrowFluc) {
		this.tomorrowFluc = tomorrowFluc;
	}
	public Date getRegDate() {
		return regDate;
	}
	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	
	
}
