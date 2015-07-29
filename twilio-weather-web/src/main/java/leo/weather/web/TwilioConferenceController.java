package leo.weather.web;

import com.twilio.sdk.verbs.Conference;
import com.twilio.sdk.verbs.Dial;
import com.twilio.sdk.verbs.Gather;
import com.twilio.sdk.verbs.Say;
import com.twilio.sdk.verbs.TwiMLException;
import com.twilio.sdk.verbs.TwiMLResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conference")
public class TwilioConferenceController {

    private final static String sayInputTemplate = "Please input 5 digits conference ID.";
    private final static String welcomeTemplate = "Welcome to conference room %s.";

    @RequestMapping(value = "/welcome",
            method = RequestMethod.GET,
            produces = "application/xml")
    public String welcome() {

        // Create a TwiML response and add our friendly message.
        final TwiMLResponse twiml = new TwiMLResponse();

        final Gather gather = new Gather();
        gather.setAction("join");  // Don't use /join
        gather.setNumDigits(5);
        gather.setMethod("GET");

        final Say say = new Say(sayInputTemplate);

        try {
            gather.append(say);
            twiml.append(gather);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }

        return twiml.toXML();
    }


    @RequestMapping(value = "/join",
            method = RequestMethod.GET,
            produces = "application/xml")
    public String join(@RequestParam(value = "Digits") String conferenceId) {

        // Create a TwiML response and add our friendly message.
        TwiMLResponse twiml = new TwiMLResponse();
        Say say = new Say(String.format(welcomeTemplate, conferenceId));
        Dial dial = new Dial();
        Conference conf = new Conference(conferenceId);

        try {
            twiml.append(say);
            dial.append(conf);
            twiml.append(dial);
        } catch (TwiMLException e) {
            e.printStackTrace();
        }

        return twiml.toXML();
    }
}
