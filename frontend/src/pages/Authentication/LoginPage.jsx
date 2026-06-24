import LoginForm from "../../components/common/LoginForm.jsx";
import "../../assets/styles/css/authenticationStyles/LoginPage.css";
import { Link } from "react-router-dom";
import {IconWorld} from '@tabler/icons-react'
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
function LoginPage() {
  return (
    <div className="login-page">
      <header className="login-header">
        <div className="logo">
          <div className="logo-icon">🛡️</div>
          <div className="logo-text">
            <strong>E-CONTRACT</strong>
            <span>Management System</span>
          </div>
        </div>
        
        <NavDropdown title= {     
                       <span className="lang-btn"> 
                          <IconWorld stroke={2} />
                            <span className="languge">
                               English     
                            </span>
                        </span>
                     }  id="basic-nav-dropdown">
                                
                        <NavDropdown.Item href="#action/3.1">English</NavDropdown.Item>
                        <NavDropdown.Item href="#action/3.2"> Vietnamese </NavDropdown.Item>
                       
                    </NavDropdown>

      </header>

      <main className="login-main">
        <div className="login-card">
          <div className="left-panel">
            <div className="illustration">
              {/* SVG minh họa contract/folder */}
              <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                <rect x="20" y="70" width="160" height="110" rx="12" fill="#5254d3"/>
                <rect x="20" y="55" width="80" height="30" rx="8" fill="#6366f1"/>
                <rect x="50" y="40" width="90" height="115" rx="8" fill="white" opacity="0.95"/>
                <text x="90" y="60" textAnchor="middle" fontSize="10" fill="#6366f1" fontWeight="700">CONTRACT</text>
                <rect x="62" y="68" width="60" height="5" rx="2.5" fill="#e0e0f0"/>
                <rect x="62" y="80" width="50" height="4" rx="2" fill="#e0e0f0"/>
                <rect x="62" y="91" width="55" height="4" rx="2" fill="#e0e0f0"/>
                <path d="M65 118 Q75 108 85 115 Q92 120 100 112" stroke="#5254d3" strokeWidth="2.5" fill="none" strokeLinecap="round"/>
                <circle cx="148" cy="108" r="22" fill="#6366f1"/>
                <polyline points="139,108 145,115 158,100" stroke="white" strokeWidth="3" fill="none" strokeLinecap="round" strokeLinejoin="round"/>
                <rect x="84" y="142" width="32" height="24" rx="5" fill="rgba(255,255,255,0.25)"/>
                <path d="M91 142 v-6 a9 9 0 0 1 18 0 v6" stroke="white" strokeWidth="2.5" fill="none"/>
                <circle cx="100" cy="154" r="3" fill="white"/>
              </svg>
            </div>
            <h2>Welcome Back!</h2>
            <p>Sign in to continue to your<br/>E-Contract Management System.</p>
          </div>

          <div className="right-panel">
            <LoginForm />
          </div>
        </div>
      </main>

      
    </div>
  );
}

export default LoginPage;