package com.robohire.repository;

import com.robohire.model.Interview;
import com.robohire.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Optional<Interview> findByInterviewId(String interviewId);
    List<Interview> findByUserOrderByCreatedAtDesc(User user);
}
