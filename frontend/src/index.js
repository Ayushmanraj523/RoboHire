import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import { UserProvider } from './context/UserContext';
import { InterviewProvider } from './context/InterviewContext';
import reportWebVitals from './reportWebVitals';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <UserProvider>
      <InterviewProvider>
        <App />
      </InterviewProvider>
    </UserProvider>
  </React.StrictMode>
);

reportWebVitals();
