import { Route, Routes } from "react-router-dom";
import LoginPage from "./pages/Authentication/LoginPage.jsx";
// import RegisterPage from "./pages/Authentication/RegisterPage.jsx";
// import FogotPassword from "./pages/Authentication/FogotPassword.jsx";
import Homepage from "./pages/homePage/HomePage.jsx";
import ContractTypes from "./pages/contractType/ContractTypes.jsx";
import CreateContractType from "./pages/contractType/CreateContractType.jsx";

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
            />*/}
            <Route
                path="/home_page"
                element={<Homepage/>}
            />
            <Route
                path="/contract-types"
                element={<ContractTypes />}
            />
            <Route
                path="/contract-types/new"
                element={<CreateContractType />}
            />
        </Routes>
    )

}
export default AppRouter