import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';
import { useInterview } from '../context/InterviewContext';
import { useSpeechRecognition } from '../hooks/useSpeechRecognition';
import { interviewAPI } from '../services/interviewAPI';

const InterviewRoom = () => {
  const navigate = useNavigate();
  const { user, logout } = useUser();
  const { currentQuestion, questions, setQuestions, nextQuestion, resetInterview, saveAnswer } = useInterview();
  const { transcript, isListening, startListening, stopListening, resetTranscript, supported } = useSpeechRecognition();
  
  const [timeLeft, setTimeLeft] = useState(40);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (timeLeft === 0) {
      handleAutoSubmit();
    }

    const timer = setInterval(() => {
      setTimeLeft(prev => prev > 0 ? prev - 1 : 0);
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft]);

  const handleAutoSubmit = () => {
    if (isListening) {
      stopListening();
    }
    saveAnswer(questions[currentQuestion], transcript);
    
    if (currentQuestion < questions.length - 1) {
      nextQuestion();
      resetTranscript();
      setTimeLeft(40);
    } else {
      handleEndInterview();
    }
  };

  const handleSkip = () => {
    if (isListening) {
      stopListening();
    }
    saveAnswer(questions[currentQuestion], transcript || 'Skipped');
    
    if (currentQuestion < questions.length - 1) {
      nextQuestion();
      resetTranscript();
      setTimeLeft(40);
    } else {
      handleEndInterview();
    }
  };

  const handleEndInterview = async () => {
    setIsLoading(true);
    resetInterview();
    navigate('/feedback');
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const toggleRecording = () => {
    if (isListening) {
      stopListening();
    } else {
      startListening();
    }
  };

  if (!supported) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-indigo-900 flex items-center justify-center text-white">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-4">Speech Recognition Not Supported</h2>
          <p>Please use Chrome, Edge, or Safari browser for voice interview.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-indigo-900 text-white">
      <div className="container mx-auto px-6 py-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">Interview Room</h1>
          <div className="flex gap-4">
            <button 
              onClick={handleEndInterview}
              disabled={isLoading}
              className="bg-gradient-to-r from-red-600 to-red-700 px-6 py-2 rounded-xl hover:from-red-700 hover:to-red-800 transition shadow-lg disabled:opacity-50"
            >
              End Interview
            </button>
            <button 
              onClick={handleLogout}
              className="bg-white/10 backdrop-blur-lg px-6 py-2 rounded-xl hover:bg-white/20 transition border border-white/20"
            >
              Logout
            </button>
          </div>
        </div>

        <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-8 mb-6 border border-white/20 shadow-2xl">
          <div className="text-center mb-8">
            <div className="w-32 h-32 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full mx-auto mb-4 flex items-center justify-center shadow-xl">
              <span className="text-5xl">🤖</span>
            </div>
            <h2 className="text-2xl font-semibold mb-2">AI Interviewer</h2>
            <div className="flex items-center justify-center gap-4">
              <p className="text-blue-200">Time Remaining:</p>
              <div className={`text-3xl font-bold ${
                timeLeft <= 10 ? 'text-red-400' : 'text-green-400'
              }`}>
                {timeLeft}s
              </div>
            </div>
          </div>

          <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 mb-6 border border-white/10">
            <h3 className="text-lg font-semibold mb-3">Question {currentQuestion + 1} of {questions.length}:</h3>
            <p className="text-blue-100 text-lg">{questions[currentQuestion]}</p>
          </div>

          <div className="bg-white/5 backdrop-blur-md rounded-xl p-6 mb-6 border border-white/10 min-h-32">
            <h3 className="text-sm font-semibold mb-2 text-blue-200">Your Answer (Voice-to-Text):</h3>
            <p className="text-white">{transcript || 'Start speaking to see your answer here...'}</p>
          </div>

          <div className="flex justify-center gap-4">
            <button 
              onClick={toggleRecording}
              className={`px-8 py-3 rounded-xl font-semibold transition shadow-lg ${
                isListening 
                  ? 'bg-gradient-to-r from-red-600 to-red-700 hover:from-red-700 hover:to-red-800' 
                  : 'bg-gradient-to-r from-green-600 to-green-700 hover:from-green-700 hover:to-green-800'
              }`}
            >
              {isListening ? '⏸ Stop Recording' : '🎤 Start Recording'}
            </button>
            <button 
              onClick={handleSkip}
              className="bg-white/10 backdrop-blur-lg px-8 py-3 rounded-xl font-semibold hover:bg-white/20 transition border border-white/20"
            >
              {currentQuestion < questions.length - 1 ? 'Skip Question' : 'Finish Interview'}
            </button>
          </div>
        </div>

        <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-6 border border-white/20">
          <h3 className="text-lg font-semibold mb-3">Interview Progress</h3>
          <div className="flex gap-2">
            {questions.map((_, index) => (
              <div 
                key={index}
                className={`h-2 rounded flex-1 ${
                  index <= currentQuestion 
                    ? 'bg-gradient-to-r from-blue-600 to-indigo-600' 
                    : 'bg-white/20'
                }`}
              ></div>
            ))}
          </div>
          <p className="text-blue-200 text-sm mt-2">Question {currentQuestion + 1} of {questions.length}</p>
        </div>
      </div>
    </div>
  );
};

export default InterviewRoom;
