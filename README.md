# Twilio-Weathr-Demo

This is a demonstration of integrating Twilio Voice/SMS API and Yahoo Weather Service.

## Installation

```console
$ cd twilio-weather-demo
$ mvn package
$ java -jar twilio-weather-web/target/twilio-weather-web-1.0-SNAPSHOT.jar --TWILIO_ACCOUNT_SID=123456789 --TWILIO_AUTH_TOKEN=987654321
```

Please replace the TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN with your twilio account SID and authen token.

## Usage

1. Buy your twilio number from www.twilio.com
2. Set Request URL of Voice for your twilio number to http://<<IP_OR_FQDN>>:8080/voice/welcome, and HTTP method is GET.
3. Set Request URL of SMS/MMS for your twilio number to http://<<IP_OR_FQDN>>:8080/message/welcome, and HTTP method is GET.
4. Text a zip code or call your twilio number.

## Notes

The weather data is provided by Yahoo.

The modules of weather-model and weather-service is contributed from <i>Maven by Example</i>:

http://books.sonatype.com/mvnex-book/reference/index.html
