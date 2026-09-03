package com.alerta.alerta_nacional.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCleanupReportDto {
    private String fileName;
    private int totalIdsProcessed;
    private int totalPostsDeleted;
    private int totalErrors;
    private LocalDateTime executionTimestamp;

    @Builder.Default
    private List<PostDeleteResultDto> details = new ArrayList<>();

    @Builder.Default
    private List<String> globalErrors = new ArrayList<>();
}
