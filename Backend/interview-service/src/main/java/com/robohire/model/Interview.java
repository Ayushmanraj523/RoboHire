package com.robohire.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an interview session.
 * Stores interview questions, answers, and AI-generated feedback.
 */
@Entity
@Table(name = "interviews", indexes = {
    @Index(name = "idx_interview_id", columnList = "interview_id"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "interview_id", nullable = false, unique = true, length = 36)
    private String interviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;
    
    @Column(columnDefinition = "TEXT")
    private String questions;
    
    @Column(columnDefinition = "TEXT")
    private String answers;
    
    @Column(name = "overall_score")
    private Integer overallScore;
    
    @Column(name = "technical_accuracy", columnDefinition = "TEXT")
    private String technicalAccuracy;
    
    @Column(name = "feedback_report", columnDefinition = "TEXT")
    private String feedbackReport;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
