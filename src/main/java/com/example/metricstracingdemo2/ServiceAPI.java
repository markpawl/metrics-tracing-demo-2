package com.example.metricstracingdemo2;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jaegertracing.Configuration;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;

@RestController
@RequestMapping("/")
public class ServiceAPI {

	@Autowired
	MeterRegistry registry;
	
	// vars used by 'healthCheck' endpoint
	public static long instanceId = new Random().nextInt();
	public static int count = 0;
	
	// vars used by 'random' endpoint
	public static Random random = new Random(System.currentTimeMillis());
	public Tracer tracer = new Configuration("back-end-service").getTracer();
	
	/*
	 * returns a string containing current date/time, internal request counter and message
	 */	
	@GetMapping
	public String healthCheck() {
		// get response string
		String output = makeHealthCheckResponse();
		
		// increment Prometheus 'request-count' metric
		registry.counter("custom.metrics.request.count", "value", "HEALTHCHECK").increment();

		// return response
		return output;
		
	}

	private String makeHealthCheckResponse() {
		
		// increment internal counter
		count += 1;
		
		// get and format date
		Date date = new Date();
		String dateformat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.FULL).format(date);
		
		// assemble output and return
		String output = "<h3>The metrics-tracing BACK-END-API is up and running!</h3>" + "<br/>Instance: " + instanceId
				+ ", " + "<br/>DateTime: " + dateformat + "<br/>CallCount: " + count;	
		return output;
	}	
	
	/*
	 * generates and returns a random integer
	 */
	@GetMapping("/random")
	public String randomNumber(@RequestHeader HttpHeaders headers) {

		// retrieve parent span tracing context from request header
        SpanContext parentContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers.toSingleValueMap()));

        // start trace
        Span span = tracer.buildSpan("get-random-back-end").asChildOf(parentContext).start();		
		
        // get random number result
		int randomInt = random.nextInt();
		String output = String.valueOf(randomInt);
		
		// Mark the trace as completed
		span.finish();

		// return the result
		return output;
	}	
	
}
