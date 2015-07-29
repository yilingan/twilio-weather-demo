package leo.weather.web;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;

import leo.weather.model.Weather;
import leo.weather.service.YahooParser;
import leo.weather.service.YahooRetriever;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.twilio.sdk.verbs.Message;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

@RestController
@RequestMapping("/message")
public class TwilioMessageController {

    private static Logger log = Logger.getLogger(TwilioMessageController.class);

    @RequestMapping(value = "/welcome",
            method = RequestMethod.GET,
            produces = "application/xml")
    public String messageWelcome(@RequestParam(value="Body", required=true) String zip) {

        final String msg = fetchWeather(zip);

        return makeTwilioResponse(msg);
    }

    @RequestMapping(value = "/welcome",
            method = RequestMethod.POST,
            produces = "application/xml")
    public String messageWelcomeForm(@RequestParam Map<String, Object> payload) {

        final String zip = (String) payload.get("Body");

        final String msg = fetchWeather(zip);

        return makeTwilioResponse(msg);
    }

    private String fetchWeather(String zip) {
        String msg = null;

        if (zip == null || "".equals(zip)) {
            msg = "Only a zip code is accepted.";
        } else {
            try {
                // Retrieve Data
                InputStream dataIn = new YahooRetriever().retrieve(zip);
                // Parse Data
                Weather weather = new YahooParser().parse(dataIn);
                // Render Message
                msg = render(weather);
            } catch (Exception e) {
                msg = "Weather service is currently unavailable. Please try it later.";
                log.error("Can't get weater data or format data.", e);
            }
        }

        return msg;
    }

    private String makeTwilioResponse(String content) {
        final TwiMLResponse twiml = new TwiMLResponse();
        final Message message = new Message(content);
        try {
            twiml.append(message);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }

        return twiml.toXML();
    }

    private String render(final Weather weather) throws Exception {
        final Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("message.vm"));
        final VelocityContext context = new VelocityContext();
        context.put("weather", weather);
        final StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "", reader);
        return writer.toString();
    }

}
