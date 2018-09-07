package com.tillo.rtc;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@RestController
@SpringBootApplication
public class RtcApplication {

	public static void main(String[] args) {
		SpringApplication.run(RtcApplication.class, args);
	}
}
