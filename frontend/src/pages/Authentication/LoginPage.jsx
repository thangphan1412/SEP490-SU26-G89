import backGoundLogin from "../../assets/images/authenticationIMG/backGroundLogin.jpg";
import LoginForm from "../../components/common/LoginForm.jsx";
import "../../assets/styles/css/authenticationStyles/LoginFromStyle.css"
import {Link} from "react-router-dom";

function LoginPage() {
    return (
        <div className="login-container">

                <img
                    className="login-backgound"
                    src={backGoundLogin}
                    alt="Background Login"
                />
         <div className="login-form-wrapper">
             <LoginForm/>
             <div className="other-methods-login">
                 <h4 style={{justifyContent:"center"}}>Hoặc</h4>
                 <Link className="m-md-2" >Quên Mật Khẩu</Link>
                 <Link>Đăng ký</Link>
             </div>
         </div>

        </div>
    );
}

export default LoginPage;