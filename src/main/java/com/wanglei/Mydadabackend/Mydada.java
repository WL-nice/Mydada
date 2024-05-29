package com.wanglei.Mydadabackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.wanglei.Mydadabackend.mapper")//扫描mapper
@EnableScheduling
public class Mydada {

	public static void main(String[] args) {

		SpringApplication.run(Mydada.class, args);
	}

}
