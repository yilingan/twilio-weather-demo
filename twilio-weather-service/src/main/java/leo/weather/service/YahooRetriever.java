package leo.weather.service;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

public class YahooRetriever {

    private static Logger log = Logger.getLogger(YahooRetriever.class);

    private final static String yahooApi = "http://weather.yahooapis.com/forecastrss?p=%s";

    public InputStream retrieve(String zipcode) throws Exception {
        log.info("Retrieving Weather Data");
        URLConnection conn = new URL(String.format(yahooApi, zipcode)).openConnection();
        return conn.getInputStream();
    }
}
