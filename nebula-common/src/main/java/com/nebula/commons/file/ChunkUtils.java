package com.nebula.commons.file;

public class ChunkUtils {

    public static final int CHUNK_SIZE = 1024 * 1024; // 1MB

    // returns total chunks needed for a file size
    public static int calculateTotalChunks(long fileSize) {
        return (int) Math.ceil((double) fileSize / CHUNK_SIZE);
    }

    // returns the offset/start byte for a specific chunk index
    public static long getChunkOffset(int chunkIndex) {
        return (long) chunkIndex * CHUNK_SIZE;
    }
}
