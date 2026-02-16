package com.group12.athleticaX;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AthleticaXApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                      .ignoreIfMissing()
                      .load();
        dotenv.entries().forEach(entry ->
        System.setProperty(entry.getKey(), entry.getValue())
);

        String dbUser = dotenv.get("DB_USERNAME","");
        String dbPass = dotenv.get("DB_PASSWORD","");
        String dbUrl  = dotenv.get("DB_URL","");  // optional: put full JDBC URL in .env
        System.out.println("DB_URL = " + dbUrl);

        SpringApplication app = new SpringApplication(AthleticaXApplication.class);

        // pass DB info to Spring as properties
        app.setDefaultProperties(Map.of(
            "spring.datasource.username", dbUser,
            "spring.datasource.password", dbPass,
            "spring.datasource.url", dbUrl,  // or construct using host/port/dbname
            "jwt.secret", dotenv.get("JWT_KEY","")
        ));

        app.run(args);
    }
}
