package com.robohire.controller;

import com.robohire.dto.*;
import com.robohire.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for interview-related operations.
 * Handles question generation and answer submission with AI feedback.
 */
@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * Generate AI-powered interview questions based on resume.
     * @param request Contains resume text and user ID
     * @return QuestionResponse with interview ID and generated questions
     */
    @PostMapping("/generate-questions")
    public ResponseEntity<QuestionResponse> generateQuestions(@Valid @RequestBody InterviewRequest request) {
        log.info("Generating questions for user: {}", request.getUserId());
        QuestionResponse response = interviewService.generateQuestions(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Submit interview answers and receive AI-generated feedback.
     * @param submission Contains interview ID and candidate answers
     * @return FeedbackReport with scores and analysis
     */
    @PostMapping("/submit-answers")
    public ResponseEntity<FeedbackReport> submitAnswers(@Valid @RequestBody AnswerSubmission submission) {
        log.info("Submitting answers for interview: {}", submission.getInterviewId());
        FeedbackReport report = interviewService.submitAnswers(submission);
        return ResponseEntity.ok(report);
    }
}
