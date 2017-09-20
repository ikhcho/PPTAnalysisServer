package kr.co.ppt.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import kr.co.ppt.morp.NewsMorpVO;
import kr.co.ppt.stock.StockVO;

public class Tool {
	private static final Resource RESOURCE = new ClassPathResource("/openDate.txt");
	public static List<String> realDate = getOpenDate(); 
	
	//날짜 포멧 체크
	private static String dateFormat(String date){
		if(date.contains("-")){
			date = date.split("-")[0] + date.split("-")[1] + date.split("-")[2];
		}else if(date.contains(".")){
			date = date.split(".")[0] + date.split(".")[1] + date.split(".")[2];
		}else if(date.contains("/")){
			date = date.split("/")[0] + date.split("/")[1] + date.split("/")[2];
		}
		return date;
	}
	
	private static String dateString(Calendar cal){
		String date = String.valueOf(cal.get(Calendar.YEAR));
		if(cal.get(Calendar.MONTH)+1 < 10){
			date+="0" + String.valueOf(cal.get(Calendar.MONTH)+1);
		}else{
			date+=String.valueOf(cal.get(Calendar.MONTH)+1);
		}
		if(cal.get(Calendar.DATE)<10){
			date+="0" + String.valueOf(cal.get(Calendar.DATE));
		}else{
			date+= String.valueOf(cal.get(Calendar.DATE));
		}
		return date;
	}
	
	//날짜 기간 얻기
	public static String[] dateRange(String startDate,int period){
		startDate = dateFormat(startDate);
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(startDate.substring(0,4)), 
				Integer.parseInt(startDate.substring(4,6))-1, 
				Integer.parseInt(startDate.substring(6)));//Init Date = yyyyMM(+1)dd
		String[] dateRange = new String[period];
		for(int i=0; i<period; i++){
			dateRange[i] = dateString(cal);
			cal.add(Calendar.DATE, 1);
		}
		
