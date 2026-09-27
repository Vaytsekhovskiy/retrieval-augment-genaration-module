package org.vced.ragutillib.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoldersInfoDTO {
    private List<String> rootFoldersPaths;
    private List<String> exceptFolderPaths;
    private String mountPoint;
}