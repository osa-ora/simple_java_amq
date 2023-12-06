package osa.ora.amq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AmqApplication {
    public static final String QUEUE_NAME="my-queue";
    //public static final String QUEUE_NAME="my-topic";

    public static void main(String[] args) {
        SpringApplication.run(AmqApplication.class, args);
    }
}
