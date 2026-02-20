import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/interview';

export const interviewAPI = {
  generateQuestions: async (resumeText, userId) => {
    const response = await axios.post(`${API_BASE_URL}/generate-questions`, {
      resumeText,
      userId
    });
    return response.data;
  },

  submitAnswers: async (interviewId, answers) => {
    const response = await axios.post(`${API_BASE_URL}/submit-answers`, {
      interviewId,
      answers
    });
    return response.data;
  }
};
