import { Outlet } from "react-router-dom";
import HeaderForm from "./HeaderForm.jsx";
import NavbarForm from "./NavbarForm.jsx";
import "../../assets/styles/css/layoutStyles/MainLayout.css";
function MainLayout() {
    return (
        <div className="app-layout">
            <HeaderForm />
            <div className="app-body">
                <NavbarForm />
                <main className="main-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}


export default MainLayout;