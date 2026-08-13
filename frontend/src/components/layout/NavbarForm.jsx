import { NavLink, useLocation } from "react-router-dom"
import { navConfig } from "../../config/pageConfig/navConfig.js"
import "../../assets/styles/css/layoutStyles/Navbar.css"

function NavbarForm() {
    const location = useLocation()
    const currentUserRole = localStorage.getItem("role") || ""; // Lấy role hiện tại

    const activeSection = navConfig.find((section) =>
        section.matchPaths.some((prefix) => location.pathname.startsWith(prefix))
    )

    if (!activeSection) return null

    return (
        <div className="sidebar-container">
            {activeSection.children
                // Lọc những trang được phép vào theo Role (nếu có config allowedRoles)
                .filter(item => !item.allowedRoles || item.allowedRoles.includes(currentUserRole))
                .map((item) => (
                <NavLink
                    key={item.path}
                    to={item.path}
                    className={({ isActive }) => isActive ? "sidebar-item active" : "sidebar-item"}
                >
                    {item.label}
                </NavLink>
            ))}
        </div>
    )
}

export default NavbarForm