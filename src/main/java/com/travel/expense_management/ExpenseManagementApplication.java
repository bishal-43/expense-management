package com.travel.expense_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
public class ExpenseManagementApplication {

	public static void main(String[] args) {

		SpringApplication.run(ExpenseManagementApplication.class, args);
	}

}
