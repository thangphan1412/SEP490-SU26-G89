import ForgotPasswordFrom from "../../components/common/ForgotPasswordForm.jsx";
import NavDropdown from "react-bootstrap/NavDropdown";
import {IconWorld} from "@tabler/icons-react";
import IconBackground from "../../components/common/IconBackground.jsx";
import LoginForm from "../../components/common/LoginForm.jsx";

function ForgotPassword(){
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
                        <ForgotPasswordFrom/>
                    </div>
                </div>
            </main>


        </div>
    );
}
 export default ForgotPassword