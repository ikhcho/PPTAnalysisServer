package kr.co.ppt.crawler;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.List;

import kr.co.ppt.morp.TypeThread;
import kr.co.ppt.util.Tool;

import org.jsoup.Jsoup;


public class CrawlerThread extends Thread{

	private String url;
	private String startDate;
	private int period;
	
	public CrawlerThread(String url, String startDate, int period) {
		super();
		this.url = url;
		this.startDate = startDate;
		this.period = period;
	}
	
	@Override
	public void run() {
	
		String log = "";
		int count = 0;
		String[] dateRange = Tool.dateRange(startDate, period);
		for(int i=0; i<period; i++){
			//daum news page document
			int page=1;
			long timeStart = System.currentTimeMillis();
			try{
				String category = url.split("breakingnews/")[1].replace("?", "");
				FileOutputStream fos = new FileOutputStream("D:\\PPT\\news\\" + category + dateRange[i] + ".json");
				String content="";
				while(true){
					//origin news document
					DaumNewsDom daum = new DaumNewsDom();
					daum.setDom(Jsoup.connect(url + "page=" + page + "&regDate=" + dateRange[i]).get());
					if(daum.hasContent()){
						System.out.println(url + "page=" + page + "&regDate=" + dateRange[i]+" - 끝");
						fos.close();
						break;
					}else{
						List<String> hrefList = daum.getHref();
						for (String href : hrefList) {
							try {
								DaumNewsDom news = new DaumNewsDom();
								news.setDom(Jsoup.connect(href).get());
								content="";
								if(news.getContent().equals("")){
									continue;
								}else{
									content += "{\"category\" : \"" + category 
											+ "\", \"newsDate\" : \"" + news.getWriteDate().split(" ")[1].replaceAll("\\.", "")
											+ "\", \"time\" : \"" + news.getWriteDate().split(" ")[2]
													+ "\", \"link\" : \"" + href
													+ "\", \"content\" : \"" + news.getContent() + "\"},";
									byte[] b = content.getBytes("utf-8");
									fos.write(b);
									fos.flush();
									count++;
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								System.out.println("=====에러URL=====");
								System.out.println(href);
								System.out.println(e1.getMessage());
								log += "에러 : " + href + "\r\n";
								log += e1.getMessage().toString() + "\r\n";
								continue;
							}
						}
						page++;
					}
					if(page%50 == 0){
						System.out.println(url + "page=" + page + "&regDate=" + dateRange[i]);
					}
				}
			}catch (Exception e2){
				e2.printStackTrace();
				continue;
			}
			try {
				long timeEnd = System.currentTimeMillis();
				System.out.println(count + "건 : " + ((timeEnd - timeStart) / 1000) + "sec");
				FileOutputStream logFile = new FileOutputStream("D:\\PPT\\log\\" + url.split("breakingnews/")[1].replace("?", "") + dateRange[i] + "_log.txt");
				log = count + "건 : " + ((timeEnd - timeStart) / 1000.0) + "sec\r\n" + log;
				byte[] b = log.getBytes("utf-8");
				logFile.write(b);
				logFile.flush();
				logFile.close();
				log="";//초기화
				count = 0;//초기화
				//마지막 ,으로 끝나는지 체크
				String category = url.split("breakingnews/")[1].replace("?", "");
				FileReader fr = new FileReader("D:\\PPT\\news\\" + category + dateRange[i] + ".json");
				BufferedReader br = new BufferedReader(fr);
				String text="";
				String data="";
				while((text = br.readLine()) != null){
					data += text;
				}
				if(data.substring(data.length()-1, data.length()).equals(",")){
					data = data.substring(0, data.length()-1);
					FileOutputStream fos = new FileOutputStream("D:\\PPT\\news\\" + category + dateRange[i] + ".json");
					fos.write(data.getBytes("utf-8"));
					fos.flush();
					fos.close();
				}
				TypeThread t1 = new TypeThread(category, dateRange[i], 1);
				t1.start();
			} catch (Exception e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
		}
	}
}
