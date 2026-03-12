package com.fabriciosanches.fichatecnica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FichaTecnicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(FichaTecnicaApplication.class, args);
	}

}
