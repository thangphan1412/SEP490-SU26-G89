import { Route, Routes } from "react-router-dom";
import LoginPage from "./pages/Authentication/LoginPage.jsx";
// import RegisterPage from "./pages/Authentication/RegisterPage.jsx";
// import FogotPassword from "./pages/Authentication/FogotPassword.jsx";
import Homepage from "./pages/homePage/HomePage.jsx";
import ViewProfile from "./pages/CompanyProfileManagement/ViewProfile.jsx";
import UpdateProfile from "./pages/CompanyProfileManagement/UpdateProfile.jsx";
import UserViewProfile from "./pages/UserProfileManagement/ViewProfile.jsx";
import UserUpdateProfile from "./pages/UserProfileManagement/UpdateProfile.jsx";
import ListUser from "./pages/UserManagement/ListUser.jsx";
import CreateUser from "./pages/UserManagement/CreateUser.jsx";
import ViewUser from "./pages/UserManagement/ViewUser.jsx";
import UpdateUser from "./pages/UserManagement/UpdateUser.jsx";
import ListProject from "./pages/ProjectManagement/ListProject.jsx";
import CreateProject from "./pages/ProjectManagement/CreateProject.jsx";
import ViewProject from "./pages/ProjectManagement/ViewProject.jsx";
import UpdateProject from "./pages/ProjectManagement/UpdateProject.jsx";
import ListContract from "./pages/ContractManagement/ListContract.jsx";
import CreateContract from "./pages/ContractManagement/CreateContract.jsx";
import ViewContract from "./pages/ContractManagement/ViewContract.jsx";
import UpdateContract from "./pages/ContractManagement/UpdateContract.jsx";
import PermissionCreatePage from "./pages/Permission Management/CreatePermissionPage.jsx";
import ListPermissionPage from "./pages/Permission Management/ListPermissionPage.jsx";
import ContractTypes from "./pages/contractType/ContractTypes.jsx";
import CreateContractType from "./pages/contractType/CreateContractType.jsx";
import ViewContractType from "./pages/contractType/ViewContractType.jsx";
import UpdateContractType from "./pages/contractType/UpdateContractType.jsx";

function AppRouter() {
    return (
        <Routes>
            <Route
                path="/login"
                element={<LoginPage />}
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
                element={<Homepage />}
            />
            <Route
                path="/company-profile/view"
                element={<ViewProfile />}
            />
            <Route
                path="/company-profile/update"
                element={<UpdateProfile />}
            />
            <Route
                path="/user-profile/view"
                element={<UserViewProfile />}
            />
            <Route
                path="/user-profile/update"
                element={<UserUpdateProfile />}
            />
            <Route
                path="/user-management/list"
                element={<ListUser />}
            />
            <Route
                path="/user-management/create"
                element={<CreateUser />}
            />
            <Route
                path="/user-management/view/:id"
                element={<ViewUser />}
            />
            <Route
                path="/user-management/update/:id"
                element={<UpdateUser />}
            />
            <Route
                path="/project-management/list"
                element={<ListProject />}
            />
            <Route
                path="/project-management/create"
                element={<CreateProject />}
            />
            <Route
                path="/project-management/view"
                element={<ViewProject />}
            />
            <Route
                path="/project-management/update"
                element={<UpdateProject />}
            />
            <Route
                path="/contract-management/list"
                element={<ListContract />}
            />
            <Route
                path="/contract-management/create"
                element={<CreateContract />}
            />
            <Route
                path="/contract-management/view/:id"
                element={<ViewContract />}
            />
            <Route
                path="/contract-management/update/:id"
                element={<UpdateContract />}
            />
            <Route path="/permission/create" element={<PermissionCreatePage />}></Route>
            <Route path="/permission/list" element={<ListPermissionPage />}></Route>
            
            <Route
                path="/contract-types"
                element={<ContractTypes />}
            />
            <Route
                path="/contract-types/new"
                element={<CreateContractType />}
            />
            <Route
                path="/contract-types/detail"
                element={<ViewContractType />}
            />
            <Route
                path="/contract-types/update"
                element={<UpdateContractType />}
            />
        </Routes>
    )

}
export default AppRouter
