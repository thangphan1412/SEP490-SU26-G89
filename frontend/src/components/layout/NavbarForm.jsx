import { NavLink } from "react-router-dom"
import "../../assets/styles/css/layoutStyles/Navbar.css"

function NavbarForm() {
    return (
        //profiuel
        <div className="sidebar-container">
            <NavLink
                to="/user-profile/view"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                My Profile
            </NavLink>
            <NavLink
                to="/user-profile/update"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Update Profile
            </NavLink>
            <NavLink
                to="/company-profile/view"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Company Profile
            </NavLink>
            <NavLink
                to="/company-profile/update"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Update Company Profile
            </NavLink>
            {/*user profile admin and ceo and accountet and manager*/}
            <NavLink
                to="/user-profile/view"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                User View Profile
            </NavLink>
            <NavLink
                to="/user-profile/update"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                User Update Profile
            </NavLink>
            <NavLink
                to="/user-management/list"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                List User
            </NavLink>
            <NavLink
                to="/user-management/create"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Create User
            </NavLink>

            {/*Project*/}
            <NavLink
                to="/project-management/list"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                List Project
            </NavLink>
            <NavLink
                to="/project-management/create"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Create Project
            </NavLink>
            <NavLink
                to="/project-management/view"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                View Project
            </NavLink>
            <NavLink
                to="/project-management/update"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                UpdateProject
            </NavLink>

            {/*COntract*/}
            <NavLink
                to="/contract-management/create"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Create Contract
            </NavLink>
            <NavLink
                to="/contract-management/list"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                List Contract
            </NavLink>
            <NavLink
                to="/contract-management/view/:id"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                View Contract
            </NavLink>

            <NavLink
                to="/contract-management/update/:id"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Update Contract
            </NavLink>

            {/*permisstion*/}
            <NavLink
                to="/permission/list"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                List Permission
            </NavLink>
            <NavLink
                to="/permission/update"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Update Permission
            </NavLink>
            <NavLink
                to="/permission/create"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Permission CreatePage
            </NavLink>
            <NavLink
                to="/permission/view"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                View Permission
            </NavLink>
            {/*COntract type*/}
            <NavLink
                to="/contract-types"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                List ContractType
            </NavLink>
            <NavLink
                to="/contract-types/new"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Create Contract Type
            </NavLink>
            <NavLink
                to="/contract-types/detail/:id"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                View Contract Type
            </NavLink>
            <NavLink
                to="/contract-types/update/:id"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Update Contract Type
            </NavLink>
            {/*Department*/}
            <NavLink
                to="/department-management/list"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                List Department
            </NavLink>
            <NavLink
                to="/department-management/create"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Create Department
            </NavLink>
            <NavLink
                to="/department-management/view/:id"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                View Department
            </NavLink>
            <NavLink
                to="/department-management/update/:id"
                className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
            >
                Update Department
            </NavLink>
        </div>
    )
}

export default NavbarForm