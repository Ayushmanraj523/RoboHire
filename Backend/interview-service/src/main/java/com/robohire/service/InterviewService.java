package com.robohire.service;

import com.robohire.dto.*;
import com.robohire.exception.ApiException;
import com.robohire.exception.ResourceNotFoundException;
import com.robohire.model.Interview;
import com.robohire.model.User;
import com.robohire.repository.InterviewRepository;
import com.robohire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for interview management.
 * Handles business logic for question generation and answer submission.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final GeminiService geminiService;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;

    /**
     * Generate AI-powered interview questions based on resume.
     * @param request Contains resume text and user ID
     * @return QuestionResponse with interview ID and questions
     * @throws ResourceNotFoundException if user not found
     * @throws ApiException if question generation fails
     */
    @Transactional
    public QuestionResponse generateQuestions(InterviewRequest request) {
        log.info("Generating questions for user: {}", request.getUserId());
        
        User user = userRepository.findByEmail(request.getUserId())
            .orElseThrow(() -> {
                log.error("User not found: {}", request.getUserId());
                return new ResourceNotFoundException("User not found");
            });

        List<String> questions;
        try {
            questions = geminiService.generateQuestions(request.getResumeText());
            log.info("Successfully generated {} questions", questions.size());
        } catch (Exception e) {
            log.error("Failed to generate questions", e);
            throw new ApiException("Failed to generate interview questions. Please try again.");
        }
        
        String interviewId = UUID.randomUUID().toString();
        
        Interview interview = Interview.builder()
                .interviewId(interviewId)
                .user(user)
                .resumeText(request.getResumeText())
                .questions(String.join("|||", questions))
                .build();
        
        interviewRepository.save(interview);
        log.info("Interview created with ID: {}", interviewId);
        
        return new QuestionResponse(interviewId, questions);
    }

    /**
     * Submit interview answers and receive AI-generated feedback.
     * @param submission Contains interview ID and answers
     * @return FeedbackReport with scores and analysis
     * @throws ResourceNotFoundException if interview not found
     * @throws ApiException if feedback generation fails
     */
    @Transactional
    public FeedbackReport submitAnswers(AnswerSubmission submission) {
        log.info("Submitting answers for interview: {}", submission.getInterviewId());
        
        Interview interview = interviewRepository.findByInterviewId(submission.getInterviewId())
            .orElseThrow(() -> {
                log.error("Interview not found: {}", submission.getInterviewId());
                return new ResourceNotFoundException("Interview not found");
            });

        FeedbackReport report;
        try {
            report = geminiService.analyzeFeedback(submission.getAnswers());
            log.info("Feedback generated with overall score: {}", report.getOverallScore());
        } catch (Exception e) {
            log.error("Failed to generate feedback", e);
            throw new ApiException("Failed to generate feedback. Please try again.");
        }
        
        interview.setAnswers(submission.getAnswers().toString());
        interview.setOverallScore(report.getOverallScore());
        interview.setTechnicalAccuracy(report.getTechnicalAccuracy());
        interview.setCompletedAt(LocalDateTime.now());
        
        interviewRepository.save(interview);
        log.info("Interview completed: {}", submission.getInterviewId());
        
        return report;
    }

    /**
     * Retrieve all interviews for a specific user.
     * @param email User email
     * @return List of interviews
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public List<Interview> getUserInterviews(String email) {
        log.info("Fetching interviews for user: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found: {}", email);
                return new ResourceNotFoundException("User not found");
            });
        
        return interviewRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
