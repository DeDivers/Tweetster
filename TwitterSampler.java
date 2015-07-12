package D;
//This is not functional until the empty strings are filled in.
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.IOException;
import java.util.Arrays;
import java.net.URL;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.SortedSet;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;

public class TwitterSampler {
	private Twitter twit;
	private GeoLocation geo;
	
	
	public TwitterSampler() {
		//These empty strings are where the Consumer/ consumer hidden & 
		//User/User hidden tokens go
		String sa = "";
		String sah = "";
		String at = "";
		String ath = "";
		TwitterFactory tf = new TwitterFactory();
		twit = tf.getInstance();
		twit.setOAuthConsumer(sa, sah);
		twit.setOAuthAccessToken(new AccessToken(at, ath));
	}
	
	public void makeStatusUpdate(String st) throws TwitterException{
		StatusUpdate su = new StatusUpdate(st);
		if (geo != null) {
			su.setLocation(geo);
		}
		twit.updateStatus(su);
	}
	
	public void setGeoLocation(double lat, double longi) {
		geo = new GeoLocation(lat, longi);
	}
	
	public void setGeoLocation() {
		geo = null;
	}
	
	public List<Status> querySearch(String str, int e, String mode) throws TwitterException{
		Query query = new Query(str);
		if (!(geo == null)) {
			query.setGeoCode(geo, 10, Query.MILES);//Set at 10 mile radius by default. Change radius here.
		}
		int end = e;//how many tweets wanted to collect
		long lastID = Long.MAX_VALUE;
	    ArrayList<Status> tweets = new ArrayList<Status>();
	    query.setCount(100);
	    while (end > tweets.size()) { //Allows for more than 100 tweets to be collected at one time
	    	try {
	    		QueryResult result = twit.search(query);
	    		for (Status st: result.getTweets()) {
	    			if (mode.equals("w")) { //if the tweets wanted are just text use this
	    				if (!st.isRetweet()) {
	    					tweets.add(st);
	    				}
	    			} else if (mode.equals("p")) { // if the tweets wanted contain pictures use this
	    				if (!st.isRetweet() && st.getMediaEntities().length > 0) {
	    					tweets.add(st);
	    					System.out.println("Found:" + tweets.size());
	    				}
	    			}
	    		}
	    		//tweets.addAll(result.getTweets());
	    		for (Status t: tweets) {
	    			if (t.getId() < lastID) {
	    				lastID = t.getId();
	    			}
	    		}
	    	} catch (TwitterException t) {
	    		System.out.println("Couldn't connect: " + t);
	    	}
	    	query.setMaxId(lastID - 1);
	    }
	    return tweets;
	}
	
	public void writeTweets(List<Status> ls) throws Exception{ //Writes whole tweets to a text file
		//QueryResult qr = twit.search(query);
		//List<Status> ls = qr.getTweets();
		PrintStream pr = new PrintStream(new File("Tweets_of_the_day.txt"));//File name and location can be customized
		int x = 1;//used to count the number of tweets to discern them from one another
		
	    for (Status st: ls) {
	    	System.out.println(x + " " + "@" + st.getUser().getScreenName() + " " + st.getUser().getId() + " " + st.getUser().getName() + " - " + st.getText() + " " + st.getCreatedAt());
	    	//if (!st.isRetweet()) {
	    	//System.out.println(x + " " + "@" + st.getUser().getScreenName() + " " + st.getUser().getId() + " " + st.getUser().getName() + " - " + st.getText() + " " + st.getCreatedAt());
	    	pr.println(x + " " + "@" + st.getUser().getScreenName() + " " + st.getUser().getId() + " " + st.getUser().getName() + " - " + st.getText() + " " + st.getCreatedAt());
	    	x++;
	    	//					if (x == 501) {
	    	
	    	//						System.exit(0);
	    	//					}
	    	//}
	    }
	    pr.close();
	}
	
	public void wordCount(List<Status> ls) throws Exception {
		PrintStream oF = new PrintStream(new File("Words.txt"));//File name and location can be customized
		TreeMap<String, Integer> mappedStrings = new TreeMap<>();
		int x = 0;
		for (Status st: ls) {
			String message = st.getText();
			String[] messageSplit = message.split(" ");
			x++;
			for (String ms: messageSplit) {
				ms = ms.replaceAll("\\W", "");//Gets rid of all non-alphanumeric characters
				ms = ms.toLowerCase();
				if (ms.equals(" ") || ms.equals("")) {//Gets rid of empty or space strings
					System.out.print("");
				} else if (mappedStrings.containsKey(ms)) {
					mappedStrings.replace(ms, mappedStrings.get(ms) + 1);//Adds 1 to value if word is the same
				} else {
					mappedStrings.put(ms, 1); //Places the word in the map with an initial value of 1.
				}
			}
		}

		if (x == ls.size()) {//stops the printing to file from continuing
		// //
			for (String str: mappedStrings.keySet()) {
				oF.println(str + " : " + mappedStrings.get(str)); //Prints string and value to text file
			}
			oF.close();
		}
	}
	
	public void pictureSearch(List<Status> ls) throws Exception {
		int x = 1;//used to name the pictures in order
		for (Status st : ls) {
			MediaEntity[] me = st.getMediaEntities();
			//System.out.println(me);
			String[] kam = Arrays.toString(me).split(",");
			String[] spl = kam[2].split("=");
			String img_url = spl[1];
			//The above process extracts the image url from the Status
			URL url = new URL(img_url);
			InputStream is = url.openStream();
			OutputStream os = new FileOutputStream("Twitter\\" + x + ".jpg");//Output file location can be customized here
			byte[] by = new byte[2048];
			int length;
			while ((length = is.read(by)) != -1) {
				os.write(by, 0, length);
			}
			
			is.close();
			os.close();
			x++;
		}
	}
	
	public static void main(String[] args) throws Exception {
		TwitterSampler cs = new TwitterSampler();
		cs.wordCount(cs.querySearch("pizza", 500, "w"));
	}
}

