import React from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo.png';

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-indigo-900">
      <nav className="container mx-auto px-6 py-6 flex justify-between items-center">
        <div className="flex items-center gap-3">
          <img src={logo} alt="RoboHire Logo" className="w-12 h-12" />
          <h1 className="text-2xl font-bold text-white">RoboHire</h1>
        </div>
        <div className="flex gap-4">
          <button 
            onClick={() => navigate('/login')}
            className="px-6 py-2 text-white font-semibold hover:text-blue-300 transition"
          >
            Login
          </button>
          <button 
            onClick={() => navigate('/register')}
            className="px-6 py-2 bg-gradient-to-r from-blue-600 to-indigo-700 text-white rounded-xl font-semibold hover:from-blue-700 hover:to-indigo-800 transition shadow-lg"
          >
            Get Started
          </button>
        </div>
      </nav>

      <div className="container mx-auto px-6 py-20">
        <div className="text-center max-w-4xl mx-auto">
          <h1 className="text-6xl font-bold text-white mb-6">
            Master Your Interview Skills with <span className="bg-gradient-to-r from-blue-400 to-indigo-400 bg-clip-text text-transparent">AI</span>
          </h1>
          <p className="text-xl text-blue-200 mb-8">
            Practice with AI-driven mock interviews and get instant, actionable feedback
          </p>
          <p className="text-lg text-blue-300 mb-10">
            Join thousands of professionals who improved their interview performance with RoboHire
          </p>
          <button 
            onClick={() => navigate('/register')}
            className="bg-gradient-to-r from-blue-600 to-indigo-700 text-white px-10 py-4 rounded-2xl text-lg font-semibold hover:from-blue-700 hover:to-indigo-800 transition shadow-2xl"
          >
            Start Practicing Now
          </button>
        </div>

        <div className="mt-24 grid md:grid-cols-3 gap-8">
          <div className="bg-white/10 backdrop-blur-lg p-8 rounded-2xl shadow-xl border border-white/20 hover:bg-white/15 transition">
            <div className="text-4xl mb-4">🤖</div>
            <h3 className="text-2xl font-semibold text-white mb-3">AI-Powered Interviews</h3>
            <p className="text-blue-200">Experience realistic interview scenarios with advanced AI technology that adapts to your responses.</p>
          </div>
          <div className="bg-white/10 backdrop-blur-lg p-8 rounded-2xl shadow-xl border border-white/20 hover:bg-white/15 transition">
            <div className="text-4xl mb-4">📊</div>
            <h3 className="text-2xl font-semibold text-white mb-3">Instant Feedback</h3>
            <p className="text-blue-200">Get detailed analysis and personalized improvement suggestions after each session.</p>
          </div>
          <div className="bg-white/10 backdrop-blur-lg p-8 rounded-2xl shadow-xl border border-white/20 hover:bg-white/15 transition">
            <div className="text-4xl mb-4">📈</div>
            <h3 className="text-2xl font-semibold text-white mb-3">Track Progress</h3>
            <p className="text-blue-200">Monitor your performance metrics and see your improvement over time.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LandingPage;
