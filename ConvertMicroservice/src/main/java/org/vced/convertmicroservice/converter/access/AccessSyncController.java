package org.vced.convertmicroservice.converter.access;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/convert")
public class AccessSyncController {

    private final AccessSyncService accessSyncService;

    @PostMapping("/admin/update-authorities")
    public ResponseEntity<Integer> updateAuthorities() {
        Integer updated = accessSyncService.updateAuthoritiesForAll();
        return ResponseEntity.ok(updated);
    }
}
