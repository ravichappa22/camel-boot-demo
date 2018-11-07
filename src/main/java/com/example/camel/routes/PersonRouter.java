package com.example.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonDataFormat;

public class PersonRouter extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		//JsonDataFormat dataFormat = new JsonDataFormat();
		
		from("direct:postPersons");

	}

}
