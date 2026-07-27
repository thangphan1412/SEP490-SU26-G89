import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Button, NavDropdown, Spinner, Alert, Stack, Badge } from "react-bootstrap";

// Import các Icon từ Tabler Icons
import {
    IconWorld,
    IconEdit,
    IconUser,
    IconShieldCheck,
    IconClock,
    IconSignature,
    IconInfoCircle
} from "@tabler/icons-react";

// IMPORT HÀM GỌI API PROFILE
import { getMyProfile } from "../../services/userService/userApi.js";

function ViewProfile() {
    const navigate = useNavigate();

    // State quản lý dữ liệu và loading
    const [userProfile, setUserProfile] = useState(null);
    const [loading, setLoading] = useState(true);

    // Lấy thông tin từ Backend
    useEffect(() => {
        const fetchUserProfile = async () => {
            setLoading(true);
            try {
                const response = await getMyProfile();
                const data = response.data.data;

                // Ghép firstName và lastName thành fullName, map các trường thật và hardcode các trường thừa
                setUserProfile({
                    fullName: `${data.firstName || ""} ${data.lastName || ""}`.trim(),
                    email: data.email || "",
                    phoneNumber: data.numberPhone || "",
                    accountStatus: data.status || "Active",
                    // Các trường dưới đây không có trong DTO cập nhật profile nên hardcode
                    department: "Legal Department",
                    position: "Contract Manager",
                    employeeId: "EMP-00023",
                    dateJoined: "March 15, 2023",
                    timeZone: "(GMT+07:00) Bangkok, Hanoi, Jakarta",
                    language: "English",
                    defaultSignature: "Default Work Signature",
                    lastUpdated: "May 22, 2025",
                });
            } catch (error) {
                console.error("Lỗi khi tải thông tin cá nhân:", error);
                // Xử lý lỗi chưa đăng nhập hoặc token hết hạn ở đây
            } finally {
                setLoading(false);
            }
        };

        fetchUserProfile();
    }, []);

    return (
        <div className="bg-light min-vh-screen">
            {/* --- HEADER ĐỒNG BỘ --- */}
            {/*<header className="d-flex justify-content-between align-items-center px-4 py-3 bg-white border-bottom mb-4">*/}
            {/*    <div className="d-flex align-items-center gap-2">*/}
            {/*        <span className="fs-4">🛡️</span>*/}
            {/*        <div className="d-flex flex-column lh-sm">*/}
            {/*            <strong className="text-dark">E-CONTRACT</strong>*/}
            {/*            <small className="text-muted" style={{ fontSize: "12px" }}>Management System</small>*/}
            {/*        </div>*/}
            {/*    </div>*/}
            {/*    <NavDropdown title={<span className="text-dark fw-semibold"><IconWorld size={20} className="me-1"/>English</span>} id="lang-dropdown">*/}
            {/*        <NavDropdown.Item>English</NavDropdown.Item>*/}
            {/*        <NavDropdown.Item>Vietnamese</NavDropdown.Item>*/}
            {/*    </NavDropdown>*/}
            {/*</header>*/}

            {/* --- MAIN CONTENT --- */}
            <Container fluid="lg" className="mb-5">
                <section className="border shadow-sm rounded-4 overflow-hidden bg-white">

                    {/* Panel Header */}
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 mb-2 fw-bold text-dark">My Profile</h1>
                            <p className="text-muted mb-0">
                                View your personal information and account details.
                            </p>
                        </div>
                        <Button
                            variant="primary"
                            className="fw-bold px-4 d-flex align-items-center gap-2"
                            onClick={() => navigate("/user-profile/update")}
                            disabled={loading}
                        >
                            <IconEdit size={19} color="#ffffff" />
                            Edit Profile
                        </Button>
                    </div>

                    {/* Check Loading State */}
                    {loading ? (
                        <div className="text-center py-5 my-5 text-secondary">
                            <Spinner animation="border" className="mb-3" style={{ width: "3rem", height: "3rem" }} />
                            <p>Loading your profile details...</p>
                        </div>
                    ) : userProfile ? (
                        <>
                            {/* --- PERSONAL INFORMATION CARD --- */}
                            <Card className="m-4 border rounded-3 p-4">
                                <div className="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
                                    <div className="d-flex align-items-center gap-2">
                                        <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                            <IconUser size={20} />
                                        </div>
                                        <h2 className="h5 fw-bold mb-0 text-dark">Personal Information</h2>
                                    </div>
                                    <span className={`badge ${userProfile.accountStatus === 'Active' ? 'bg-success text-success' : 'bg-secondary text-secondary'} bg-opacity-10 px-3 py-2 rounded-pill fw-bold`}>
                                        {userProfile.accountStatus}
                                    </span>
                                </div>

                                <Row className="g-4">
                                    {/* Avatar Block */}
                                    <Col md={3} className="text-center border-end d-flex flex-column align-items-center justify-content-center pb-4 pb-md-0">
                                        <div className="position-relative mb-3">
                                            <div
                                                className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm"
                                                style={{ width: "100px", height: "100px", fontSize: "36px", boxShadow: "0 0 0 4px #edf2ff" }}
                                            >
                                                {userProfile.fullName ? userProfile.fullName.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase() : 'U'}
                                            </div>
                                        </div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">{userProfile.fullName}</h3>
                                        <p className="small text-muted mb-0">{userProfile.position}</p>
                                    </Col>

                                    {/* Detailed Information Grid */}
                                    <Col md={9} className="px-4">
                                        <Row className="g-3 small">

                                            {/* Full Name */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Full Name</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.fullName}</Col>
                                                </Row>
                                            </Col>

                                            {/* Email */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Email</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold text-break">{userProfile.email}</Col>
                                                </Row>
                                            </Col>

                                            {/* Phone Number */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Phone Number</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.phoneNumber}</Col>
                                                </Row>
                                            </Col>

                                            {/* Department */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Department</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.department}</Col>
                                                </Row>
                                            </Col>

                                            {/* Position */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Position</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.position}</Col>
                                                </Row>
                                            </Col>

                                            {/* Employee ID */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Employee ID</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.employeeId}</Col>
                                                </Row>
                                            </Col>

                                            {/* Date Joined */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Date Joined</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.dateJoined}</Col>
                                                </Row>
                                            </Col>

                                            {/* Time Zone */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-start g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Time Zone</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.timeZone}</Col>
                                                </Row>
                                            </Col>

                                            {/* Language */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Language</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.language}</Col>
                                                </Row>
                                            </Col>

                                            {/* Account Status */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Account Status</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-success fw-bold">{userProfile.accountStatus}</Col>
                                                </Row>
                                            </Col>

                                            {/* Default Signature */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Default Signature</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.defaultSignature}</Col>
                                                </Row>
                                            </Col>

                                            {/* Last Updated */}
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Last Updated</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.lastUpdated}</Col>
                                                </Row>
                                            </Col>
                                        </Row>
                                    </Col>
                                </Row>
                            </Card>

                            {/* --- ACCOUNT & ACCESS SUMMARY CARD --- */}
                            <Card className="mx-4 mb-4 border rounded-3 p-4">
                                <Stack direction="horizontal" className="justify-content-between align-items-start border-bottom pb-3 mb-4">
                                    <div className="d-flex align-items-center gap-2">
                                        <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                            <IconShieldCheck size={20} />
                                        </div>
                                        <div>
                                            <h2 className="h5 fw-bold mb-0 text-dark">Account & Access Summary</h2>
                                            <p className="small text-muted mb-0">
                                                Your profile is used across contracts, approvals, and internal workflows.
                                            </p>
                                        </div>
                                    </div>
                                    <Badge bg="primary" className="bg-opacity-10 text-primary border border-primary border-opacity-25 px-3 py-2 fs-6">
                                        Verified
                                    </Badge>
                                </Stack>

                                <Row className="g-3">
                                    {/* Role */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3">
                                                <IconUser size={24} />
                                            </div>
                                            <span className="small text-muted d-block mb-1">Role</span>
                                            <h3 className="h6 fw-bold text-dark my-1">Contract Manager</h3>
                                            <p className="small text-muted mb-0">Assigned role</p>
                                        </div>
                                    </Col>

                                    {/* Permissions */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3">
                                                <IconShieldCheck size={24} />
                                            </div>
                                            <span className="small text-muted d-block mb-1">Permissions</span>
                                            <h3 className="h6 fw-bold text-dark my-1">18 active</h3>
                                            <p className="small text-muted mb-0">Access permissions</p>
                                        </div>
                                    </Col>

                                    {/* Signature Library */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3">
                                                <IconSignature size={24} />
                                            </div>
                                            <span className="small text-muted d-block mb-1">Signature Library</span>
                                            <h3 className="h6 fw-bold text-dark my-1">6 signatures</h3>
                                            <p className="small text-muted mb-0">Personal e-signatures</p>
                                        </div>
                                    </Col>

                                    {/* Last Login */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3">
                                                <IconClock size={24} />
                                            </div>
                                            <span className="small text-muted d-block mb-1">Last Login</span>
                                            <h3 className="h6 fw-bold text-dark my-1">May 22, 2025</h3>
                                            <p className="small text-muted mb-0">Recent account activity</p>
                                        </div>
                                    </Col>
                                </Row>
                            </Card>

                            {/* --- INFO ALERT --- */}
                            <Alert variant="info" className="mx-4 mb-4 d-flex align-items-center gap-2 py-3">
                                <IconInfoCircle size={20} />
                                <span>
                                    To update your personal information, click <strong>Edit Profile</strong>.
                                </span>

                            </Alert>
                        </>
                    ) : (
                        <div className="text-center py-5 my-5 text-danger">
                            <p className="fw-bold">Profile not found or an error occurred.</p>
                        </div>
                    )}
                </section>
            </Container>
        </div>
    );
}

export default ViewProfile;