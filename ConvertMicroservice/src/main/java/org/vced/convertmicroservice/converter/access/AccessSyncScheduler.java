package org.vced.convertmicroservice.converter.access;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccessSyncScheduler {

    private final AccessSyncService accessSyncService;

    @PostConstruct
    public void init() {
        log.debug("AccessSyncScheduler initialized");
    }

    @Scheduled(cron = "0 0 0 * * *") // Каждую полночь (second=0, minute=0, hour=0, any day of month, any month, any day of week)
    public void scheduleAuthoritiesUpdate() {
        log.info("Starting scheduled authorities update");
        try {
            int updated = accessSyncService.updateAuthoritiesForAll();
            log.info("Scheduled authorities update completed successfully. Updated {} documents", updated);
        } catch (Exception e) {
            log.error("Scheduled authorities update failed", e);
        }
    }
}