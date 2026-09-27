package org.vced.ragutillib.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatsDTO {
    private String name;
    private String documentType;
    private int totalChunks;
}
