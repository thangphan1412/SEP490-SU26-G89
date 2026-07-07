import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import NavDropdown from "react-bootstrap/NavDropdown";
import {
    IconWorld,
    IconPlus,
    IconSearch,
    IconChevronDown,
    IconFilter,
    IconRefresh,
    IconArrowsSort,
    IconDots,
    IconChevronLeft,
    IconChevronRight
} from "@tabler/icons-react";

// Import CSS
import "../../assets/styles/css/userManagementStyles/UserListPage.css";

// Tạm thời comment API import để sau này bạn dùng
// import { getUsersList } from '../Services/apiUserManagement';

function StatusBadge({ status }) {
    const styleClassByStatus = {
        Active: "badge-active",
        Inactive: "badge-inactive",
        Deactivated: "badge-deactivated",
    };

    return (
        <span className={`status-badge ${styleClassByStatus[status] || ""}`}>
            {status}
        </span>
    );
}

function ListUser() {
    const navigate = useNavigate();

    // 1. KHỞI TẠO CÁC STATE CẦN THIẾT
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");
    const [pagination, setPagination] = useState({
        page: 0,
        size: 10,
        totalElements: 0
    });

    // 2. HÀM FETCH DATA (Chờ kết nối API Spring Boot)
    const fetchData = (currPage, currKeyword) => {
        setLoading(true);

        // TODO: THAY THẾ BẰNG API THỰC TẾ CỦA BẠN
        /*
        getUsersList({ page: currPage, size: pagination.size, keyword: currKeyword })
            .then(response => {
                const data = response.data;
                setUsers(data.content || []);
                setPagination(prev => ({
                    ...prev,
                    page: data.number || 0,
                    size: data.size || 10,
                    totalElements: data.totalElements || 0,
                }));
            })
            .catch(err => console.error("Lỗi khi tải danh sách User:", err))
            .finally(() => setLoading(false));
        */

        // MOCK API (Giả lập gọi API mất 500ms, hiện tại trả về mảng rỗng)
        setTimeout(() => {
            setUsers([]); // Đang chưa có dữ liệu thật
            setPagination(prev => ({
                ...prev,
                page: currPage,
                totalElements: 0 // Giả lập tổng số bản ghi = 0
            }));
            setLoading(false);
        }, 500);
    };

    // 3. EFFECT CHÍNH: Gọi API khi load trang hoặc khi page thay đổi
    useEffect(() => {
        fetchData(pagination.page, searchTerm);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [pagination.page]);

    // 4. XỬ LÝ CÁC SỰ KIỆN
    const handleSearchInputChange = (e) => {
        setSearchTerm(e.target.value);
    };

    const handleSearch = () => {
        setPagination(prev => ({ ...prev, page: 0 })); // Reset về trang đầu
        fetchData(0, searchTerm);
    };

    const handleRefresh = () => {
        setSearchTerm("");
        setPagination(prev => ({ ...prev, page: 0 }));
        fetchData(0, "");
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < Math.ceil(pagination.totalElements / pagination.size)) {
            setPagination(prev => ({ ...prev, page: newPage }));
        }
    };

    return (
        <div className="list-user-page">
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
                    className="nav-dropdown"
                >
                    <NavDropdown.Item href="#action/3.1">English</NavDropdown.Item>
                    <NavDropdown.Item href="#action/3.2">Vietnamese</NavDropdown.Item>
                </NavDropdown>
            </header>

            {/* --- MAIN LIST PANEL --- */}
            <main>
                <section className="list-panel">
                    <div className="panel-header">
                        <div>
                            <h1 className="page-title">Users</h1>
                            <p className="page-description">
                                Manage employee accounts, roles, departments, and access status.
                            </p>
                        </div>
                        <button
                            type="button"
                            className="btn-primary"
                            onClick={() => navigate("/user-management/create")}
                        >
                            <IconPlus size={20} color="#ffffff" />
                            New User
                        </button>
                    </div>

                    {/* --- TOOLBAR & SEARCH --- */}
                    <div className="toolbar">
                        <label className="search-box">
                            <IconSearch size={23} color="#3f4d6f" />
                            <input
                                type="text"
                                aria-label="Search users"
                                placeholder="Search users..."
                                className="search-input"
                                value={searchTerm}
                                onChange={handleSearchInputChange}
                                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                            />
                        </label>

                        {/* Thay thế .map() bằng các thẻ viết rõ ràng */}
                        <label className="select-box">
                            <span className="select-label">Department</span>
                            <select className="select-input">
                                <option>All</option>
                            </select>
                            <span className="select-icon">
                                <IconChevronDown size={18} color="#243452" />
                            </span>
                        </label>

                        <label className="select-box">
                            <span className="select-label">Role</span>
                            <select className="select-input">
                                <option>All</option>
                            </select>
                            <span className="select-icon">
                                <IconChevronDown size={18} color="#243452" />
                            </span>
                        </label>

                        <label className="select-box">
                            <span className="select-label">Status</span>
                            <select className="select-input">
                                <option>All</option>
                            </select>
                            <span className="select-icon">
                                <IconChevronDown size={18} color="#243452" />
                            </span>
                        </label>

                        <button type="button" className="btn-filter" onClick={handleSearch}>
                            <IconFilter size={20} color="#243452" />
                            Filters
                        </button>
                        <button type="button" className="btn-icon" onClick={handleRefresh} disabled={loading}>
                            <IconRefresh size={22} color="#243452" className={loading ? "animate-spin" : ""} />
                        </button>
                    </div>

                    {/* --- TABLE DATA --- */}
                    <div className="table-wrap">
                        <table className="user-table">
                            <thead>
                            <tr>
                                {/* Thay thế .map() bằng các cột (th) rõ ràng */}
                                <th className="th-cell">
                                        <span className="th-content">
                                            User Name
                                            <IconArrowsSort size={15} color="#243452" />
                                        </span>
                                </th>
                                <th className="th-cell">
                                        <span className="th-content">
                                            Email
                                            <IconArrowsSort size={15} color="#243452" />
                                        </span>
                                </th>
                                <th className="th-cell">
                                        <span className="th-content">
                                            Department
                                            <IconArrowsSort size={15} color="#243452" />
                                        </span>
                                </th>
                                <th className="th-cell">
                                        <span className="th-content">
                                            Role
                                            <IconArrowsSort size={15} color="#243452" />
                                        </span>
                                </th>
                                <th className="th-cell">
                                        <span className="th-content">
                                            Status
                                            <IconArrowsSort size={15} color="#243452" />
                                        </span>
                                </th>
                                <th className="th-cell">
                                        <span className="th-content">
                                            Last Active
                                            <IconArrowsSort size={15} color="#243452" />
                                        </span>
                                </th>
                                <th className="th-cell">
                                        <span className="th-content">
                                            Actions
                                        </span>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: '#52617f' }}>
                                        Đang tải danh sách người dùng...
                                    </td>
                                </tr>
                            ) : users.length === 0 ? (
                                <tr>
                                    <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: '#52617f', fontStyle: 'italic' }}>
                                        {searchTerm ? 'Không tìm thấy người dùng nào phù hợp.' : 'Chưa có dữ liệu người dùng.'}
                                    </td>
                                </tr>
                            ) : (
                                users.map((user) => (
                                    <tr key={user.id || user.email} className="tr-row">
                                        <td className="name-cell">
                                            <span className="avatar">
                                                {user.name ? user.name.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase() : 'U'}
                                            </span>
                                            <span className="user-name">{user.name}</span>
                                        </td>
                                        <td className="td-cell">{user.email}</td>
                                        <td className="td-cell">{user.department}</td>
                                        <td className="td-cell">{user.role}</td>
                                        <td className="td-cell">
                                            <StatusBadge status={user.status} />
                                        </td>
                                        <td className="td-cell">{user.lastActive}</td>
                                        <td className="action-cell">
                                            <button
                                                type="button"
                                                className="btn-action"
                                                onClick={() => navigate(`/user-management/view/${user.id}`)}
                                            >
                                                <IconDots size={20} color="#111827" />
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>

                    {/* --- PAGINATION --- */}
                    <div className="table-footer">
                        <span>
                            Showing {users.length === 0 ? 0 : (pagination.page * pagination.size) + 1} to {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of {pagination.totalElements} results
                        </span>
                        <div className="pagination">
                            <button
                                type="button"
                                className="btn-page"
                                onClick={() => handlePageChange(pagination.page - 1)}
                                disabled={pagination.page === 0 || loading}
                            >
                                <IconChevronLeft size={18} color="#243452" />
                            </button>

                            <span className="current-page">{pagination.page + 1}</span>

                            <button
                                type="button"
                                className="btn-page"
                                onClick={() => handlePageChange(pagination.page + 1)}
                                disabled={loading || (pagination.page + 1) >= Math.ceil(pagination.totalElements / pagination.size)}
                            >
                                <IconChevronRight size={18} color="#243452" />
                            </button>

                            <select
                                className="per-page-select"
                                value={pagination.size}
                                onChange={(e) => {
                                    setPagination(prev => ({ ...prev, size: Number(e.target.value), page: 0 }));
                                    fetchData(0, searchTerm);
                                }}
                            >
                                <option value={10}>10 / page</option>
                                <option value={20}>20 / page</option>
                                <option value={50}>50 / page</option>
                            </select>
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
}

export default ListUser;