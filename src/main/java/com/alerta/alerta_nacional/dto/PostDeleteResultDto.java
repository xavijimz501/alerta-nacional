package com.alerta.alerta_nacional.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDeleteResultDto {
    private String postId;
    private String status; // SUCCESS, NOT_FOUND, FAILED
    private String message;
}
