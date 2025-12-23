package com.nebula.tracker;

import com.nebula.tracker.service.TrackerServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class TrackerMain {

    public static void main(String[] args) {
        SpringApplication.run(TrackerMain.class, args);
    }

    @Bean
    CommandLineRunner startGrpcServer(TrackerServiceImpl trackerService) {
        return args -> {
            int port = 9000;
            Server server = ServerBuilder.forPort(port)
                    .addService(trackerService)
                    .build()
                    .start();

            System.out.println("--- NEBULA TRACKER ONLINE (Port " + port + ") ---");

            server.awaitTermination();
        };
    }
}
