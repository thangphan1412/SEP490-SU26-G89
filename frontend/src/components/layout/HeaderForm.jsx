import { NavLink } from "react-router-dom"
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import "../../assets/styles/css/layoutStyles/Header.css"

import {    IconContract,
            IconLayoutDashboard,
            IconCreditCard,
            IconShieldCheck,
            IconBuildingSkyscraper,
            IconUserPlus,
            IconReportAnalytics,
            IconSettings,
            IconUserCircle
        } from '@tabler/icons-react';
function HeaderForm(){
    const fullName = localStorage.getItem("fullName") || "Alex Morgan";
    const role = localStorage.getItem("role") || "Admin";

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

                        {/* HIỂN THỊ DỰA TRÊN ROLE */}
                        {/* Chỉ CEO và Manager mới thấy Employee Management */}
                        {['CEO', 'Manager', 'Admin'].includes(role) && (
                            <Nav.Link as={NavLink} to="/user-management/list?type=employee" className="nav-item">
                                <IconUserPlus stroke={2} />Employee Management
                            </Nav.Link>
                        )}

                        {/* Chỉ CEO mới thấy Customer Management */}
                        {['CEO', ].includes(role) && (
                            <Nav.Link as={NavLink} to="/user-management/list?type=customer" className="nav-item">
                                <IconUserPlus stroke={2} />Customer Management
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

                        <NavDropdown.Item as={NavLink} to="/user-profile/view">Dashboard</NavDropdown.Item>
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
