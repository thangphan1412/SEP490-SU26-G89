import { useState } from "react";
import {Link, useNavigate} from "react-router-dom";
import "../../assets/styles/css/layoutStyles/LoginFormStyle.css";
import {IconMail,
     IconLock ,
     IconEyeX,
     IconEye
    } from '@tabler/icons-react';
import authenService from "../../services/userService/authenService.js";


function LoginForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(true);
  const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const handelLogin = async()=> {
        try {
            localStorage.removeItem("token");
            localStorage.removeItem("role");
            localStorage.removeItem("fullName");

            const response = await authenService.login({ email, password });
            console.log("Response data:", response.data);
            const { token, role, fullName } = response.data.data;
            console.log("token:", token, "role:", role, "fullName:", fullName);
            localStorage.setItem("token", token);
            localStorage.setItem("role", role ?? "");
            localStorage.setItem("fullName", fullName ?? "");

            navigate("/home_page");
        } catch (error) {
            console.log(error.response?.data);
        }
    }

  return (
    <div className="login-form-container">
      <h1>Sign in to your account</h1>
      <p className="form-subtitle">Enter your credentials to access your account.</p>

      <div className="form-group">
        <label>Email address</label>
        <div className="input-wrapper">
          <IconMail stroke={2} />
          <input type="email" onChange={(e) =>setEmail(e.target.value)} placeholder="john.doe@company.com" />
        </div>
      </div>

      <div className="form-group">
        <label>Password</label>
        <div className="input-wrapper">
          <IconLock stroke={2} />
          <input type={showPassword ? "text" : "password"} onChange={(e)=> setPassword(e.target.value)} placeholder="••••••••••" />
          <button className="eye-btn" onClick={() => setShowPassword(!showPassword)} type="button">
            {showPassword ? <IconEyeX stroke={2} />: <IconEye stroke={2} /> }
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
        <Link to="/forgot_password" className="forgot-link">Forgot password?</Link>
      </div>

      <button className="btn-signin" type="button" onClick={handelLogin}>
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