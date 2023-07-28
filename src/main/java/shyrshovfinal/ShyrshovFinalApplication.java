package shyrshovfinal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import shyrshovfinal.configs.SwaggerConfig;

@SpringBootApplication
@Import(SwaggerConfig.class)
public class ShyrshovFinalApplication extends SpringBootServletInitializer{

    public static void main(String[] args) {
        SpringApplication.run(ShyrshovFinalApplication.class, args);
    }

}
