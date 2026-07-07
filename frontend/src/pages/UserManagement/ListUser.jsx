import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Form, Button, Table, Pagination, NavDropdown, Stack } from "react-bootstrap";
import { IconWorld, IconPlus, IconSearch, IconFilter, IconRefresh, IconArrowsSort, IconDots } from "@tabler/icons-react";

function ListUser() {
    const navigate = useNavigate();

    // 1. STATE & PAGINATION chuẩn thực tế
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");
    const [pagination, setPagination] = useState({
        page: 0,
        size: 10,
        totalElements: 0
    });

    // 2. FETCH DATA FROM API
    const fetchData = (currPage, currKeyword) => {
        setLoading(true);
        setTimeout(() => {
            setUsers([]); // Để mảng rỗng chờ API Spring Boot của bạn
            setPagination(prev => ({ ...prev, page: currPage, totalElements: 0 }));
            setLoading(false);
        }, 500);
    };

    useEffect(() => {
        fetchData(pagination.page, searchTerm);
    }, [pagination.page]);

    const handleSearch = () => {
        setPagination(prev => ({ ...prev, page: 0 }));
        fetchData(0, searchTerm);
    };

    const handleRefresh = () => {
        setSearchTerm("");
        setPagination(prev => ({ ...prev, page: 0 }));
        fetchData(0, "");
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
                            <Button variant="primary" className="fw-bold px-3 py-2 d-flex align-items-center gap-2" onClick={() => navigate("/user-management/create")}>
                                <IconPlus size={20} /> New User
                            </Button>
                        </Stack>

                        {/* Toolbar / Search Filters */}
                        <Row className="g-3 align-items-end mb-4">
                            <Col lg={4} md={6}>
                                <Form.Group className="position-relative">
                                    <IconSearch className="position-absolute start-0 top-50 translate-middle-y ms-3 text-muted" size={20} />
                                    <Form.Control type="text" placeholder="Search users..." className="ps-5 py-2" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
                                </Form.Group>
                            </Col>
                            <Col lg={2} md={6}>
                                <Form.Label className="small fw-bold text-muted mb-1">Department</Form.Label>
                                <Form.Select className="py-2"><option>All</option></Form.Select>
                            </Col>
                            <Col lg={2} md={6}>
                                <Form.Label className="small fw-bold text-muted mb-1">Role</Form.Label>
                                <Form.Select className="py-2"><option>All</option></Form.Select>
                            </Col>
                            <Col lg={2} md={6}>
                                <Form.Label className="small fw-bold text-muted mb-1">Status</Form.Label>
                                <Form.Select className="py-2"><option>All</option></Form.Select>
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
                                        <th key={h} className="text-secondary py-3 px-3 fs-6 fw-bold">
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
                                            {/* Map dữ liệu thật của bạn tại đây */}
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </Table>
                        </div>

                        {/* Footer & Pagination */}
                        <Stack direction="horizontal" className="justify-content-between align-items-center text-muted small">
                            <span>Showing 0 to 0 of 0 results</span>
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