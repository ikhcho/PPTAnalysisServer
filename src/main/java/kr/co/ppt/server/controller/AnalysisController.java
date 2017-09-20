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
		aService.fit = dService.selectTFIDFMongo("economic", 3.9, 6.1);
		aService.meg = dService.selectTFIDFMongo("economic", 4.1, 6.5);
		System.out.println(comName+"의 주가 예측 요청");
		String[] dateRange = Tool.dateRange(from, to);
		return aService.trainAnalyze(comName,newsCode,function,dateRange,false);
	}
	
	@RequestMapping("/compare.do")
	@ResponseBody
	public String compare(String comName, String newsCode, String function, String from, String to){
		System.out.println(comName+"의 주가 예측 요청");
		String[] dateRange = Tool.dateRange(from, to);
		String result = aService.trainAnalyze(comName,newsCode,function,dateRange,false);
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
	
	@RequestMapping("/realtimeAnalyze.do")
	@ResponseBody
	public String analyze(String predicDate, String newsCode){
		aService.fit = dService.selectTFIDFMongo("economic", 3.9, 6.1);
		aService.meg = dService.selectTFIDFMongo("economic", 4.1, 6.5);
		return  aService.realtimeAnalyze(predicDate, newsCode).toJSONString();
	}
	
	@RequestMapping("/makeCSV.do")
	public String makeCSV(Model model){
		aService.fit = dService.selectTFIDFMongo("economic", 3.9, 6.1);
		aService.meg = dService.selectTFIDFMongo("economic", 4.1, 6.5);
		String newsCode = "economic";
		String[] dateRange = Tool.dateRange("20160101", "20170630");
		String[] dateRange2 = Tool.dateRange("20170701", "20170911");
		List<String> list = new ArrayList<String>();
		for(CompanyVO companyVO : sService.selectComList()){
			try {
					list.add(aService.trainAnalyze(companyVO.getName(),newsCode,"fit1",dateRange,true));
					list.add(aService.trainAnalyze(companyVO.getName(),newsCode,"fit2",dateRange,true));
					list.add(aService.trainAnalyze(companyVO.getName(),newsCode,"meg1",dateRange2,true));
					list.add(aService.trainAnalyze(companyVO.getName(),newsCode,"meg2",dateRange2,true));
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
			System.out.println(companyVO.getName() +" 종료");
		}
		model.addAttribute("list", list);
		
		return "trainAnalyze";
	}
	
	
	@RequestMapping("/test.do")
	public String anal(Model model){
		aService.fit = dService.selectTFIDFMongo("economic", 3.9, 6.1);
		aService.meg = dService.selectTFIDFMongo("economic", 4.1, 6.5);
		String newsCode = "economic";
		String from = "20170701";
		String to = "20170911";
		String[] dateRange = Tool.dateRange(from, to);
		String[] functions = {"opi1","opi2","pro1","pro2","fit1","fit2","meg1","meg2"};
		String path = "D:\\PPT\\analysis\\opi_pro.csv";
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
							fos.write((aService.trainAnalyze(companyVO.getName(),newsCode,functions[i],dateRange,false)+",").getBytes("utf-8"));
							fos.write((aService.dTreeAnalyze(companyVO.getName(),newsCode,functions[i],dateRange)+",").getBytes("utf-8"));
						}
						fos.write((aService.trainAnalyze(companyVO.getName(),newsCode,functions[7],dateRange,false)+",").getBytes("utf-8"));
						fos.write((aService.dTreeAnalyze(companyVO.getName(),newsCode,functions[7],dateRange)+"\n").getBytes("utf-8"));
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
