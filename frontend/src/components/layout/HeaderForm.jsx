import { NavLink } from "react-router-dom"
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import "../../assets/styles/css/layoutStyles/Header.css"

import {    IconContract,
            IconLayoutDashboard,
            IconCreditCard,
            IconShieldCheck,
            IconUserShield,
            IconBuildingSkyscraper,
            IconUserPlus,
            IconReportAnalytics,
            IconUserCircle
        } from '@tabler/icons-react';
function HeaderForm(){
    const fullName = localStorage.getItem("fullName") || "Guest";
    const role = localStorage.getItem("role") || "";

    return(

        <div className="header-container-fluid">
            <Navbar className="header-navbar">
                <div className="brand-wrapper">
                    <div className="brand-icon">
                        <IconContract stroke={2} />
                    </div>
                    <div className="brand-text">
                        <Navbar.Brand as={NavLink} to="/home_page" className="brand-name">E-CONTRACT</Navbar.Brand>
                        <span className="brand-sub">Management System</span>
                    </div>
                </div>

                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="nav-list">
                        <Nav.Link as={NavLink} to="/signature-management/list" className="nav-item" ><IconLayoutDashboard stroke={2}/>Signature Management</Nav.Link>
                        <Nav.Link as={NavLink}
                                  to="/contract-management/list"
                                  // className={({ isActive  }) => isActive ? "nav-item active": "nav-item"}
                            className="nav-item"

                        ><IconContract stroke={2}/>Contract Management</Nav.Link>
                        <Nav.Link as={NavLink} to="/project-management/list" className="nav-item"><IconCreditCard stroke={2} />Project Management</Nav.Link>
                        <Nav.Link as={NavLink} to="/permission/list" className="nav-item"><IconShieldCheck stroke={2} />Permission Management</Nav.Link>
                        <Nav.Link as={NavLink} to="/department-management/list" className="nav-item"><IconBuildingSkyscraper stroke={2} />Department Management</Nav.Link>

                        {['CEO', 'Admin'].includes(role) && (
                            <Nav.Link as={NavLink} to="/role-management/list" className="nav-item">
                                <IconUserShield stroke={2} />Role Management
                            </Nav.Link>
                        )}

                        {/* HIỂN THỊ MENU USER MANAGEMENT */}
                        {['CEO', 'Administrator', 'Accountant', 'HeadOfDepartment'].includes(role) && (
                            <Nav.Link as={NavLink} to="/user-management/list" className="nav-item">
                                <IconUserPlus stroke={2} />User Management
                            </Nav.Link>
                        )}

                        <Nav.Link as={NavLink} to="#" className="nav-item"><IconReportAnalytics stroke={2} />Reports</Nav.Link>
                        {/*<Nav.Link href="#" className="nav-item"><IconSettings stroke={2} />Settings</Nav.Link>*/}

                    </Nav>
                </Navbar.Collapse>
                <div className="user-info">

                     <NavDropdown title= {

                       <span className="user-info">

                          <IconUserCircle stroke={2} />
                            <span className="user-text">
                                <span  className="user-name">{fullName}</span>
                                <span  className="user-role">{role}</span>

                            </span>
                        </span>
                     }  id="basic-nav-dropdown">

                         {/* --- ĐÃ FIX Ở ĐÂY: GIẤU DASHBOARD VỚI CÁC ROLE KHÁC --- */}
                         {['CEO', 'Administrator', 'Accountant'].includes(role) && (
                             <NavDropdown.Item as={NavLink} to="/dashboard/agreement-statistics">Dashboard</NavDropdown.Item>
                         )}
                         <NavDropdown.Item as={NavLink} to="/user-profile/view">My profile</NavDropdown.Item>
                         <NavDropdown.Item as={NavLink} to="/company-profile/view">
                            Company profile
                         </NavDropdown.Item>
                         <NavDropdown.Item as={NavLink} to="#action/3.3">Setting</NavDropdown.Item>
                         <NavDropdown.Divider />
                         <NavDropdown.Item as={NavLink} to="/login">
                            Logout
                         </NavDropdown.Item>
                     </NavDropdown>

                </div>

            </Navbar>
        </div>
    )
}
export default HeaderForm
