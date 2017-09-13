package kr.co.ppt.dictionary;

public class ProDicVO {
	private String term;
	private double inc;
	private double dec;
	private double equ;
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public double getInc() {
		return inc;
	}
	public void setInc(double inc) {
		this.inc = inc;
	}
	public double getDec() {
		return dec;
	}
	public void setDec(double dec) {
		this.dec = dec;
	}
	public double getEqu() {
		return equ;
	}
	public void setEqu(double equ) {
		this.equ = equ;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\"term\" : \"");
		builder.append(term);
		builder.append("\", \"inc\" : ");
		builder.append(inc);
		builder.append(", \"dec\" : ");
		builder.append(dec);
		builder.append(", \"equ\" : ");
		builder.append(equ);
		builder.append("}");
		return builder.toString();
	}
}
