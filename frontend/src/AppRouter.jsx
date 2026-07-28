import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/authentication/LoginPage.jsx";
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
import ViewPermissionPage from "./pages/Permission Management/ViewPermissionPage.jsx";
import UpdatePermissionPage from "./pages/Permission Management/UpdatePermissionPage.jsx";
import ListContractType from "./pages/contractType/ListContractType.jsx";
import CreateContractType from "./pages/contractType/CreateContractType.jsx";
import ViewContractType from "./pages/contractType/ViewContractType.jsx";
import UpdateContractType from "./pages/contractType/UpdateContractType.jsx";
import ListDepartment from "./pages/Departments/ListDepartment.jsx";
import CreateDepartment from "./pages/Departments/CreateDepartment.jsx";
import ViewDepartment from "./pages/Departments/ViewDepartment.jsx";
import UpdateDepartment from "./pages/Departments/UpdateDepartment.jsx";
import ProtectedRoute from "./components/common/ProtectedRouter.jsx";
import HeaderForm from "./components/layout/HeaderForm.jsx";
import MainLayout from "./components/layout/MainLayout.jsx";
import ForgotPassword from "./pages/authentication/ForgotPassword.jsx";
import Resetpassword from "./pages/authentication/Resetpassword.jsx";
import ViewPhase from "./pages/Phase Management/ViewPhase.jsx";
import AgreementStatistics from "./pages/Dashboard/AgreementStatistics.jsx";
import TotalAgreements from "./pages/Dashboard/TotalAgreements.jsx";
import PendingSignatureAgreements from "./pages/Dashboard/PendingSignatureAgreements.jsx";
import ContractStatisticalReports from "./pages/Dashboard/ContractStatisticalReports.jsx";
import SignatureList from "./pages/signature/SignatureList.jsx";

function AppRouter() {
    return (
        <Routes>
            <Route path="/"
                element={<Navigate to="/login" replace />}
            />
            <Route
                path="/login"
                element={<LoginPage />}
            />
            <Route
                path="/forgot_password"
                element={<ForgotPassword />}
            />
            <Route
                path="/reset-password"
                element={<Resetpassword />}
            />

            <Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
                <Route
                    path="/home_page"

                    element={
                        <ProtectedRoute>
                            <Homepage />
                        </ProtectedRoute>
                    }
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
                    path="/phase-management/view/:projectId/:phaseId"
                    element={<ViewPhase />}
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
                <Route
                    path="/permission/create"
                    element={<PermissionCreatePage />}
                />
                <Route
                    path="/permission/list"
                    element={<ListPermissionPage />}
                />
                <Route
                    path="/permission/view"
                    element={<ViewPermissionPage />}
                />
                <Route
                    path="/permission/update"
                    element={<UpdatePermissionPage />}
                />
                <Route
                    path="/contract-types"
                    element={<ListContractType />}
                />
                <Route
                    path="/contract-types/new"
                    element={<CreateContractType />}
                />
                <Route
                    path="/contract-types/detail/:id"
                    element={<ViewContractType />}
                />
                <Route
                    path="/contract-types/update/:id"
                    element={<UpdateContractType />}
                />
                <Route
                    path="/department-management/list"
                    element={<ListDepartment />}
                />
                <Route
                    path="/department-management/create"
                    element={<CreateDepartment />}
                />
                <Route
                    path="/department-management/view/:id"
                    element={<ViewDepartment />}
                />
                <Route
                    path="/department-management/update/:id"
                    element={<UpdateDepartment />}
                />
                <Route
                    path="/dashboard/agreement-statistics"
                    element={<AgreementStatistics />}
                />
                <Route
                    path="/dashboard/total-agreements"
                    element={<TotalAgreements />}
                />
                <Route
                    path="/dashboard/pending-signature-agreements"
                    element={<PendingSignatureAgreements />}
                />
                <Route
                    path="/dashboard/contract-statistical-reports"
                    element={<ContractStatisticalReports />}
                />
                <Route
                    path="/signature-management/list"
                    element={<SignatureList />}
                />
            </Route>


        </Routes>
    )

}
export default AppRouter
