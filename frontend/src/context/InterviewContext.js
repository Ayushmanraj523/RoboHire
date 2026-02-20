import React, { createContext, useState, useContext } from 'react';

const InterviewContext = createContext();

export const InterviewProvider = ({ children }) => {
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [questions, setQuestions] = useState([
    "Tell me about yourself and your experience in software development.",
    "What are your greatest strengths as a developer?",
    "Describe a challenging project you worked on and how you overcame obstacles.",
    "How do you stay updated with the latest technology trends?",
    "Where do you see yourself in 5 years?"
  ]);
  const [answers, setAnswers] = useState([]);
  const [interviewId, setInterviewId] = useState(null);

  const nextQuestion = () => {
    if (currentQuestion < questions.length - 1) {
      setCurrentQuestion(currentQuestion + 1);
    }
  };

  const saveAnswer = (question, answer) => {
    setAnswers(prev => [...prev, { question, answer }]);
  };

  const resetInterview = () => {
    setCurrentQuestion(0);
    setAnswers([]);
  };

  return (
    <InterviewContext.Provider value={{
      currentQuestion,
      questions,
      setQuestions,
      answers,
      interviewId,
      setInterviewId,
      nextQuestion,
      saveAnswer,
      resetInterview
    }}>
      {children}
    </InterviewContext.Provider>
  );
};

export const useInterview = () => {
  const context = useContext(InterviewContext);
  if (!context) {
    throw new Error('useInterview must be used within InterviewProvider');
  }
  return context;
};
