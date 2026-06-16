import { Route, Routes} from "react-router-dom";
import LoginPage from "./pages/Authentication/LoginPage.jsx";

function AppRouter(){
    return(
        <Routes>
            <Route
                path="/login"
                element = {<LoginPage />}
            />
        </Routes>
    )

}
export default AppRouter