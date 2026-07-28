import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Container, Card, Row, Col, Form, Button, Table, Pagination, Stack } from "react-bootstrap";
import { IconWorld, IconPlus, IconSearch, IconFilter, IconRefresh, IconArrowsSort, IconEdit, IconEye } from "@tabler/icons-react";

import { getAllUsers } from "../../services/userService/userApi.js";
// IMPORT THÊM HÀM LẤY DEPARTMENT TỪ API CỦA BẠN
import departmentApi from "../../services/departmentService/departmentApi";

function ListUser() {
    const navigate = useNavigate();
    const location = useLocation();

    // Thông tin user đang đăng nhập
    const currentUserRole = localStorage.getItem("role") || "";
    const currentUserDept = localStorage.getItem("departmentName") || "";

    const searchParams = new URLSearchParams(location.search);
    const viewType = searchParams.get("type") || "employee";

    const [users, setUsers] = useState([]);
    const [departmentsDB, setDepartmentsDB] = useState([]); // Lưu danh sách phòng ban từ DB
    const [loading, setLoading] = useState(true);

    // STATE CHO TÌM KIẾM VÀ FILTER
    const [searchTerm, setSearchTerm] = useState("");
    const [filterRole, setFilterRole] = useState("All");
    const [filterDept, setFilterDept] = useState("All");
    const [filterStatus, setFilterStatus] = useState("All");
    const [pagination, setPagination] = useState({ page: 0, size: 10, totalElements: 0 });

    // XÁC ĐỊNH DANH SÁCH ROLE ĐƯỢC PHÉP HIỂN THỊ TRONG FILTER DỰA VÀO ROLE NGƯỜI ĐĂNG NHẬP
    const availableRoles = (currentUserRole === 'CEO' || currentUserRole === 'Admin')
        ? (viewType === 'customer' ? ['Customer'] : ['Manager', 'Employee'])
        : ['Employee']; // Manager chỉ thấy Employee

    // Lấy danh sách department khi component mount
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
    }, []);

    const fetchData = async (currPage, currentKeyword, currentRole, currentDept, currentStatus) => {
        setLoading(true);
        try {
            // TRUYỀN TOÀN BỘ PARAM XUỐNG API ĐỂ BACKEND LÀM VIỆC
            const response = await getAllUsers(viewType, currentKeyword, currentRole, currentDept, currentStatus);
            const data = response.data?.data || [];

            // FRONTEND GIỜ CHỈ CẦN HIỂN THỊ, KHÔNG CẦN .filter() NỮA CHÚT NÀO HẾT
            setUsers(data);
            setPagination(prev => ({ ...prev, page: currPage, totalElements: data.length }));

        } catch (error) {
            console.error("Lỗi khi tải danh sách:", error);
            if (error.response?.status === 403) alert("Bạn không có quyền xem danh sách này!");
            setUsers([]);
        } finally {
            setLoading(false);
        }
    };

    // Gọi lại API khi Đổi Menu (viewType) hoặc thay đổi Filter
    useEffect(() => {
        // Tự động reset filter khi chuyển view
        setSearchTerm(""); setFilterRole("All"); setFilterStatus("All");
        // Nếu là Manager, tự động ép filterDept thành phòng của họ. Nếu là CEO, để "All"
        setFilterDept(currentUserRole === 'Manager' ? currentUserDept : "All");

        fetchData(0, "", "All", currentUserRole === 'Manager' ? currentUserDept : "All", "All");
    }, [viewType]);

    // Xử lý nút Search & Refresh
    const handleSearch = () => fetchData(0, searchTerm, filterRole, filterDept, filterStatus);
    const handleRefresh = () => {
        setSearchTerm(""); setFilterRole("All"); setFilterStatus("All");
        const defaultDept = currentUserRole === 'Manager' ? currentUserDept : "All";
        setFilterDept(defaultDept);
        fetchData(0, "", "All", defaultDept, "All");
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

            {/* --- MAIN PANEL --- */}
            <Container fluid="lg" className="mb-5">
                <Card className="border shadow-sm rounded-4 overflow-hidden bg-white">
                    <Card.Body className="p-4">

                        {/* Title & Button */}
                        <Stack direction="horizontal" className="justify-content-between align-items-center mb-4">
                            <div>
                                <h1 className="h3 fw-bold mb-1">Users</h1>
                                <p className="text-muted mb-0">Manage employee accounts, roles, departments, and access status.</p>
                            </div>
                            <Button variant="primary" className="fw-bold px-3 py-2 d-flex align-items-center gap-2" onClick={() => navigate(`/user-management/create?type=${viewType}`)}>
                                <IconPlus size={20} /> New User
                            </Button>
                        </Stack>

                        {/* Toolbar / Search Filters */}
                        <Row className="g-3 align-items-end mb-4">
                            <Col lg={4} md={6}>
                                <Form.Group className="position-relative">
                                    <IconSearch className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={20} />
                                    <Form.Control
                                        type="text"
                                        placeholder="Search users by name or email..."
                                        className="ps-5 py-2"
                                        value={searchTerm}
                                        onChange={(e) => setSearchTerm(e.target.value)}
                                        onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                                    />
                                </Form.Group>
                            </Col>

                            {/* FILTER DEPARTMENT */}
                            <Col lg={2} md={6}>
                                <Form.Label className="small fw-bold text-muted mb-1">Department</Form.Label>
                                <Form.Select
                                    className="py-2"
                                    value={filterDept}
                                    onChange={(e) => setFilterDept(e.target.value)}
                                    disabled={currentUserRole === 'Manager'} // Khóa cứng nếu là Manager
                                >
                                    {currentUserRole !== 'Manager' && <option value="All">All</option>}
                                    {currentUserRole === 'Manager' ? (
                                        <option value={currentUserDept}>{currentUserDept}</option>
                                    ) : (
                                        departmentsDB
                                            // Lọc ra các phòng ban có trạng thái là Active (nếu DB có trường này)
                                            .filter(d => d.departmentStatus === 'ACTIVE' || d.departmentStatus === 'Active' || !d.departmentStatus)
                                            .map(d => (
                                                <option key={d.departmentName} value={d.departmentName}>
                                                    {d.departmentName}
                                                </option>
                                            ))
                                    )}
                                </Form.Select>
                            </Col>

                            {/* FILTER ROLE */}
                            <Col lg={2} md={6}>
                                <Form.Label className="small fw-bold text-muted mb-1">Role</Form.Label>
                                <Form.Select
                                    className="py-2"
                                    value={filterRole}
                                    onChange={(e) => setFilterRole(e.target.value)}
                                >
                                    <option value="All">All</option>
                                    {/* Lấy mảng availableRoles đã được tính toán rất chuẩn từ trên map vào */}
                                    {availableRoles.map(r => (
                                        <option key={r} value={r}>{r}</option>
                                    ))}
                                </Form.Select>
                            </Col>

                            {/* FILTER STATUS */}
                            <Col lg={2} md={6}>
                                <Form.Label className="small fw-bold text-muted mb-1">Status</Form.Label>
                                <Form.Select
                                    className="py-2"
                                    value={filterStatus}
                                    onChange={(e) => setFilterStatus(e.target.value)}
                                >
                                    <option value="All">All</option>
                                    <option value="ACTIVE">Active</option>
                                    <option value="INACTIVE">Inactive</option>
                                </Form.Select>
                            </Col>

                            <Col lg={2} md={12} className="d-flex gap-2">
                                <Button variant="outline-secondary" className="w-100 py-2 d-flex align-items-center justify-content-center gap-1" onClick={handleSearch}>
                                    <IconFilter size={18} /> Filters
                                </Button>
                                <Button variant="outline-secondary" className="py-2" onClick={handleRefresh} disabled={loading}>
                                    <IconRefresh size={18} className={loading ? "spin" : ""} />
                                </Button>
                            </Col>
                        </Row>

                        {/* Table Data */}
                        <div className="table-responsive border rounded-3 mb-4">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                <tr>
                                    {["User Name", "Email", "Department", "Role", "Status", "Last Active", "Actions"].map((h) => (
                                        <th key={h} className="text-secondary py-3 px-3 fs-6 fw-bold text-nowrap">
                                                <span className="d-inline-flex align-items-center gap-1">
                                                    {h} {h !== "Actions" && <IconArrowsSort size={14} />}
                                                </span>
                                        </th>
                                    ))}
                                </tr>
                                </thead>
                                <tbody>
                                {loading ? (
                                    <tr><td colSpan="7" className="text-center py-5 text-muted">Đang tải danh sách người dùng...</td></tr>
                                ) : users.length === 0 ? (
                                    <tr><td colSpan="7" className="text-center py-5 text-muted fst-italic">Chưa có dữ liệu người dùng.</td></tr>
                                ) : (
                                    users.map((u) => (
                                        <tr key={u.id}>
                                            {/* Ghép First Name và Last Name */}
                                            <td className="fw-semibold">
                                                <div className="d-flex align-items-center gap-2">
                                                    <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style={{ width: "32px", height: "32px", fontSize: "13px" }}>
                                                        {u.firstName?.charAt(0) || ''}{u.lastName?.charAt(0) || ''}
                                                    </div>
                                                    {u.firstName} {u.lastName}
                                                </div>
                                            </td>

                                            {/* Dữ liệu thật từ DB */}
                                            <td>{u.email}</td>

                                            {/* ĐÃ SỬA: Lấy Department từ DB */}
                                            <td>{u.departmentName || "N/A"}</td>

                                            {/* Dữ liệu thật từ DB */}
                                            <td><span className="badge bg-light text-dark border">{u.role || "N/A"}</span></td>
                                            <td>
                                                <span className={`badge ${u.status === 'Active' || u.status === 'ACTIVE' ? 'bg-success bg-opacity-10 text-success' : 'bg-secondary bg-opacity-10 text-secondary'} px-3 py-2 rounded-pill fw-bold`}>
                                                ● {u.status || "INACTIVE"}
                                                </span>
                                            </td>

                                            {/* Dữ liệu Hardcode */}
                                            <td className="text-muted small">Just now</td>

                                            {/* Actions */}
                                            <td>
                                                <Button variant="link" className="p-0 me-3 text-primary" onClick={() => navigate(`/user-management/view/${u.id}`)} title="View Detail">
                                                    <IconEye size={18} />
                                                </Button>
                                                <Button variant="link" className="p-0 text-warning" onClick={() => navigate(`/user-management/update/${u.id}`)} title="Edit User">
                                                    <IconEdit size={18} />
                                                </Button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </Table>
                        </div>

                        {/* Footer & Pagination */}
                        <Stack direction="horizontal" className="justify-content-between align-items-center text-muted small">
                            <span>Showing {users.length > 0 ? 1 : 0} to {users.length} of {pagination.totalElements} results</span>
                            <div className="d-flex align-items-center gap-2">
                                <Pagination className="mb-0">
                                    <Pagination.Prev disabled />
                                    <Pagination.Item active>1</Pagination.Item>
                                    <Pagination.Next disabled />
                                </Pagination>
                                <Form.Select size="sm" style={{ width: "110px" }}>
                                    <option>10 / page</option>
                                </Form.Select>
                            </div>
                        </Stack>

                    </Card.Body>
                </Card>
            </Container>
        </div>
    );
}

export default ListUser;