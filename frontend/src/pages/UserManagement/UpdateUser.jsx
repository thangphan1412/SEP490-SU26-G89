import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Form, Button, NavDropdown, Spinner, Stack, Alert } from "react-bootstrap";
import {
    IconWorld,
    IconDeviceFloppy,
    IconUser,
    IconPhone,
    IconMail,
    IconId,
    IconBuilding,
    IconCalendar,
    IconShieldCheck,
    IconBriefcase,
    IconLock,
    IconUserShield,
    IconClipboardList,
    IconGitMerge,
    IconInfoCircle
} from "@tabler/icons-react";

// Dữ liệu User mẫu ban đầu (khi chưa có API)
const initialUser = {
    fullName: "Emma Nguyen",
    phoneNumber: "(555) 123-4567",
    email: "emma.nguyen@company.com",
    employeeId: "EMP-00987",
    department: "Legal",
    startDate: "Jun 15, 2023",
    role: "Legal Reviewer",
    status: "Active",
    position: "Senior Legal Counsel",
    accessScope: "Department Level Access",
    deactivateAccount: false,
};

function UpdateUser({ onUpdateUser }) {
    const navigate = useNavigate();
    const [user, setUser] = useState(initialUser);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (event) => {
        const { name, value, checked, type } = event.target;
        setUser((currentUser) => ({
            ...currentUser,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setIsSubmitting(true);

        try {
            // Giả lập thời gian gửi API lên Backend mất 1.5 giây
            await new Promise((resolve) => setTimeout(resolve, 1500));

            onUpdateUser?.(user);

            alert("Cập nhật thông tin người dùng thành công!");
            navigate("/user-management/list");

        } catch (error) {
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra trong quá trình lưu dữ liệu, vui lòng thử lại!");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="bg-light min-vh-screen">
            {/* --- HEADER --- */}
            <header className="d-flex justify-content-between align-items-center px-4 py-3 bg-white border-bottom mb-4">
                <div className="d-flex align-items-center gap-2">
                    <span className="fs-4">🛡️</span>
                    <div className="d-flex flex-column lh-sm">
                        <strong className="text-dark">E-CONTRACT</strong>
                        <small className="text-muted" style={{ fontSize: "12px" }}>Management System</small>
                    </div>
                </div>

                <NavDropdown
                    title={
                        <span className="text-dark fw-semibold">
                            <IconWorld size={20} className="me-1" />
                            English
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

                    {/* Header Panel */}
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 fw-bold mb-1">Update User</h1>
                            <p className="text-muted mb-0">
                                Edit employee information, change role, department, or deactivate access.
                            </p>
                        </div>
                        <div className="d-flex gap-2">
                            <Button
                                variant="outline-secondary"
                                className="fw-bold px-3"
                                onClick={() => navigate("/user-management/list")}
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
                                        <IconDeviceFloppy size={19} color="#fff" />
                                        <span>Save Changes</span>
                                    </>
                                )}
                            </Button>
                        </div>
                    </div>

                    {/* --- FORM SECTION --- */}
                    <Card className="m-4 border rounded-3 p-4">
                        <h2 className="h5 fw-bold mb-4 text-dark">User Information</h2>

                        <Row className="g-4">
                            {/* Full Name */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Full Name</Form.Label>
                                    <div className="position-relative">
                                        <IconUser className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                        <Form.Control id="fullName" name="fullName" type="text" value={user.fullName} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                    </div>
                                </Form.Group>
                            </Col>

                            {/* Phone Number */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Phone Number</Form.Label>
                                    <div className="position-relative">
                                        <IconPhone className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                        <Form.Control id="phoneNumber" name="phoneNumber" type="text" value={user.phoneNumber} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                    </div>
                                </Form.Group>
                            </Col>

                            {/* Email Address */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Email Address</Form.Label>
                                    <div className="position-relative">
                                        <IconMail className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                        <Form.Control id="email" name="email" type="email" value={user.email} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                    </div>
                                </Form.Group>
                            </Col>

                            {/* Employee ID */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Employee ID</Form.Label>
                                    <div className="position-relative">
                                        <IconId className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                        <Form.Control id="employeeId" name="employeeId" type="text" value={user.employeeId} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                    </div>
                                </Form.Group>
                            </Col>

                            {/* Department */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Department</Form.Label>
                                    <div className="position-relative">
                                        <IconBuilding className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} style={{ zIndex: 5 }} />
                                        <Form.Select id="department" name="department" value={user.department} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2">
                                            <option value="Legal">Legal</option>
                                            <option value="HR">HR</option>
                                            <option value="Finance">Finance</option>
                                            <option value="Sales">Sales</option>
                                        </Form.Select>
                                    </div>
                                </Form.Group>
                            </Col>

                            {/* Start Date */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Start Date</Form.Label>
                                    <div className="position-relative">
                                        <IconCalendar className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                        <Form.Control id="startDate" name="startDate" type="text" value={user.startDate} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                    </div>
                                </Form.Group>
                            </Col>

                            {/* Role */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Role</Form.Label>
                                    <div className="position-relative">
                                        <IconShieldCheck className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} style={{ zIndex: 5 }} />
                                        <Form.Select id="role" name="role" value={user.role} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2">
                                            <option value="Legal Reviewer">Legal Reviewer</option>
                                            <option value="Contract Manager">Contract Manager</option>
                                            <option value="Approver">Approver</option>
                                            <option value="Viewer">Viewer</option>
                                        </Form.Select>
                                    </div>
                                </Form.Group>
                            </Col>

                            {/* Status */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Status</Form.Label>
                                    <Form.Select id="status" name="status" value={user.status} onChange={handleChange} disabled={isSubmitting} className="py-2">
                                        <option value="Active">Active</option>
                                        <option value="Inactive">Inactive</option>
                                        <option value="Deactivated">Deactivated</option>
                                    </Form.Select>
                                </Form.Group>
                            </Col>

                            {/* Position */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Position</Form.Label>
                                    <div className="position-relative">
                                        <IconBriefcase className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                        <Form.Control id="position" name="position" type="text" value={user.position} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                    </div>
                                </Form.Group>
                            </Col>

                            {/* Access Scope */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold text-secondary">Access Scope</Form.Label>
                                    <div className="position-relative">
                                        <IconLock className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} style={{ zIndex: 5 }} />
                                        <Form.Select id="accessScope" name="accessScope" value={user.accessScope} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2">
                                            <option value="Department Level Access">Department Level Access</option>
                                            <option value="Full Access">Full Access</option>
                                            <option value="Limited Access">Limited Access</option>
                                        </Form.Select>
                                    </div>
                                </Form.Group>
                            </Col>
                        </Row>

                        {/* --- DEACTIVATE ACCOUNT TOGGLE --- */}
                        <Stack direction="horizontal" gap={3} className="mt-4 pt-4 border-top align-items-center">
                            <Form.Check
                                type="switch"
                                id="deactivateAccount"
                                name="deactivateAccount"
                                checked={user.deactivateAccount}
                                onChange={handleChange}
                                disabled={isSubmitting}
                                style={{ transform: "scale(1.3)" }}
                            />
                            <div>
                                <h3 className="h6 fw-bold mb-1 text-dark">Deactivate Account</h3>
                                <p className="small text-muted mb-0">
                                    Revoke this user's access to the system. This action can be reversed later if needed.
                                </p>
                            </div>
                        </Stack>
                    </Card>

                    {/* --- ACCESS PREVIEW SECTION --- */}
                    <Card className="mx-4 mb-4 border rounded-3 p-4">
                        <h2 className="h5 fw-bold mb-4 text-dark">Access Preview</h2>

                        <Row className="g-3">
                            {/* Current Role */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-start gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconUserShield size={24} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Current Role</h3>
                                        <p className="small text-muted mb-2">Legal Reviewer</p>
                                        <span className="badge bg-white text-secondary border px-2 py-1 small">Approval Level: L2</span>
                                    </div>
                                </div>
                            </Col>

                            {/* Department Access */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-start gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconBuilding size={24} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Department Access</h3>
                                        <p className="small text-muted mb-2">Legal Department</p>
                                        <span className="badge bg-white text-secondary border px-2 py-1 small">12 Modules</span>
                                    </div>
                                </div>
                            </Col>

                            {/* Approval Workflow */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-start gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconGitMerge size={24} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Approval Workflow</h3>
                                        <p className="small text-muted mb-2">Can review & approve</p>
                                        <span className="badge bg-white text-secondary border px-2 py-1 small">Level 2 Access</span>
                                    </div>
                                </div>
                            </Col>

                            {/* Audit Trail */}
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-start gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex">
                                        <IconClipboardList size={24} />
                                    </div>
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Audit Trail</h3>
                                        <p className="small text-muted mb-2">All actions are logged</p>
                                        <span className="badge bg-white text-secondary border px-2 py-1 small">Full Visibility</span>
                                    </div>
                                </div>
                            </Col>
                        </Row>

                        <Alert variant="info" className="mt-4 mb-0 d-flex align-items-center gap-2">
                            <IconInfoCircle size={20} />
                            <span>Please review all changes carefully before saving. Updates will take effect immediately after confirmation.</span>
                        </Alert>
                    </Card>
                </Form>
            </Container>
        </div>
    );
}

export default UpdateUser;