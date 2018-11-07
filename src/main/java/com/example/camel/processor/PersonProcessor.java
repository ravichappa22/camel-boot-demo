package com.example.camel.processor;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.camel.routes.Person;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by rchappa1 on 11/6/18.
 */
@Component
public class PersonProcessor implements Processor {

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void process(Exchange exchange) throws Exception {
       Person person = exchange.getIn().getBody(Person.class);
       System.out.println("LastName=" + person.getLastName ());
       person.setId (UUID.randomUUID ().toString ());
       exchange.getIn().setBody (mapper.writeValueAsString (person));
       //exchange.getOut().setHeader ("rabbitmq.REPLY_TO", "member-validation.css.response.queue");
    }
}
