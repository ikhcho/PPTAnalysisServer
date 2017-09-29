package kr.co.ppt.dictionary;

import java.io.BufferedReader;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ProDicVO {
	private String term;
	private double inc;
	private double dec;
	private double equ;
	private JSONArray prodicArr;
	
	public ProDicVO() {
	}
	
	public ProDicVO(String newsCode, String comName) {
		try {
			FileReader fr = new FileReader("D:\\PPT\\prodic\\" + newsCode + "\\" + comName + ".json");
			BufferedReader br = new BufferedReader(fr);
			String text = "";
			String data = "";
			data = "";
			while ((text = br.readLine()) != null) {
				data += text;
			}
			br.close();
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
			prodicArr = (JSONArray) jsonObject.get("dictionary");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
	public JSONArray getProdicArr() {
		return prodicArr;
	}

	public void setProdicArr(JSONArray prodicArr) {
		this.prodicArr = prodicArr;
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
