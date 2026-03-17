package com.bank.LMS.Service.customer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
public class LoanDocumentStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-bytes:5242880}")
    private long maxBytes;

    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public StoredFile store(MultipartFile file, String applicationNo, String docType) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(docType + " file is required.");
        }

        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(docType + " exceeds max size " + maxBytes + " bytes.");
        }

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()
        );

        String ext = getExt(original);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Invalid file type for " + docType + ". Allowed: " + ALLOWED_EXT);
        }

        String safeDoc = docType.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        String safeOriginal = original.replaceAll("[^a-zA-Z0-9._-]", "_");

        String storedName = safeDoc + "_" + TS.format(LocalDateTime.now()) + "_" + UUID.randomUUID() + "_" + safeOriginal;

        Path base = Paths.get(uploadDir, "loan_docs", applicationNo).toAbsolutePath().normalize();
        Files.createDirectories(base);

        Path target = base.resolve(storedName).normalize();
        if (!target.startsWith(base)) {
            throw new SecurityException("Invalid path.");
        }

        System.out.println("Upload base folder: " + base);
        System.out.println("Target file path   : " + target);
        System.out.println("Original file      : " + original);
        System.out.println("File size          : " + file.getSize());

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        Path uploadBase = Paths.get(uploadDir).toAbsolutePath().normalize();
        String relative = uploadBase.relativize(target).toString().replace("\\", "/");

        return new StoredFile(
                storedName,
                relative,
                file.getContentType(),
                file.getSize(),
                original
        );
    }

    private String getExt(String name) {
        int i = name.lastIndexOf('.');
        if (i == -1) return "";
        return name.substring(i + 1).toLowerCase();
    }

    public record StoredFile(
            String fileName,
            String filePath,
            String mimeType,
            long sizeBytes,
            String originalFileName
    ) {}
}