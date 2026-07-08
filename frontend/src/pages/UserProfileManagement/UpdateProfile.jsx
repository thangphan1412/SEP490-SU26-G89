import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Form, Button, NavDropdown, Spinner, Stack, Alert } from "react-bootstrap";

// Import Icon chuẩn từ Tabler Icons
import {
    IconWorld,
    IconDeviceFloppy,
    IconEdit,
    IconFileDescription,
    IconShieldCheck,
    IconBuilding,
    IconClock,
    IconInfoCircle
} from "@tabler/icons-react";

const initialUserProfile = {
    fullName: "Alex Morgan",
    email: "alex.morgan@econtract.com",
    phoneNumber: "+84 28 3822 5678",
    department: "Legal Department",
    position: "Contract Manager",
    employeeId: "EMP-00023",
    timeZone: "(GMT+07:00) Bangkok, Hanoi, Jakarta",
    language: "English",
};

function UpdateProfile({ onSaveProfile }) {
    const navigate = useNavigate();
    const [userProfile, setUserProfile] = useState(initialUserProfile);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setUserProfile((currentProfile) => ({
            ...currentProfile,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setIsSubmitting(true);

        try {
            // Giả lập API lưu profile mất 1.5 giây
            await new Promise((resolve) => setTimeout(resolve, 1500));

            onSaveProfile?.(userProfile);

            alert("Cập nhật thông tin cá nhân thành công!");
            navigate("/user-profile/view");

        } catch (error) {
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra, vui lòng thử lại!");
        } finally {
            setIsSubmitting(false);
        }
    };

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

            {/* --- MAIN CONTENT --- */}
            <Container fluid="lg" className="mb-5">
                <Form onSubmit={handleSubmit} className="border shadow-sm rounded-4 overflow-hidden bg-white">

                    {/* Panel Header */}
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 mb-1 fw-bold text-dark">Update Profile</h1>
                            <p className="text-muted mb-0">
                                Update your personal information and account preferences.
                            </p>
                        </div>
                        <div className="form-actions d-flex gap-2">
                            <Button
                                variant="outline-secondary"
                                className="fw-bold px-3"
                                onClick={() => navigate("/user-profile/view")}
                                disabled={isSubmitting}
                            >
                                Cancel
                            </Button>
                            <Button
                                type="submit"
                                variant="primary"
                                className="fw-bold px-4 d-flex align-items-center gap-2"
                                disabled={isSubmitting}
                            >
                                {isSubmitting ? (
                                    <>
                                        <Spinner animation="border" size="sm" />
                                        <span>Saving...</span>
                                    </>
                                ) : (
                                    <>
                                        <IconDeviceFloppy size={19} color="#ffffff" />
                                        <span>Save Changes</span>
                                    </>
                                )}
                            </Button>
                        </div>
                    </div>

                    {/* --- PROFILE INFORMATION FORM --- */}
                    <Card className="m-4 border rounded-3 p-4">
                        <div className="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
                            <div className="d-flex align-items-center gap-2">
                                <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                    <IconEdit size={22} />
                                </div>
                                <h2 className="h5 fw-bold mb-0 text-dark">Profile Information</h2>
                            </div>
                            <span className="badge bg-success bg-opacity-10 text-success px-3 py-2 rounded fw-bold small border border-success border-opacity-25">
                                Verified Profile
                            </span>
                        </div>

                        <Row className="g-4">
                            {/* Avatar Section bên trái */}
                            <Col md={3} className="text-center border-end d-flex flex-column align-items-center justify-content-center pb-4 pb-md-0">
                                <div className="position-relative mb-3">
                                    <div
                                        className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm"
                                        style={{ width: "100px", height: "100px", fontSize: "36px", boxShadow: "0 0 0 4px #edf2ff" }}
                                    >
                                        {userProfile.fullName ? userProfile.fullName.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase() : 'U'}
                                    </div>
                                    <div
                                        className="position-absolute bg-white border rounded-circle d-flex align-items-center justify-content-center border-secondary-subtle text-secondary"
                                        style={{ width: "26px", height: "26px", right: "-4px", bottom: "4px" }}
                                    >
                                        <IconFileDescription size={15} />
                                    </div>
                                </div>
                                <Button size="sm" variant="outline-secondary" className="fw-semibold px-3">
                                    Change Avatar
                                </Button>
                                <p className="small text-muted mt-2 mb-0 text-center">
                                    Used across profile and approvals.
                                </p>
                            </Col>

                            {/* Form Input Section bên phải */}
                            <Col md={9} className="px-4">
                                <Row className="g-4">
                                    {/* Full Name */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">
                                                Full Name <span className="text-danger">*</span>
                                            </Form.Label>
                                            <Form.Control id="fullName" name="fullName" type="text" value={userProfile.fullName} onChange={handleChange} required disabled={isSubmitting} className="py-2" />
                                            <Form.Text className="text-muted">Display name used in the system.</Form.Text>
                                        </Form.Group>
                                    </Col>

                                    {/* Email */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">
                                                Email <span className="text-danger">*</span>
                                            </Form.Label>
                                            <Form.Control id="email" name="email" type="email" value={userProfile.email} onChange={handleChange} required disabled={isSubmitting} className="py-2" />
                                            <Form.Text className="text-muted">Work email for system notifications.</Form.Text>
                                        </Form.Group>
                                    </Col>

                                    {/* Phone Number */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">
                                                Phone Number <span className="text-danger">*</span>
                                            </Form.Label>
                                            <Form.Control id="phoneNumber" name="phoneNumber" type="tel" value={userProfile.phoneNumber} onChange={handleChange} required disabled={isSubmitting} className="py-2" />
                                        </Form.Group>
                                    </Col>

                                    {/* Department */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">
                                                Department <span className="text-danger">*</span>
                                            </Form.Label>
                                            <Form.Select id="department" name="department" value={userProfile.department} onChange={handleChange} required disabled={isSubmitting} className="py-2">
                                                <option value="Legal Department">Legal Department</option>
                                                <option value="Sales Department">Sales Department</option>
                                                <option value="Finance Department">Finance Department</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>

                                    {/* Position */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">
                                                Position <span className="text-danger">*</span>
                                            </Form.Label>
                                            <Form.Select id="position" name="position" value={userProfile.position} onChange={handleChange} required disabled={isSubmitting} className="py-2">
                                                <option value="Contract Manager">Contract Manager</option>
                                                <option value="Legal Specialist">Legal Specialist</option>
                                                <option value="Department Manager">Department Manager</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>

                                    {/* Employee ID */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Employee ID</Form.Label>
                                            <Form.Control id="employeeId" name="employeeId" type="text" value={userProfile.employeeId} disabled className="py-2" />
                                        </Form.Group>
                                    </Col>

                                    {/* Time Zone */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">
                                                Time Zone <span className="text-danger">*</span>
                                            </Form.Label>
                                            <Form.Select id="timeZone" name="timeZone" value={userProfile.timeZone} onChange={handleChange} required disabled={isSubmitting} className="py-2">
                                                <option value="(GMT+07:00) Bangkok, Hanoi, Jakarta">(GMT+07:00) Bangkok, Hanoi, Jakarta</option>
                                                <option value="(GMT+08:00) Singapore, Kuala Lumpur">(GMT+08:00) Singapore, Kuala Lumpur</option>
                                                <option value="(GMT+09:00) Tokyo, Seoul">(GMT+09:00) Tokyo, Seoul</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>

                                    {/* Language */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">
                                                Language <span className="text-danger">*</span>
                                            </Form.Label>
                                            <Form.Select id="language" name="language" value={userProfile.language} onChange={handleChange} required disabled={isSubmitting} className="py-2">
                                                <option value="English">English</option>
                                                <option value="Vietnamese">Vietnamese</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Card>

                    {/* --- USAGE PREVIEW --- */}
                    <Card className="mx-4 mb-4 border rounded-3 p-4">
                        <Stack direction="horizontal" gap={3} className="align-items-start mb-4">
                            <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                <IconShieldCheck size={22} />
                            </div>
                            <div>
                                <h2 className="h5 fw-bold mb-1 text-dark">Profile Usage Preview</h2>
                                <p className="small text-muted mb-0">
                                    Changes will be reflected across contract templates, approvals, and notifications after saving.
                                </p>
                            </div>
                        </Stack>

                        <Row className="g-3">
                            {/* Contracts */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-center gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconFileDescription size={22} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Contracts</h3>
                                        <p className="small text-muted mb-0">Profile details appear on owned contracts.</p>
                                    </div>
                                </div>
                            </Col>

                            {/* Approvals */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-center gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconShieldCheck size={22} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Approvals</h3>
                                        <p className="small text-muted mb-0">Name and position show in approval flows.</p>
                                    </div>
                                </div>
                            </Col>

                            {/* Department */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-center gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconBuilding size={22} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Department</h3>
                                        <p className="small text-muted mb-0">Department controls workflow routing.</p>
                                    </div>
                                </div>
                            </Col>

                            {/* Notifications */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-center gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconClock size={22} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Notifications</h3>
                                        <p className="small text-muted mb-0">Email and phone receive system alerts.</p>
                                    </div>
                                </div>
                            </Col>
                        </Row>
                    </Card>

                    {/* --- INFO ALERT --- */}
                    <Alert variant="info" className="mx-4 mb-4 d-flex align-items-center gap-3 py-3">
                        <IconInfoCircle size={22} className="text-primary" />
                        <span>
                            Review all changes carefully before saving. Updated profile data will apply to future documents.
                        </span>
                    </Alert>
                </Form>
            </Container>
        </div>
    );
}

export default UpdateProfile;