import React, { useState, useEffect } from "react";
// THÊM useParams để lấy ID từ URL
import {useLocation, useNavigate, useParams} from "react-router-dom";
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

// IMPORT HÀM GỌI API
import {getAllDepartments, getUserById, updateUser} from "../../services/userService/userApi.js";
import departmentApi from "../../services/departmentService/departmentApi";

function UpdateUser({ onUpdateUser }) {
    const navigate = useNavigate();
    const { id } = useParams();
    const location = useLocation();

    // Thông tin phân quyền
    const currentUserRole = localStorage.getItem("role") || "";
    const currentUserDept = localStorage.getItem("departmentName") || "";

    const searchParams = new URLSearchParams(location.search);
    const viewType = searchParams.get("type") || "employee";

    // Tính toán Role có sẵn giống hệt bên Create
    const availableRoles = (currentUserRole === 'CEO' || currentUserRole === 'Admin')
        ? (viewType === 'customer' ? ['Customer'] : ['Manager', 'Employee'])
        : ['Employee'];

    const [departmentsDB, setDepartmentsDB] = useState([]);
    const [isLoadingData, setIsLoadingData] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [user, setUser] = useState({
        firstName: "",
        lastName: "",
        phoneNumber: "",
        email: "",
        role: "", // Sẽ được API ghi đè
        status: "ACTIVE",
        employeeId: "EMP-00987",
        department: "",
        startDate: "2023-06-15",
        position: "Senior Legal Counsel",
        accessScope: "Department Level Access",
        sendUpdateEmail: true,
    });

    useEffect(() => {
        const fetchDepts = async () => {
            try {
                // Gọi qua departmentApi
                const res = await departmentApi.getAllDepartments();
                setDepartmentsDB(res.data?.data || res.data || []);
            } catch (error) {
                console.error("Lỗi lấy danh sách department:", error);
            }
        };
        fetchDepts();

        // Lấy data user hiện tại
        const fetchUser = async () => {
            try {
                const response = await getUserById(id);
                const data = response.data.data;

                setUser(prev => ({
                    ...prev,
                    firstName: data.firstName || "",
                    lastName: data.lastName || "",
                    email: data.email || "",
                    phoneNumber: data.numberPhone || "",
                    role: data.role || availableRoles[0], // Lấy role từ DB, nếu không có thì lấy mặc định
                    status: data.status || "ACTIVE",
                    // Nhớ map thêm department từ BE trả về nếu có
                    department: data.departmentName || (currentUserRole === 'Manager' ? currentUserDept : ""),
                }));
            } catch (error) {
                console.error("Lỗi khi tải dữ liệu user:", error);
                alert("Không thể tải thông tin người dùng!");
                navigate(`/user-management/list?type=${viewType}`);
            } finally {
                setIsLoadingData(false);
            }
        };

        if (id) {
            fetchUser();
        }
    }, [id, navigate, currentUserRole, currentUserDept, viewType]);

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
            const payload = {
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                numberPhone: user.phoneNumber,
                role: user.role,
                status: user.status,
                departmentName: user.department,
                sendWelcomeEmail: user.sendUpdateEmail
            };

            await updateUser(id, payload);
            if (onUpdateUser) onUpdateUser(user);

            alert("Cập nhật thông tin người dùng thành công!");
            navigate(`/user-management/list?type=${viewType}`);

        } catch (error) {
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra: " + (error.response?.data?.message || "Vui lòng thử lại!"));
        } finally {
            setIsSubmitting(false);
        }
    };

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
                                onClick={() => navigate(`/user-management/list?type=${viewType}`)} // Thêm ?type=${viewType} vào
                                disabled={isSubmitting || isLoadingData}
                            >
                                Cancel
                            </Button>
                            <Button
                                type="submit"
                                variant="primary"
                                className="fw-bold px-4 d-flex align-items-center gap-2"
                                disabled={isSubmitting || isLoadingData}
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

                    {/* Hiển thị vòng load nếu đang tải API */}
                    {isLoadingData ? (
                        <div className="text-center py-5 my-5">
                            <Spinner animation="border" variant="primary" />
                            <p className="text-muted mt-3">Đang tải thông tin người dùng...</p>
                        </div>
                    ) : (
                        <>
                            {/* --- FORM SECTION --- */}
                            <Card className="m-4 border rounded-3 p-4">
                                <h2 className="h5 fw-bold mb-4 text-dark">User Information</h2>

                                <Row className="g-4">
                                    {/* --- TÁCH THÀNH FIRST NAME VÀ LAST NAME --- */}
                                    {/* First Name */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">First Name</Form.Label>
                                            <div className="position-relative">
                                                <IconUser className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                                <Form.Control id="firstName" name="firstName" type="text" required value={user.firstName} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* Last Name */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Last Name</Form.Label>
                                            <div className="position-relative">
                                                <IconUser className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                                <Form.Control id="lastName" name="lastName" type="text" required value={user.lastName} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
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
                                                <Form.Control id="email" name="email" type="email" required value={user.email} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
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

                                    {/* DROPDOWN DEPARTMENT */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Department</Form.Label>
                                            <div className="position-relative">
                                                <IconBuilding className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} style={{ zIndex: 5 }} />
                                                <Form.Select name="department" value={user.department} onChange={handleChange}
                                                             disabled={isSubmitting || currentUserRole === 'Manager'} className="ps-5 py-2">
                                                    <option value="">Select department</option>
                                                    {departmentsDB.map(d => (
                                                        <option key={d.departmentName} value={d.departmentName}>{d.departmentName}</option>
                                                    ))}
                                                </Form.Select>
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* Start Date */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Start Date</Form.Label>
                                            <div className="position-relative">
                                                <IconCalendar
                                                    className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted"
                                                    size={18}
                                                    style={{ zIndex: 5 }}
                                                />
                                                <Form.Control
                                                    type="date"
                                                    name="startDate"
                                                    value={user.startDate}
                                                    onChange={handleChange}
                                                    required
                                                    disabled={isSubmitting}
                                                    className="ps-5 py-2"
                                                />
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* DROPDOWN ROLE */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Role</Form.Label>
                                            <div className="position-relative">
                                                <IconShieldCheck className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} style={{ zIndex: 5 }} />
                                                <Form.Select name="role" value={user.role} onChange={handleChange}
                                                             disabled={isSubmitting || availableRoles.length === 1} className="ps-5 py-2">
                                                    <option value="">Select role</option>
                                                    {availableRoles.map(r => (
                                                        <option key={r} value={r}>{r}</option>
                                                    ))}
                                                </Form.Select>
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* Status */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Status</Form.Label>
                                            <Form.Select id="status" name="status" value={user.status} onChange={handleChange} disabled={isSubmitting} className="py-2">
                                                <option value="ACTIVE">Active</option>
                                                <option value="INACTIVE">Inactive</option>
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

                                {/* --- SEND NOTIFICATION EMAIL TOGGLE --- */}
                                <Stack direction="horizontal" gap={3} className="mt-4 pt-4 border-top align-items-center">
                                    <Form.Check
                                        type="switch"
                                        id="sendUpdateEmail"
                                        name="sendUpdateEmail"
                                        checked={user.sendUpdateEmail}
                                        onChange={handleChange}
                                        disabled={isSubmitting}
                                        style={{ transform: "scale(1.3)" }}
                                    />
                                    <div>
                                        <h3 className="h6 fw-bold mb-1 text-dark">Send Notification Email</h3>
                                        <p className="small text-muted mb-0">
                                            Send an email to the user notifying them of these account changes.
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
                                                <p className="small text-muted mb-2">{user.role}</p>
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
                                                <p className="small text-muted mb-2">{user.department}</p>
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
                        </>
                    )}
                </Form>
            </Container>
        </div>
    );
}

export default UpdateUser;