package com.homepage.controller;

import com.homepage.dto.FileInfoDTO;
import com.homepage.dto.Result;
import com.homepage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;

    @GetMapping("/list")
    public Result<List<FileInfoDTO>> listFiles(@RequestParam(required = false) String path) {
        return Result.success(fileService.listFiles(path));
    }

    @PostMapping("/upload")
    public Result<FileInfoDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String directory) {
        return Result.success(fileService.uploadFile(file, directory));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        return fileService.downloadFile(id);
    }

    @GetMapping("/share/{token}")
    public ResponseEntity<Resource> downloadByToken(@PathVariable String token) {
        return fileService.downloadByToken(token);
    }

    @PostMapping("/{id}/share")
    public Result<FileInfoDTO> createShareLink(
            @PathVariable Long id,
            @RequestParam(required = false) Integer expireHours) {
        return Result.success(fileService.createShareLink(id, expireHours));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return Result.success();
    }
}
