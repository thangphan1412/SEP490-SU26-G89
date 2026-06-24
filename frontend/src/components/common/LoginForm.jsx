import { useState } from "react";
import { Link } from "react-router-dom";
import "../../assets/styles/css/layoutStyles/LoginFormStyle.css";
import {IconMail,
     IconLock 
    } from '@tabler/icons-react';

function LoginForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(true);
  const see = <IconEyeX stroke={2} />;
  const notSee =<IconEyeX stroke={2} />;
  return (
    <div className="login-form-container">
      <h1>Sign in to your account</h1>
      <p className="form-subtitle">Enter your credentials to access your account.</p>

      <div className="form-group">
        <label>Email address</label>
        <div className="input-wrapper">
          <IconMail stroke={2} />
          <input type="email" placeholder="john.doe@company.com" />
        </div>
      </div>

      <div className="form-group">
        <label>Password</label>
        <div className="input-wrapper">
          <IconLock stroke={2} />
          <input type={showPassword ? "text" : "password"} placeholder="••••••••••" />
          <button className="eye-btn" onClick={() => setShowPassword(!showPassword)} type="button">
            {showPassword ? "see" : "notSee"}
          </button>
        </div>
      </div>

      <div className="form-options">
        <label className="remember-label" onClick={() => setRemember(!remember)}>
          <div className={`checkbox-custom ${remember ? "checked" : ""}`}>
            {remember && <span>✓</span>}
          </div>
          Remember me
        </label>
        <Link to="/forgot-password" className="forgot-link">Forgot password?</Link>
      </div>

      <button className="btn-signin" type="button">
        Sign In <span>→</span>
      </button>

      <div className="divider"><span>or</span></div>

      <p className="signup-row">
        Don't have an account? <Link to="/register">Sign up</Link>
      </p>
    </div>
  );
}

export default LoginForm;