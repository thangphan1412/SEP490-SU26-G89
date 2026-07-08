import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Button, NavDropdown, Spinner, Alert, Stack } from "react-bootstrap";
import {
    IconWorld,
    IconEdit,
    IconUser,
    IconMail,
    IconPhone,
    IconId,
    IconBuilding,
    IconBriefcase,
    IconShieldCheck,
    IconCalendar,
    IconClock,
    IconClipboardList,
    IconInfoCircle
} from "@tabler/icons-react";

function ViewUser() {
    const navigate = useNavigate();

    // State quản lý dữ liệu và trạng thái loading
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Mô phỏng gọi API lấy chi tiết User giống file cũ của bạn
    useEffect(() => {
        const fetchUserDetails = async () => {
            setLoading(true);
            try {
                // Chờ API 1 giây
                await new Promise(resolve => setTimeout(resolve, 1000));

                // Trả về dữ liệu giả lập
                setUser({
                    fullName: "Emma Nguyen",
                    email: "emma.nguyen@econtract.com",
                    phoneNumber: "+1 (555) 123-4567",
                    employeeId: "EMP-001248",
                    department: "Legal Department",
                    position: "Senior Legal Counsel",
                    role: "Contract Manager",
                    accessScope: "Department Level",
                    dateJoined: "Jan 15, 2024",
                    lastLogin: "May 22, 2025, 09:32 AM",
                    status: "Active"
                });
            } catch (error) {
                console.error("Lỗi khi lấy dữ liệu người dùng:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchUserDetails();
    }, []);

    return (
        <div className="bg-light min-vh-screen">
            {/* --- HEADER ĐỒNG BỘ --- */}
            <header className="d-flex justify-content-between align-items-center px-4 py-3 bg-white border-bottom mb-4">
                <div className="d-flex align-items-center gap-2">
                    <span className="fs-4">🛡️</span>
                    <div className="d-flex flex-column lh-sm">
                        <strong className="text-dark">E-CONTRACT</strong>
                        <small className="text-muted" style={{ fontSize: "12px" }}>Management System</small>
                    </div>
                </div>
                <NavDropdown title={<span className="text-dark fw-semibold"><IconWorld size={20} className="me-1"/>English</span>} id="lang-dropdown">
                    <NavDropdown.Item>English</NavDropdown.Item>
                    <NavDropdown.Item>Vietnamese</NavDropdown.Item>
                </NavDropdown>
            </header>

            {/* --- MAIN CONTENT PANEL --- */}
            <Container fluid="lg" className="mb-5">
                <section className="border shadow-sm rounded-4 overflow-hidden bg-white">

                    {/* Header Panel */}
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 fw-bold mb-1">User Details</h1>
                            <p className="text-muted mb-0">Review employee information, role permissions, and department assignment.</p>
                        </div>
                        <Button
                            variant="primary"
                            className="fw-bold px-4 d-flex align-items-center gap-2"
                            onClick={() => navigate("/user-management/update")}
                            disabled={loading}
                        >
                            <IconEdit size={19} color="#ffffff" /> Edit User
                        </Button>
                    </div>

                    {/* Kiểm tra trạng thái loading */}
                    {loading ? (
                        <div className="text-center py-5 my-5 text-secondary">
                            <Spinner animation="border" className="mb-3" style={{ width: "3rem", height: "3rem" }} />
                            <p>Loading user details...</p>
                        </div>
                    ) : user ? (
                        <>
                            {/* --- EMPLOYEE INFORMATION CARD --- */}
                            <Card className="m-4 border rounded-3 p-4">
                                <h2 className="h5 fw-bold mb-4 text-dark">Employee Information</h2>

                                <Row className="g-4">
                                    {/* Cột 1: Khối Avatar */}
                                    <Col md={3} className="text-center border-end d-flex flex-column align-items-center justify-content-center">
                                        <div className="position-relative mb-3">
                                            <div
                                                className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm"
                                                style={{ width: "100px", height: "100px", fontSize: "36px", boxShadow: "0 0 0 4px #edf2ff" }}
                                            >
                                                {user.fullName ? user.fullName.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase() : 'U'}
                                            </div>
                                        </div>
                                        <h3 className="h6 fw-bold mb-2 text-dark">{user.fullName}</h3>
                                        <span className="badge bg-success bg-opacity-10 text-success px-3 py-2 rounded-pill fw-bold">
                                            ● {user.status}
                                        </span>
                                    </Col>

                                    {/* Cột 2: Thông tin chi tiết bên trái (Đã fix căn thẳng dấu hai chấm) */}
                                    <Col md={4} className="border-end px-3">
                                        <Stack gap={3} className="small">
                                            {/* Full Name */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconUser size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Full Name</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.fullName}</Col>
                                                </Row>
                                            </div>
                                            {/* Email */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-start g-0">
                                                    <Col xs={1} className="d-flex text-secondary pt-1"><IconMail size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2 pt-1">Email</Col>
                                                    <Col xs={1} className="text-muted text-center pt-1">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold text-break pt-1">{user.email}</Col>
                                                </Row>
                                            </div>
                                            {/* Phone Number */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconPhone size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Phone Number</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.phoneNumber}</Col>
                                                </Row>
                                            </div>
                                            {/* Employee ID */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconId size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Employee ID</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.employeeId}</Col>
                                                </Row>
                                            </div>
                                            {/* Department */}
                                            <div className="pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconBuilding size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Department</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.department}</Col>
                                                </Row>
                                            </div>
                                        </Stack>
                                    </Col>

                                    {/* Cột 3: Thông tin chi tiết bên phải (Đã fix căn thẳng dấu hai chấm) */}
                                    <Col md={5} className="px-3">
                                        <Stack gap={3} className="small">
                                            {/* Position */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconBriefcase size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Position</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.position}</Col>
                                                </Row>
                                            </div>
                                            {/* Role */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconShieldCheck size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Role</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.role}</Col>
                                                </Row>
                                            </div>
                                            {/* Access Scope */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconWorld size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Access Scope</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.accessScope}</Col>
                                                </Row>
                                            </div>
                                            {/* Date Joined */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconCalendar size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Date Joined</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.dateJoined}</Col>
                                                </Row>
                                            </div>
                                            {/* Last Login */}
                                            <div className="pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconClock size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Last Login</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.lastLogin}</Col>
                                                </Row>
                                            </div>
                                        </Stack>
                                    </Col>
                                </Row>
                            </Card>

                            {/* --- ROLE & ACCESS SUMMARY CARD --- */}
                            <Card className="mx-4 mb-4 border rounded-3 p-4">
                                <h2 className="h5 fw-bold mb-4 text-dark">Role & Access Summary</h2>

                                <Row className="g-3">
                                    {/* Assigned Role */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-2">
                                                <IconShieldCheck size={24} />
                                            </div>
                                            <span className="small text-muted d-block mb-1">Assigned Role</span>
                                            <h4 className="h6 fw-bold text-dark my-1">{user.role}</h4>
                                            <small className="text-muted">Full access to contract lifecycle.</small>
                                        </div>
                                    </Col>

                                    {/* Department Access */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-2">
                                                <IconBuilding size={24} />
                                            </div>
                                            <span className="small text-muted d-block mb-1">Department Access</span>
                                            <h4 className="h6 fw-bold text-dark my-1">{user.department}</h4>
                                            <small className="text-muted">Can view & manage contracts within unit.</small>
                                        </div>
                                    </Col>

                                    {/* Approval Rights */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-2">
                                                <IconClipboardList size={24} />
                                            </div>
                                            <span className="small text-muted d-block mb-1">Approval Rights</span>
                                            <h4 className="h6 fw-bold text-dark my-1">Up to $250,000</h4>
                                            <small className="text-muted">Authorized to approve changes.</small>
                                        </div>
                                    </Col>

                                    {/* Recent Activity */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-2">
                                                <IconClock size={24} />
                                            </div>
                                            <span className="small text-muted d-block mb-1">Recent Activity</span>
                                            <h4 className="h6 fw-bold text-dark my-1">32 Activities</h4>
                                            <small className="text-muted text-break">Last active on {user.lastLogin}.</small>
                                        </div>
                                    </Col>
                                </Row>
                            </Card>

                            {/* --- ALERT INFO --- */}
                            <Alert variant="info" className="mx-4 mb-4 d-flex align-items-center gap-2">
                                <IconInfoCircle size={20} />
                                <span>
                                    To make changes to this user, click <strong className="text-primary" style={{ cursor: "pointer" }} onClick={() => navigate("/user-management/update")}>Edit User</strong>.
                                </span>
                            </Alert>
                        </>
                    ) : (
                        <div className="text-center py-5 my-5 text-danger">
                            <p className="fw-bold">User not found or an error occurred.</p>
                        </div>
                    )}
                </section>
            </Container>
        </div>
    );
}

export default ViewUser;