package kr.co.ppt.dictionary;


public class OpiDicVO {

	private String term;
	private float weight;
	
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
