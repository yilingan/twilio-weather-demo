package leo.weather.web.leo.weather.web.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WeatherUtilTest {

    @Test
    public void testIsZipValid() {
        assertTrue(WeatherUtil.isZipValid("95050"));

        assertTrue(WeatherUtil.isZipValid("95050-1234"));

        assertFalse(WeatherUtil.isZipValid("950506"));

        assertFalse(WeatherUtil.isZipValid("acd9987"));
    }

    @Test
    public void testPickNumber() {

        assertEquals("95050", WeatherUtil.pickNum("95050"));

        assertEquals("95050", WeatherUtil.pickNum(" 95050 "));

        assertEquals("95050", WeatherUtil.pickNum(" 95050 Hey"));

        assertEquals("950506", WeatherUtil.pickNum("950506"));

        assertEquals("95050", WeatherUtil.pickNum("uui95d050x"));

        assertEquals("", WeatherUtil.pickNum(""));

        assertNull(WeatherUtil.pickNum(null));

    }
}
