package com.nebula.peer.service;

import com.nebula.commons.crypto.HashUtils;
import com.nebula.commons.file.ChunkUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {
    private static final String STORAGE_DIR = "storage";

    public FileService() {
        new File(STORAGE_DIR).mkdirs();
    }

    // Split: Takes a local file path, splits it into chunks, and returns the Content Hash(ID)
    public String splitAndStore(String filePath) throws IOException {
        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        // calculate hash of the entire file (this becomes content id)
        byte[] fileBytes = Files.readAllBytes(inputFile.toPath());
        String contentHash = HashUtils.sha256(fileBytes);

        // prepare the directory for this content
        // structure: storage/{contentHash}
        Path contentDir = Paths.get(STORAGE_DIR, contentHash);
        Files.createDirectories(contentDir);

        // Read and split
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            byte[] buffer = new byte[ChunkUtils.CHUNK_SIZE];
            int bytesRead;
            int chunkIndex = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] actualData;
                if (bytesRead < ChunkUtils.CHUNK_SIZE) {
                    actualData = new byte[bytesRead];
                    System.arraycopy(buffer, 0, actualData, 0, bytesRead);
                } else {
                    actualData = buffer;
                }

                Path chunkPath = contentDir.resolve("chunk_" + chunkIndex);
                Files.write(chunkPath, actualData);

                System.out.println("Stored Chunk " + chunkIndex + " (");
                chunkIndex++;
            }
        }

        System.out.println("File Split Complete. Content ID: " + contentHash);
        return contentHash;
    }
}
