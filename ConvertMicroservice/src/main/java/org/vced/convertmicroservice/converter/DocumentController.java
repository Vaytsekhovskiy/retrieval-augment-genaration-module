package org.vced.convertmicroservice.converter;


import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.vced.convertmicroservice.watcher.FilePathAbstractService;
import org.vced.ragutillib.document.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/convert")
public class DocumentController {
    private final DocumentService documentService;
    private final ImageService imageService;
    private final FilePathAbstractService filePathAbstractService;

    @GetMapping("/open/{id}")
    public ResponseEntity<InputStreamResource> getPdf(@PathVariable String id) throws IOException {
        long cleanId = Long.parseLong(
                id.replace("\u00A0", "").replace(" ", "")
        );
        Path path = Path.of(documentService.getPdfPath(cleanId));
        // Создаем ByteArrayInputStream из ByteArrayOutputStream
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(Files.readAllBytes(path)));
        // Установка заголовков ответа
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF); // Указываем MIME-тип файла
        headers.setContentDisposition(ContentDisposition.builder("inline") // Указываем, что файл должен быть открыт в браузере
                .filename(String.valueOf(id))
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadOriginalDoc(@PathVariable String id) throws IOException {
        long cleanId = Long.parseLong(
                id.replace("\u00A0", "").replace(" ", "")
        );

        Path path = Path.of(documentService.getDocument(cleanId).getOriginalPath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(path.getFileName().toString(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .contentLength(Files.size(path))
                .body(resource);
    }
    @PostMapping("/folder")
    public CompletableFuture<ResponseEntity<String>> loadFolder(@RequestParam String path)
            throws IllegalArgumentException {
        filePathAbstractService.addRootPath(path);
        return null;
    }
    @DeleteMapping("/folder")
    public ResponseEntity<Void> deleteFolder(
            @RequestBody Map<String, String> body) {
        String path = body.get("path");
        boolean isExceptFolder = Boolean.parseBoolean(body.get("isExceptFolder"));
        filePathAbstractService.removePath(path, isExceptFolder);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/folder-except")
    public CompletableFuture<ResponseEntity<String>> loadFolderExcept(@RequestParam String path)
            throws IllegalArgumentException {
        filePathAbstractService.addRootPathExcept(path);
        return null;
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleException(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }
    @PostMapping("/reset")
    public ResponseEntity<String> resetDocs() {
        var allRootPaths = filePathAbstractService.getRootPaths();
        filePathAbstractService.processRootPaths(allRootPaths, true);
        filePathAbstractService.processRootPaths(allRootPaths, false);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/folders")
    public ResponseEntity<FoldersInfoDTO> getProcessedFolder() {
        return ResponseEntity.ok()
                .body(FoldersInfoDTO
                        .builder()
                        .rootFoldersPaths(filePathAbstractService.getRootPaths())
                        .exceptFolderPaths(filePathAbstractService.getExceptPaths())
                        .mountPoint(filePathAbstractService.getMountPoint())
                        .build());
    }
    @GetMapping("/states")
    public SseEmitter streamStates() {
        return FilePathAbstractService.getPublisher().subscribe();
    }
    @GetMapping("/stats")
    public ResponseEntity<DocumentsStatsDTO> getStats() {
        return ResponseEntity.ok(documentService.getDocumentsStats());
    }
    @GetMapping("/documents")
    public List<DocumentStatsDTO> getDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        size = Math.min(size, 400); // Мало ли
        return documentService.findAllDocumentsLimit(page, size);
    }
    @GetMapping("/image/{id}")
    public ResponseEntity<String> getEncodedImage(@PathVariable long id, @RequestParam(name = "is-article", defaultValue = "false") boolean isArticle){
        String encodedImage;
        InputStreamResource fis;
        CustomImage customImage = imageService.getImageById(id);
        if (!isArticle){
            try {
                fis = new InputStreamResource(new FileInputStream(customImage.getPath()));
                encodedImage = "data:image/png;base64,";
                encodedImage += new String(Base64.getEncoder().encode(fis.getContentAsByteArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String imageUrl = customImage.getPath(); // URL из YouTrack
            encodedImage = "data:image/png;base64,";
            try {
                encodedImage += getBase64EncodedImage(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return ResponseEntity.ok()
                .body(encodedImage);
    }



    public String getBase64EncodedImage(String url) throws IOException {
        try {
            URL imageUrl = new URL(url);
            URLConnection connection = imageUrl.openConnection();
            connection.connect();
            InputStream is = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
