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
import { getUserById, updateUser } from "../../services/userService/userApi.js";
import departmentApi from "../../services/departmentService/departmentApi";
import roleApi from "../../services/roleService/roleApi";

function UpdateUser({ onUpdateUser }) {
    const navigate = useNavigate();
    const { id } = useParams();
    const location = useLocation();

    // Thông tin phân quyền
    const currentUserRole = localStorage.getItem("role") || "";
    const currentUserDept = localStorage.getItem("departmentName") || "";

    const searchParams = new URLSearchParams(location.search);
    const viewType = searchParams.get("type") || "employee";

    // Tính toán Role có sẵn
    const availableRoles = (currentUserRole === 'CEO' || currentUserRole === 'Administrator')
        ? (viewType === 'customer' ? ['External Parners'] : ['HeadOfDepartment', 'Employee', 'Accountant'])
        : ['Employee'];

    const [departmentsDB, setDepartmentsDB] = useState([]);
    const [rolesDB, setRolesDB] = useState([]);
    const [isLoadingData, setIsLoadingData] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [user, setUser] = useState({
        firstName: "",
        lastName: "",
        phoneNumber: "",
        email: "",
        role: "",
        status: "ACTIVE",
        employeeId: "",
        department: "",
        startDate: "",
        dob: "",
        sendUpdateEmail: true,
    });

    useEffect(() => {
        const fetchDepts = async () => {
            try {
                const [deptRes, roleRes] = await Promise.all([
                    departmentApi.getAllDepartments(),
                    roleApi.getAllRoles()
                ]);
                setDepartmentsDB(deptRes.data?.data || deptRes.data || []);
                const allRoles = roleRes.data?.data || roleRes.data || [];
                setRolesDB(allRoles.filter(r => availableRoles.includes(r.roleName)));
            } catch (error) {
                console.error("Lỗi lấy dữ liệu API:", error);
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
                    department: data.departmentName || (currentUserRole === 'HeadOfDepartment' ? currentUserDept : ""),
                    dob: data.dob || "",
                    employeeId: data.employeeId || "N/A",
                    startDate: data.startDate || "",
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
                sendWelcomeEmail: user.sendUpdateEmail,
                dob: user.dob,
                startDate: user.startDate
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

    // 1. VIẾT HÀM KIỂM TRA QUYỀN TRUY CẬP (AUTHORIZATION CHECK)
    const checkPermission = () => {
        // Nếu vào tab Customer Management -> Chỉ CEO và Administrator được vào
        if (viewType === "customer") {
            return ['CEO', 'Administrator'].includes(currentUserRole);
        }
        // Nếu vào tab Employee Management -> Thêm HeadOfDepartment được vào
        if (viewType === "employee") {
            return ['CEO', 'Administrator', 'HeadOfDepartment'].includes(currentUserRole);
        }
        return false;
    };

    // 2. NẾU KHÔNG CÓ QUYỀN -> HIỂN THỊ MÀN HÌNH BÁO LỖI LUÔN, KHÔNG RENDER UI BÊN DƯỚI
    if (!checkPermission()) {
        return (
            <div className="bg-light min-vh-screen d-flex align-items-center justify-content-center">
                <Card className="p-5 text-center shadow border-0 rounded-4" style={{ maxWidth: "500px" }}>
                    <div className="text-danger mb-3">
                        {/* Bạn có thể dùng icon Tabler ở đây */}
                        <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 12m-9 0a9 9 0 1 0 18 0a9 9 0 1 0 -18 0"></path><path d="M9 12l2 2l4 -4"></path></svg>
                    </div>
                    <h2 className="fw-bold mb-3 text-dark">Access Denied</h2>
                    <h5 className="text-secondary mb-4">Bạn không có quyền truy cập vào chức năng này!</h5>
                    <Button variant="primary" className="fw-bold px-4" onClick={() => navigate("/home_page")}>
                        Quay lại Trang chủ
                    </Button>
                </Card>
            </div>
        );
    }

    return (
        <div className="bg-light min-vh-screen">

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
                                            <Form.Label className="small fw-bold text-secondary">First Name <span className="text-danger">*</span></Form.Label>
                                            <div className="position-relative">
                                                <IconUser className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                                <Form.Control id="firstName" name="firstName" type="text" required value={user.firstName} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* Last Name */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Last Name <span className="text-danger">*</span></Form.Label>
                                            <div className="position-relative">
                                                <IconUser className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                                <Form.Control id="lastName" name="lastName" type="text" required value={user.lastName} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* Phone Number */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Phone Number <span className="text-danger">*</span></Form.Label>
                                            <div className="position-relative">
                                                <IconPhone className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                                <Form.Control
                                                    id="phoneNumber"
                                                    name="phoneNumber"
                                                    type="tel"
                                                    value={user.phoneNumber}
                                                    onChange={handleChange}
                                                    disabled={isSubmitting}
                                                    className="ps-5 py-2"
                                                    required
                                                />
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* Email Address */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Email Address <span className="text-danger">*</span></Form.Label>
                                            <div className="position-relative">
                                                <IconMail className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                                <Form.Control id="email" name="email" type="email" required value={user.email} onChange={handleChange} disabled={isSubmitting} className="ps-5 py-2" />
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* User ID - KHÔNG CÓ DẤU SAO ĐỎ */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">User ID</Form.Label>
                                            <div className="position-relative">
                                                <IconId className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                                <Form.Control name="employeeId" type="text" value={user.employeeId} disabled={true} className="ps-5 py-2 bg-light text-muted" />
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* DROPDOWN DEPARTMENT */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Department <span className="text-danger">*</span></Form.Label>
                                            <div className="position-relative">
                                                <IconBuilding className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} style={{ zIndex: 5 }} />
                                                <Form.Select name="department" value={user.department} onChange={handleChange} required disabled={isSubmitting || currentUserRole === 'HeadOfDepartment'} className="ps-5 py-2">
                                                    <option value="">Select department</option>
                                                    {departmentsDB.map(d => <option key={d.departmentName} value={d.departmentName}>{d.departmentName}</option>)}
                                                </Form.Select>
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* Start Date */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Start Date <span className="text-danger">*</span></Form.Label>
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
                                            <Form.Label className="small fw-bold text-secondary">Role <span className="text-danger">*</span></Form.Label>
                                            <div className="position-relative">
                                                <IconShieldCheck className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} style={{ zIndex: 5 }} />
                                                <Form.Select name="role" value={user.role} onChange={handleChange} required disabled={isSubmitting || rolesDB.length <= 1} className="ps-5 py-2">
                                                    <option value="">Select role</option>
                                                    {rolesDB.map(r => <option key={r.id} value={r.roleName}>{r.roleName}</option>)}
                                                </Form.Select>
                                            </div>
                                        </Form.Group>
                                    </Col>

                                    {/* Status */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Status <span className="text-danger">*</span></Form.Label>
                                            <Form.Select id="status" name="status" value={user.status} onChange={handleChange} required disabled={isSubmitting} className="py-2">
                                                <option value="ACTIVE">Active</option>
                                                <option value="INACTIVE">Inactive</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>

                                    {/* Date of Birth */}
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="small fw-bold text-secondary">Date of Birth <span className="text-danger">*</span></Form.Label>
                                            <div className="position-relative">
                                                <IconCalendar className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={18} />
                                                <Form.Control
                                                    type="date"
                                                    name="dob"
                                                    value={user.dob}
                                                    onChange={handleChange}
                                                    disabled={isSubmitting}
                                                    className="ps-5 py-2"
                                                    required
                                                />
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