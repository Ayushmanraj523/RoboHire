import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';
import { useInterview } from '../context/InterviewContext';
import { interviewAPI } from '../services/interviewAPI';
import logo from '../assets/logo.png';

const Dashboard = () => {
  const navigate = useNavigate();
  const { user, logout, updateUser } = useUser();
  const { setQuestions, setInterviewId } = useInterview();
  const [uploading, setUploading] = useState(false);
  const [resumeText, setResumeText] = useState('');
  const [generatingQuestions, setGeneratingQuestions] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      setUploading(true);
      const reader = new FileReader();
      reader.onload = (event) => {
        const text = event.target.result;
        setResumeText(text);
        updateUser({ resume: file.name });
        setUploading(false);
        alert('Resume uploaded successfully!');
      };
      reader.readAsText(file);
    }
  };

  const handleStartInterview = async () => {
    if (!resumeText) {
      alert('Please upload your resume first!');
      return;
    }

    setGeneratingQuestions(true);
    try {
      const response = await interviewAPI.generateQuestions(resumeText, user?.email);
      setQuestions(response.questions);
      setInterviewId(response.interviewId);
      navigate('/interview');
    } catch (error) {
      console.error('Error generating questions:', error);
      alert('Failed to generate questions. Please try again.');
    } finally {
      setGeneratingQuestions(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">
      <nav className="bg-white shadow-md">
        <div className="container mx-auto px-6 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <img src={logo} alt="RoboHire Logo" className="w-10 h-10" />
            <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-700 bg-clip-text text-transparent">RoboHire</h1>
          </div>
          <button 
            onClick={handleLogout}
            className="px-6 py-2 bg-gradient-to-r from-red-600 to-red-700 text-white rounded-xl hover:from-red-700 hover:to-red-800 transition shadow-lg"
          >
            Logout
          </button>
        </div>
      </nav>

      <div className="container mx-auto px-6 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Dashboard</h1>

        <div className="grid md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white p-6 rounded-2xl shadow-lg hover:shadow-xl transition">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Profile</h2>
            <div className="space-y-2">
              <p className="text-gray-600"><span className="font-medium">Name:</span> {user?.name || 'Guest'}</p>
              <p className="text-gray-600"><span className="font-medium">Email:</span> {user?.email || 'N/A'}</p>
              <p className="text-gray-600"><span className="font-medium">Interviews:</span> {user?.interviews || 0}</p>
              {user?.resume && (
                <p className="text-gray-600"><span className="font-medium">Resume:</span> {user.resume}</p>
              )}
            </div>
          </div>

          <div className="bg-white p-6 rounded-2xl shadow-lg hover:shadow-xl transition">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Resume Upload</h2>
            <input 
              type="file" 
              accept=".txt,.pdf,.doc,.docx"
              onChange={handleFileUpload}
              className="block w-full text-sm text-gray-600 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:bg-blue-50 file:text-blue-600 hover:file:bg-blue-100 file:cursor-pointer"
            />
            <p className="text-xs text-gray-500 mt-2">Upload .txt file for best results</p>
            <button 
              onClick={() => document.querySelector('input[type="file"]').click()}
              disabled={uploading}
              className="mt-4 w-full bg-gradient-to-r from-blue-600 to-indigo-700 text-white py-2 rounded-xl hover:from-blue-700 hover:to-indigo-800 transition shadow-lg disabled:opacity-50"
            >
              {uploading ? 'Uploading...' : 'Upload Resume'}
            </button>
          </div>

          <div className="bg-gradient-to-br from-blue-600 to-indigo-700 p-6 rounded-2xl shadow-lg hover:shadow-xl transition">
            <h2 className="text-xl font-semibold text-white mb-4">Quick Actions</h2>
            <button 
              onClick={handleStartInterview}
              disabled={generatingQuestions || !resumeText}
              className="w-full bg-white text-blue-600 py-2 rounded-xl hover:bg-blue-50 transition font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {generatingQuestions ? 'Generating Questions...' : 'Start New Interview'}
            </button>
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-lg">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Interview History</h2>
          {user?.interviewHistory && user.interviewHistory.length > 0 ? (
            <div className="space-y-3">
              {user.interviewHistory.map((interview, index) => (
                <div key={index} className="flex justify-between items-center p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl hover:from-blue-100 hover:to-indigo-100 transition">
                  <div>
                    <p className="font-medium text-gray-800">{interview.type}</p>
                    <p className="text-sm text-gray-600">Date: {interview.date}</p>
                  </div>
                  <button 
                    onClick={() => navigate('/feedback')}
                    className="text-blue-600 font-semibold hover:underline"
                  >
                    View Report
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-gray-500">No interview history yet. Start your first interview!</p>
              <button 
                onClick={handleStartInterview}
                disabled={!resumeText}
                className="mt-4 px-6 py-2 bg-gradient-to-r from-blue-600 to-indigo-700 text-white rounded-xl hover:from-blue-700 hover:to-indigo-800 transition shadow-lg disabled:opacity-50"
              >
                Start Interview
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
