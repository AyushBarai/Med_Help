package com.medhelp.common.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles saving and reading PDF files.
 *
 * RIGHT NOW: saves to a local folder (./uploads/).
 * Runs fine on your laptop. No AWS account needed to start.
 *
 * TO SWITCH TO S3 LATER:
 *  1. Set app.aws.s3.use-local-storage=false in application.yml
 *  2. Fill in the uploadToS3() method with AWS SDK calls
 *  3. No other code changes needed — ReportService calls this service,
 *     not S3 directly
 */
@Service
@Slf4j
public class StorageService {

    @Value("${app.aws.s3.use-local-storage:true}")
    private boolean useLocalStorage;

    @Value("${app.aws.s3.local-storage-path:./uploads}")
    private String localPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Saves PDF bytes and returns the URL/path where it was stored.
     *
     * @param fileName  e.g. "LAB-20240115-0001-v1.pdf"
     * @param bytes     the PDF bytes from PdfReportBuilder
     * @return the URL or path that was stored in Report.pdfUrl
     */
    public String save(String fileName, byte[] bytes) throws IOException {
        if (useLocalStorage) {
            return saveLocally(fileName, bytes);
        } else {
            return uploadToS3(fileName, bytes);
        }
    }

    /** Returns the raw bytes of a stored PDF (for streaming to browser) */
    public byte[] load(String pdfUrl) throws IOException {
        if (useLocalStorage) {
            Path path = Paths.get(pdfUrl);
            return Files.readAllBytes(path);
        } else {
            return downloadFromS3(pdfUrl);
        }
    }

    // ---- LOCAL STORAGE ----

    private String saveLocally(String fileName, byte[] bytes) throws IOException {
        // Create uploads directory if it doesn't exist
        Path dir = Paths.get(localPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            log.info("Created uploads directory: {}", dir.toAbsolutePath());
        }

        Path filePath = dir.resolve(fileName);
        Files.write(filePath, bytes);

        log.info("PDF saved locally: {}", filePath.toAbsolutePath());
        return filePath.toAbsolutePath().toString();
    }

    private byte[] loadLocally(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    // ---- S3 (fill in when ready) ----

    private String uploadToS3(String fileName, byte[] bytes) {
        // TODO:
        // S3Client s3 = S3Client.builder().region(Region.of(region)).build();
        // PutObjectRequest req = PutObjectRequest.builder()
        //     .bucket(bucket)
        //     .key("reports/" + fileName)
        //     .contentType("application/pdf")
        //     .build();
        // s3.putObject(req, RequestBody.fromBytes(bytes));
        // return "https://" + bucket + ".s3." + region + ".amazonaws.com/reports/" + fileName;
        throw new UnsupportedOperationException("S3 not configured yet. Set app.aws.s3.use-local-storage=true");
    }

    private byte[] downloadFromS3(String url) {
        // TODO: implement S3 download
        throw new UnsupportedOperationException("S3 not configured yet.");
    }
}