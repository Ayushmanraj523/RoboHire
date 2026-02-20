import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const errorMessage = error.response?.data?.message || 'An error occurred';
    console.error('API Error:', errorMessage);
    return Promise.reject(error);
  }
);

export const authAPI = {
  register: async (name, email, password) => {
    const response = await axiosInstance.post('/auth/register', {
      name,
      email,
      password
    });
    return response.data;
  },

  login: async (email, password) => {
    const response = await axiosInstance.post('/auth/login', {
      email,
      password
    });
    return response.data;
  }
};

export const interviewAPI = {
  generateQuestions: async (resumeText, userId) => {
    const response = await axiosInstance.post('/interview/generate-questions', {
      resumeText,
      userId
    });
    return response.data;
  },

  submitAnswers: async (interviewId, answers) => {
    const response = await axiosInstance.post('/interview/submit-answers', {
      interviewId,
      answers
    });
    return response.data;
  }
};

export default axiosInstance;
