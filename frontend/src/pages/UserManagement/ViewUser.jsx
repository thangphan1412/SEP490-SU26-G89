import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import NavDropdown from "react-bootstrap/NavDropdown";

// Import các Icon từ Tabler Icons
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
    IconWorld as IconGlobe,
    IconCalendar,
    IconClock,
    IconCheck,
    IconClipboardList,
    IconInfoCircle,
    IconLoader2
} from "@tabler/icons-react";

// Import CSS
import "../../assets/styles/css/userManagementStyles/ViewUserPage.css";

function ViewUser() {
    const navigate = useNavigate();
    // const { id } = useParams(); // Sẽ dùng khi có API thực tế để lấy ID từ URL

    // State quản lý dữ liệu và loading
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Mô phỏng gọi API lấy chi tiết User
    useEffect(() => {
        const fetchUserDetails = async () => {
            setLoading(true);
            try {
                // TODO: Gắn API thật (VD: const res = await getUserById(id))
                // Mô phỏng chờ API 1 giây
                await new Promise(resolve => setTimeout(resolve, 1000));

                // Trả về dữ liệu giả lập
                setUser({
                    fullName: "Emma Nguyen",
                    email: "emma.nguyen@econtract.com",
                    phoneNumber: "+1 (555) 123-4567",
                    employeeId: "EMP-001248",
                    department: "Legal Department",
                    position: "Senior Legal Counsel",
                    role: "Contract Manager",
                    accessScope: "Department Level",
                    dateJoined: "Jan 15, 2024",
                    lastLogin: "May 22, 2025, 09:32 AM",
                    status: "Active"
                });
            } catch (error) {
                console.error("Lỗi khi lấy dữ liệu người dùng:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchUserDetails();
    }, []);

    return (
        <div className="view-user-page">
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
            <main>
                <section className="view-panel">
                    <div className="panel-header">
                        <div>
                            <h1 className="page-title">User Details</h1>
                            <p className="page-description">
                                Review employee information, role permissions, and department assignment.
                            </p>
                        </div>
                        {/* Truyền thêm ID vào param để trang Update biết cần update ai */}
                        <button
                            type="button"
                            className="btn-primary"
                            onClick={() => navigate("/user-management/update")}
                            disabled={loading}
                        >
                            <IconEdit size={20} color="#fff" />
                            Edit User
                        </button>
                    </div>

                    {/* Kiểm tra trạng thái loading */}
                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '100px 0', color: '#52617f' }}>
                            <IconLoader2 size={40} className="animate-spin" style={{ margin: '0 auto 16px' }} />
                            <p>Loading user details...</p>
                        </div>
                    ) : user ? (
                        <>
                            {/* --- THÔNG TIN NHÂN VIÊN CƠ BẢN --- */}
                            <section className="info-card">
                                <h2 className="card-title">Employee Information</h2>
                                <div className="employee-grid">

                                    {/* Cột 1: Avatar */}
                                    <div className="photo-column">
                                        <div className="photo-placeholder">
                                            <div className="photo-face" />
                                        </div>
                                        <h3 className="employee-name">{user.fullName}</h3>
                                        <span className="badge-active">● {user.status}</span>
                                    </div>

                                    {/* Cột 2: Cột chi tiết bên trái */}
                                    <div className="details-column">
                                        <div className="detail-row">
                                            <IconUser size={21} className="detail-icon" />
                                            <span className="detail-label">Full Name</span>
                                            <span className="detail-value">{user.fullName}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconMail size={21} className="detail-icon" />
                                            <span className="detail-label">Email</span>
                                            <span className="detail-value">{user.email}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconPhone size={21} className="detail-icon" />
                                            <span className="detail-label">Phone Number</span>
                                            <span className="detail-value">{user.phoneNumber}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconId size={21} className="detail-icon" />
                                            <span className="detail-label">Employee ID</span>
                                            <span className="detail-value">{user.employeeId}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconBuilding size={21} className="detail-icon" />
                                            <span className="detail-label">Department</span>
                                            <span className="detail-value">{user.department}</span>
                                        </div>
                                    </div>

                                    {/* Cột 3: Cột chi tiết bên phải */}
                                    <div className="details-column">
                                        <div className="detail-row">
                                            <IconBriefcase size={21} className="detail-icon" />
                                            <span className="detail-label">Position</span>
                                            <span className="detail-value">{user.position}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconShieldCheck size={21} className="detail-icon" />
                                            <span className="detail-label">Role</span>
                                            <span className="detail-value">{user.role}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconGlobe size={21} className="detail-icon" />
                                            <span className="detail-label">Access Scope</span>
                                            <span className="detail-value">{user.accessScope}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconCalendar size={21} className="detail-icon" />
                                            <span className="detail-label">Date Joined</span>
                                            <span className="detail-value">{user.dateJoined}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconClock size={21} className="detail-icon" />
                                            <span className="detail-label">Last Login</span>
                                            <span className="detail-value">{user.lastLogin}</span>
                                        </div>
                                        <div className="detail-row">
                                            <IconCheck size={21} className="detail-icon" />
                                            <span className="detail-label">Account Status</span>
                                            <span className="badge-active">{user.status}</span>
                                        </div>
                                    </div>
                                </div>
                            </section>

                            {/* --- TÓM TẮT QUYỀN TRUY CẬP --- */}
                            <section className="info-card">
                                <h2 className="card-title">Role & Access Summary</h2>
                                <div className="summary-grid">

                                    <div className="summary-item">
                                        <span className="summary-icon"><IconShieldCheck size={25} /></span>
                                        <p className="summary-title">Assigned Role</p>
                                        <h3 className="summary-value">{user.role}</h3>
                                        <p className="summary-text">Full access to contract lifecycle management and approvals.</p>
                                    </div>

                                    <div className="summary-item">
                                        <span className="summary-icon"><IconBuilding size={25} /></span>
                                        <p className="summary-title">Department Access</p>
                                        <h3 className="summary-value">{user.department}</h3>
                                        <p className="summary-text">Can view and manage all users and contracts within the Legal Department.</p>
                                    </div>

                                    <div className="summary-item">
                                        <span className="summary-icon"><IconClipboardList size={25} /></span>
                                        <p className="summary-title">Approval Rights</p>
                                        <h3 className="summary-value">Up to $250,000</h3>
                                        <p className="summary-text">Authorized to approve contracts and changes up to a value of $250,000.</p>
                                    </div>

                                    <div className="summary-item">
                                        <span className="summary-icon"><IconClock size={25} /></span>
                                        <p className="summary-title">Recent Activity</p>
                                        <h3 className="summary-value">32 Activities</h3>
                                        <p className="summary-text">Last activity: Viewed contract CN-2025-0456 on {user.lastLogin}.</p>
                                    </div>

                                </div>
                            </section>

                            {/* --- ALERT INFO --- */}
                            <section className="info-alert">
                                <IconInfoCircle size={22} color="#2450f5" />
                                <span>
                                    To make changes to this user, click <strong className="link-highlight">Edit User</strong>.
                                </span>
                            </section>
                        </>
                    ) : (
                        <div style={{ textAlign: 'center', padding: '100px 0', color: '#e11d48' }}>
                            <p>User not found or an error occurred.</p>
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}

export default ViewUser;