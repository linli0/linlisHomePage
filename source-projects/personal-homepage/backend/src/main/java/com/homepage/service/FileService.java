package com.homepage.service;

import com.homepage.dto.FileInfoDTO;
import com.homepage.entity.FileInfo;
import com.homepage.repository.FileInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileInfoRepository fileInfoRepository;

    @Value("${app.file.storage-path:./uploads}")
    private String storagePath;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            log.info("File storage path: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public List<FileInfoDTO> listFiles(String subPath) {
        Path currentPath = rootLocation.resolve(subPath != null ? subPath : "").normalize();
        if (!currentPath.startsWith(rootLocation)) {
            throw new RuntimeException("Invalid path");
        }

        try {
            return Files.list(currentPath)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not list files", e);
        }
    }

    @Transactional
    public FileInfoDTO uploadFile(MultipartFile file, String directory) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + "." + extension;

            Path targetPath = rootLocation.resolve(directory != null ? directory : "").normalize();
            if (!targetPath.startsWith(rootLocation)) {
                throw new RuntimeException("Invalid directory");
            }

            Files.createDirectories(targetPath);
            Path destinationFile = targetPath.resolve(filename).normalize();
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            FileInfo fileInfo = FileInfo.builder()
                    .name(originalFilename)
                    .path(destinationFile.toString())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .extension(extension)
                    .isDirectory(false)
                    .isPublic(true)
                    .build();

            FileInfo saved = fileInfoRepository.save(fileInfo);
            return convertToDTODetailed(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public ResponseEntity<Resource> downloadFile(Long id) {
        FileInfo fileInfo = fileInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Path path = Paths.get(fileInfo.getPath());
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found", e);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"")
                .body(resource);
    }

    public ResponseEntity<Resource> downloadByToken(String token) {
        FileInfo fileInfo = fileInfoRepository.findByShareToken(token)
                .orElseThrow(() -> new RuntimeException("File not found or expired"));

        if (fileInfo.getExpiresAt() != null && fileInfo.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Share link expired");
        }

        fileInfo.setDownloadCount(fileInfo.getDownloadCount() + 1);
        fileInfoRepository.save(fileInfo);

        return downloadFile(fileInfo.getId());
    }

    @Transactional
    public FileInfoDTO createShareLink(Long id, Integer expireHours) {
        FileInfo fileInfo = fileInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        String token = UUID.randomUUID().toString().replace("-", "");
        fileInfo.setShareToken(token);
        fileInfo.setExpiresAt(expireHours != null ? 
                LocalDateTime.now().plusHours(expireHours) : null);

        FileInfo saved = fileInfoRepository.save(fileInfo);
        return convertToDTODetailed(saved);
    }

    @Transactional
    public void deleteFile(Long id) {
        FileInfo fileInfo = fileInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            Files.deleteIfExists(Paths.get(fileInfo.getPath()));
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
        }

        fileInfoRepository.deleteById(id);
    }

    private FileInfoDTO convertToDTO(Path path) {
        try {
            boolean isDirectory = Files.isDirectory(path);
            return FileInfoDTO.builder()
                    .name(path.getFileName().toString())
                    .path(path.toString())
                    .isDirectory(isDirectory)
                    .fileSize(isDirectory ? 0L : Files.size(path))
                    .fileSizeFormatted(formatFileSize(isDirectory ? 0L : Files.size(path)))
                    .createdAt(LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(path).toInstant(),
                            java.time.ZoneId.systemDefault()))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Could not read file attributes", e);
        }
    }

    private FileInfoDTO convertToDTODetailed(FileInfo fileInfo) {
        return FileInfoDTO.builder()
                .id(fileInfo.getId())
                .name(fileInfo.getName())
                .path(fileInfo.getPath())
                .url("/api/files/download/" + fileInfo.getId())
                .shareToken(fileInfo.getShareToken())
                .shareUrl(fileInfo.getShareToken() != null ? "/api/files/share/" + fileInfo.getShareToken() : null)
                .fileSize(fileInfo.getFileSize())
                .fileSizeFormatted(formatFileSize(fileInfo.getFileSize()))
                .mimeType(fileInfo.getMimeType())
                .extension(fileInfo.getExtension())
                .isDirectory(fileInfo.getIsDirectory())
                .isPublic(fileInfo.getIsPublic())
                .downloadCount(fileInfo.getDownloadCount())
                .createdAt(fileInfo.getCreatedAt())
                .expiresAt(fileInfo.getExpiresAt())
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1).toLowerCase();
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        return String.format("%.2f %s", size / Math.pow(1024, exp), units[exp]);
    }
}
