package kr.co.ppt.morp;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.ppt.util.Tool;

public class KomoranThread extends Thread{

	private String fileName;
	private List<String> dataSet;
	
	public KomoranThread(String fileName, List<String> dataSet) {
		super();
		this.fileName = fileName;
		this.dataSet = dataSet;
	}


	@Override
	public void run() {
		try {
			FileOutputStream fos = new FileOutputStream("D:\\PPT\\mining\\" + fileName + ".json");
			MorpDevider1 mor = new MorpDevider1();
			Map<String,Integer> map = mor.countNoun(fileName, dataSet);
			
			List<String> list = Tool.countSort(map);
			String result = "{";
			Iterator<String> iter = list.iterator();
			while( iter.hasNext()) {
				String key = (String)iter.next();
				int value = map.get( key );
				if(value<5){
					break;
				}else{
					result += "\"" + key + "\"" + " : " + "\"" + value + "\",";
				}
			}
			result = result.substring(0, result.length()-1);
			result +="}";
			if(result.length()==1)
				result="";
			byte[] b = result.getBytes("utf-8");
			fos.write(b);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}

}
