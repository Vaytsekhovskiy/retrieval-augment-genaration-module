package org.vced.botmicroservice.config;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bot_config")
@Data
public class BotConfigEntity {

    @Id
    private Long id = 1L; // всегда одна строка

    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "jsonb")
    private BotConfigDTO config;

    private Long updatedAt;
}
