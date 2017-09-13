package kr.co.ppt.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import oracle.net.aso.e;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
public class DecisionTree {
	RConnection connection = null;

	public void dTree(){
		String filePath = null;

		List<String[]> infoList = null;
		String[] ele = null;
		String[] var = null;
		String[] splits = null;
		String[] yVal = null;
				
		try {
			infoList = new ArrayList<>();
			connection = new RConnection();

			// R스크립트가 있는 경로명을 입력
			connection.eval("source('D:/PPT/R/dTree.R')");

			filePath = "'D:/PPT/analysis/AK홀딩스_pro1.csv'";
			connection.eval("df <- read.csv(" + filePath + ")");
			System.out.println("기업명 : " + filePath.substring(9, filePath.length() - 5));
			// 의사결정트리 생성
			connection.eval("data1 <- dTree(df)");
			// dTree 정의
			connection.eval("intrain<-createDataPartition(y=df$result, p=1, list=FALSE)");
			connection.eval("train<-df[intrain, ]");
			connection.eval("test<-df[-intrain, ]");
			connection.eval("treemod<-tree(result~. , data=train)");
			
			// 트리의 leaf의 최적 개수를 구함
			connection.eval("cv.trees<-cv.tree(treemod, FUN=prune.misclass )");
			connection.eval("bValue <- cv.trees$size[which.min(cv.trees$dev)]");
			int value = connection.eval("getBest(dTree(df))").asInteger();
			System.out.println("bestSize : " + value);
			
			//result 정의
			connection.eval("prune.trees <- prune.misclass(data1, best=2)");
			
			var = connection.eval("getVar(result(dTree(df)))").asStrings();
			splits = connection.eval("getSplits(result(dTree(df)))").asStrings();
			yVal = connection.eval("getYval(result(dTree(df)))").asStrings();

			infoList = makeList(var, splits, yVal, ele, infoList);
			printList(infoList);
			//makeDecision(infoList);
			System.out.println("result읽기 완료");
		} catch (RserveException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	public List<String[]> makeList(String[] var, String[] splits, String[] yVal, String[] ele,
			List<String[]> list) {
		for (int i = 0; i < var.length; i++) {
			ele = new String[3];
			if (var[i].equals("") || splits[i].equals("")) {
				ele[0] = ("<leaf>");
				ele[1] = ("-");
				ele[2] = (yVal[i]);
				list.add(ele);
			} else {
				ele[0] = (var[i]);
				ele[1] = (splits[i]);
				ele[2] = (yVal[i]);
				list.add(ele);
			}
		}

		return list;
	}

	public void printList(List<String[]> l) {
		System.out.println("===");
		for (int i = 0; i < l.size(); i++) {
			System.out.print("[" + (i + 1) + "] ");
			for (int j = 0; j < l.get(i).length; j++)
				System.out.print(l.get(i)[j] + " ");
			System.out.println();
		}
	}
	
	//기준, 조건, 결과
	public String makeDecision(double incScore, double decScore, double equScore, String[] root){
		if(root[0].contains("leaf")){
			return root[2];
		}
		if(root[0].contains("Inc")){
			if(root[1].contains("<")){
				if(incScore < Double.parseDouble(root[1].substring(1))){
					//continue;
				}
			}
		}
		return "";
	}
}
