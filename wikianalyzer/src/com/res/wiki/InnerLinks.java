package com.res.wiki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.res.common.Conf;

public class InnerLinks implements Conf {
	// find all inner links and print them
	
	private static final String WIKI_REG_LOCAITON = "https://en.wikipedia.org";
	
	private static final String FOLDER_LOCATION = DRIVE_LOCATION+":\\csp\\Wiki\\";
	
	private static final String BROWSER_AGENT = "Mozilla/17.0";
	private static final int TIMEOUT = 100000;
	
	private static final String PAGE_NOT_EXIST = "does not have an article";

	private static final int CAP_LIMIT = 10;
	private static final int SPECIAL_ALL_PAGES_CAP_LIMIT = 7000;
	
	private static Set<String> wikiLinks = new TreeSet<String>();

	private static String WIKI_BASE_URL = "https://en.wikipedia.org";
	private static String WIKI_SPECIAL_PAGE_LINKS_FILE = "wiki_special_page_links.csv";
	private static String WIKI_ALL_INNER_LINKS_FILE = "wiki_all_inner_links.csv";
	private static String WIKI_ALL_INNER_LINKS_SOURCE_FILE = "wiki_all_inner_links1.txt";
	private static String WIKI_DEAD_LINKS_DESTINATION_FILE = "wiki_dead_links.csv";
	//private static String WIKI_SPECIAL_PAGE_LINKS_SOURCE_FILE = "special_page_links.txt";
	private static String WIKI_INNER_LINKS_FILE = "wiki_inner_links.csv";
	private static String WIKI_SPECIAL_STARTING_PAGE = "/w/index.php?title=Special:AllPages&from=South+African+pigeon+grass";
	
	private static int counter = 0;
	private static int specialAllPagescounter = 1;
	public static void main(String[] args) throws Exception{
		
		//writeAllSpecialPages();
		
		//readSpecialPageLinkFile(new File(FOLDER_LOCATION+WIKI_SPECIAL_PAGE_LINKS_SOURCE_FILE));
		
		getAllDeadLinks(new File(FOLDER_LOCATION+WIKI_ALL_INNER_LINKS_SOURCE_FILE), new File(FOLDER_LOCATION+WIKI_DEAD_LINKS_DESTINATION_FILE));
		
		//storeAllPages();
		
		//storeDeadLinks("/w/index.php?title=Special:AllPages&from=%22FF.SS.%22+-+Cio%C3%A8%3A+%22...che+mi+hai+portato+a+fare+sopra+a+Posillipo+se+non+mi+vuoi+pi%C3%B9+bene%3F%22");
				
		System.out.println("DONE!");
	}
	
	// https://en.wikipedia.org/wiki/Special:AllPages
	@SuppressWarnings("unused")
	private static void writeAllSpecialPages() throws InterruptedException, IOException{		
		
		FileWriter fWriter = new FileWriter(new File(FOLDER_LOCATION+WIKI_SPECIAL_PAGE_LINKS_FILE).getAbsoluteFile());
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		
		try{
			getSpecialAllPages(WIKI_SPECIAL_STARTING_PAGE, bWriter);
		}catch(HttpStatusException e){
			//e.printStackTrace();
			System.out.println("Some error occurred : "+e.getMessage());
		}
		
		if (bWriter != null) bWriter.close();
	    if (fWriter != null) fWriter.close();
	}
	
	@SuppressWarnings("unused")
	private static void writeAllInnerLinksFromSpecialPages(String fullUrl) throws InterruptedException, IOException{
		
		
		
		FileWriter fWriter = new FileWriter(new File(FOLDER_LOCATION+WIKI_INNER_LINKS_FILE).getAbsoluteFile());
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		
		try{
			getInnerLinksFromSpecialPage(fullUrl, bWriter);
		}catch(HttpStatusException e){
			//e.printStackTrace();
			System.out.println("Some error occurred : "+e.getMessage());
		}
		
		if (bWriter != null) bWriter.close();
	    if (fWriter != null) fWriter.close();
	}
	
	private static void getSpecialAllPages(String url, BufferedWriter bWriter) throws InterruptedException, IOException{
		
		if(specialAllPagescounter >= SPECIAL_ALL_PAGES_CAP_LIMIT){
			return;
		}
		
		specialAllPagescounter++;
		
		Document doc = Jsoup.connect(WIKI_BASE_URL+url).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
		for (Element link : doc.select("td.mw-allpages-nav a")) {
			if(link.ownText().startsWith("Next page")){
				System.out.println("["+specialAllPagescounter+"]"+link.attr("href"));
				
				bWriter.write(WIKI_BASE_URL+link.attr("href"));	
				bWriter.newLine();
				
				getSpecialAllPages(link.attr("href"), bWriter);
			}
        }
	}
	
	static int innerLinkCounter = 0;
	private static void getInnerLinksFromSpecialPage(String url, BufferedWriter bWriter) throws InterruptedException, IOException{
		String fullUrl = url; 
		Document doc = Jsoup.connect(fullUrl).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
				
		//System.out.println(fullUrl);
		
		for (Element link : doc.select("ul.mw-allpages-chunk li a")) {
			
			innerLinkCounter++;
			
			String fullLink = WIKI_REG_LOCAITON + link.attr("href"); 
			
			System.out.println("["+innerLinkCounter+"] " + fullLink);
			
			bWriter.write(fullLink);	
			bWriter.newLine();
        }
	}
	
	@SuppressWarnings("unused")
	private static void storeAllPages() throws InterruptedException, IOException{
		String url = "/wiki/Dopamine";		
		
		FileWriter fWriter = new FileWriter(new File(FOLDER_LOCATION+"wiki_pages.csv").getAbsoluteFile());
		BufferedWriter bWriter = new BufferedWriter(fWriter);
	
		readUrl(url, bWriter);
		
		if (bWriter != null) bWriter.close();
	    if (fWriter != null) fWriter.close();		
	}
	
