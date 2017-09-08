package kr.co.ppt.mongo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONReader {
	//파일 타입
	public final static int NEWS_JSON = 0;
	public final static int WORDS_JSON = 1;
	public final static int OPI_DIC_JSON = 2;
	public final static int PRO_DIC_JSON = 3;
	public final static int PRO2_DIC_JSON = 4;
	
	/**
	 * 날짜별 파일들 이름 추출
	 * @param path	경로
	 * @param from	시작날짜
	 * @param to	마지막날짜
	 * @param category 카테고리
	 * @return TreeSet
	 */
	public TreeSet<String> makeFileName(String path, String category, Calendar from, Calendar to){
		
		Calendar cal = Calendar.getInstance();
		cal = from;
		TreeSet<String> fileSet = new TreeSet<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		
		while(true) {
			if(cal.get(Calendar.YEAR) == to.get(Calendar.YEAR) 
					&& cal.get(Calendar.MONTH) == to.get(Calendar.MONTH) 
					&& cal.get(Calendar.DATE) == to.get(Calendar.DATE)) break;
			else {
				StringBuffer sb = new StringBuffer();
				sb.append(path)
				.append(category)
				.append(sdf.format(cal.getTime()))
				.append(".json");
				
				System.out.println(sb.toString());
				fileSet.add(sb.toString());
				
				cal.add(Calendar.DATE, 1);
			}
		}
		
		return fileSet;
	}
	
	/**
	 * 파일들을 읽어서 type에 따라 JSON으로 만들고, insertMany메소드를 사용하기 위한 List<Document>로 반환
	 * @param fileSet
	 * @param type
	 * @return List<Document>
	 */
	
	public List<Document> setDocumentList(TreeSet<String> fileSet, int type) {
		List<Document> documentList = new ArrayList<>();
		Iterator<String> it = fileSet.iterator();
		
		while(it.hasNext()) {
			File file = new File(it.next());
			String json = readJSON(file, type);
			JSONParser parser = new JSONParser();
			//System.out.println(json);
			
			if(type == NEWS_JSON) {
				try {
					JSONObject jsonObject = (JSONObject)parser.parse(json);
					JSONArray news = (JSONArray)jsonObject.get("news");
					
					Document document = new Document()
							.append("news", Arrays.asList(news));
					
					documentList.add(document);
					
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("NEWS_JSON Error : " + e.getMessage());
					e.printStackTrace();
				}
			} else if(type == WORDS_JSON) {
				try {
					JSONObject jsonObject = (JSONObject)parser.parse(json);	//json object로 변환
					String category = (String)jsonObject.get("category");
					
					String newsDate = (String)jsonObject.get("newsDate");
					
					
					JSONArray prev = (JSONArray)jsonObject.get("prev");
					JSONArray begin = (JSONArray)jsonObject.get("begin");
					JSONArray append = (JSONArray)jsonObject.get("append");
					
					Document document = new Document()
							.append("category", category)
							.append("newsDate", newsDate)
							.append("prev", Arrays.asList(prev))
							.append("begin", Arrays.asList(begin))
							.append("append", Arrays.asList(append));
					
					documentList.add(document);
					
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("WORDS_JSON Error : " + e.getMessage());
				}
			}  else {
				throw new IllegalArgumentException("잘못된 type 입니다. 다시 입력해주세요.");
			}
			
		}
		
		return documentList;
	}
	
	public Document setDocument(String path, int type) {
		File file = new File(path);
		String json = readJSON(file, type);
		JSONParser parser = new JSONParser();
		Document document = null;
		try {
			JSONObject jsonObject = (JSONObject) parser.parse(json);
			String comName = (String) jsonObject.get("comName");
			String comCode = (String) jsonObject.get("comCode");
			String newsCode = (String) jsonObject.get("category");
			JSONArray dictionary = (JSONArray) jsonObject.get("dictionary");
			switch (type) {
			case OPI_DIC_JSON:
				String opinion = (String) jsonObject.get("opinion");
				document = new Document().append("comName", comName).append("comCode", comCode)
						.append("opinion", opinion).append("newsCode", newsCode).append("dictionary", dictionary);
				break;
			case PRO_DIC_JSON:
			case PRO2_DIC_JSON:
				document = new Document().append("comName", comName).append("comCode", comCode)
						.append("newsCode", newsCode).append("dictionary", dictionary);
				break;
			}
		} catch (Exception e){
			System.out.println("Error : " + e.getMessage());
		}
		return document;
	}
	/**
	 * 파일 읽어서 String 타입으로 반환
	 * @param file
	 * @return
	 */
	public String readJSON(File file, int type) {
		String json = null;
		
		try (
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);
				){
			while(br.ready()) {
				if(type == WORDS_JSON || type == OPI_DIC_JSON || type == PRO_DIC_JSON || type == PRO2_DIC_JSON)
					json = br.readLine();
				else if(type == NEWS_JSON) {
					StringBuffer sb = new StringBuffer();
					sb.append("{\"news\" : [");
					while(br.ready()) {
						sb.append(br.readLine());
					}
					sb.append("]}");
					
					json = sb.toString();
				} else {
					throw new IllegalArgumentException("잘못된 type 입니다. 다시 입력해주세요.");
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("readJSON Error : " + e.getMessage());
		}
		
		return json;
		
	}
	
}
