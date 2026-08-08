import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Button, Spinner, Alert, Stack, Badge } from "react-bootstrap";

import {
    IconEdit,
    IconUser,
    IconShieldCheck,
    IconClock,
    IconSignature,
    IconInfoCircle
} from "@tabler/icons-react";

import { getMyProfile } from "../../services/userService/userApi.js";

// Hàm tính thời gian Last Active
const timeAgo = (dateString) => {
    if (!dateString || dateString === "Never logged in") return "Never logged in";
    const utcDateString = dateString.endsWith('Z') ? dateString : dateString + 'Z';
    const date = new Date(utcDateString);
    if (isNaN(date.getTime())) return "Never logged in";
    const now = new Date();
    const seconds = Math.floor((now - date) / 1000);
    if (seconds <= 0) return "Just now";
    if (seconds < 60) return "Just now";
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes} min${minutes !== 1 ? 's' : ''} ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours} hour${hours !== 1 ? 's' : ''} ago`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days} day${days !== 1 ? 's' : ''} ago`;
    const weeks = Math.floor(days / 7);
    if (weeks < 4) return `${weeks} week${weeks !== 1 ? 's' : ''} ago`;
    const months = Math.floor(days / 30);
    if (months < 12) return `${months} month${months !== 1 ? 's' : ''} ago`;
    const years = Math.floor(days / 365);
    return `${years} year${years !== 1 ? 's' : ''} ago`;
};

// Hàm format ngày tháng đẹp (Ví dụ: May 22, 2025)
const formatDate = (dateString) => {
    if (!dateString || dateString === "N/A") return "N/A";
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-US', options);
};

