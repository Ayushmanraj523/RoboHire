package com.robohire.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * DTO for submitting interview answers.
 */
@Data
public class AnswerSubmission {
    
    @NotBlank(message = "Interview ID is required")
    private String interviewId;
    
    private List<Answer> answers;
    
    @Data
    public static class Answer {
        @NotBlank(message = "Question is required")
        private String question;
        
        private String answer;
    }
}
