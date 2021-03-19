package com.ausclouds.bdbsec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableAutoConfiguration
@SpringBootApplication
@MapperScan("com.example.jzxy.mapper")
public class JzxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(JzxyApplication.class, args);
		
	}

}
