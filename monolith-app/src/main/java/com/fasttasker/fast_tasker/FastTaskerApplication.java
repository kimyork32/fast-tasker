package com.fasttasker.fast_tasker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.fasttasker.fast_tasker",
        "com.fasttasker.common"
})
public class FastTaskerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastTaskerApplication.class, args);
	}

}
