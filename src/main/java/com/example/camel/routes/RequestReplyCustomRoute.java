package com.example.camel.routes;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JacksonXMLDataFormat;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.camel.processor.PersonProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;

/**
 * Created by rchappa1 on 11/6/18.
 */
@Component
public class RequestReplyCustomRoute extends RouteBuilder {

    @Autowired
    private PersonProcessor personProcessor;

    @Override
    public void configure() throws Exception {

    	//Jack personFormat = new JacksonXMLDataFormat(Person.class);
        //personFormat.d(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        rest ("/person/add").post().bindingMode (RestBindingMode.json)
                .type(Person.class)
                .route().from("direct:handlePersonRequest").routeId("handlePersonRequest")
                .marshal().json(JsonLibrary.Jackson)
                .to(ExchangePattern.InOnly,
                "rabbitmq://{{messaging.host}}?connectionFactory=#connectionFactory&autoDelete=false" +
                        "&routingKey={{member-validation.css.request.queue}}")
                .id("sendRequestToPersonQueue");

        from("rabbitmq://{{messaging.host}}/{{messaging.vh}}?connectionFactory=#connectionFactory"
                + "&queue={{member-validation.css.request.queue}}&deadLetterExchange={{messaging.vh}}"
                + "&deadLetterRoutingKey={{member-validation.css.reject.queue}}"
                + "&deadLetterQueue={{member-validation.css.reject.queue}}&autoAck=false&autoDelete=false"
                + "&concurrentConsumers={{member_validation.concurrent.consumers}}"
                + "&threadPoolSize={{member_validation.consumer.threadpool.size}}"
                + "&prefetchCount={{member_validation.prefetch.size}}"
                + "&prefetchEnabled={{member_validation.prefetch.enabled}}")
                .routeId("personRoute").autoStartup (false)
                .to("log:endpointRouteBuilder?level=INFO&showBody=true&multiline=true&showHeaders=true")
                .removeHeaders("rabbitmq.*")
                .log("ReceiveRequestFromCSS: ${body} and header ReplyQueue = ${header.ReplyQueue}")
                //.unmarshal(personFormat)
                .process(personProcessor)
                .choice()
                    .when(header("ReplyQueue").isNull())
                            .to("direct:sendReplyToResponse")
                    .when(header("ReplyQueue").isEqualToIgnoreCase("member-validation.css.alternate.response.queue"))
                            .to("direct:sendReplyToAlternate");

                    /*.otherwise()
                            .to(ExchangePattern.InOnly, "rabbitmq://{{messaging.host}}/{{messaging.vh}}?connectionFactory=#connectionFactory" +
                                "&routingKey=${header.ReplyQueue}&queue=${header.ReplyQueue}&declare=false&autoDelete=false")
                                       .log("Sent message to response queue = ${header.ReplyQueue}").endChoice ();*/


            from("direct:sendReplyToResponse").to(ExchangePattern.InOnly, "rabbitmq://{{messaging.host}}/{{messaging.vh}}?connectionFactory=#connectionFactory" +
                "&routingKey={{member-validation.css.response.queue}}&queue={{member-validation.css.response.queue}}&declare=true&autoDelete=false").
                log ("Sent message to default response queue ${header.ReplyQueue} ").end();

           from("direct:sendReplyToAlternate").to(ExchangePattern.InOnly, "rabbitmq://{{messaging.host}}/{{messaging.vh}}?connectionFactory=#connectionFactory" +
                "&routingKey={{member-validation.css.alternate.response.queue}}&queue={{member-validation.css.alternate.response.queue}}&declare=true&autoDelete=false").
                log ("Sent message to default response queue ${header.ReplyQueue}").end();

    }
}
