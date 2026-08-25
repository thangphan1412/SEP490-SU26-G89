import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Button, NavDropdown, Spinner, Alert, Stack, Badge } from "react-bootstrap";

// Import các Icon từ Tabler Icons
import {
    IconWorld,
    IconEdit,
    IconCheck,
    IconFileDescription,
    IconFileExport,
    IconShieldCheck,
    IconClock,
    IconInfoCircle
} from "@tabler/icons-react";

// IMPORT HÀM GỌI API
import { getCompanyProfile } from "../../services/companyService/companyApi";

function ViewProfile({ onEditProfile }) {
    const navigate = useNavigate();

    // State quản lý dữ liệu và loading
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);

    // Ở ViewProfile.jsx, xác định quyền Edit:
    const currentUserRole = localStorage.getItem("role");
    const canEditProfile = currentUserRole === 'Accountant';

    // Gọi API lấy dữ liệu Company Profile
    useEffect(() => {
        const fetchProfile = async () => {
            setLoading(true);
            try {
                // Gọi API lấy thông tin Company
                const response = await getCompanyProfile();

                // Gán dữ liệu trả về từ API vào state
                setProfile(response.data.data);
            } catch (error) {
                console.error("Lỗi khi tải dữ liệu công ty:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    const handleEditProfile = () => {
        onEditProfile?.(profile);
        navigate("/company-profile/update");
    };

    return (
        <div className="bg-light min-vh-screen">

            {/* --- MAIN CONTENT --- */}
            <Container fluid="lg" className="mb-5">
                <section className="border shadow-sm rounded-4 overflow-hidden bg-white">

                    {/* Panel Header */}
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 mb-2 fw-bold text-dark">Company Profile</h1>
                            <p className="text-muted mb-0">
                                Manage your company's legal identity information for automatic use in contracts and documents.
                            </p>
                        </div>
                        {/*Ẩn nút Edit nếu không phải Accountant:*/}
                        {canEditProfile && (
                            <Button variant="primary" onClick={handleEditProfile} disabled={loading}>
                                <IconEdit size={19} color="#ffffff" />
                                <span>Edit Profile</span>
                            </Button>
                        )}
                    </div>

                    {/* Check Loading State */}
                    {loading ? (
                        <div className="text-center py-5 my-5 text-secondary">
                            <Spinner animation="border" className="mb-3" style={{ width: "3rem", height: "3rem" }} />
                            <p>Loading company profile details...</p>
                        </div>
                    ) : profile ? (
                        <>
                            {/* --- COMPANY LEGAL INFORMATION --- */}
                            <Card className="m-4 border rounded-3 p-4">
                                <Card.Header className="bg-white border-0 p-0 mb-3">
                                    <h2 className="h5 fw-bold mb-0 text-dark">Company Legal Information</h2>
                                </Card.Header>
                                <Card.Body className="p-0">
                                    <Row className="g-0 small">
                                        <Col md={6} className="d-flex py-3 border-bottom border-light px-2">
                                            <span className="text-muted w-50 fw-medium">Company Name</span>
                                            <strong className="text-dark w-50">{profile.companyName}</strong>
                                        </Col>
                                        <Col md={6} className="d-flex py-3 border-bottom border-light px-2">
                                            <span className="text-muted w-50 fw-medium">Email</span>
                                            <strong className="text-dark w-50 text-break">{profile.email}</strong>
                                        </Col>
                                        <Col md={6} className="d-flex py-3 border-bottom border-light px-2">
                                            <span className="text-muted w-50 fw-medium">Registered Address</span>
                                            <strong className="text-dark w-50">{profile.registeredAddress}</strong>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>

                            {/* --- AUTO-FILL USAGE --- */}
                            <Card className="mx-4 mb-4 border rounded-3 p-4">
                                <Stack direction="horizontal" className="justify-content-between align-items-start border-bottom pb-3 mb-4">
                                    <div>
                                        <h2 className="h5 fw-bold mb-2 text-dark">Auto-fill Usage</h2>
                                        <p className="small text-muted mb-0">
                                            These details are automatically inserted into contract templates and generated legal documents.
                                        </p>
                                    </div>
                                    <Badge bg="success" className="px-3 py-2 fs-6 d-inline-flex align-items-center gap-1">
                                        <IconCheck size={16} strokeWidth={3} />
                                        <span>Verified</span>
                                    </Badge>
                                </Stack>

                                <Row className="g-3">
                                    {/* Contract Templates */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3">
                                                <IconFileDescription size={24} />
                                            </div>
                                            <h3 className="h6 fw-bold text-dark mb-2">Contract Templates</h3>
                                            <p className="small text-muted mb-0">Company information is auto-filled in all contract templates.</p>
                                        </div>
                                    </Col>

                                    {/* Generated Documents */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3">
                                                <IconFileExport size={24} />
                                            </div>
                                            <h3 className="h6 fw-bold text-dark mb-2">Generated Documents</h3>
                                            <p className="small text-muted mb-0">Used in quotes, agreements, reports, and legal documents.</p>
                                        </div>
                                    </Col>

                                    {/* Compliance */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3">
                                                <IconShieldCheck size={24} />
                                            </div>
                                            <h3 className="h6 fw-bold text-dark mb-2">Compliance</h3>
                                            <p className="small text-muted mb-0">Ensures consistent and accurate company information.</p>
                                        </div>
                                    </Col>

                                    {/* Last Verified */}
                                    <Col lg={3} md={6}>
                                        <div className="p-3 border rounded h-100 bg-light">
                                            <div className="text-primary mb-3">
                                                <IconClock size={24} />
                                            </div>
                                            <h3 className="h6 fw-bold text-dark mb-2">Last Verified</h3>
                                            <p className="small text-muted mb-0">
                                                Verified on {profile.lastVerifiedDate} by {profile.verifiedBy}.
                                            </p>
                                        </div>
                                    </Col>
                                </Row>
                            </Card>

                            {/* --- INFO ALERT --- */}
                            <Alert variant="info" className="mx-4 mb-4 d-flex align-items-center gap-2 py-3">
                                <IconInfoCircle size={20} />
                                <span>To update your company information, click <strong className="text-primary" style={{ cursor: "pointer" }} onClick={handleEditProfile}>Edit Profile</strong>.</span>
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