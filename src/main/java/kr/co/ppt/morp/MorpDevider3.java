package kr.co.ppt.morp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpRequest;

import kr.co.ppt.util.Tool;
import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;
import kr.co.shineware.util.common.model.Pair;

public class MorpDevider3 {
	private static final Resource RESOURCE = new ClassPathResource("/models-full");
	
	public Map<String,Integer> countNoun(String fileName, List<String> text){
		Map<String,Integer> map = new HashMap<String,Integer>();
		int size = text.size();
		for(String feed : text){
			try{
				devide(feed,map);
				System.out.println(fileName + " 남은개수 :" + --size);
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
		return map;
	}
	
	public Map<String,Integer> countNoun(String text){
		Map<String,Integer> map = new HashMap<String,Integer>();
		try {
			devide(text,map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Tool.sortMap(map);
	}
	
	private void devide(String text, Map<String,Integer> map) throws IOException{
		Komoran komoran = new Komoran(RESOURCE.getURI().getPath().substring(1));
		List<List<Pair<String, String>>> result = komoran.analyze(text);
		List<String> analyzeResultList = new ArrayList<String>();
		for (List<Pair<String, String>> eojeolResult : result) {
			for (Pair<String, String> wordMorph : eojeolResult) {
				if (wordMorph.getSecond().equals("NNG") || wordMorph.getSecond().equals("NNP")) {
					analyzeResultList.add(wordMorph.getFirst());
				}
			}
		}

		for (String t : analyzeResultList) {
			if (map.containsKey(t)) {
				int cnt = map.get(t);
				map.replace(t, cnt, cnt + 1);
			} else {
				map.put(t, 1);
			}
		}
	}
	
}
