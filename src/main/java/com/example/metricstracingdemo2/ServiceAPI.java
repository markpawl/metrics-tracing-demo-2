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

	public static long instanceId = new Random().nextInt();
	public static int count = 0;
	
	public static Random random = new Random(System.currentTimeMillis());

	@Autowired
	MeterRegistry registry;

	@GetMapping
	public String healthCheck() {
		count += 1;
		Date date = new Date();
		String dateformat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.FULL)
				.format(date);
		String output = "<h3>The metrics-tracing-demo-2 app is up and running!</h3>" + "<br/>Instance: " + instanceId + ", "
				+ "<br/>DateTime: " + dateformat + "<br/>CallCount: " + count;
		
		registry.counter("custom.metrics.reqcount", "value", "GET_ROOT" ).increment();
		// registry.gauge("custom.metrics.reqgauge", count);
		
		return output;
		
	}

	@GetMapping("/random")
	public String randomNumber(@RequestHeader HttpHeaders headers) {
		/*
		 * String parentContext = headers.getFirst("Parent-Context"); Span mainSpan =
		 * tracer.buildSpan(parentContext).start(); String thisContext =
		 * "get-random-back-end"; Span getNumberSpan =
		 * tracer.buildSpan(thisContext).asChildOf(mainSpan).start();
		 */
		
        SpanContext parentContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers.toSingleValueMap()));
        Span span = tracer.buildSpan("get-random-back-end").asChildOf(parentContext).start();		
		
		int randomInt = random.nextInt();
		String output = String.valueOf(randomInt);
		
		span.finish();
		/*
		 * getNumberSpan.finish(); mainSpan.finish();
		 */
		
		return output;
	}	
	
	public Tracer tracer = new Configuration("back-end-service").getTracer();

	
}
