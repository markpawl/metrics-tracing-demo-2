package com.example.metricstracingdemo2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.jaegertracing.Configuration;



@SpringBootApplication
public class MetricsTracingDemo2Application {

	public static void main(String[] args) {
		SpringApplication.run(MetricsTracingDemo2Application.class, args);
	}

	@Bean
	public io.opentracing.Tracer jaegerTracer() {
	    return new Configuration("spring-boot-2").getTracer();	
	
	}
	
}
