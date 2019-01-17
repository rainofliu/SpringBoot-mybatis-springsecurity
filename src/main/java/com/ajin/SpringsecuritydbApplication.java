package com.ajin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.ajin.dao")
@SpringBootApplication
public class SpringsecuritydbApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringsecuritydbApplication.class, args);
    }

}

