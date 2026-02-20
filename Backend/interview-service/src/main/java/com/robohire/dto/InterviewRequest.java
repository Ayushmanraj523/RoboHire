package com.robohire.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for interview question generation request.
 */
@Data
public class InterviewRequest {
    
    @NotBlank(message = "Resume text is required")
    private String resumeText;
    
    @NotBlank(message = "User ID is required")
    private String userId;
}
