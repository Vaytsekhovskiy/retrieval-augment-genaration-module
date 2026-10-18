package org.vced.botmicroservice.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotConfigRepository extends JpaRepository<BotConfigEntity, Long> {}
