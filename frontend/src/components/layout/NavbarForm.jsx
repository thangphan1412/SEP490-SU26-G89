import { NavLink, useLocation } from "react-router-dom"
import { navConfig } from "../../config/pageConfig/navConfig.js"
import "../../assets/styles/css/layoutStyles/Navbar.css"

function NavbarForm() {
    const location = useLocation()

    const activeSection = navConfig.find((section) =>
        section.matchPaths.some((prefix) => location.pathname.startsWith(prefix))
    )

    if (!activeSection) return null

    return (
        <div className="sidebar-container">
            {activeSection.children.map((item) => (
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