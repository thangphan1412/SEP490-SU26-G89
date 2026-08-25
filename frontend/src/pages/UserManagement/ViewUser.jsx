import React, { useState, useEffect } from "react";
// THÊM useLocation ĐỂ LẤY THAM SỐ TYPE TỪ URL
import { useNavigate, useParams, useLocation } from "react-router-dom";
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
    IconInfoCircle,
    IconArrowLeft // Thêm icon nút Back (tùy chọn)
} from "@tabler/icons-react";

// IMPORT HÀM GỌI API
import { getUserById } from "../../services/userService/userApi.js";

// --- HÀM TÍNH TOÁN THỜI GIAN "TIME AGO" (ĐÃ FIX LỆCH MÚI GIỜ UTC+7) ---
const timeAgo = (dateString) => {
    if (!dateString) return "Never logged in";

    // MẤU CHỐT LÀ ĐÂY: Thêm chữ 'Z' vào cuối chuỗi để ép JS hiểu đây là giờ UTC (Múi giờ +0).
    // Sau đó trình duyệt ở VN sẽ tự động cộng thêm 7 tiếng để tính toán cho chuẩn.
    const utcDateString = dateString.endsWith('Z') ? dateString : dateString + 'Z';
    const date = new Date(utcDateString);

    if (isNaN(date.getTime())) return "Never logged in";

    const now = new Date();
    const seconds = Math.floor((now - date) / 1000);

    // Xử lý trường hợp đồng hồ máy tính của bạn bị lệch chậm hơn server vài giây sinh ra số âm
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
// --------------------------------------------------------

function ViewUser() {
    const navigate = useNavigate();
    const { id } = useParams(); // Lấy ID người dùng từ đường dẫn URL
    const location = useLocation();

    const currentUserRole = localStorage.getItem("role") || "";

    // Đọc URL xem đang ở luồng Employee hay Customer để giữ flow
    const searchParams = new URLSearchParams(location.search);
    const viewType = searchParams.get("type") || "employee";

    // State quản lý dữ liệu và trạng thái loading
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Dùng useEffect để lấy thông tin User từ Backend
    useEffect(() => {
        const fetchUserDetails = async () => {
            setLoading(true);
            try {
                // Gọi API lấy chi tiết user
                const response = await getUserById(id);
                const data = response.data.data;
                // Gán dữ liệu thật từ BE và Hardcode các trường chưa làm ở DB
                setUser({
                    firstName: data.firstName || "",
                    lastName: data.lastName || "",
                    email: data.email || "",
                    phoneNumber: data.numberPhone || "",
                    role: data.role || "N/A",
                    status: data.status || "Inactive",
                    department: data.departmentName || "N/A",
                    dob: data.dob || "N/A",
                    employeeId: data.employeeId || "N/A",
                    lastActive: data.lastActive || "Chưa đăng nhập",
                    dateJoined: data.startDate || "N/A",
                    companyName: data.companyName || "N/A",
                    companyEmail: data.companyEmail || "N/A",
                    registeredAddress: data.registeredAddress || "N/A",
                });
            } catch (error) {
                console.error("Lỗi khi lấy dữ liệu người dùng:", error);
                alert("Không thể tải thông tin người dùng!");
                // Giữ nguyên viewType khi có lỗi trả về trang list
                navigate(`/user-management/list?type=${viewType}`);
            } finally {
                setLoading(false);
            }
        };
        if (id) {
            fetchUserDetails();
        }
    }, [id, navigate, viewType]);

    // Xác định ai có quyền bấm nút Edit
    const canCreateAndUpdate = ['Accountant', 'HeadOfDepartment'].includes(currentUserRole);

    // 1. VIẾT HÀM KIỂM TRA QUYỀN TRUY CẬP (AUTHORIZATION CHECK)
    const checkPermission = () => {
        // Cả 4 Role đều được phép vào màn hình View
        return ['CEO', 'Administrator', 'Accountant', 'HeadOfDepartment'].includes(currentUserRole);
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

            {/* --- MAIN CONTENT PANEL --- */}
            <Container fluid="lg" className="mb-5">
                <section className="border shadow-sm rounded-4 overflow-hidden bg-white mt-4">

                    {/* Header Panel */}
                    <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white">
                        <div>
                            <h1 className="h3 fw-bold mb-1">User Details</h1>
                            <p className="text-muted mb-0">Review user information, role permissions, and department assignment.</p>
                        </div>
                        <div className="d-flex gap-2">
                            {/* Nút Back */}
                            <Button
                                variant="outline-secondary"
                                className="fw-bold px-3 d-flex align-items-center gap-2"
                                onClick={() => navigate(`/user-management/list`)} // Đã xóa chữ ?type=...
                            >
                                <IconArrowLeft size={19} /> Back
                            </Button>

                            {/* CHỈ ACCOUNTANT VÀ HEAD CÓ QUYỀN MỚI THẤY NÚT NÀY */}
                            {canCreateAndUpdate && (
                                <Button
                                    variant="primary"
                                    className="fw-bold px-4 d-flex align-items-center gap-2"
                                    onClick={() => navigate(`/user-management/update/${id}`)}
                                    disabled={loading}
                                >
                                    <IconEdit size={19} color="#ffffff" /> Edit User
                                </Button>
                            )}
                        </div>
                    </div>

                    {/* Kiểm tra trạng thái loading */}
                    {loading ? (
                        <div className="text-center py-5 my-5 text-secondary">
                            <Spinner animation="border" className="mb-3" style={{ width: "3rem", height: "3rem" }} />
                            <p>Loading user details...</p>
                        </div>
                    ) : user ? (
                        <>
                            {/* --- User INFORMATION CARD --- */}
                            <Card className="m-4 border rounded-3 p-4">
                                <h2 className="h5 fw-bold mb-4 text-dark">User Information</h2>

                                <Row className="g-4">
                                    {/* Cột 1: Khối Avatar */}
                                    <Col md={3} className="text-center border-end d-flex flex-column align-items-center justify-content-center">
                                        <div className="position-relative mb-3">
                                            <div
                                                className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm"
                                                style={{ width: "100px", height: "100px", fontSize: "36px", boxShadow: "0 0 0 4px #edf2ff" }}
                                            >
                                                {/* Lấy chữ cái đầu của First Name và Last Name */}
                                                {user.firstName?.charAt(0)}{user.lastName?.charAt(0)}
                                            </div>
                                        </div>
                                        {/* Ghép First Name và Last Name */}
                                        <h3 className="h6 fw-bold mb-2 text-dark">{user.firstName} {user.lastName}</h3>
                                        <span className={`badge ${user.status === 'Active' || user.status === 'ACTIVE' ? 'bg-success bg-opacity-10 text-success' : 'bg-secondary bg-opacity-10 text-secondary'} px-3 py-2 rounded-pill fw-bold`}>
                                            ● {user.status}
                                        </span>
                                    </Col>

                                    {/* Cột 2: Thông tin chi tiết bên trái */}
                                    <Col md={4} className="border-end px-3">
                                        <Stack gap={3} className="small">
                                            {/* Full Name */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconUser size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Full Name</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.firstName} {user.lastName}</Col>
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
                                                    <Col xs={6} className="text-dark fw-semibold">{user.phoneNumber || 'N/A'}</Col>
                                                </Row>
                                            </div>
                                            {/* DOB */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconCalendar size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Date of Birth</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.dob}</Col>
                                                </Row>
                                            </div>
                                        </Stack>
                                    </Col>

                                    {/* Cột 3: Thông tin chi tiết bên phải */}
                                    <Col md={5} className="px-3">
                                        <Stack gap={3} className="small">
                                            {/* User ID */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconId size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">User ID</Col>
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
                                            {/* Role */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconShieldCheck size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Role</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{user.role}</Col>
                                                </Row>
                                            </div>
                                            {/* Date Joined */}
                                            <div className="border-bottom border-light pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconCalendar size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Date Joined</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">
                                                        {user.dateJoined !== "N/A"
                                                            ? new Date(user.dateJoined).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
                                                            : "N/A"}
                                                    </Col>
                                                </Row>
                                            </div>
                                            {/* Last Login */}
                                            <div className="pb-2">
                                                <Row className="align-items-center g-0">
                                                    <Col xs={1} className="d-flex text-secondary"><IconClock size={18} /></Col>
                                                    <Col xs={4} className="text-muted fw-medium ps-2">Last Login</Col>
                                                    <Col xs={1} className="text-muted text-center">:</Col>
                                                    <Col xs={6} className="text-dark fw-semibold">{timeAgo(user.lastActive)}</Col>
                                                </Row>
                                            </div>
                                        </Stack>
                                    </Col>
                                </Row>
                            </Card>

                            {/* --- MÀN HÌNH VIEW USER --- */}
                            <Card className="mx-4 mb-4 border rounded-3 p-4">
                                <h2 className="h5 fw-bold mb-4 text-dark">
                                    {user.role === 'External Parners' ? 'Partner Company Information' : 'Internal Company Information'}
                                </h2>
                                <Row className="g-3 small">
                                    <Col md={6} className="d-flex py-2 border-bottom"><span className="text-muted w-50">Company Name:</span><strong className="w-50">{user.companyName}</strong></Col>
                                    <Col md={6} className="d-flex py-2 border-bottom"><span className="text-muted w-50">Email:</span><strong className="w-50 text-break">{user.companyEmail}</strong></Col>
                                    <Col md={12} className="d-flex py-2 border-bottom"><span className="text-muted w-25">Registered Address:</span><strong className="w-75">{user.registeredAddress}</strong></Col>
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
                                            <small className="text-muted text-break">Last active on {timeAgo(user.lastActive)}.</small>
                                        </div>
                                    </Col>
                                </Row>
                            </Card>

                            {/* --- ALERT INFO --- */}
                            {canCreateAndUpdate && (
                                <Alert variant="info" className="mx-4 mb-4 d-flex align-items-center gap-2">
                                    <IconInfoCircle size={20} />
                                    <span>
                                        To make changes to this user, click <strong className="text-primary" style={{ cursor: "pointer" }} onClick={() => navigate(`/user-management/update/${id}`)}>Edit User</strong>.
                                    </span>
                                </Alert>
                            )}
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