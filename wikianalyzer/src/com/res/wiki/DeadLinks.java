package com.res.wiki;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DeadLinks {
	
	private static final String BROWSER_AGENT = "Mozilla/17.0";
	private static final int TIMEOUT = 30000;
	
	private static final String PAGE_NOT_EXIST = "does not have an article";	
	private static final String PAGE_NOT_EXIST_1 = "The resource cannot be found";
	private static final String PAGE_NOT_EXIST_2 = "could not find the page";	
	
	public static void main(String[] args) throws InterruptedException{
		
		readUrl();
		
		System.out.println("DONE!");
	}
	
	public static int getDeadLinks(String url){
		
		int counter = 0;
		
		try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
            
            //System.out.println(doc); // we can keep it for archiving purpose
            
            String content = doc.toString();
            
            // profile available            	
        	//System.out.println(url);
            	
        	// get details
            Elements links = doc.getElementsByTag("a");
            
            outerloop: for (Element element : links) {
				
            	// external text
            	String linkClass = element.attr("class");            	
            	if(linkClass.equalsIgnoreCase("external text")){
            		String linkUrl = element.attr("href");
            		
            		//if(!linkUrl.contains("movementdisorders"))continue outerloop;
            			
            		if(validUrl(linkUrl)){
            			//System.out.println("Valid : ["+linkUrl+"]");
            			//System.out.print("..");
            		}else {
            			//System.out.println("");
            			//System.err.println("INVALID : ["+linkUrl+"]");
            			System.err.println(linkUrl);
            			counter++;
            		}
            	}
			}
        } catch (Exception e) {
        	System.err.println("Error while reading web file "+e.getMessage());
            e.printStackTrace();
        }
		
		return counter;
	}
	
	public static List<String> getExteralLinks(String url){
		
		List<String> externalLinks = new ArrayList<String>();
		int counter = 0;
		
		try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
            
            //System.out.println(doc); // we can keep it for archiving purpose
            
            String content = doc.toString();
            
            // profile available            	
        	//System.out.println(url);
            	
        	// get details
            Elements links = doc.getElementsByTag("a");
            
            outerloop: for (Element element : links) {
				
            	// external text
            	String linkClass = element.attr("class");            	
            	if(linkClass.equalsIgnoreCase("external text")){
            		String linkUrl = element.attr("href");
            		System.out.println(linkUrl);
            		
            		externalLinks.add(linkUrl);
            	}
			}
        } catch (Exception e) {
        	System.err.println("Error while reading web file "+e.getMessage());
            e.printStackTrace();
        }
		
		return externalLinks;
	}
	
	public static void writeExteralLinksIntoFile(String url, BufferedWriter bWriter){
		
		try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
            
        	// get details
            Elements links = doc.getElementsByTag("a");
            
            int counter = 0;
            outerloop: for (Element element : links) {
				
            	// external text
            	String linkClass = element.attr("class");            	
            	if(linkClass.equalsIgnoreCase("external text")){
            		
            		counter++;
            		
            		String linkUrl = element.attr("href");
            		System.out.println(linkUrl);
            		
            		if(counter == 1){
            			bWriter.write(url+"|");
            		}else {
            			bWriter.write(" "+"|"+linkUrl);
            		}
            		bWriter.newLine();
            	}
			}
        } catch (Exception e) {
        	System.err.println("Error while reading web file "+e.getMessage());
            //e.printStackTrace();
        }
	}
	
	private static void readUrl(){
		String url = "https://en.wikipedia.org/wiki/Dopamine";
		try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
            
            //System.out.println(doc); // we can keep it for archiving purpose
            
            String content = doc.toString();
            
            if(content.contains(PAGE_NOT_EXIST)){
            	return;
            }
            
            // profile available            	
        	//System.out.println(url);
            	
        	// get details
            Elements links = doc.getElementsByTag("a");
            
            outerloop: for (Element element : links) {
				
            	//System.out.println(element.ownText());
            	//String linkUrl = element.attr("href").toString();
            	
				//if(element.attr("href").toString().startsWith("/wiki/")){
					//System.out.println(element);	
					
					// get local links
				//}
            	
            	
            	// external text
            	String linkClass = element.attr("class");            	
            	if(linkClass.equalsIgnoreCase("external text")){
            		String linkUrl = element.attr("href");
            		
            		//if(!linkUrl.contains("movementdisorders"))continue outerloop;
            			
            		if(validUrl(linkUrl)){
            			//System.out.println("Valid : ["+linkUrl+"]");
            			//System.out.print("..");
            		}else {
            			//System.out.println("");
            			//System.err.println("INVALID : ["+linkUrl+"]");
            			System.err.println(linkUrl);
            		}
            	}
			}
            
            
        } catch (Exception e) {
        	System.err.println("Error while reading web file "+e.getMessage());
            e.printStackTrace();
        }
	}
	
	public static boolean validUrl(String url){
		try {
			
			// if no 'http', add one
			if(!url.startsWith("http")){
				url = "http:"+url;
			}
			
			
            Document doc = Jsoup.connect(url).followRedirects(false).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
            String content = doc.toString();
            
            // to find follow redirect
            /*
             * <html>
				 <head></head>
				 <body></body>
				</html>
             * 
             */
            if(content.contains("<body></body>")){
            	return testRedirectedLink(url);
            }
            
            //System.out.println(content);
            
        } catch(UnsupportedMimeTypeException e){
        	return true; // though pdf file can't be verified, make it valid
        } catch(HttpStatusException e){
        	return false;
        } catch (Exception e) {
        	System.err.println("Error while reading web file "+e.getMessage());
            e.printStackTrace();
            return false;
        }
		
		return true;
	}
	
	private static boolean testRedirectedLink(String url){
		try{
			//Document doc = Jsoup.connect(url).followRedirects(true).timeout(TIMEOUT).userAgent(BROWSER_AGENT).get();
            //String content = doc.toString();
            
			Response response = Jsoup.connect(url).followRedirects(true).timeout(TIMEOUT).userAgent(BROWSER_AGENT).execute();
			
            //System.out.println(response.statusCode() + " " + response.url());
			
			if(response.url().toString().contains("404")){
				return false;
			}
			
			Document doc = response.parse();
			
			if(doc.toString().contains(PAGE_NOT_EXIST) ||
					doc.toString().contains(PAGE_NOT_EXIST_1) ||
					doc.toString().contains(PAGE_NOT_EXIST_2)){
				return false;
			}
		}catch(UnsupportedMimeTypeException e){
			// do nothing
		}catch(Exception e){
			System.err.println(e.getMessage());
			//e.printStackTrace();
		}
		
		return true;
	}
}
