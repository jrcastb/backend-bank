package com.devsu.backendbank;

import org.springframework.boot.SpringApplication;

public class TestBackendBankApplication {

    public static void main(String[] args) {
        SpringApplication.from(BackendBankApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