	@SuppressWarnings("unused")
	private static void storeDeadLinks(String url) throws InterruptedException, IOException{
		//String url = "/wiki/Dopamine";		
		
		FileWriter fWriter = new FileWriter(new File(FOLDER_LOCATION+"wiki.csv").getAbsoluteFile());
		BufferedWriter bWriter = new BufferedWriter(fWriter);
	
		writeExternalLinks(url, bWriter);
		
		if (bWriter != null) bWriter.close();
	    if (fWriter != null) fWriter.close();		
	}
	
	private static void readUrl(String url, BufferedWriter bWriter){
		url = WIKI_REG_LOCAITON+url;
		
		if(url.contains("/wiki/Category"))
			return;
		
		if(counter > CAP_LIMIT){
			return;
		}
		
		try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
            
            //System.out.println(doc); // we can keep it for archiving purpose
            
            String content = doc.toString();
            
            if(content.contains(PAGE_NOT_EXIST)){
            	return;
            }
            
            // profile available            	
        	//System.out.println(url);
            	
        	// get inner links
            Elements links = doc.getElementsByTag("a");
            
            outerloop: for (Element element : links) {
				
            	//System.out.println(element.ownText());
            	String linkUrl = element.attr("href").toString();
            	
				if(linkUrl.startsWith("/wiki/")){
					
					if(linkUrl.contains("/wiki/Category") ||  
							linkUrl.contains("/wiki/Talk:") ||
							linkUrl.contains("/wiki/Portal:") ||
							linkUrl.contains("/wiki/Special:") ||
							linkUrl.contains("/wiki/Wikipedia:") ||
							linkUrl.contains("/wiki/Help:") ||
							linkUrl.contains("/wiki/Main_Page")
							)
						continue outerloop;
					
					counter++;
					
					System.out.println(linkUrl);
					
					String fullUrl = WIKI_REG_LOCAITON+linkUrl; 
					if(!wikiLinks.contains(fullUrl)){
						
						wikiLinks.add(fullUrl);
						
						int deadLinks = DeadLinks.getDeadLinks(fullUrl);
						//int deadLinks = 0;
						
						DeadLinks.writeExteralLinksIntoFile(fullUrl, bWriter);
						
						//bWriter.write(fullUrl+"|"+deadLinks);	
						//bWriter.newLine();
						
						//readUrl(linkUrl, bWriter);
					}
					// get local links
				}
			}
            
            
        } catch (Exception e) {
        	System.err.println("Error while reading web file "+e.getMessage());
            //e.printStackTrace();
        }
	}
	
	private static void writeExternalLinks(String url, BufferedWriter bWriter){
		url = WIKI_REG_LOCAITON+url;
		
		if(url.contains("/wiki/Category"))
			return;
		
		if(counter > CAP_LIMIT){
			return;
		}
		
		try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
            
            String content = doc.toString();
            
            if(content.contains(PAGE_NOT_EXIST)){
            	return;
            }
            	
        	// get inner links
            Elements links = doc.getElementsByTag("a");
            
            outerloop: for (Element element : links) {
				
            	//System.out.println(element.ownText());
            	String linkUrl = element.attr("href").toString();
            	
				if(linkUrl.startsWith("/wiki/")){
					
					if(linkUrl.contains("/wiki/Category") ||  
							linkUrl.contains("/wiki/Talk:") ||
							linkUrl.contains("/wiki/Portal:") ||
							linkUrl.contains("/wiki/Special:") ||
							linkUrl.contains("/wiki/Wikipedia:") ||
							linkUrl.contains("/wiki/Help:") ||
							linkUrl.contains("/wiki/Main_Page")
							)
						continue outerloop;
					
					counter++;
					
					System.out.println(linkUrl);
					
					String fullUrl = WIKI_REG_LOCAITON+linkUrl; 
					if(!wikiLinks.contains(fullUrl)){
						
						wikiLinks.add(fullUrl);
						
						DeadLinks.writeExteralLinksIntoFile(fullUrl, bWriter);						
					}
					// get local links
				}
			}
            
            
        } catch (Exception e) {
        	System.err.println("Error while reading web file "+e.getMessage());
            //e.printStackTrace();
        }
	}
	
	@SuppressWarnings("unused")
	private static void readSpecialPageLinkFile(File file) throws Exception{
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			
			FileWriter fWriter = new FileWriter(new File(FOLDER_LOCATION+WIKI_ALL_INNER_LINKS_FILE).getAbsoluteFile());
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
		    for(String line; (line = br.readLine()) != null; ) {
		    	//System.out.println(i+" ==> "+line);
		    	
		    	getInnerLinksFromSpecialPage(line, bWriter);
		    }
		    
		    if (bWriter != null) bWriter.close();
		    if (fWriter != null) fWriter.close();
		}
	}
	
	private static void getAllDeadLinks(File srcFile, File destFile) throws Exception{
		
		try(BufferedReader br = new BufferedReader(new FileReader(srcFile))) {
			
			FileWriter fWriter = new FileWriter(destFile.getAbsoluteFile());
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
		    for(String line; (line = br.readLine()) != null; ) {
		    	//System.out.println(i+" ==> "+line);
		    	int deadLinkCount = DeadLinks.getDeadLinks(line);
		    	System.out.println(line+" ==> "+deadLinkCount);
		    	bWriter.write(line+"|"+deadLinkCount);
		    	bWriter.newLine();
		    }
		    
		    if (bWriter != null) bWriter.close();
		    if (fWriter != null) fWriter.close();
		}
	}
}
