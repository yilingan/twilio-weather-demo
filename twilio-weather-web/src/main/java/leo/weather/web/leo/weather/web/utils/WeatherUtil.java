package leo.weather.web.leo.weather.web.utils;

public class WeatherUtil {

    private WeatherUtil() {
    }

    public static boolean isZipValid(String zip) {
        if(zip == null || zip.equals("")) {
            return false;
        }

        String zipCodePattern = "\\d{5}(-\\d{4})?";
        return zip.matches(zipCodePattern);
    }

    public static String pickNum(String text) {
        if(text == null) {
            return text;
        }
        return text.replaceAll("\\D+", "");
    }
}
