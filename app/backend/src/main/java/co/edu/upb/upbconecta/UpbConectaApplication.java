package co.edu.upb.upbconecta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class UpbConectaApplication {

    /** Las fechas sin zona (LocalDateTime) se interpretan en hora de Colombia, no en la del servidor (UTC en Docker). */
    public static final String ZONA_HORARIA = "America/Bogota";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZONA_HORARIA));
        SpringApplication.run(UpbConectaApplication.class, args);
    }
}
