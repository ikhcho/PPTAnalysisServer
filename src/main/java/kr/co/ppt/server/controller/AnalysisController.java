package kr.co.ppt.server.controller;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	@RequestMapping("/RTA.do")
	@ResponseBody
	public String getRTA(String comName, String newsCode, String callback){
		if(comName != null){
			if(callback == null)
				return aService.selectOneRTA(comName).toJSONString();
			else
				return  callback+"("+aService.selectOneRTA(comName).toJSONString()+")";
		}
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
		aService.insertRTA(aService.realtimeAnalyze(predicDate, newsCode));
		return  "끝";
	}
	
	@RequestMapping("/fit.do")
	public String tfidf(Model model, String newsCode){
		String[] dateRange = Tool.dateRange("20170101","20170630");
		double[][] thres = {
				{2.5,4.5},{3,5},{3.5,5.5},{4,6},{4.5,6.5}, //2
				{2.5,5.5},{3,6},{3.5,6.5},{4,7},{4.5,7.5}, //3
				{2.5,6.5},{3,7},{3.5,7.5},{4,8},{4.5,8.5}, //4
				{2.5,7.5},{3,8},{3.5,8.5},{4,9},{4.5,9.5}, //5
				};
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
						fos.write((companyVO.getName()+",").getBytes("utf-8"));
						int fit1Max = 0;
						double fit1From =0;
						double fit1To =0;
						for(int i=0; i<thres.length; i++){
							int value = aService.getTfidfThreshold(companyVO.getName(),newsCode,"fit1",dateRange,thres[i][0],thres[i][1]);
							if(value>fit1Max){
								fit1Max = value;
								fit1From = thres[i][0];
								fit1To = thres[i][1];
							}
							System.out.println("from : " + thres[i][0] + ", to : " + thres[i][1] + ", value : " + value);
						}
						fos.write((fit1From+","+fit1To+",").getBytes("utf-8"));
						
						int fit2Max = 0;
						double fit2From =0;
						double fit2To =0;
						for(int i=0; i<thres.length; i++){
							int value = aService.getTfidfThreshold(companyVO.getName(),newsCode,"fit2",dateRange,thres[i][0],thres[i][1]);
							if(value>fit2Max){
								fit2Max = value;
								fit2From = thres[i][0];
								fit2To = thres[i][1];
							}
							System.out.println("from : " + thres[i][0] + ", to : " + thres[i][1] + ", value : " + value);
						}
						fos.write((fit2From+","+fit2To+",").getBytes("utf-8"));
						
						int meg1Max = 0;
						double meg1From =0;
						double meg1To =0;
						for(int i=0; i<thres.length; i++){
							int value = aService.getTfidfThreshold(companyVO.getName(),newsCode,"meg1",dateRange,thres[i][0],thres[i][1]);
							if(value>meg1Max){
								meg1Max = value;
								meg1From = thres[i][0];
								meg1To = thres[i][1];
							}
							System.out.println("from : " + thres[i][0] + ", to : " + thres[i][1] + ", value : " + value);
						}
						fos.write((meg1From+","+meg1To+",").getBytes("utf-8"));
						
						int meg2Max = 0;
						double meg2From =0;
						double meg2To =0;
						for(int i=0; i<thres.length; i++){
							int value = aService.getTfidfThreshold(companyVO.getName(),newsCode,"meg2",dateRange,thres[i][0],thres[i][1]);
							if(value>meg2Max){
								meg2Max = value;
								meg2From = thres[i][0];
								meg2To = thres[i][1];
							}
							System.out.println("from : " + thres[i][0] + ", to : " + thres[i][1] + ", value : " + value);
						}
						fos.write((meg2From+","+meg2To+"\n").getBytes("utf-8"));
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
		//aService.fit = dService.selectTFIDFMongo(newsCode, 3.9, 6.1);
		//aService.meg = dService.selectTFIDFMongo(newsCode, 4.1, 6.5);
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
		String path = "D:\\PPT\\analysis\\"+newsCode+"_responsibility.csv";
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
