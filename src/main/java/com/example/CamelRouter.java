package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.TelegramConstants;
import org.apache.camel.component.telegram.TelegramMediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for routing the messages from and to the Telegram chat, as well polling a webcam.
 */
@Component
public class CamelRouter extends RouteBuilder {

    @Autowired
    private Bot bot;

    @Override
    public void configure() throws Exception {

        //Telegram bot route
        from("telegram:bots/{{telegram.auth.token}}")
                .choice().
                    when(body().isNull())
                    .log("No body out there")
                    .endChoice()
                    .when(body().isEqualTo("do-not-reply"))
                        .log("OK, won't reply")
                    .endChoice()
                    .when(body().isEqualTo("/cam"))
                        .to("direct:cam")
                        .setHeader(TelegramConstants.TELEGRAM_MEDIA_TYPE, constant(TelegramMediaType.PHOTO_JPG))
                        .setHeader(TelegramConstants.TELEGRAM_MEDIA_TITLE_CAPTION, constant("Aw yiss"))
                    .endChoice()
                    .otherwise().bean(bot)
                .end()
        .to("telegram:bots/{{telegram.auth.token}}");

        //Web cam route, throttling to 1 per/second for demo...
        from("direct:cam").throttle(1).to("webcam:foo");

    }
}
