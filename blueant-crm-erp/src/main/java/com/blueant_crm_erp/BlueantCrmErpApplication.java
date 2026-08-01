package com.blueant_crm_erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BlueantCrmErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlueantCrmErpApplication.class, args);
	}

}