		return dateRange;
	}

	// 날짜 기간 얻기
	public static String[] dateRange(String startDate, String endDate) {
		
		startDate = dateFormat(startDate);
		endDate = dateFormat(endDate);
		Calendar from = Calendar.getInstance();
		from.set(Integer.parseInt(startDate.substring(0, 4)),
				Integer.parseInt(startDate.substring(4, 6)) - 1,
				Integer.parseInt(startDate.substring(6)));
		
		Calendar to = Calendar.getInstance();
		to.set(Integer.parseInt(endDate.substring(0, 4)), 
				Integer.parseInt(endDate.substring(4, 6)) - 1,
				Integer.parseInt(endDate.substring(6)));
		int period = (int)((to.getTime().getTime() - from.getTime().getTime()) / 86400000);
		String[] dateRange = new String[period + 1];
		int i = 0;
		while (true) {
			
			dateRange[i] = dateString(from);
			if (dateString(to).equals(dateString(from))){
				break;
			}else {
				i++;
				from.add(Calendar.DATE, 1);
			}
		}
		return dateRange;
	}
		
	//문자열 날짜 변환
	public static String getDate(String date){
		date = dateFormat(date);
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(date.substring(0,4)), 
				Integer.parseInt(date.substring(4,6))-1, 
				Integer.parseInt(date.substring(6)));//Init Date = yyyyMM(+1)dd
		return dateString(cal);
	}
	
	//특정 일수 이전/이후 날짜 얻기
 	public static String getDate(String date,int day){
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(date.substring(0,4)), 
				Integer.parseInt(date.substring(4,6))-1, 
				Integer.parseInt(date.substring(6)));
		cal.add(Calendar.DATE, day);
		return dateString(cal);
	}
	
 	//주식관련 월요일 체크
	public static Boolean isMonday(String date){
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(date.substring(0,4)), 
				Integer.parseInt(date.substring(4,6))-1, 
				Integer.parseInt(date.substring(6)));
		if(cal.get(Calendar.DAY_OF_WEEK) == 2){
			return true;
		}
		return false;
	}
	
	//단일 Map의 keyset으로 카운팅
	public static Map<String,Integer> countWord(Set<String> keySet){
		Map<String,Integer> map = new HashMap<String,Integer>();
		Iterator<String> iter = keySet.iterator();
		String t = "";
		while (iter.hasNext()) {
			t = iter.next();
			if (map.containsKey(t)) {
				int cnt = map.get(t);
				map.replace(t, cnt, cnt + 1);
			} else {
				map.put(t, 1);
			}
		}
		return map;
	}
	
	//단일 Map keyset의 List로 카운팅
	public static Map<String,Integer> countWord(List<Set<String>> keySetList){
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(Set<String> keySet : keySetList){
			Iterator<String> iter = keySet.iterator();
			String t = "";
			while (iter.hasNext()) {
				t = iter.next();
				if (map.containsKey(t)) {
					int cnt = map.get(t);
					map.replace(t, cnt, cnt + 1);
				} else {
					map.put(t, 1);
				}
			}
		}
		return map;
	}
	
	//List에 들어있는 모든 Map 정보 합치기
	public static Map<String, Integer> mergeMap(List<Map<String, Integer>> mapList) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (Map<String, Integer> obj : mapList) {
			Iterator<String> iter = obj.keySet().iterator();
			String key = "";
			while (iter.hasNext()) {
				key = iter.next();
				if (map.containsKey(key)) {
					int cnt = map.get(key);
					map.replace(key, cnt, cnt + obj.get(key));
				} else {
					map.put(key, obj.get(key));
				}
			}
		}
		return map;
	}

	//단어맵 정렬 - 반환타입 : key 리스트
	public static List<String> countSort(Map map){
		List<String> list = new ArrayList<String>();
		list.addAll(map.keySet());
		Collections.sort(list,new Comparator() {
			public int compare(Object o1,Object o2) {
				Object v1 = map.get(o1);
				Object v2 = map.get(o2);
				
				return ((Comparable) v2).compareTo(v1);
			}
			
		});
		
		return list;
	}
	
	//단어맵 정렬 - 반환타입 : map 리스트
	public static Map<String, Integer> sortMap(Map<String, Integer> map) {
		List<String> list = new ArrayList<String>();
		list.addAll(map.keySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				Object v1 = map.get(o1);
				Object v2 = map.get(o2);

				return ((Comparable) v2).compareTo(v1);
			}

		});
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for(String key : list){
			int value = map.get(key);
			sortedMap.put(key, value);
		}
		return sortedMap;
	}
		
	//장 열린 일자 얻기
	public static List<String> getOpenDate(){
		List<String> dateRange = new ArrayList<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(RESOURCE.getURI().getPath().substring(1)));
			String data = "";
			String text = "";
			while ((text = br.readLine()) != null) {
				data += text;
			}
			br.close();
			String[] date = data.split(",");
			for(int i=0; i<date.length; i++){
				dateRange.add(date[i].trim());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return dateRange;
	}
	
	//장 열린 일자 Set 얻기
	public static Set<String> getOpenDate(Set<String> dateSet){
		Set<String> realDateSet = new HashSet<String>();
		realDateSet.addAll(dateSet);
		Iterator<String> dateIter = dateSet.iterator();
		String lastDate = realDate.get(realDate.size()-1);
		while(dateIter.hasNext()){
			String date = dateIter.next();
			if(realDate.contains(date)){
				continue;
			}else{
				realDateSet.remove(date);
				int i=1;
				while(true){
					date = getDate(date,i);
					if(realDate.contains(date)){
						realDateSet.add(date);
						break;
					}else if(Integer.parseInt(date) > Integer.parseInt(lastDate)){
						break;
					}else{
						i++;
					}
				}
			}
		}
		return realDateSet;
	}
	
	//장 열린 일수 얻기
	public static int openDateCnt(String from, String to) {
		int cnt = 0;
		for (String date : realDate) {
			if (Integer.parseInt(from) <= Integer.parseInt(date) && Integer.parseInt(to) >= Integer.parseInt(date))
				cnt++;
		}
		return cnt;
	}
	
	//장이 열렸는지 확인
	public static boolean isOpen(String predicDate){
		if(realDate.contains(predicDate))
			return true;
		else
			return false;
	}
	
	//주말 및 공휴일과 같은 장이 열리지 않은 날을 위한 morpVO 합치기
	public static List<NewsMorpVO> mergeVO(NewsMorpVO morpVO, int predic, boolean checkOpen){
		List<NewsMorpVO> morpList = new ArrayList<NewsMorpVO>();
		String newsDate = morpVO.getNewsDate();
		String predicDate = getDate(morpVO.getNewsDate(), predic);
		morpList.add(morpVO);
		if(checkOpen){
			if(!isOpen(newsDate) && isOpen(predicDate)){
				morpList.addAll(mergeVO(new NewsMorpVO("D:\\PPT\\mining\\"+morpVO.getCategory()+getDate(morpVO.getNewsDate(), predic)+".json"),predic,true));// 전날 MorpVO
			}
		}
		return morpList;
	}
	
}
