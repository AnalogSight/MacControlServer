package com.hyeok.maccontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hyeok.melon.MelonSearch;
import com.hyeok.melon.MelonSong;

public class Command {
	private String command;
	
	public Command(String command) {
		this.command = command;
	}

	public void commandwork() {
		if(command.equals("playpause")) {
				StartCommand("say 음악을 재생합니다.");
				StartCommand(new String[] {"osascript", "-e", "tell application \"iTunes\" to play"});
		} else if(command.equals("prev")) {
			StartCommand(new String[] {"osascript", "-e", "tell application \"iTunes\" to previous track"});
		} else if(command.equals("next")) {
			StartCommand(new String[] {"osascript", "-e", "tell application \"iTunes\" to next track"});
		} else if(command.equals("stop")) {
				StartCommand("say 음악을 일시정지합니다.");
				StartCommand(new String[] {"osascript", "-e", "tell application \"iTunes\" to pause"});
		} else if(command.contains("틀어줘")) {
			
				MelonSearch search = MelonSearch.getinstance();
				search.setOrder(MelonSearch.POPULAR);
				search.setSize(100);
				search.setSongName(command.replace(" 틀어줘", ""));
				search.Search();
				MelonSong melonsong = new MelonSong("11136919");
				try {
					melonsong.getSongData(search.getSIDList().get(0));
					StartCommand("open -a safari " + melonsong.getMusicURL());
					StartCommand("say " + melonsong.getSingerName()+"의 "+melonsong.getSongName() + "을 멜론에서 재생합니다.");
				} catch (IndexOutOfBoundsException e) {
					StartCommand("say " + command.replace(" 틀어줘", "") + "라는 음악이 없습니다.");
				}
		} else if(command.contains("currenttime")) {
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat format = new SimpleDateFormat("현재 시간은 MM월 dd일 hh시 mm분 입니다.");
				String msg = format.format(calendar.getTime());
				StartCommand("say " + msg);
		} else if(command.contains("찾아줘")) {
				String URL = String.format(StringUtil.NAVER_SEARCH_ENGINE, command.replace("찾아줘", "").replace(" ", ""));
				StartCommand("open -a safari "+ URL);
		} else if(command.equals("svoldown")) {
				StartCommand(new String[] {"osascript", "-e", "set volume output volume ((output volume of (get volume settings)) - 10)"});
		} else if(command.equals("svolup")) {
			StartCommand(new String[] {"osascript", "-e", "set volume output volume ((output volume of (get volume settings)) + 10)"});
		} else if(command.equals("svoldown!")) {
			StartCommand(new String[] {"osascript", "-e", "set volume output volume ((output volume of (get volume settings)) - 30)"});
		} else if(command.equals("svolup!")) {
			StartCommand(new String[] {"osascript", "-e", "set volume output volume ((output volume of (get volume settings)) + 30)"});
		} else if(command.equals("rtimekeyword")) {
			String msg = null;
			if((msg = DaumRtimeKeyword()) != null) {
				StartCommand("say "+msg);
			} else {
				StartCommand("say "+StringUtil.ERROR_RKEYWORD_CONNECTION);
			}
		} else if(command.equals("weather")) {
			String msg = null;
			if((msg = WeatherDescription()) != null) {
				StartCommand("say "+msg);
			} else {
				StartCommand("say "+StringUtil.ERROR_RKEYWORD_CONNECTION);
			}
		}
	}
	
	private boolean StartCommand(String[] command) {
		Process process;
		try {
			for(String strcommand : command) {
				System.out.print(strcommand);
				System.out.print(" ");
			}
			System.out.println("");
			process = Runtime.getRuntime().exec(command);
			Scanner inputscanner = new Scanner(process.getInputStream());
			Scanner errorscanner = new Scanner(process.getErrorStream());
			while(inputscanner.hasNext()) {
				System.out.println(inputscanner.nextLine());
			}
			while(errorscanner.hasNext()) {
				System.out.println(errorscanner.nextLine());
			}
			return true;
		} catch(IOException e) {
			System.out.println("명령어 실행에 실패하였습니다.");
			e.printStackTrace();
			return false;
		}
	}
		
		
	private boolean StartCommand(String command) {
		Process process;
		try {
			System.out.println(command);
			process = Runtime.getRuntime().exec(command);
			Scanner inputscanner = new Scanner(process.getInputStream());
			Scanner errorscanner = new Scanner(process.getErrorStream());
			while(inputscanner.hasNext()) {
				System.out.println(inputscanner.nextLine());
			}
			while(errorscanner.hasNext()) {
				System.out.println(errorscanner.nextLine());
			}
			return true;
		} catch(IOException e) {
			System.out.println("명령어 실행에 실패하였습니다.");
			e.printStackTrace();
			return false;
		}	
	}
	
	private String WeatherDescription() {
		URL url;
		try {
			url = new URL("http://www.kma.go.kr/weather/forecast/mid-term-rss3.jsp?stnId=109");
			InputStream stream = url.openStream();
			DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();
			Document doc = docBuild.parse(new InputSource(new InputStreamReader(stream, "UTF-8")));
			doc.getDocumentElement().normalize();
			NodeList DescriptionList = (NodeList)doc.getElementsByTagName("description");
			Element DescriptionElement = (Element)DescriptionList.item(1);
			NodeList HeaderNodeList = (NodeList)DescriptionElement.getElementsByTagName("header");
			Element HeaderElement = (Element)HeaderNodeList.item(0);
			NodeList WfNodeList = (NodeList)HeaderElement.getElementsByTagName("wf");
			Element WfElement = (Element)WfNodeList.item(0);
			return WfElement.getFirstChild().getNodeValue().replace("<br />", "").replace(".", ". ");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return null;
	}
	@SuppressWarnings("unused")
	private String DaumRtimeKeyword() {
		try {
			URL url = new URL("http://img.search.hanmail.net/jumpkeyword/API/REALTIME_ISSUE_TOTAL.xml");
			InputStream stream = url.openStream();
			DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();
			Document doc = docBuild.parse(new InputSource(new InputStreamReader(stream, "euc-kr")));
			doc.getDocumentElement().normalize();
			String time_str = doc.getDocumentElement().getAttribute("lastModified");
			System.out.println(String.format("%s년 %s월 %s일 %s시 %s분 %s초", time_str.substring(0,4), time_str.substring(4,6), time_str.substring(6,8), time_str.substring(8,10), time_str.substring(10,12), time_str.substring(12, 14)));
			NodeList WordNodeList = doc.getElementsByTagName("word");
			for(int i=0; i<WordNodeList.getLength(); i++) {
				Element WordElement = (Element) WordNodeList.item(i);
				NodeList rank = WordElement.getElementsByTagName("rank");
				NodeList keyword = WordElement.getElementsByTagName("keyword");
				NodeList value = WordElement.getElementsByTagName("value");
				NodeList type = WordElement.getElementsByTagName("type");
				Element rankElemnt = (Element) rank.item(0);
				Element keywordElement = (Element) keyword.item(0);
				Element valueElement = (Element) value.item(0);
				Element typeElement = (Element) type.item(0);
				String str_rank = rankElemnt.getFirstChild().getNodeValue();
				String str_keyword = keywordElement.getFirstChild().getNodeValue();
				String str_type = typeElement.getFirstChild().getNodeValue();
				String str_value = "";
				if (valueElement.getFirstChild() != null) {
					str_value = valueElement.getFirstChild().getNodeValue();
				}
				System.out.println(String.format("%s위 %s %s%s", str_rank, str_keyword, str_value, str_type));
				return String.format("%s위 %s", str_rank, str_keyword);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}