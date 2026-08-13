import React, {useEffect, useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import {Container, Card, Row, Col, Form, Button, Alert, NavDropdown, Spinner, Stack} from "react-bootstrap";
import {
    IconWorld,
    IconUserPlus,
    IconShieldCheck,
    IconBuilding,
    IconMail,
    IconUserCheck,
    IconInfoCircle
} from "@tabler/icons-react";
// IMPORT FILE API VÀO ĐÂY
import { createUser } from "../../services/userService/userApi.js";
import departmentApi from "../../services/departmentService/departmentApi";
import roleApi from "../../services/roleService/roleApi";

function CreateUser() {
    const navigate = useNavigate();
    const location = useLocation();

    const currentUserRole = localStorage.getItem("role") || "";
    const currentUserDept = localStorage.getItem("departmentName") || "";

    // Lấy type từ URL (VD: ?type=customer)
    const searchParams = new URLSearchParams(location.search);
    const viewType = searchParams.get("type") || "employee";

    const [departmentsDB, setDepartmentsDB] = useState([]);
    const [rolesDB, setRolesDB] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Tính toán Role có sẵn
    const availableRoles = ['Accountant'].includes(currentUserRole)
        ? ['HeadOfDepartment', 'Employee', 'External Parners']
        : ['Employee']; // Dành cho HeadOfDepartment

    const [user, setUser] = useState({
        firstName: "", lastName: "", email: "", initialPassword: "", confirmPassword: "",
        department: "",
        dob: "",
        role: availableRoles[0] || "", // Mặc định lấy role đầu tiên
        position: "", phoneNumber: "", employeeId: "", startDate: "",
        status: "ACTIVE", sendWelcomeEmail: true
    });

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Gọi API song song cho nhanh
                const [deptRes, roleRes] = await Promise.all([
                    departmentApi.getAllDepartments(),
                    roleApi.getAllRoles()
                ]);

                setDepartmentsDB(deptRes.data?.data || deptRes.data || []);

                // Lọc Role từ DB dựa trên mảng availableRoles
                const allRoles = roleRes.data?.data || roleRes.data || [];
                const filteredRoles = allRoles.filter(r => availableRoles.includes(r.roleName));
                setRolesDB(filteredRoles);

                // Gán mặc định role đầu tiên nếu có
                if (filteredRoles.length > 0) {
                    setUser(prev => ({ ...prev, role: filteredRoles[0].roleName }));
                }

            } catch (error) {
                console.error("Lỗi lấy dữ liệu API:", error);
            }
        };
        fetchData();

        // ÉP BUỘC CHỌN PHÒNG BAN VÀ ROLE NẾU LÀ HeadOfDepartment
        if (currentUserRole === 'HeadOfDepartment') {
            setUser(prev => ({ ...prev, department: currentUserDept, role: 'Employee' }));
        }
    }, [currentUserRole, currentUserDept]);

    const handleChange = (e) => {
        const {name, value, checked, type} = e.target;
        setUser(prev => ({...prev, [name]: type === "checkbox" ? checked : value}));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (user.initialPassword !== user.confirmPassword) {
            alert("Mật khẩu xác nhận không khớp!");
            return;
        }

        setIsSubmitting(true);

        try {
            // Chuẩn bị Payload
            const payload = {
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                password: user.initialPassword,
                numberPhone: user.phoneNumber,
                dob: user.dob,
                startDate: user.startDate,
                role: user.role,
                status: user.status,
                departmentName: user.department,
                // ĐÃ THÊM: Truyền cờ gửi mail xuống BE
                sendWelcomeEmail: user.sendWelcomeEmail
            };

            // Gọi API từ thư mục config/api/userApi.js
            await createUser(payload);

            alert("Tạo tài khoản thành công!");
            navigate("/user-management/list");

        } catch (error) {
            console.error("Lỗi:", error);
            const responseData = error.response?.data;

            // 1. Lấy thông báo chung
            let alertMessage = responseData?.message || "Vui lòng thử lại!";

            // 2. Móc lỗi chi tiết (Nếu Backend có gửi kèm trong biến data)
            if (responseData?.data && typeof responseData.data === 'object') {
                // Lấy tất cả các câu của Backend ghép thành nhiều dòng
                const detailedErrors = Object.values(responseData.data).join('\n- ');
                alertMessage += "\n\nChi tiết lỗi:\n- " + detailedErrors;
            }

            // 3. Hiển thị lên màn hình
            alert("Có lỗi xảy ra: " + alertMessage);
        } finally {
            setIsSubmitting(false);
        }
    };

    // 1. VIẾT HÀM KIỂM TRA QUYỀN TRUY CẬP (AUTHORIZATION CHECK)
    const checkPermission = () => {
        return ['Accountant', 'HeadOfDepartment'].includes(currentUserRole);
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

            <Container fluid="lg" className="mb-5">
                <Form onSubmit={handleSubmit} className="border shadow-sm rounded-4 overflow-hidden bg-white">
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 fw-bold mb-1">Create User</h1>
                            <p className="text-muted mb-0">Add a new user account and assign access permissions.</p>
                        </div>
                        <div className="d-flex gap-2">
                            <Button variant="outline-secondary" className="fw-bold px-3"
                                    onClick={() => navigate(`/user-management/list?type=${viewType}`)}
                                    disabled={isSubmitting}>Cancel</Button>
                            <Button type="submit" variant="primary"
                                    className="fw-bold px-4 d-flex align-items-center gap-2" disabled={isSubmitting}>
                                {isSubmitting ? <Spinner animation="border" size="sm"/> : <IconUserPlus size={19}/>}
                                {isSubmitting ? "Creating..." : "Create User"}
                            </Button>
                        </div>
                    </div>

                    {/* Section 1: User Info */}
                    <Card className="m-4 border rounded-3 p-4">
                        <h2 className="h5 fw-bold mb-4 text-dark">User Information</h2>
                        <Row className="g-4">
                            {/* --- TÁCH THÀNH 2 TRƯỜNG FIRST NAME VÀ LAST NAME --- */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">First Name <span
                                        className="text-danger">*</span></Form.Label>
                                    <Form.Control type="text" name="firstName" value={user.firstName}
                                                  onChange={handleChange} required placeholder="Enter first name"
                                                  disabled={isSubmitting}/>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Last Name <span
                                        className="text-danger">*</span></Form.Label>
                                    <Form.Control type="text" name="lastName" value={user.lastName}
                                                  onChange={handleChange} required placeholder="Enter last name"
                                                  disabled={isSubmitting}/>
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Email Address <span
                                        className="text-danger">*</span></Form.Label>
                                    <Form.Control type="email" name="email" value={user.email} onChange={handleChange}
                                                  required placeholder="Enter email address" disabled={isSubmitting}/>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Phone Number <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="phoneNumber"
                                        value={user.phoneNumber}
                                        onChange={handleChange}
                                        placeholder="Enter phone number"
                                        disabled={isSubmitting}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Initial Password <span
                                        className="text-danger">*</span></Form.Label>
                                    <Form.Control type="password" name="initialPassword" value={user.initialPassword}
                                                  onChange={handleChange} required placeholder="Enter initial password"
                                                  disabled={isSubmitting}/>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Confirm Password <span
                                        className="text-danger">*</span></Form.Label>
                                    <Form.Control type="password" name="confirmPassword" value={user.confirmPassword}
                                                  onChange={handleChange} required
                                                  placeholder="Confirm initial password" disabled={isSubmitting}/>
                                </Form.Group>
                            </Col>

                            {/* DROPDOWN DEPARTMENT */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Department <span className="text-danger">*</span></Form.Label>
                                    <Form.Select name="department" value={user.department} onChange={handleChange} required disabled={isSubmitting || currentUserRole === 'HeadOfDepartment'}>
                                        <option value="">Select department</option>
                                        {departmentsDB.map(d => <option key={d.departmentName} value={d.departmentName}>{d.departmentName}</option>)}
                                    </Form.Select>
                                </Form.Group>
                            </Col>

                            {/* DROPDOWN ROLE */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Role <span className="text-danger">*</span></Form.Label>
                                    <Form.Select name="role" value={user.role} onChange={handleChange} required disabled={isSubmitting || rolesDB.length <= 1}>
                                        <option value="">Select role</option>
                                        {rolesDB.map(r => <option key={r.id} value={r.roleName}>{r.roleName}</option>)}
                                    </Form.Select>
                                </Form.Group>
                            </Col>

                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Date of Birth <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="date"
                                        name="dob"
                                        value={user.dob}
                                        onChange={handleChange}
                                        disabled={isSubmitting}
                                        required
                                    />
                                </Form.Group>
                            </Col>

                            {/* User ID */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">User ID</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value="Auto-generated after creation"
                                        disabled={true}
                                        className="bg-light text-muted fst-italic"
                                    />
                                </Form.Group>
                            </Col>

                            {/* Start Date */}
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Start Date <span
                                        className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control type="date" name="startDate" value={user.startDate}
                                                  onChange={handleChange} required
                                                  disabled={isSubmitting}/>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="small fw-bold">Status <span className="text-danger">*</span></Form.Label>
                                    <Form.Select name="status" value={user.status} onChange={handleChange} required
                                                 disabled={isSubmitting}>
                                        <option value="ACTIVE">Active</option>
                                        <option value="INACTIVE">Inactive</option>
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                        </Row>
                        <Form.Check type="switch" id="sendWelcomeEmail" name="sendWelcomeEmail"
                                    label={<div><strong>Send welcome email</strong><br/><small className="text-muted">Send
                                        an email invitation with login details to the new user</small></div>}
                                    checked={user.sendWelcomeEmail} onChange={handleChange} className="mt-4"
                                    disabled={isSubmitting}/>
                    </Card>

                    {/* Section 2: Onboarding cards */}
                    <Card className="mx-4 mb-4 border rounded-3 p-4">
                        <h2 className="h5 fw-bold mb-4 text-dark">Access & Onboarding</h2>
                        <Row className="g-3">
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-start gap-3 bg-light">
                                    <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle">
                                        <IconShieldCheck size={24}/></div>
                                    <div><h3 className="h6 fw-bold mb-1">Role Permissions</h3><p
                                        className="small text-muted mb-0">User permissions assigned based on role.</p>
                                    </div>
                                </div>
                            </Col>
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-start gap-3 bg-light">
                                    <div className="p-2 bg-info bg-opacity-10 text-info rounded-circle"><IconBuilding
                                        size={24}/></div>
                                    <div><h3 className="h6 fw-bold mb-1">Department Access</h3><p
                                        className="small text-muted mb-0">Access limited to modules within
                                        department.</p></div>
                                </div>
                            </Col>
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-start gap-3 bg-light">
                                    <div className="p-2 bg-success bg-opacity-10 text-success rounded-circle"><IconMail
                                        size={24}/></div>
                                    <div><h3 className="h6 fw-bold mb-1">Email Notification</h3><p
                                        className="small text-muted mb-0">Welcome email sent with login guide.</p></div>
                                </div>
                            </Col>
                            <Col lg={3} md={6}>
                                <div className="p-3 border rounded h-100 d-flex align-items-start gap-3 bg-light">
                                    <div className="p-2 bg-warning bg-opacity-10 text-warning rounded-circle">
                                        <IconUserCheck size={24}/></div>
                                    <div><h3 className="h6 fw-bold mb-1">Account Activation</h3><p
                                        className="small text-muted mb-0">Account activated based on status.</p></div>
                                </div>
                            </Col>
                        </Row>
                        <Alert variant="info" className="mt-4 mb-0 d-flex align-items-center gap-2"><IconInfoCircle
                            size={20}/>Please review information carefully before creating the account.</Alert>
                    </Card>
                </Form>
            </Container>
        </div>
    );
}

export default CreateUser;