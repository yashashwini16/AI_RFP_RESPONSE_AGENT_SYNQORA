package com.azentio.rfp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AzentioRfpApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzentioRfpApplication.class, args);
        System.out.println("=================================================");
        System.out.println("🚀 AZENTIO AI RFP RESPONSE AGENT BACKEND ONLINE!");
        System.out.println("   API Base: http://localhost:8080/api/v1/rfp");
        System.out.println("   Health:   http://localhost:8080/actuator/health");
        System.out.println("=================================================");
    }
}
