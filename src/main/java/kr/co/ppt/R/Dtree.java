package kr.co.ppt.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import oracle.net.aso.e;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
public class Dtree {
	private RConnection connection = null;
	private List<String[]> tree = new ArrayList<String[]>();
	int bestSize = 0;
	private String[] var = null;
	private String[] splits = null;
	private String[] yVal = null;
	
	public void setDtree(String comName, String newsCode, String function) throws Exception{
		connection = new RConnection();

		// R스크립트가 있는 경로명을 입력
		connection.eval("source('D:/PPT/R/dTree.R')");

		String filePath = "'D:/PPT/analysis/" + newsCode + "/" + comName + "_" + function + ".csv'";
		connection.eval("df <- read.csv(" + filePath + ")");
		System.out.println("기업명 : " + comName + "_" + function);
		
		// dTree 정의
		connection.eval("set.seed(1000)");
		connection.eval("intrain<-createDataPartition(y=df$result, p=0.8, list=FALSE)");
		connection.eval("train<-df[intrain, ]");
		connection.eval("test<-df[-intrain, ]");
		connection.eval("treemod<-tree(result~. , data=train)");

		// 의사결정트리 생성
		connection.eval("data1 <- dTree(df)");
		
		// 트리의 leaf의 최적 개수를 구함
		//connection.eval("cv.trees<-cv.tree(treemod, FUN=prune.misclass )");
		//connection.eval("bValue <- cv.trees$size[which.min(cv.trees$dev)]");
		//bestSize = connection.eval("getBest(dTree(df))").asInteger();

		// result 정의
		//connection.eval("prune.trees <- prune.misclass(dTree(df), best=getBest(dTree(df)))");

		var = connection.eval("getVar(data1$frame)").asStrings();
		splits = connection.eval("getSplits(data1$frame)").asStrings();
		yVal = connection.eval("getYval(data1$frame)").asStrings();
		connection.close();
		bestSize = yVal.length/2+1;
		makeTree();
		printList();
	}
	
	public void setDtree(JSONArray jsonArr){
		for (int i = 0; i < jsonArr.size(); i++) {
			String[] branch = new String[4]; //[0] = 기준, [1] = 조건, [2] = 결과, [3] = 층
			JSONObject jsonObject = (JSONObject)jsonArr.get(i);
			branch[0] = (String)jsonObject.get("var");
			branch[1] = (String)jsonObject.get("splits");
			branch[2] = (String)jsonObject.get("yVal");
			branch[3] = (String)jsonObject.get("floor");
			tree.add(branch);
		}
	}
	
	public JSONArray getDtree(){
		JSONArray jsonArr = new JSONArray();
		JSONParser parser = new JSONParser();
		for (int i = 0; i < tree.size(); i++) {
			String data="";
			data += "{\"branch\" : " + (i+1);
			data += ", \"var\" : \"" + tree.get(i)[0] + "\""; 
			data += ", \"splits\" : \"" + tree.get(i)[1] + "\""; 
			data += ", \"yVal\" : \"" + tree.get(i)[2] + "\""; 
			data += ", \"floor\" : \"" + tree.get(i)[3] +"\"}"; 
			try {
				jsonArr.add((JSONObject)parser.parse(data));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonArr;
	}
	private void makeTree() {
		for (int i = 0; i < var.length; i++) {
			String[] branch = new String[4]; //[0] = 기준, [1] = 조건, [2] = 결과, [3] = 층
			if (var[i].equals("") || splits[i].equals("")) {
				branch[0] = ("leaf");
				branch[1] = ("-");
				branch[2] = (yVal[i]);
				tree.add(branch);
			} else {
				branch[0] = (var[i]);
				branch[1] = (splits[i]);
				branch[2] = (yVal[i]);
				tree.add(branch);
			}
		}
		calcFloor();
	}

	private void calcFloor(){
		int floor = 1;
		int[] prune = new int[bestSize]; // bestSize = 최대 층수
		//prune[0] = 1;//1층은 1개만 존재하므로 1로 초기화해줘 2로 맞춘다.
		for(int i=0; i<tree.size(); i++){
			tree.get(i)[3] = String.valueOf(floor);
			prune[floor-1]++; // 층수 카운트증가
			if(!tree.get(i)[0].equals("leaf")){
				floor++;
			}else{
				if(prune[floor-1]%2 == 0){//각층은 짝수로 존재해야한다. 
					for(int j=floor; j>0; j--){
						if(prune[j-1]%2 !=0){
							floor = j;
							break;
						}
					}
				}
			}
		}
	}
	
	public void printList() {
		System.out.println("===");
		for (int i = 0; i < tree.size(); i++) {
			System.out.print("[" + (i + 1) + "] ");
			for (int j = 0; j < tree.get(i).length; j++)
				System.out.print(tree.get(i)[j] + " ");
			System.out.println();
		}
	}
	
	public String getDecision(double incScore, double decScore, double equScore){
		int branch = 0;
		//System.out.println("현재 브랜치 : " + (branch+1));
		while(!tree.get(branch)[0].equals("leaf")){
			if (tree.get(branch)[0].contains("Inc")) {
				branch = compare(incScore, branch);
			}else if(tree.get(branch)[0].contains("Dec")){
				branch = compare(decScore, branch);
			}else if(tree.get(branch)[0].contains("Equ")){
				branch = compare(equScore, branch);
			}
			//System.out.println("현재 브랜치 : " + (branch+1));
		}
		return tree.get(branch)[2];
	}
	
	private int compare(double score, int branch){
		if (tree.get(branch)[1].contains("<")) {
			if (score < Double.parseDouble(tree.get(branch)[1].substring(1))) {//참
				return trueBranch(branch);
			}else{//거짓
				return falseBranch(branch);
			}
		}else{
			if (score > Double.parseDouble(tree.get(branch)[1].substring(1))) {//참
				return trueBranch(branch);
			}else{//거짓
				return falseBranch(branch);
			}
		}
	}
	
	private int trueBranch(int branch){
		for(int i=branch+1; i<tree.size();i++){
			if(Integer.parseInt(tree.get(i)[3]) == Integer.parseInt(tree.get(branch)[3])+1){
				branch = i;
				return branch;
			}
		}
		return 1;
	}
	
	private int falseBranch(int branch){
		boolean stop = false;
		for(int i=branch+1; i<tree.size();i++){
			if(Integer.parseInt(tree.get(i)[3]) == Integer.parseInt(tree.get(branch)[3])+1){
				if(stop){
					branch = i;
					return branch;
				}else
					stop = true;
			}
		}
		return 1;
	}
}
