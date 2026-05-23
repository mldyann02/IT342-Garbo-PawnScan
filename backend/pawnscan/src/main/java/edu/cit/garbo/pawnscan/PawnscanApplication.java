package edu.cit.garbo.pawnscan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PawnscanApplication {

	public static void main(String[] args) {
		SpringApplication.run(PawnscanApplication.class, args);
	}

}







