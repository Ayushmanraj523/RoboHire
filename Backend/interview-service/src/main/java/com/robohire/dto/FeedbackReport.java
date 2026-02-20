package com.robohire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackReport {
    private int overallScore;
    private String technicalAccuracy;
    private List<QuestionFeedback> questionFeedbacks;
    private List<String> areasForImprovement;
    private List<String> strengths;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionFeedback {
        private String question;
        private String answer;
        private int score;
        private String feedback;
        private boolean accurate;
    }
}
