import { Outlet, useLocation } from "react-router-dom";
import HeaderForm from "./HeaderForm.jsx";
import NavbarForm from "./NavbarForm.jsx";
import "../../assets/styles/css/layoutStyles/MainLayout.css";

const CENTERED_WORKSPACE_PATHS = [
    "/project-management",
    "/permission",
    "/phase-management",
    "/task-management",
];

function MainLayout() {
    const location = useLocation();
    const useCenteredWorkspace = CENTERED_WORKSPACE_PATHS.some(function (path) {
        return location.pathname.startsWith(path);
    });

    return (
        <div className="app-layout">
            <HeaderForm />
            <div className={useCenteredWorkspace ? "app-body app-body--centered" : "app-body"}>
                {!useCenteredWorkspace && <NavbarForm />}
                <main className={useCenteredWorkspace ? "main-content main-content--centered" : "main-content"}>
                    <Outlet />
                </main>
            </div>
        </div>
    );
}


export default MainLayout;
