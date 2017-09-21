package kr.co.ppt.morp;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import kr.co.ppt.util.Tool;

public class TypeThread extends Thread{

	private String type;
	private String startDate;
	private int period;
	
	public TypeThread(String type, String startDate, int period) {
		super();
		this.type = type;
		this.startDate = startDate;
		this.period = period;
	}

	@Override
	public void run() {
		String[] dateRange = Tool.dateRange(startDate, period);
		for(int i=0; i<period; i++){
			FileReader fr;
			try {
				fr = new FileReader("D:\\PPT\\news\\"+type+dateRange[i]+".json");
				BufferedReader br = new BufferedReader(fr);
				String text;
				
				List<String> prev = new ArrayList<String>();//장 시작 전
				List<String> begin = new ArrayList<String>();//장 시작 후
				List<String> append = new ArrayList<String>();//장 마감 후
				
				//시간 분석을 통한 데이타셋 분류
				String data="{\"" + type + "\":[";
				while((text = br.readLine()) != null){
					data+=text;
				}
				data+="]}";
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
				JSONArray daily = (JSONArray) jsonObject.get(type);
				if(Integer.parseInt(dateRange[i])<20160801){//2016년 8월 1일부로 장마감시간 변경
					try{
						for(int j=0; j<daily.size(); j++){
							JSONObject news = (JSONObject) daily.get(j);
							String time = (String)news.get("time");
							String content = (String)news.get("content");
							if(Integer.parseInt(time.split(":")[0])<9){
								prev.add(content);
							}else if(Integer.parseInt(time.split(":")[0])>= 15){
								append.add(content);
							}else{
								begin.add(content);
							}
						}
					}catch(Exception e){
						e.printStackTrace();
						continue;
					}
				}else{
					try{
						for(int j=0; j<daily.size(); j++){
							JSONObject news = (JSONObject) daily.get(j);
							String time = (String)news.get("time");
							String content = (String)news.get("content");
							if(Integer.parseInt(time.split(":")[0])<9){
								prev.add(content);
							}else if(Integer.parseInt(time.split(":")[0])> 15){
								append.add(content);
							}else if(Integer.parseInt(time.split(":")[0]) == 15 && Integer.parseInt(time.split(":")[1])>30){
								append.add(content);
							}else{
								begin.add(content);
							}
						}
					}catch(Exception e){
						e.printStackTrace();
						continue;
					}
				}
				br.close();
				fr.close();
				
				//형태소 저장
				if(begin.size()==0){
					KomoranThread prevThread = new KomoranThread(type+dateRange[i]+"_prev",prev);
					prevThread.start();
					prevThread.join();
				}
				if(append.size()==0 && begin.size()!=0){
					KomoranThread beginThread = new KomoranThread(type+dateRange[i]+"_begin",begin);
					beginThread.start();
					beginThread.join();
				}
				if(append.size()!=0){
					KomoranThread appendThread = new KomoranThread(type+dateRange[i]+"_append",append);
					appendThread.start();
					appendThread.join();
				}
				
				//File 통합
				FileOutputStream fos = new FileOutputStream("D:\\PPT\\mining\\"+type+dateRange[i]+".json");
				FileReader prevFr = new FileReader("D:\\PPT\\mining\\"+type+dateRange[i]+"_prev.json");
				BufferedReader prevBr = new BufferedReader(prevFr);
				fos.write(("{\"category\" : \"" + type + "\"").getBytes("utf-8"));
				fos.write((", \"newsDate\" : \"" + dateRange[i] + "\"").getBytes("utf-8"));
				fos.write(", \"prev\" : [".getBytes("utf-8"));
				while((text = prevBr.readLine()) != null){
					fos.write(text.getBytes("utf-8"));
					fos.flush();
				}
				fos.write("], \"begin\" : [".getBytes("utf-8"));
				prevBr.close();
				prevFr.close();
				try{
					FileReader beginFr = new FileReader("D:\\PPT\\mining\\"+type+dateRange[i]+"_begin.json");
					BufferedReader beginBr = new BufferedReader(beginFr);
					while((text = beginBr.readLine()) != null){
						fos.write(text.getBytes("utf-8"));
						fos.flush();
					}
					beginBr.close();
					beginFr.close();
				}catch(Exception e){
					
				}
				fos.write("], \"append\" : [".getBytes("utf-8"));
				try{
					FileReader appendFr = new FileReader("D:\\PPT\\mining\\"+type+dateRange[i]+"_append.json");
					BufferedReader appendBr = new BufferedReader(appendFr);
					while((text = appendBr.readLine()) != null){
						fos.write(text.getBytes("utf-8"));
						fos.flush();
					}
					appendBr.close();
					appendFr.close();
				}catch(Exception e){
					
				}
				fos.write("]}".getBytes("utf-8"));
				fos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
