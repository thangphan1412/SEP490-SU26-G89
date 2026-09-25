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
    const [errorMessage, setErrorMessage] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const handelLogin = async(event)=> {
        event.preventDefault();
        if (submitting) return;
        setErrorMessage("");
        setSubmitting(true);
        try {
            localStorage.removeItem("token");
            localStorage.removeItem("role");
            localStorage.removeItem("fullName");
            localStorage.removeItem("departmentName");

            const response = await authenService.login({ email: email.trim(), password });
            const { token, role, fullName, departmentName , hasSignatureKey} = response.data.data;
            if (!token) throw new Error("Đăng nhập không thành công. Vui lòng thử lại.");
            localStorage.setItem("token", token);
            localStorage.setItem("role", role ?? "");
            localStorage.setItem("fullName", fullName ?? "");
            localStorage.setItem("departmentName", departmentName ?? "");
            localStorage.setItem("hasSignatureKey", hasSignatureKey);
            localStorage.setItem("email", email.trim());
            if(!hasSignatureKey){
                navigate("/signature-management/create-signature");
            } else {
                navigate("/home_page");
            }

        } catch (error) {
            const status = error.response?.status;
            setErrorMessage(status === 401
                ? "Email hoặc mật khẩu không chính xác. Vui lòng thử lại."
                : typeof error.response?.data?.message === "string"
                  ? error.response.data.message
                  : !error.response
                    ? "Không thể đăng nhập. Vui lòng kiểm tra kết nối và thử lại."
                    : "Đăng nhập không thành công. Vui lòng thử lại.");
        } finally {
            setSubmitting(false);
        }
    }

  return (
    <form className="login-form-container" onSubmit={handelLogin}>
      <h1>Sign in to your account</h1>
      <p className="form-subtitle">Enter your credentials to access your account.</p>
      {errorMessage && <div role="alert" className="alert alert-danger">{errorMessage}</div>}

      <div className="form-group">
        <label>Email address</label>
        <div className="input-wrapper">
          <IconMail stroke={2} />
          <input type="email" value={email} required autoComplete="username" onChange={(e) =>setEmail(e.target.value)} placeholder="john.doe@company.com" />
        </div>
      </div>

      <div className="form-group">
        <label>Password</label>
        <div className="input-wrapper">
          <IconLock stroke={2} />
          <input type={showPassword ? "text" : "password"} value={password} required autoComplete="current-password" onChange={(e)=> setPassword(e.target.value)} placeholder="••••••••••" />
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

      <button className="btn-signin" type="submit" disabled={submitting}>
        {submitting ? "Signing in..." : "Sign In"} <span>→</span>
      </button>

      <div className="divider"><span>or</span></div>

      <p className="signup-row">
        Don't have an account? <Link to="/register">Sign up</Link>
      </p>
    </form>
  );
}

export default LoginForm;
