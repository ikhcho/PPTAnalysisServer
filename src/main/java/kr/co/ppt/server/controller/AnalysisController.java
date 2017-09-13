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
		String[] functions = {"pro1","pro2","fit1","fit2"};
		List<String> list = new ArrayList<String>();
		for(CompanyVO companyVO : sService.selectComList()){
			for(String function : functions){
				list.add(aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,function,dateRange));
			}
		}
		model.addAttribute("list", list);
		
		return "trainAnalyze";
	}
	
	@RequestMapping("/test.do")
	public String anal(Model model){
		aService.fit = dService.selectTFIDFMongo("economic", 3, 7);
		aService.meg = dService.selectTFIDFMongo("economic", 3, 5);
		String newsCode = "economic";
		String from = "20170701";
		String to = "20170828";
		String[] dateRange = Tool.dateRange(from, to);
		String[] functions = {"opi1","opi2","pro1","pro2","fit1","fit2","meg1","meg2"};
		String path = "D:\\PPT\\analysis\\result.csv";
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			fos.write(("comName,opi1,opi2,pro1,pro2,fit1,fit2,meg1,meg2\n").getBytes("utf-8"));
			for(CompanyVO companyVO : sService.selectComList()){
				try{
					fos.write((companyVO.getName()+",").getBytes("utf-8"));
					for(int i=0; i<7; i++){
						fos.write((aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,functions[i],dateRange)+",").getBytes("utf-8"));
					}
					fos.write((aService.trainAnalyzeWithMongo(companyVO.getName(),newsCode,functions[7],dateRange)+"\n").getBytes("utf-8"));
					fos.flush();
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
