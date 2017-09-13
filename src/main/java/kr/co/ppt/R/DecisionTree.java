package kr.co.ppt.R;

import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
public class DecisionTree {
	RConnection connection = null;

	public void d(){
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

			filePath = "'D:/PPT/analysis/서울식품공업_fit1.csv'";
			connection.eval("df <- read.csv(" + filePath + ")");
			System.out.println("기업명 : " + filePath.substring(9, filePath.length() - 5));

			// 의사결정트리 생성
			connection.eval("data1 <- dTree(df)");

			// 트리의 leaf의 최적 개수를 구함
			int value = connection.eval("getBest(data1)").asInteger();
			System.out.println("bestSize : " + value);

			var = connection.eval("getVar(result(dTree(df)))").asStrings();
			splits = connection.eval("getSplits(result(dTree(df)))").asStrings();
			yVal = connection.eval("getYval(result(dTree(df)))").asStrings();

			infoList = makeList(var, splits, yVal, ele, infoList);
			printList(infoList);

			System.out.println("result읽기 완료");
		} catch (RserveException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	public static List<String[]> makeList(String[] var, String[] splits, String[] yVal, String[] ele,
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

	public static void printList(List<String[]> l) {
		for (int i = 0; i < l.size(); i++) {
			System.out.print("[" + (i + 1) + "] ");
			for (int j = 0; j < l.get(i).length; j++)
				System.out.print(l.get(i)[j] + " ");
			System.out.println();
		}
	}
}
