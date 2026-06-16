import backGoundLogin from "../../assets/images/authenticationIMG/backGroundLogin.jpg";
import LoginForm from "../../components/common/LoginForm.jsx";
import "../../assets/styles/css/authenticationStyles/LoginFromStyle.css"

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
         </div>

        </div>
    );
}

export default LoginPage;