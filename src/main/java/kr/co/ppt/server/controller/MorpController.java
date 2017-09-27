package kr.co.ppt.server.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.ppt.server.service.MorpService;
import kr.co.ppt.util.Tool;

@Controller
@RequestMapping("/morp")
public class MorpController {
	private boolean[] ableMethod = {true,true,true};
	private int requestCnt = 1;
	@Autowired
	MorpService mService;
	
	@RequestMapping("/reqMorp.do")
	public String reqMorp(String type, String data){
		System.out.println("요청");
		System.out.println("현재 요청인원 : " + requestCnt);
		System.out.println(data);
		try {
			data = URLEncoder.encode(data,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(ableMethod[0]){
			if(type.equals("text"))
				return "redirect:/morp/reqText1.do?text="+data;
			else if(type.equals("news"))
				return "redirect:/morp/reqNews1.do?url="+data;
		}else if(ableMethod[1]){
			if(type.equals("text"))
				return "redirect:/morp/reqText2.do?text="+data;
			else if(type.equals("news"))
				return "redirect:/morp/reqNews2.do?url="+data;
		}else if(ableMethod[2]){
			if (type.equals("text"))
				return "redirect:/morp/reqText3.do?text="+data;
			else if (type.equals("news"))
				return "redirect:/morp/reqNews3.do?url="+data;
		}else{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "redirect:/morp/reqMorp.do?type="+type+"&data="+data;
	}
	
	@RequestMapping("/reqText1.do")
	@ResponseBody
	public String reqText1(String text){
		System.out.println("reqText1요청");
		ableMethod[0] = false;
		requestCnt++;
		Map<String,Integer> morp = Tool.sortMap(mService.getTextMorp1(text).getMorp());
		JSONObject obj = new JSONObject(morp);
		requestCnt--;
		ableMethod[0] = true;
		return obj.toJSONString();
	}
	
	@RequestMapping("/reqText2.do")
	@ResponseBody
	public String reqText2(String text){
		System.out.println("reqText2요청");
		ableMethod[1] = false;
		requestCnt++;
		Map<String,Integer> morp = mService.getTextMorp2(text).getMorp();
		JSONObject obj = new JSONObject(morp);
		requestCnt--;
		ableMethod[1] = true;
		return obj.toJSONString();
	}
	@RequestMapping("/reqText3.do")
	@ResponseBody
	public String reqText3(String text){
		System.out.println("reqText3요청");
		ableMethod[2] = false;
		requestCnt++;
		Map<String,Integer> morp = mService.getTextMorp3(text).getMorp();
		JSONObject obj = new JSONObject(morp);
		requestCnt--;
		ableMethod[2] = true;
		return obj.toJSONString();
	}
	
	@RequestMapping("/reqNews1.do")
	@ResponseBody
	public String reqNews1(String url){
		System.out.println("reqNews1요청");
		ableMethod[0] = false;
		requestCnt++;
		Map<String,Integer> morp = mService.getNewsMorp1(url).getMorp();
		JSONObject obj = new JSONObject(morp);
		Iterator iter = morp.keySet().iterator();
		String result = "[";
		while(iter.hasNext()){
			String key = (String)iter.next();
			result += "{\"key\":\"";
			result += key;
			result += "\",\"value\":";
			result += (morp.get(key)*5)+"},";
		}
		result = result.substring(0, result.length()-1);
		result += "]";
		requestCnt--;
		ableMethod[0] = true;
		return result;
	}
	@RequestMapping("/reqNews2.do")
	@ResponseBody
	public String reqNews2(String url){
		System.out.println("reqNews2요청");
		ableMethod[1] = false;
		requestCnt++;
		Map<String,Integer> morp = mService.getNewsMorp2(url).getMorp();
		JSONObject obj = new JSONObject(morp);
		Iterator iter = morp.keySet().iterator();
		String result = "";
		while(iter.hasNext()){
			result += "\'";
			result += iter.next();
			result += "\',";
		}
		requestCnt--;
		ableMethod[1] = true;
		return obj.toJSONString();
	}
	
	@RequestMapping("/reqNews3.do")
	@ResponseBody
	public String reqNews3(String url){
		System.out.println("reqNews3요청");
		ableMethod[2] = false;
		requestCnt++;
		Map<String,Integer> morp = mService.getNewsMorp3(url).getMorp();
		JSONObject obj = new JSONObject(morp);
		Iterator iter = morp.keySet().iterator();
		String result = "";
		while(iter.hasNext()){
			result += "\'";
			result += iter.next();
			result += "\',";
		}
		requestCnt--;
		ableMethod[2] = true;
		return obj.toJSONString();
	}
	
	@RequestMapping("/reqFile.do")
	@ResponseBody
	public String reqFile(String text){
		return "";
	}
}
