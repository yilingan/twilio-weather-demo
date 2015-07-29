package leo.weather.web;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Play;
import com.twilio.sdk.verbs.Record;
import com.twilio.sdk.verbs.Redirect;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import leo.weather.model.Weather;
import leo.weather.service.YahooParser;
import leo.weather.service.YahooRetriever;
import leo.weather.web.leo.weather.web.utils.WeatherUtil;

@RestController
@RequestMapping("/voice")
public class TwilioVoiceController {

    private static Logger log = Logger.getLogger(TwilioVoiceController.class);

    private final static String WELCOME = "Thank you for calling Weather Service. Please say 5-digits zip code.";
    private final static String WAIT = "Please wait.";
    private final static String THANK_YOU = "Thank you for calling, Goodbye!";
    private final static String OK = "OK";
    private final static String TRY_ANOTHER_WAY = "Sorry, I didn't get you. Let's try another way, please input 5-digits zip code.";
    private final static String NOT_COMPLETE = "Sorry, we can't complete your call. Goodbye.";
    private final static String GOODBYE = "Sorry, we can't get you. Goodbye.";

    private final static int SAY_SECONDS = 5;

    // Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN at program parameters, or os env, or application.properties.
    @Value("${TWILIO_ACCOUNT_SID}")
    private String ACCOUNT_SID;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String AUTH_TOKEN;

    @RequestMapping(value = "/welcome",
            method = RequestMethod.GET,
            produces = "application/xml")
    public String welcome(@RequestParam("CallSid") String callSid) {

        log.info("Hit welcome(). CallSID: " + callSid);

        // Create a TwiML response and add our friendly message.
        final TwiMLResponse twiml = new TwiMLResponse();

        final Say say = new Say(WELCOME);

        final Record record = new Record();
        record.setAction("record");
        record.setMethod("GET");
        record.setMaxLength(SAY_SECONDS);
        record.setPlayBeep(false);
        record.setTranscribe(true);
        record.setTranscribeCallback("transcribe");

        try {
            twiml.append(say);
            twiml.append(record);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }

        return twiml.toXML();
    }

    @RequestMapping(value = "/record",
            method = RequestMethod.GET,
            produces = "application/xml")
    @ResponseBody
    public String record(@RequestParam("CallSid") String callSid,
                         @RequestHeader(value = "Host", defaultValue = "localhost:8080") String host) {

        log.info("Hit record(). CallSID: " + callSid);

        // Play hold music to caller, and wait for transcription is done and call /transcribe
        final TwiMLResponse twiml = new TwiMLResponse();

        final Say say = new Say(WAIT);

        final Play play = new Play("http://" + host + "/moh.ogg"); // ~15 seconds

        // In case of no response of /transcribe from Twilio, timeout and typing.
        final Redirect redirect = new Redirect("type");
        redirect.setMethod("GET");

        try {
            twiml.append(say);
            twiml.append(play);
            twiml.append(redirect);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }

        return twiml.toXML();
    }

    @RequestMapping(value = "/transcribe",
            method = RequestMethod.POST,
            produces = "application/xml")
    public String transcribe(@RequestParam(value = "CallSid", required = true) String callSid,
                             @RequestParam(value = "TranscriptionText", defaultValue = "(null)") String text,
                             @RequestHeader(value = "Host", defaultValue = "localhost") String host) {

        log.info("Hit transcribe(). CallSID: " + callSid + ", TranscriptionText: " + text);

        String zip = WeatherUtil.pickNum(text);

        String redirectTo = null;

        if (WeatherUtil.isZipValid(zip)) {  // Get number only.
            redirectTo = "http://" + host + "/voice/say/" + zip;
        } else {
            // Didn't get zip, need typing.
            redirectTo = "http://" + host + "/voice/type";
        }

        try {
            redirectCall(callSid, redirectTo);
            return makeTwilioSayResponse(OK).toXML();
        } catch (TwilioRestException e) {
            e.printStackTrace();
            return makeTwilioSayResponse(NOT_COMPLETE).toXML();
        }
    }

    @RequestMapping(value = "/say/{zip}",
            method = RequestMethod.GET,
            produces = "application/xml")
    public String sayZipcode(@RequestParam("CallSid") String callSid,
                             @PathVariable("zip") String zip) {

        log.info("Hit sayZipcode(). CallSID: " + callSid + ", zip: " + zip);

        final String msg = fetchWeather(zip);
        final TwiMLResponse response = makeTwilioSayResponse(msg, THANK_YOU);

        return response.toXML();
    }

    @RequestMapping(value = "/type",
            method = RequestMethod.GET,
            produces = "application/xml")
    @ResponseBody
    public String typeZipcode(@RequestParam("CallSid") String callSid) {

        log.info("Hit typeZipcode(). CallSID: " + callSid);

        // Create a TwiML response and add our friendly message.
        final TwiMLResponse twiml = new TwiMLResponse();

        final Gather gather = new Gather();
        gather.setAction("type_done");
        gather.setNumDigits(5);
        gather.setMethod("GET");

        final Say say = new Say(TRY_ANOTHER_WAY);

        try {
            gather.append(say);
            twiml.append(gather);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }

        return twiml.toXML();
    }

    @RequestMapping(value = "/type_done",
            method = RequestMethod.GET,
            produces = "application/xml")
    @ResponseBody
    public String typeZipcodeDone(@RequestParam("CallSid") String callSid, @RequestParam("Digits") String zip) {

        log.info("Hit typeZipcodeDone(). CallSID: " + callSid + ", zip: " + zip);

        final String msg = fetchWeather(zip);
        final TwiMLResponse response = makeTwilioSayResponse(msg, THANK_YOU);

        return response.toXML();
    }

    /**
     * Return a string of inquired weather.
     */
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
                log.error("Can't get weather data or format data.", e);
            }
        }

        return msg;
    }

    private String render(final Weather weather) throws Exception {
        final Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("voice.vm"));
        final VelocityContext context = new VelocityContext();
        context.put("weather", weather);
        final StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "", reader);
        return writer.toString();
    }

    /**
     * Make Say TwiML Reponse.
     */
    private TwiMLResponse makeTwilioSayResponse(String content) {
        final TwiMLResponse twiml = new TwiMLResponse();
        final Say say = new Say(content);
        try {
            twiml.append(say);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }
        return twiml;
    }

    private TwiMLResponse makeTwilioSayResponse(String content, String suffix) {
        final TwiMLResponse twiml = new TwiMLResponse();
        Say say = null;
        Say say2 = null;

        if (content != null) {
            say = new Say(content);
        }
        if (suffix != null) {
            say2 = new Say(suffix);
        }

        try {
            if (say != null) twiml.append(say);
            if (say2 != null) twiml.append(say2);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }
        return twiml;
    }

    private void redirectCall(String callSid, String redirectTo) throws TwilioRestException {

        log.info("Hit redirectCall(). CallSID: " + callSid + ", redirectTo: " + redirectTo);

        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

        // Get an object from its sid. If you do not have a sid,
        // check out the list resource examples on this page
        Call call = client.getAccount().getCall(callSid);
        // Build a filter for the CallList
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("Url", redirectTo));
        params.add(new BasicNameValuePair("Method", "GET"));
        call.update(params);
    }
}
