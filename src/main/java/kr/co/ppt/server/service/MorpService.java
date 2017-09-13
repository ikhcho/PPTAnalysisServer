package kr.co.ppt.server.service;

import org.springframework.stereotype.Service;

import kr.co.ppt.crawler.JsoupDom;
import kr.co.ppt.crawler.UserReqDom;
import kr.co.ppt.morp.MorpDevider1;
import kr.co.ppt.morp.MorpDevider2;
import kr.co.ppt.morp.MorpDevider3;
import kr.co.ppt.morp.MorpVO;

@Service
public class MorpService {
	
	public MorpVO getTextMorp1(String text){
		MorpVO morpVO = new MorpVO();
		MorpDevider1 devider = new MorpDevider1();
		morpVO.setPlainText(text);
		morpVO.setMorp(devider.countNoun(text));
		return morpVO;
	}
	
	public MorpVO getNewsMorp1(String url){
		JsoupDom userReqDom = new UserReqDom(url);
		String content = userReqDom.getContent();
		return getTextMorp1(content);
	}
	
	public MorpVO getTextMorp2(String text){
		MorpVO morpVO = new MorpVO();
		MorpDevider2 devider = new MorpDevider2();
		morpVO.setPlainText(text);
		morpVO.setMorp(devider.countNoun(text));
		return morpVO;
	}
	
	public MorpVO getNewsMorp2(String url){
		JsoupDom userReqDom = new UserReqDom(url);
		String content = userReqDom.getContent();
		return getTextMorp2(content);
	}
	
	public MorpVO getTextMorp3(String text){
		MorpVO morpVO = new MorpVO();
		MorpDevider3 devider = new MorpDevider3();
		morpVO.setPlainText(text);
		morpVO.setMorp(devider.countNoun(text));
		return morpVO;
	}
	
	public MorpVO getNewsMorp3(String url){
		JsoupDom userReqDom = new UserReqDom(url);
		String content = userReqDom.getContent();
		return getTextMorp3(content);
	}
	
}
