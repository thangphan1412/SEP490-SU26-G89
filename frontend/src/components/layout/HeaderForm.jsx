import Container from 'react-bootstrap/Container';
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
import {useEffect, useState} from "react";

function HeaderForm(){
    const [fullName, setFullName] = useState("");
    const [role, setRole] = useState("");
    useEffect(() => {
        const storedFullName = localStorage.getItem("fullName");
        const storedRole = localStorage.getItem("role");
        if(storedFullName) setFullName(storedFullName);
        if(storedRole) setRole(storedRole)
    }, []);
    return(
     
        <div className="header-container-fluid">
            <Navbar className="header-navbar">
                <div className="brand-wrapper">
                    <div className="brand-icon">
                        <IconContract stroke={2} />
                    </div>
                    <div className="brand-text">
                        <Navbar.Brand href="home" className="brand-name">E-CONTRACT</Navbar.Brand>
                        <span className="brand-sub">Management System</span>
                    </div>
                </div>

                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="nav-list">
                        <Nav.Link href="#" className="nav-item" ><IconLayoutDashboard stroke={2}/>Signature Management</Nav.Link>
                        <Nav.Link href="/contract-management/list" className="nav-item"><IconContract stroke={2}/>Contract Management</Nav.Link>
                        <Nav.Link href="/project-management/list" className="nav-item"><IconCreditCard stroke={2} />Project Management</Nav.Link>
                        <Nav.Link href="/permission/list" className="nav-item"><IconShieldCheck stroke={2} />Permission Management</Nav.Link>
                        <Nav.Link href="/user-management/list" className="nav-item"><IconBuildingSkyscraper stroke={2} />Department Management</Nav.Link>
                        <Nav.Link href="/user-management/list" className="nav-item"><IconUserPlus stroke={2} />Employee Management</Nav.Link>
                        <Nav.Link href="#" className="nav-item"><IconReportAnalytics stroke={2} />Reports</Nav.Link>
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
                                
                        <NavDropdown.Item href="#action/3.1">Action</NavDropdown.Item>
                        <NavDropdown.Item href="#action/3.2">
                            Another action
                        </NavDropdown.Item>
                        <NavDropdown.Item href="#action/3.3">Something</NavDropdown.Item>
                        <NavDropdown.Divider />
                        <NavDropdown.Item href="#action/3.4">
                            Separated link
                        </NavDropdown.Item>
                    </NavDropdown>

                </div>
                
            </Navbar>
        </div>
    )
}
export default HeaderForm