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
	
	@RequestMapping("/trainAnalyze.do")
	@ResponseBody
	public String trainAnalyze(String comName, String newsCode, String function, String from, String to){
		System.out.println(comName+"의 주가 예측 요청");
		String[] dateRange = Tool.dateRange(from, to);
		//list.addAll(aService.trainAnalyze(comName,newsCode,function,dateRange));
		
		return aService.trainAnalyzeWithMongo(comName,newsCode,function,dateRange);
	}
	
	@RequestMapping("/compare.do")
	@ResponseBody
	public String compare(String comName, String newsCode, String function, String from, String to){
		System.out.println(comName+"의 주가 예측 요청");
		String[] dateRange = Tool.dateRange(from, to);
		String result = aService.trainAnalyzeWithMongo(comName,newsCode,function,dateRange);
		result += "<br/>";
		result += aService.dTreeAnalyze(comName,newsCode,function,dateRange);
		return result;
	}
	
	@RequestMapping("/analyze.do")
	@ResponseBody
	public String analyze(String url, String comName, String newsCode, String function){
		System.out.println(comName+"의 주가 예측 요청");
		MorpVO morpVO = mService.getNewsMorp3(url);
		return  aService.analyze(morpVO, comName, newsCode, function);
	}
	
	@RequestMapping("/makeCSV.do")
	public String makeCSV(Model model){
		String newsCode = "economic";
		String from = "20160101";
		String to = "20170630";
		String[] dateRange = Tool.dateRange(from, to);
		String[] functions = {"pro1","pro2"};
		List<String> list = new ArrayList<String>();
		for(CompanyVO companyVO : sService.selectComList()){
			try {
				for(String function : functions){
					list.add(aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,function,dateRange));
				}
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
			System.out.println(companyVO.getName() +" 종료");
		}
		model.addAttribute("list", list);
		
		return "trainAnalyze";
	}
	
	@RequestMapping("/fit.do")
	public String tfidf(Model model){
		String newsCode = "economic";
		String[] fitDateRange = Tool.dateRange("20160101","20170630");
		String[] megDateRange = Tool.dateRange("20170701", "20170903");
		double[][] thres = {
				{0.5,2.5}, {1,3},{1.5,3.5},{2,4},{2.5,4.5},{3,5},{3.5,5.5},{4,6},{4.5,6.5}, //2
				{0.5,3.5}, {1,4},{1.5,4.5},{2,5},{2.5,5.5},{3,6},{3.5,6.5},{4,7},{4.5,7.5}, //3
				{0.5,4.5}, {1,5},{1.5,5.5},{2,6},{2.5,6.5},{3,7},{3.5,7.5},{4,8},{4.5,8.5}, //4
				{0.5,5.5}, {1,6},{1.5,6.5},{2,7},{2.5,7.5},{3,8},{3.5,8.5},{4,9},{4.5,9.5}, //5
				};
		String path = "D:\\PPT\\tfidf필터링기준.csv";
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
							int value = aService.getTfidfThreshold(companyVO.getName(),newsCode,"fit1",fitDateRange,thres[i][0],thres[i][1]);
							if(value>fit1Max){
								fit1Max = value;
								fit1From = thres[i][0];
								fit1To = thres[i][1];
							}
							System.out.println("from : " + thres[i][0] + ", to : " + thres[i][1] + ", value : " + value);
						}
						fos.write((fit1From+","+fit1To+",").getBytes("utf-8"));
						System.out.println(aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,"fit1",fitDateRange,fit1From,fit1To));
						
						int fit2Max = 0;
						double fit2From =0;
						double fit2To =0;
						for(int i=0; i<thres.length; i++){
							int value = aService.getTfidfThreshold(companyVO.getName(),newsCode,"fit2",fitDateRange,thres[i][0],thres[i][1]);
							if(value>fit2Max){
								fit2Max = value;
								fit2From = thres[i][0];
								fit2To = thres[i][1];
							}
							System.out.println("from : " + thres[i][0] + ", to : " + thres[i][1] + ", value : " + value);
						}
						fos.write((fit2From+","+fit2To+",").getBytes("utf-8"));
						aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,"fit2",fitDateRange,fit2From,fit2To);
						
						int meg1Max = 0;
						double meg1From =0;
						double meg1To =0;
						for(int i=0; i<thres.length; i++){
							int value = aService.getTfidfThreshold(companyVO.getName(),newsCode,"meg1",megDateRange,thres[i][0],thres[i][1]);
							if(value>meg1Max){
								meg1Max = value;
								meg1From = thres[i][0];
								meg1To = thres[i][1];
							}
						}
						fos.write((meg1From+","+meg1To+",").getBytes("utf-8"));
						aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,"meg1",megDateRange,meg1From,meg1To);
						
						int meg2Max = 0;
						double meg2From =0;
						double meg2To =0;
						for(int i=0; i<thres.length; i++){
							int value = aService.getTfidfThreshold(companyVO.getName(),newsCode,"meg2",megDateRange,thres[i][0],thres[i][1]);
							if(value>meg2Max){
								meg2Max = value;
								meg2From = thres[i][0];
								meg2To = thres[i][1];
							}
						}
						fos.write((meg2From+","+meg2To+"\n").getBytes("utf-8"));
						aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,"meg2",megDateRange,meg2From,meg2To);
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
	
	@RequestMapping("/test.do")
	public String anal(Model model){
		aService.fit = dService.selectTFIDFMongo("economic", 3, 7);
		aService.meg = dService.selectTFIDFMongo("economic", 3, 5);
		String newsCode = "economic";
		String from = "20170701";
		String to = "20170911";
		String[] dateRange = Tool.dateRange(from, to);
		String[] functions = {"opi1","opi2","pro1","pro2"};
		String path = "D:\\PPT\\analysis\\opi_pro.csv";
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			fos.write(("comName,opi1_before,opi2_before,pro1_before,pro2_before,opi1_after,opi2_after,pro1_after,pro2_after\n").getBytes("utf-8"));
			boolean go = false;
			for(CompanyVO companyVO : sService.selectComList()){
				try{
					if(companyVO.getName().equals("한국카본"))
						go=true;
					//if(go){
						fos.write((companyVO.getName()+",").getBytes("utf-8"));
						for(int i=0; i<3; i++){
							fos.write((aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,functions[i],dateRange)+",").getBytes("utf-8"));
							fos.write((aService.dTreeAnalyze(companyVO.getName(),newsCode,functions[i],dateRange)+",").getBytes("utf-8"));
						}
						fos.write((aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,functions[3],dateRange)+",").getBytes("utf-8"));
						fos.write((aService.dTreeAnalyze(companyVO.getName(),newsCode,functions[3],dateRange)+"\n").getBytes("utf-8"));
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
