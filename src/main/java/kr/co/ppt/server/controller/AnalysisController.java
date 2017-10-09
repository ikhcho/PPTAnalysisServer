package kr.co.ppt.server.controller;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.ppt.morp.MorpVO;
import kr.co.ppt.server.service.AnalysisService;
import kr.co.ppt.server.service.DictionaryService;
import kr.co.ppt.server.service.MorpService;
import kr.co.ppt.server.service.StockService;
import kr.co.ppt.stock.CompanyVO;
import kr.co.ppt.util.Tool;

@Controller
@RequestMapping("/analysis")
public class AnalysisController {

	@Autowired
	AnalysisService aService;
	@Autowired
	MorpService mService;
	@Autowired
	StockService sService;
	@Autowired
	DictionaryService dService;
	
	@RequestMapping("/getReliability.do")
	@ResponseBody
	public String getReliability(String comName, String newsCode, String anaCode,String userDic){
		String reliability = "";
		String from = "20170701";
		String to = "20170831";
		String[] dateRange = Tool.dateRange(from, to);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String today = sdf.format(new Date());
		JSONParser parser = new JSONParser();
		JSONArray userDicArr;
		try {
			userDicArr = (JSONArray)parser.parse(userDic);
			reliability = aService.myAnalyzeForRel(today, comName, newsCode, anaCode, userDicArr,dateRange);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reliability;
	}

	@RequestMapping("/insertUserDic.do")
	@ResponseBody
	public String insertUserDic(String comName, String newsCode, String anaCode,String userDic){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String today = sdf.format(new Date());
		JSONParser parser = new JSONParser();
		JSONArray userDicArr;
		try {
			userDicArr = (JSONArray)parser.parse(userDic);
			aService.insertMyAnalysis(aService.myAnalyzeWithFile(today, comName, newsCode, anaCode, userDicArr));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "완료";
	}
	
	@RequestMapping("/RTA.do")
	@ResponseBody
	public String getRTA(String comName, String newsCode, String callback){
		if(comName != null){
			if(callback == null)
				return aService.selectOneRTA(comName).toJSONString();
			else
				return  callback+"("+aService.selectOneRTA(comName).toJSONString()+")";
		}
		System.out.println("요청");
		return aService.selectAllRTA().toJSONString();
	}
	
	@RequestMapping("/trainAnalyze.do")
	@ResponseBody
	public String trainAnalyze(String comName, String newsCode, String anaCode, String from, String to){
		System.out.println(comName+"의 주가 예측 요청");
		String[] dateRange = Tool.dateRange(from, to);
		return aService.trainAnalyze(comName,newsCode,anaCode,dateRange,false);
	}
	
	@RequestMapping("/compare.do")
	@ResponseBody
	public String compare(String comName, String newsCode, String anaCode, String from, String to){
		System.out.println(comName+"의 주가 예측 요청");
		String[] dateRange = Tool.dateRange(from, to);
		String result = aService.trainAnalyze(comName,newsCode,anaCode,dateRange,false);
		result += "<br/>";
		result += aService.dTreeAnalyze(comName,newsCode,anaCode,dateRange);
		return result;
	}
	
	@RequestMapping("/analyze.do")
	@ResponseBody
	public String analyze(String url, String comName, String newsCode, String anaCode){
		System.out.println(comName+"의 주가 예측 요청");
		MorpVO morpVO = mService.getNewsMorp3(url);
		return  aService.analyze(morpVO, comName, newsCode, anaCode);
	}
	
	@RequestMapping("/realtimeAnalyze.do")
	@ResponseBody
	public String analyze(String predicDate, String newsCode){
		aService.insertRTA(aService.realtimeAnalyzeWithFile(predicDate, newsCode));
		return  "끝";
	}
	
	@RequestMapping("/insertReliability.do")
	@ResponseBody
	public String insertReliability(){
		aService.insertReliability();
		return "끝";
	}
	
	
	
	@RequestMapping("/fit.do")
	public String tfidf(Model model, String newsCode){
		String[] dateRange = Tool.dateRange("20170101","20170630");
		String path = "D:\\PPT\\"+newsCode+"_tfidf필터링기준.csv";
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			fos.write(("comName,fit1_from,fit1_to,fit2_from,fit2_to,meg1_from,meg1_to,meg2_from,meg2_to\n").getBytes("utf-8"));
			boolean go = false;
			for(CompanyVO companyVO : sService.selectComList()){
				try{
					if(companyVO.getName().equals("한국카본"))
						go=true;
					//if(go){
						String row = aService.getTfidfThreshold(companyVO.getName(), newsCode, dateRange);
						fos.write((companyVO.getName()+row).getBytes("utf-8"));
						fos.flush();
					//}
				}catch(Exception e){
					System.out.println(companyVO.getName());
					e.printStackTrace();
					continue;
				}
				System.out.println(companyVO.getName() +" 종료");
			}
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "trainAnalyze";
	}
	
	//훈련용 CSV만들기
	@RequestMapping("/makeCSV.do")
	public String makeCSV(Model model, String newsCode){
		String[] anaCodes = {"opi1","opi2","pro1","pro2","fit1","fit2","meg1","meg2"};
		String[] dateRange = Tool.dateRange("20170101", "20170630");
		List<String> list = new ArrayList<String>();
		for(CompanyVO companyVO : sService.selectComList()){
			for(String anaCode : anaCodes){
				try {
					list.add(aService.trainAnalyze(companyVO.getName(),newsCode,anaCode,dateRange,true));
				}catch(Exception e){
					e.printStackTrace();
					continue;
				}
			}
			System.out.println(companyVO.getName() +" 종료");
		}
		model.addAttribute("list", list);
		
		return "trainAnalyze";
	}
	
	//예측 CSV결과 만들기 신뢰성 자료
	@RequestMapping("/test.do")
	public String anal(Model model,String newsCode){
		String from = "20170701";
		String to = "20170831";
		String[] dateRange = Tool.dateRange(from, to);
		String[] anaCodes = {"opi1","opi2","pro1","pro2","fit1","fit2","meg1","meg2"};
		String path = "D:\\PPT\\analysis\\"+newsCode+"_reliability.csv";
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			fos.write(("comName,opi1_b,opi1_a,opi2_b,opi2_a,pro1_b,pro1_a,pro2_b,pro2_a,fit1_b,fit1_a,fit2_b,fit2_a,meg1_b,meg1_a,meg2_b,meg2_a\n").getBytes("utf-8"));
			boolean go = false;
			for(CompanyVO companyVO : sService.selectComList()){
				try{
					if(companyVO.getName().equals("한국카본"))
						go=true;
					//if(go){
						fos.write((companyVO.getName()+",").getBytes("utf-8"));
						for(int i=0; i<7; i++){
							fos.write((aService.trainAnalyze(companyVO.getName(),newsCode,anaCodes[i],dateRange,false)+",").getBytes("utf-8"));
							fos.write((aService.dTreeAnalyze(companyVO.getName(),newsCode,anaCodes[i],dateRange)+",").getBytes("utf-8"));
						}
						fos.write((aService.trainAnalyze(companyVO.getName(),newsCode,anaCodes[7],dateRange,false)+",").getBytes("utf-8"));
						fos.write((aService.dTreeAnalyze(companyVO.getName(),newsCode,anaCodes[7],dateRange)+"\n").getBytes("utf-8"));
						fos.flush();
					//}
				}catch(Exception e){
					System.out.println(companyVO.getName());
					e.printStackTrace();
					continue;
				}
				System.out.println(companyVO.getName() +" 종료");
			}
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "trainAnalyze";
	}
}
