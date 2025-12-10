package com.nebula.peer.dto;

public record FileMetadata(String contentHash, long totalSize, int totalChunks) {}
