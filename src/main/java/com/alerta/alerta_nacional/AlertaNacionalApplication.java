package com.alerta.alerta_nacional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AlertaNacionalApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertaNacionalApplication.class, args);
	}

}
