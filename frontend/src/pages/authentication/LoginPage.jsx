import LoginForm from "../../components/layout/LoginForm.jsx";
import "../../assets/styles/css/authenticationStyles/LoginPage.css";
import { Link } from "react-router-dom";
import {IconWorld} from '@tabler/icons-react'
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import IconBackground from "../../components/common/IconBackground.jsx";
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
            <IconBackground/>
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