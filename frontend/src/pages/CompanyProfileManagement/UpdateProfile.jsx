import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Form, Button, NavDropdown, Spinner, Stack, Alert } from "react-bootstrap";
import {
    IconWorld,
    IconDeviceFloppy,
    IconBuilding,
    IconCheck,
    IconCalendar,
    IconFileDescription,
    IconEdit,
    IconShieldCheck,
    IconSignature,
    IconInfoCircle
} from "@tabler/icons-react";

// Dữ liệu mẫu (sẽ thay bằng API lấy dữ liệu thực tế)
const defaultCompanyProfile = {
    companyName: "ABC Holdings Co., Ltd.",
    email: "legal@abcholdings.vn",
    taxCode: "0312345678",
    phone: "+84 28 3822 5678",
    registeredAddress: "125 Nguyen Hue Boulevard, Ben Nghe Ward, District 1, Ho Chi Minh City, Vietnam",
    businessRegistrationNumber: "BRN-2025-00981",
    legalRepresentative: "Nguyen Minh An",
    registrationDate: "May 12, 2020",
};

function UpdateProfile({ initialProfile = defaultCompanyProfile, onSaveProfile, onCancel }) {
    const navigate = useNavigate();
    const [profile, setProfile] = useState(initialProfile);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setProfile((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleCancel = () => {
        if (onCancel) {
            onCancel();
            return;
        }
        navigate("/company-profile/view");
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setIsSubmitting(true);

        try {
            // Giả lập API call mất 1.5s
            await new Promise((resolve) => setTimeout(resolve, 1500));

            onSaveProfile?.(profile);

            alert("Cập nhật hồ sơ công ty thành công!");
            navigate("/company-profile/view");

        } catch (error) {
            console.error("Lỗi cập nhật:", error);
            alert("Có lỗi xảy ra, vui lòng thử lại!");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="update-profile-page">
            {/* --- HEADER --- */}
            <header className="page-header">
                <div className="logo">
                    <div className="logo-icon">🛡️</div>
                    <div className="logo-text">
                        <strong>E-CONTRACT</strong>
                        <span className="logo-sub-text">Management System</span>
                    </div>
                </div>

                <NavDropdown
                    title={
                        <span className="lang-btn">
                            <IconWorld stroke={2} size={20} />
                            <span className="language-text">English</span>
                        </span>
                    }
                    id="basic-nav-dropdown"
                >
                    <NavDropdown.Item href="#action/3.1">English</NavDropdown.Item>
                    <NavDropdown.Item href="#action/3.2">Vietnamese</NavDropdown.Item>
                </NavDropdown>
            </header>

            {/* --- MAIN CONTENT --- */}
            <Container fluid="lg" className="mb-5">
                <Form onSubmit={handleSubmit} className="border shadow-sm rounded-4 overflow-hidden bg-white">

                    {/* Panel Header */}
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 mb-2 fw-bold text-dark">Update Company Profile</h1>
                            <p className="text-muted mb-0">
                                Edit your company's legal identity information to keep contracts and documents accurate.
                            </p>
                        </div>
                        <div className="form-actions d-flex gap-2">
                            <Button
                                variant="outline-secondary"
                                className="fw-bold px-3"
                                onClick={handleCancel}
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

                    {/* --- COMPANY LEGAL INFORMATION FORM --- */}
                    <Card className="m-4 border rounded-3 p-4">
                        <div className="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
                            <div className="d-flex align-items-center gap-3">
                                <IconBuilding size={32} className="text-primary" />
                                <h2 className="h5 fw-bold mb-0 text-dark">Company Legal Information</h2>
                            </div>
                            <div className="d-inline-flex align-items-center gap-1 bg-success bg-opacity-10 text-success px-3 py-2 rounded fw-bold small border border-success border-opacity-25">
                                <IconCheck size={18} strokeWidth={2} />
                                <span>Verified Data</span>
                            </div>
                        </div>

                        <Row className="g-4">
                            {/* Company Name */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">
                                        Company Name <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control id="companyName" name="companyName" type="text" value={profile.companyName} onChange={handleChange} required disabled={isSubmitting} className="py-2 fw-semibold" />
                                    <Form.Text className="text-muted">The official registered name of your company.</Form.Text>
                                </Form.Group>
                            </Col>

                            {/* Email */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">
                                        Email <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control id="email" name="email" type="email" value={profile.email} onChange={handleChange} required disabled={isSubmitting} className="py-2 fw-semibold" />
                                </Form.Group>
                            </Col>

                            {/* Tax Code */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">
                                        Tax Code (MST) <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control id="taxCode" name="taxCode" type="text" value={profile.taxCode} onChange={handleChange} required disabled={isSubmitting} className="py-2 fw-semibold" />
                                    <Form.Text className="text-muted">Enter your 10-digit Tax Code as issued by the tax authority.</Form.Text>
                                </Form.Group>
                            </Col>

                            {/* Phone */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">
                                        Phone <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control id="phone" name="phone" type="tel" value={profile.phone} onChange={handleChange} required disabled={isSubmitting} className="py-2 fw-semibold" />
                                </Form.Group>
                            </Col>

                            {/* Registered Address */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">
                                        Registered Address <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control id="registeredAddress" name="registeredAddress" as="textarea" value={profile.registeredAddress} onChange={handleChange} rows={2} required disabled={isSubmitting} className="fw-semibold" style={{ resize: "vertical" }} />
                                    <Form.Text className="text-muted">Enter the full registered address as shown on your business license.</Form.Text>
                                </Form.Group>
                            </Col>

                            {/* Business Registration No */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">
                                        Business Registration No. <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control id="businessRegistrationNumber" name="businessRegistrationNumber" type="text" value={profile.businessRegistrationNumber} onChange={handleChange} required disabled={isSubmitting} className="py-2 fw-semibold" />
                                    <Form.Text className="text-muted">Your company's business registration number.</Form.Text>
                                </Form.Group>
                            </Col>

                            {/* Legal Representative */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">
                                        Legal Representative <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control id="legalRepresentative" name="legalRepresentative" type="text" value={profile.legalRepresentative} onChange={handleChange} required disabled={isSubmitting} className="py-2 fw-semibold" />
                                    <Form.Text className="text-muted">Full name of the legal representative.</Form.Text>
                                </Form.Group>
                            </Col>

                            {/* Registration Date */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">
                                        Registration Date <span className="text-danger">*</span>
                                    </Form.Label>
                                    <div className="position-relative">
                                        <Form.Control id="registrationDate" name="registrationDate" type="text" value={profile.registrationDate} onChange={handleChange} required disabled={isSubmitting} className="pe-5 py-2 fw-semibold" />
                                        <IconCalendar className="position-absolute end-0 top-50 translate-middle-y me-3 text-muted" size={18} />
                                    </div>
                                    <Form.Text className="text-muted">Date of business registration.</Form.Text>
                                </Form.Group>
                            </Col>
                        </Row>
                    </Card>

                    {/* --- AUTOMATION PREVIEW --- */}
                    <Card className="mx-4 mb-4 border rounded-3 p-4">
                        <Stack direction="horizontal" gap={3} className="align-items-start mb-4">
                            <IconFileDescription size={32} className="text-primary" />
                            <div>
                                <h2 className="h5 fw-bold mb-1 text-dark">Document Automation Preview</h2>
                                <p className="small text-muted mb-0">
                                    Updated company details will be reflected in contract templates and generated legal documents after saving.
                                </p>
                            </div>
                        </Stack>

                        <Row className="g-3">
                            {/* Contract Templates */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-center gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconFileDescription size={24} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Contract Templates</h3>
                                        <p className="small text-muted mb-0">Auto-fill company details</p>
                                    </div>
                                </div>
                            </Col>

                            {/* Generated Documents */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-center gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconEdit size={24} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Generated Documents</h3>
                                        <p className="small text-muted mb-0">Quotes, agreements, reports</p>
                                    </div>
                                </div>
                            </Col>

                            {/* Compliance */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-center gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconShieldCheck size={24} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Compliance</h3>
                                        <p className="small text-muted mb-0">Accurate legal information</p>
                                    </div>
                                </div>
                            </Col>

                            {/* Digital Signatures */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-center gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconSignature size={24} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Digital Signatures</h3>
                                        <p className="small text-muted mb-0">Verified company identity</p>
                                    </div>
                                </div>
                            </Col>
                        </Row>
                    </Card>

                    {/* --- INFO ALERT --- */}
                    <Alert variant="info" className="mx-4 mb-4 d-flex align-items-start gap-3 py-3">
                        <IconInfoCircle size={20} className="mt-1" />
                        <div>
                            <p className="fw-bold mb-1">Review all information carefully before saving changes.</p>
                            <p className="small mb-0">Unsaved changes will be lost if you leave this page.</p>
                        </div>
                    </Alert>

                </Form>
            </Container>
        </div>
    );
}

export default UpdateProfile;