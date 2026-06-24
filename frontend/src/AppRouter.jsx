import { Route, Routes} from "react-router-dom";
import LoginPage from "./pages/Authentication/LoginPage.jsx";
// import RegisterPage from "./pages/Authentication/RegisterPage.jsx";
// import FogotPassword from "./pages/Authentication/FogotPassword.jsx";
import Homepage from "./pages/homePage/HomePage.jsx";

function AppRouter(){
    return(
        <Routes>
            <Route
                path="/login"
                element = {<LoginPage />}
            />
            {/* <Route
                path="/register"
                element={<RegisterPage/>}
            /> */}
            {/* <Route
                path="/forgot_password"
                element={<FogotPassword />}
            />*/
            <Route
                path="/home_page"
                element={<Homepage/>}
            /> }
        </Routes>
    )

}
export default AppRouter