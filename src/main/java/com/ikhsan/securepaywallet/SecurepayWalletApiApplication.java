package com.ikhsan.securepaywallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Profile("!test")
@SpringBootApplication
@EnableJpaAuditing
public class SecurepayWalletApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurepayWalletApiApplication.class, args);
	}

}
