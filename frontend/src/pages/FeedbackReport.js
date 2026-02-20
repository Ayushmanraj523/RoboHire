import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useInterview } from '../context/InterviewContext';
import { interviewAPI } from '../services/interviewAPI';
import logo from '../assets/logo.png';

const FeedbackReport = () => {
  const navigate = useNavigate();
  const { answers, interviewId } = useInterview();
  const [feedback, setFeedback] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchFeedback = async () => {
      if (answers.length === 0) {
        navigate('/dashboard');
        return;
      }

      try {
        const report = await interviewAPI.submitAnswers(interviewId, answers);
        setFeedback(report);
      } catch (error) {
        console.error('Error fetching feedback:', error);
        alert('Failed to generate feedback. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchFeedback();
  }, [answers, interviewId, navigate]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-700 text-lg">Analyzing your interview...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">
      <nav className="bg-white shadow-md">
        <div className="container mx-auto px-6 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <img src={logo} alt="RoboHire Logo" className="w-10 h-10" />
            <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-700 bg-clip-text text-transparent">RoboHire</h1>
          </div>
          <button 
            onClick={() => navigate('/dashboard')}
            className="px-6 py-2 bg-gradient-to-r from-blue-600 to-indigo-700 text-white rounded-xl hover:from-blue-700 hover:to-indigo-800 transition shadow-lg"
          >
            Back to Dashboard
          </button>
        </div>
      </nav>

      <div className="container mx-auto px-6 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Honest Feedback Report</h1>

        <div className="grid md:grid-cols-3 gap-6 mb-8">
          <div className="bg-gradient-to-br from-blue-600 to-indigo-700 p-6 rounded-2xl shadow-lg text-center">
            <h3 className="text-blue-100 mb-2">Overall Score</h3>
            <p className="text-5xl font-bold text-white">{feedback?.overallScore || 0}/100</p>
          </div>
          <div className="bg-gradient-to-br from-green-600 to-green-700 p-6 rounded-2xl shadow-lg text-center col-span-2">
            <h3 className="text-green-100 mb-2">Technical Accuracy</h3>
            <p className="text-xl font-semibold text-white">{feedback?.technicalAccuracy || 'N/A'}</p>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-lg mb-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Question-by-Question Analysis</h2>
          <div className="space-y-4">
            {feedback?.questionFeedbacks?.map((qf, index) => (
              <div key={index} className={`p-4 rounded-xl border-2 ${
                qf.accurate ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'
              }`}>
                <div className="flex justify-between items-start mb-2">
                  <h3 className="font-semibold text-gray-800">Question {index + 1}</h3>
                  <span className={`px-3 py-1 rounded-full text-sm font-semibold ${
                    qf.accurate ? 'bg-green-200 text-green-800' : 'bg-red-200 text-red-800'
                  }`}>
                    {qf.score}/20
                  </span>
                </div>
                <p className="text-gray-700 mb-2">{qf.question}</p>
                <p className="text-sm text-gray-600 mb-2"><span className="font-medium">Your Answer:</span> {qf.answer}</p>
                <p className="text-sm text-gray-800"><span className="font-medium">Feedback:</span> {qf.feedback}</p>
                {!qf.accurate && (
                  <p className="text-sm text-red-600 mt-2 font-medium">⚠ This answer needs improvement</p>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          <div className="bg-white p-6 rounded-2xl shadow-lg">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Areas for Improvement</h2>
            <ul className="space-y-3">
              {feedback?.areasForImprovement?.map((area, index) => (
                <li key={index} className="flex items-start p-3 bg-yellow-50 rounded-xl">
                  <span className="text-yellow-600 mr-3 text-xl">⚠</span>
                  <p className="text-gray-700">{area}</p>
                </li>
              ))}
            </ul>
          </div>

          <div className="bg-white p-6 rounded-2xl shadow-lg">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Strengths</h2>
            <ul className="space-y-3">
              {feedback?.strengths?.map((strength, index) => (
                <li key={index} className="flex items-start p-3 bg-green-50 rounded-xl">
                  <span className="text-green-600 mr-3 text-xl">✓</span>
                  <p className="text-gray-700">{strength}</p>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FeedbackReport;
