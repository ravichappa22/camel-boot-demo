package com.example.camel.configuration;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyAppConfig {
	
	CamelContextConfiguration camelContextConfiguration(){
		return new CamelContextConfiguration() {

			@Override
			public void afterApplicationStart(CamelContext arg0) {
				System.out.println("Camel Context Started");
				
			}

			@Override
			public void beforeApplicationStart(CamelContext arg0) {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

}
