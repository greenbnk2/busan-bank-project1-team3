package kr.co.bnk.bnk_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class BnkProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(BnkProjectApplication.class, args);
    }

}
