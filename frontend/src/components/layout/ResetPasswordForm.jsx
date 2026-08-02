import {IconEye, IconEyeX, IconLock, IconMail} from "@tabler/icons-react";
import {Link, useNavigation} from "react-router-dom";
import resetpassword from "../../pages/authentication/Resetpassword.jsx";
import authenService from "../../services/userService/authenService.js";
import {use, useState} from "react";

function ResetPasswordForm(){
    const [showPassword, setShowPassword] = useState(false);
    const [email, setEmail] = useState();
    const [otp, setOtp] = useState();
    const [newPassword, setNewPassword] = useState();
    const [newPasswordConfirm , setNewPasswordConfirm] = useState();
    const navigate = useNavigation()

    const handeReset= async() => {
        try{
            const response = await authenService.reset({email, otp, newPassword, newPasswordConfirm })
            console.log(response)
            navigate("/login")
        } catch (error){
            console.log(error.response?.data);
        }
    }
    return(
        <div className="reset-form-container">
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
                <label>Email address</label>
                <div className="input-wrapper">
                    <IconMail stroke={2} />
                    <input type="email" onChange={(e) =>setOtp(e.target.value)} placeholder="john.doe@company.com" />
                </div>
            </div>
            <div className="form-group">
                <label>Password</label>
                <div className="input-wrapper">
                    <IconLock stroke={2} />
                    <input type={showPassword ? "text" : "password"} onChange={(e)=> setNewPassword(e.target.value)} placeholder="••••••••••" />
                    <button className="eye-btn" onClick={() => setShowPassword(!showPassword)} type="button">
                        {showPassword ? <IconEyeX stroke={2} />: <IconEye stroke={2} /> }
                    </button>
                </div>
            </div>
            <div className="form-group">
                <label>Password</label>
                <div className="input-wrapper">
                    <IconLock stroke={2} />
                    <input type={showPassword ? "text" : "password"} onChange={(e)=> setNewPasswordConfirm(e.target.value)} placeholder="••••••••••" />
                    <button className="eye-btn" onClick={() => setShowPassword(!showPassword)} type="button">
                        {showPassword ? <IconEyeX stroke={2} />: <IconEye stroke={2} /> }
                    </button>
                </div>
            </div>
            <button  className="btn-signin" type="button" onClick={handeReset}>
                Request reset link <span>→</span>
            </button>


            <p className="backtologin-row" style={{display:"flex",justifyContent: "center"}  }>
                <Link to="/login">Back To Login</Link>
            </p>
        </div>
    )
}
export default ResetPasswordForm