function ViewProfile() {
    const navigate = useNavigate();
    const [userProfile, setUserProfile] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchUserProfile = async () => {
            setLoading(true);
            try {
                const response = await getMyProfile();
                const data = response.data.data;

                setUserProfile({
                    fullName: `${data.firstName || ""} ${data.lastName || ""}`.trim(),
                    email: data.email || "",
                    phoneNumber: data.numberPhone || "N/A",
                    accountStatus: data.status || "INACTIVE",
                    department: data.departmentName || "N/A",
                    role: data.role || "N/A",
                    userId: data.userId || "N/A",
                    dateJoined: formatDate(data.dateJoined),
                    lastActive: data.lastActive,
                    lastUpdated: formatDate(data.lastUpdated),
                    // Hardcode tạm thời theo yêu cầu
                    defaultSignature: "Default Work Signature",
                });
            } catch (error) {
                console.error("Lỗi khi tải thông tin cá nhân:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchUserProfile();
    }, []);

    return (
        <div className="bg-light min-vh-screen">
            <Container fluid="lg" className="mb-5">
                <section className="border shadow-sm rounded-4 overflow-hidden bg-white mt-4">
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 mb-2 fw-bold text-dark">My Profile</h1>
                            <p className="text-muted mb-0">View your personal information and account details.</p>
                        </div>
                        <Button variant="primary" className="fw-bold px-4 d-flex align-items-center gap-2" onClick={() => navigate("/user-profile/update")} disabled={loading}>
                            <IconEdit size={19} color="#ffffff" /> Edit Profile
                        </Button>
                    </div>

                    {loading ? (
                        <div className="text-center py-5 my-5 text-secondary">
                            <Spinner animation="border" className="mb-3" />
                            <p>Loading your profile details...</p>
                        </div>
                    ) : userProfile ? (
                        <>
                            <Card className="m-4 border rounded-3 p-4">
                                <div className="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
                                    <div className="d-flex align-items-center gap-2">
                                        <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                            <IconUser size={20} />
                                        </div>
                                        <h2 className="h5 fw-bold mb-0 text-dark">Personal Information</h2>
                                    </div>
                                    {/* BADGE CHUẨN MÀU THEO STATUS */}
                                    <span className={`badge ${userProfile.accountStatus.toUpperCase() === 'ACTIVE' ? 'bg-success bg-opacity-10 text-success' : 'bg-secondary bg-opacity-10 text-secondary'} px-3 py-2 rounded-pill fw-bold`}>
                                        ● {userProfile.accountStatus}
                                    </span>
                                </div>

                                <Row className="g-4">
                                    <Col md={3} className="text-center border-end d-flex flex-column align-items-center justify-content-center pb-4 pb-md-0">
                                        <div className="position-relative mb-3">
                                            <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm" style={{ width: "100px", height: "100px", fontSize: "36px" }}>
                                                {userProfile.fullName ? userProfile.fullName.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase() : 'U'}
                                            </div>
                                        </div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">{userProfile.fullName}</h3>
                                        <p className="small text-muted mb-0">{userProfile.role}</p>
                                    </Col>

                                    <Col md={9} className="px-4">
                                        <Row className="g-3 small">
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Full Name</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.fullName}</Col>
                                                </Row>
                                            </Col>
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Email</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold text-break">{userProfile.email}</Col>
                                                </Row>
                                            </Col>
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Phone Number</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.phoneNumber}</Col>
                                                </Row>
                                            </Col>
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">User ID</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.userId}</Col>
                                                </Row>
                                            </Col>
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Role</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.role}</Col>
                                                </Row>
                                            </Col>
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Department</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.department}</Col>
                                                </Row>
                                            </Col>
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Date Joined</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.dateJoined}</Col>
                                                </Row>
                                            </Col>
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Last Updated</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.lastUpdated}</Col>
                                                </Row>
                                            </Col>
                                            <Col md={6} className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={4} className="text-muted fw-medium">Default Signature</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={7} className="text-dark fw-semibold">{userProfile.defaultSignature}</Col>
                                                </Row>
                                            </Col>
                                        </Row>
                                    </Col>
                                </Row>
                            </Card>

                            <Card className="mx-4 mb-4 border rounded-3 p-4">
                                <Stack direction="horizontal" className="justify-content-between align-items-start border-bottom pb-3 mb-4">
                                    <div className="d-flex align-items-center gap-2">
                                        <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                            <IconShieldCheck size={20} />
                                        </div>
                                        <div>
                                            <h2 className="h5 fw-bold mb-0 text-dark">Account & Access Summary</h2>
                                            <p className="small text-muted mb-0">Your profile is used across contracts, approvals, and internal workflows.</p>
                                        </div>
                                    </div>
                                </Stack>

                                <Row className="g-3">
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3"><IconUser size={24} /></div>
                                            <span className="small text-muted d-block mb-1">Role</span>
                                            <h3 className="h6 fw-bold text-dark my-1">{userProfile.role}</h3>
                                            <p className="small text-muted mb-0">Assigned role</p>
                                        </div>
                                    </Col>
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3"><IconShieldCheck size={24} /></div>
                                            <span className="small text-muted d-block mb-1">Department</span>
                                            <h3 className="h6 fw-bold text-dark my-1">{userProfile.department}</h3>
                                            <p className="small text-muted mb-0">Workflow routing</p>
                                        </div>
                                    </Col>
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3"><IconSignature size={24} /></div>
                                            <span className="small text-muted d-block mb-1">Signature Library</span>
                                            <h3 className="h6 fw-bold text-dark my-1">6 signatures</h3>
                                            <p className="small text-muted mb-0">Personal e-signatures</p>
                                        </div>
                                    </Col>
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3"><IconClock size={24} /></div>
                                            <span className="small text-muted d-block mb-1">Last Login</span>
                                            <h3 className="h6 fw-bold text-dark my-1">{timeAgo(userProfile.lastActive)}</h3>
                                            <p className="small text-muted mb-0">Recent account activity</p>
                                        </div>
                                    </Col>
                                </Row>
                            </Card>

                            <Alert variant="info" className="mx-4 mb-4 d-flex align-items-center gap-2 py-3">
                                <IconInfoCircle size={20} />
                                <span>To update your personal information, click <strong>Edit Profile</strong>.</span>
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