package com.nebula.peer;

import com.nebula.peer.service.FileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PeerMain {
    public static void main(String[] args) {
        SpringApplication.run(PeerMain.class, args);
    }

    @Bean
    CommandLineRunner run(FileService fileService) {
        return args -> {
            String testFilePath = "C:/Users/prana/Downloads/8817382737.pdf";

            System.out.println("---Starting Split test---");
            try {
                String hash = fileService.splitAndStore(testFilePath);
                System.out.println("SUCCESS! File ID: " + hash);
            } catch (Exception e) {
                System.err.println("Error" + e.getMessage());
                // dummy
                System.out.println("Creating dummy file for test...");
                java.nio.file.Files.write(java.nio.file.Paths.get("test_video.mp4"), "Hello NebulaStream P2P Metwork".getBytes());
                String hash = fileService.splitAndStore("test_video.mp4");
                System.out.println("SUCCESS! Dummy File ID: " + hash);
            }
        };
    }
}
