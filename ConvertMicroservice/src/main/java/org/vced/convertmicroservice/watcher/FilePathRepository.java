package org.vced.convertmicroservice.watcher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FilePathRepository extends JpaRepository<FilePath, Long> {
    boolean existsByPath(String path);

    @Modifying
    @Transactional
    void deleteFilePathByPath(String path);
}
