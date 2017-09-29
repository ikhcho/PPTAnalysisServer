package kr.co.ppt.dictionary;

import java.io.BufferedReader;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class OpiDicVO {

	private String term;
	private float weight;
	JSONObject opiDic;
	
	public OpiDicVO() {
	}
	
	public OpiDicVO(String newsCode, String comName, String opinion) {
		try {
			FileReader fr = new FileReader("D:\\PPT\\opidic\\" + newsCode + "\\" + comName + "_" + opinion + ".json");
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
			JSONArray childArray = (JSONArray) jsonObject.get("dictionary");
			opiDic = (JSONObject) childArray.get(0);
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
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	
	public JSONObject getOpiDic() {
		return opiDic;
	}

	public void setOpiDic(JSONObject opiDic) {
		this.opiDic = opiDic;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\"");
		builder.append(term);
		builder.append("\" : ");
		builder.append(weight);
		return builder.toString();
	}
	
}
