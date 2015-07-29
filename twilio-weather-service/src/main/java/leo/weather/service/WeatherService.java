package leo.weather.service;

import java.io.InputStream;

import leo.weather.model.Weather;

public class WeatherService {

    public WeatherService() {}

    public Weather retrieveForecast(String zip) throws Exception {
        // Retrieve Data
        InputStream dataIn = new YahooRetriever().retrieve(zip);

        // Parse Data
        Weather weather = new YahooParser().parse(dataIn);

        return weather;
    }
}
