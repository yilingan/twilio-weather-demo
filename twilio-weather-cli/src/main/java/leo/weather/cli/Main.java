package leo.weather.cli;

import java.io.InputStream;

import leo.weather.model.Weather;
import leo.weather.service.YahooParser;
import leo.weather.service.YahooRetriever;

import org.apache.log4j.PropertyConfigurator;


public class Main {

    public static void main(String[] args) throws Exception {
        // Configure Log4J
        PropertyConfigurator.configure(Main.class.getClassLoader().getResource("log4j.properties"));

        // Read the zip code from the command line
        // (if none supplied, use 94107)
        String zipcode = "94107";
        try {
            zipcode = args[0];
        } catch (Exception e) {
        }

        // Start the program
        new Main(zipcode).start();
    }

    private String zip;

    public Main(String zip) {
        this.zip = zip;
    }

    public void start() throws Exception {
        // Retrieve Data
        InputStream dataIn = new YahooRetriever().retrieve(zip);

        // Parse Data
        Weather weather = new YahooParser().parse(dataIn);

        // Format (Print) Data
        System.out.print(new WeatherFormatter().format(weather));
    }
}
