import { IconMail} from "@tabler/icons-react";
import {Link, useNavigate} from "react-router-dom";
import {useState} from "react";
import authenService from "../../services/userService/authenService.js";

function ForgotPasswordFrom(){
    const [email, setEmail] = useState("");
    const navigate = useNavigate()
    const handelForgot = async ()=> {
        try{
            console.log("email:", email)
            const response  = await authenService.forgot({email});
            console.log("email" , response.data)
            navigate("/reset-password")
        } catch (error){
            console.log(error.response?.data);
        }
    }
    return(
        <div className="forgot-form-container">
            <h1>Sign in to your account</h1>
            <p className="form-subtitle">Enter your credentials to access your account.</p>

            <div className="form-group">
                <label>Email address</label>
                <div className="input-wrapper">
                    <IconMail stroke={2} />
                    <input type="email" onChange={(e) =>setEmail(e.target.value)} placeholder="john.doe@company.com" />
                </div>
            </div>

            <button  className="btn-signin" type="button" onClick={handelForgot}>
               Request reset link <span>→</span>
            </button>


            <p className="backtologin-row" style={{display:"flex",justifyContent: "center"}  }>
                 <Link to="/login">Back To Login</Link>
            </p>
        </div>

    )
}
export default ForgotPasswordFrom;