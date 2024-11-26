package com.tpastushok.cosmocats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CosmicCatsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CosmicCatsApplication.class, args);
	}

}
