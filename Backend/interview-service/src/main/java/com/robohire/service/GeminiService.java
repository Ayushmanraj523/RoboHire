package com.robohire.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robohire.dto.AnswerSubmission;
import com.robohire.dto.FeedbackReport;
import com.robohire.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for Gemini AI integration with fail-safe error handling.
 * Provides question generation and feedback analysis with fallback mechanisms.
 */
@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1000;

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate interview questions based on resume with retry mechanism.
     */
    public List<String> generateQuestions(String resumeText) {
        log.info("Generating questions for resume (length: {} chars)", resumeText.length());
        
        if (resumeText == null || resumeText.trim().isEmpty()) {
            log.warn("Empty resume text provided, using fallback questions");
            return getFallbackQuestions();
        }

        String prompt = String.format(
            "Based on the following resume, generate exactly 5 technical interview questions. " +
            "Questions should be specific to the candidate's skills and experience. " +
            "Return only the questions, numbered 1-5, one per line.\n\nResume:\n%s",
            resumeText
        );

        try {
            String response = callGeminiAPIWithRetry(prompt);
            List<String> questions = parseQuestions(response);
            
            if (questions.isEmpty()) {
                log.warn("No questions parsed from AI response, using fallback");
                return getFallbackQuestions();
            }
            
            log.info("Successfully generated {} questions", questions.size());
            return questions;
            
        } catch (Exception e) {
            log.error("Failed to generate questions after retries, using fallback", e);
            return getFallbackQuestions();
        }
    }

    /**
     * Analyze interview answers with retry mechanism.
     */
    public FeedbackReport analyzeFeedback(List<AnswerSubmission.Answer> answers) {
        log.info("Analyzing feedback for {} answers", answers.size());
        
        StringBuilder prompt = new StringBuilder(
            "You are an honest technical interviewer. Analyze the following interview answers and provide:\n" +
            "1. Overall score (0-100)\n" +
            "2. Technical accuracy assessment\n" +
            "3. Individual feedback for each answer with score (0-20)\n" +
            "4. Areas for improvement\n" +
            "5. Strengths\n\n" +
            "Be honest and identify any incorrect, vague, or misleading answers.\n\n"
        );

        for (int i = 0; i < answers.size(); i++) {
            prompt.append(String.format("Question %d: %s\nAnswer: %s\n\n",
                i + 1, answers.get(i).getQuestion(), answers.get(i).getAnswer()));
        }

        prompt.append("\nProvide response in this exact format:\n" +
            "OVERALL_SCORE: [number]\n" +
            "TECHNICAL_ACCURACY: [assessment]\n" +
            "QUESTION_1_SCORE: [number]\n" +
            "QUESTION_1_FEEDBACK: [feedback]\n" +
            "QUESTION_1_ACCURATE: [true/false]\n" +
            "[repeat for all 5 questions]\n" +
            "IMPROVEMENTS: [point 1] | [point 2] | [point 3]\n" +
            "STRENGTHS: [point 1] | [point 2]");

        try {
            String response = callGeminiAPIWithRetry(prompt.toString());
            FeedbackReport report = parseFeedback(response, answers);
            log.info("Successfully generated feedback with score: {}", report.getOverallScore());
            return report;
            
        } catch (Exception e) {
            log.error("Failed to analyze feedback, using fallback", e);
            return getFallbackFeedback(answers);
        }
    }

    /**
     * Call Gemini API with retry mechanism and detailed error handling.
     */
    private String callGeminiAPIWithRetry(String prompt) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Calling Gemini API (attempt {}/{})", attempt, MAX_RETRIES);
                return callGeminiAPI(prompt);
                
            } catch (HttpClientErrorException e) {
                log.error("Gemini API client error (4xx): {} - {}", e.getStatusCode(), e.getMessage());
                lastException = e;
                
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new ApiException("Invalid Gemini API key. Please check your configuration.");
                } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new ApiException("Invalid request to Gemini API. Please check the prompt format.");
                }
                break; // Don't retry on client errors
                
            } catch (HttpServerErrorException e) {
                log.warn("Gemini API server error (5xx): {} - Attempt {}/{}", 
                    e.getStatusCode(), attempt, MAX_RETRIES);
                lastException = e;
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
            } catch (Exception e) {
                log.error("Unexpected error calling Gemini API - Attempt {}/{}", attempt, MAX_RETRIES, e);
                lastException = e;
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new ApiException("Failed to call Gemini API after " + MAX_RETRIES + " attempts: " + 
            (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    /**
     * Core Gemini API call method.
     */
    private String callGeminiAPI(String prompt) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new ApiException("Gemini API key is not configured");
            }
            
            String url = apiUrl + "?key=" + apiKey;
            log.debug("Calling Gemini API URL: {}", apiUrl);

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new ApiException("Empty response from Gemini API");
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            
            if (!root.has("candidates") || root.path("candidates").isEmpty()) {
                log.error("Invalid Gemini API response structure: {}", response.getBody());
                throw new ApiException("Invalid response structure from Gemini API");
            }
            
            String text = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
                
            if (text == null || text.trim().isEmpty()) {
                throw new ApiException("Empty text in Gemini API response");
            }
            
            log.debug("Received response from Gemini API (length: {} chars)", text.length());
            return text;

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing Gemini API response", e);
            throw new ApiException("Failed to process Gemini API response: " + e.getMessage());
        }
    }

    /**
     * Parse questions from AI response.
     */
    private List<String> parseQuestions(String response) {
        List<String> questions = new ArrayList<>();
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^\\d+\\..*")) {
                questions.add(line.replaceFirst("^\\d+\\.\\s*", ""));
            }
        }
        
        if (questions.size() < 5) {
            log.warn("Only {} questions parsed, expected 5", questions.size());
        }
        
        return questions.isEmpty() ? questions : questions.subList(0, Math.min(5, questions.size()));
    }

    /**
     * Fallback questions when AI fails.
     */
    private List<String> getFallbackQuestions() {
        log.info("Using fallback questions");
        return Arrays.asList(
            "Tell me about yourself and your professional background.",
            "What are your key technical skills and areas of expertise?",
            "Describe a challenging project you worked on and how you overcame obstacles.",
            "How do you stay updated with the latest technology trends?",
            "Where do you see yourself professionally in the next 3-5 years?"
        );
    }

    /**
     * Fallback feedback when AI fails.
     */
    private FeedbackReport getFallbackFeedback(List<AnswerSubmission.Answer> answers) {
        log.info("Using fallback feedback");
        
        FeedbackReport report = new FeedbackReport();
        report.setOverallScore(70);
        report.setTechnicalAccuracy("Unable to generate detailed analysis. Please try again.");
        
        List<FeedbackReport.QuestionFeedback> feedbacks = new ArrayList<>();
        for (AnswerSubmission.Answer answer : answers) {
            FeedbackReport.QuestionFeedback qf = new FeedbackReport.QuestionFeedback();
            qf.setQuestion(answer.getQuestion());
            qf.setAnswer(answer.getAnswer());
            qf.setScore(14);
            qf.setFeedback("Answer recorded. Detailed analysis unavailable.");
            qf.setAccurate(true);
            feedbacks.add(qf);
        }
        report.setQuestionFeedbacks(feedbacks);
        
        report.setAreasForImprovement(Arrays.asList(
            "Detailed AI analysis temporarily unavailable",
            "Please try submitting again for comprehensive feedback"
        ));
        
        report.setStrengths(Arrays.asList(
            "Interview completed successfully",
            "All questions answered"
        ));
        
        return report;
    }

    /**
     * Parse feedback from AI response.
     */
    private FeedbackReport parseFeedback(String response, List<AnswerSubmission.Answer> answers) {
        FeedbackReport report = new FeedbackReport();
        
        try {
            Pattern overallScorePattern = Pattern.compile("OVERALL_SCORE:\\s*(\\d+)");
            Matcher matcher = overallScorePattern.matcher(response);
            if (matcher.find()) {
                report.setOverallScore(Integer.parseInt(matcher.group(1)));
            } else {
                report.setOverallScore(70);
            }

            Pattern technicalAccuracyPattern = Pattern.compile("TECHNICAL_ACCURACY:\\s*(.+?)(?=\\n|$)");
            matcher = technicalAccuracyPattern.matcher(response);
            if (matcher.find()) {
                report.setTechnicalAccuracy(matcher.group(1).trim());
            } else {
                report.setTechnicalAccuracy("Good technical understanding demonstrated");
            }

            List<FeedbackReport.QuestionFeedback> questionFeedbacks = new ArrayList<>();
            for (int i = 1; i <= Math.min(5, answers.size()); i++) {
                FeedbackReport.QuestionFeedback qf = new FeedbackReport.QuestionFeedback();
                qf.setQuestion(answers.get(i - 1).getQuestion());
                qf.setAnswer(answers.get(i - 1).getAnswer());

                Pattern scorePattern = Pattern.compile("QUESTION_" + i + "_SCORE:\\s*(\\d+)");
                matcher = scorePattern.matcher(response);
                if (matcher.find()) {
                    qf.setScore(Integer.parseInt(matcher.group(1)));
                } else {
                    qf.setScore(14);
                }

                Pattern feedbackPattern = Pattern.compile("QUESTION_" + i + "_FEEDBACK:\\s*(.+?)(?=\\n|$)");
                matcher = feedbackPattern.matcher(response);
                if (matcher.find()) {
                    qf.setFeedback(matcher.group(1).trim());
                } else {
                    qf.setFeedback("Good answer provided");
                }

                Pattern accuratePattern = Pattern.compile("QUESTION_" + i + "_ACCURATE:\\s*(true|false)");
                matcher = accuratePattern.matcher(response);
                if (matcher.find()) {
                    qf.setAccurate(Boolean.parseBoolean(matcher.group(1)));
                } else {
                    qf.setAccurate(true);
                }

                questionFeedbacks.add(qf);
            }
            report.setQuestionFeedbacks(questionFeedbacks);

            Pattern improvementsPattern = Pattern.compile("IMPROVEMENTS:\\s*(.+?)(?=\\nSTRENGTHS:|$)", Pattern.DOTALL);
            matcher = improvementsPattern.matcher(response);
            if (matcher.find()) {
                String[] improvements = matcher.group(1).split("\\|");
                report.setAreasForImprovement(Arrays.asList(improvements).stream()
                    .map(String::trim).filter(s -> !s.isEmpty()).toList());
            } else {
                report.setAreasForImprovement(Arrays.asList("Continue practicing interview skills"));
            }

            Pattern strengthsPattern = Pattern.compile("STRENGTHS:\\s*(.+?)$", Pattern.DOTALL);
            matcher = strengthsPattern.matcher(response);
            if (matcher.find()) {
                String[] strengths = matcher.group(1).split("\\|");
                report.setStrengths(Arrays.asList(strengths).stream()
                    .map(String::trim).filter(s -> !s.isEmpty()).toList());
            } else {
                report.setStrengths(Arrays.asList("Good communication skills"));
            }

        } catch (Exception e) {
            log.error("Error parsing feedback, using defaults", e);
            return getFallbackFeedback(answers);
        }

        return report;
    }
}
