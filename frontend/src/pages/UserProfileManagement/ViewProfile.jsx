import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import NavDropdown from "react-bootstrap/NavDropdown";

// Import Icon chuẩn từ Tabler Icons
import {
    IconWorld,
    IconEdit,
    IconUser,
    IconShieldCheck,
    IconClock,
    IconSignature,
    IconInfoCircle,
    IconLoader2
} from "@tabler/icons-react";

// Import CSS
import "../../assets/styles/css/userProfileStyles/ViewUserProfilePage.css";

function ViewProfile() {
    const navigate = useNavigate();

    // State quản lý dữ liệu và loading
    const [userProfile, setUserProfile] = useState(null);
    const [loading, setLoading] = useState(true);

    // Hiệu ứng giả lập gọi API
    useEffect(() => {
        const fetchUserProfile = async () => {
            setLoading(true);
            try {
                // TODO: Gọi API thật từ Backend (Ví dụ: const res = await getMyProfile())
                // Mô phỏng chờ API 1 giây
                await new Promise((resolve) => setTimeout(resolve, 1000));

                // Set dữ liệu trả về
                setUserProfile({
                    fullName: "Alex Morgan",
                    email: "alex.morgan@econtract.com",
                    phoneNumber: "+84 28 3822 5678",
                    department: "Legal Department",
                    position: "Contract Manager",
                    employeeId: "EMP-00023",
                    dateJoined: "March 15, 2023",
                    timeZone: "(GMT+07:00) Bangkok, Hanoi, Jakarta",
                    language: "English",
                    accountStatus: "Active",
                    defaultSignature: "Default Work Signature",
                    lastUpdated: "May 22, 2025",
                });
            } catch (error) {
                console.error("Lỗi khi tải thông tin cá nhân:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchUserProfile();
    }, []);

    return (
        <div className="view-user-profile-page">
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
                <div className="view-panel">

                    {/* Panel Header */}
                    <section className="panel-header">
                        <div>
                            <h1 className="page-title">My Profile</h1>
                            <p className="page-description">
                                View your personal information and account details.
                            </p>
                        </div>
                        <button
                            type="button"
                            className="btn-primary"
                            onClick={() => navigate("/user-profile/update")}
                            disabled={loading}
                        >
                            <IconEdit size={19} color="#ffffff" />
                            Edit Profile
                        </button>
                    </section>

                    {/* Check Loading State */}
                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '100px 0', color: '#64708f' }}>
                            <IconLoader2 size={42} className="animate-spin" style={{ margin: '0 auto 16px' }} />
                            <p>Loading your profile details...</p>
                        </div>
                    ) : userProfile ? (
                        <>
                            {/* --- PERSONAL INFORMATION CARD --- */}
                            <section className="info-card">
                                <div className="card-header">
                                    <div className="card-title-group">
                                        <span className="round-icon">
                                            <IconUser size={22} />
                                        </span>
                                        <h2 className="card-title">Personal Information</h2>
                                    </div>
                                    <span className="status-badge">Active</span>
                                </div>

                                <div className="profile-body">
                                    {/* Avatar Block */}
                                    <div className="avatar-block">
                                        <div className="avatar-wrap">
                                            {/* Lấy 2 chữ cái đầu của tên làm Avatar */}
                                            <div className="avatar">
                                                {userProfile.fullName ? userProfile.fullName.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase() : 'U'}
                                            </div>
                                            <div className="avatar-badge">
                                                <IconEdit size={15} />
                                            </div>
                                        </div>
                                        <h3 className="avatar-name">{userProfile.fullName}</h3>
                                        <p className="avatar-role">{userProfile.position}</p>
                                    </div>

                                    {/* Detailed Information Grid */}
                                    <div className="detail-grid">

                                        <div className="detail-row">
                                            <span className="detail-label">Full Name</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.fullName}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Email</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.email}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Phone Number</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.phoneNumber}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Department</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.department}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Position</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.position}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Employee ID</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.employeeId}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Date Joined</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.dateJoined}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Time Zone</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.timeZone}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Language</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.language}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Account Status</span>
                                            <span className="colon">:</span>
                                            {/* Dùng class riêng tạo chữ màu xanh lá cho Active */}
                                            <span className="detail-value success-text">{userProfile.accountStatus}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Default Signature</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.defaultSignature}</span>
                                        </div>

                                        <div className="detail-row">
                                            <span className="detail-label">Last Updated</span>
                                            <span className="colon">:</span>
                                            <span className="detail-value">{userProfile.lastUpdated}</span>
                                        </div>

                                    </div>
                                </div>
                            </section>

                            {/* --- ACCOUNT & ACCESS SUMMARY CARD --- */}
                            <section className="info-card">
                                <div className="card-header">
                                    <div className="card-title-group">
                                        <span className="round-icon">
                                            <IconShieldCheck size={22} />
                                        </span>
                                        <div>
                                            <h2 className="card-title">Account & Access Summary</h2>
                                            <p className="card-description">
                                                Your profile is used across contracts, approvals, and internal workflows.
                                            </p>
                                        </div>
                                    </div>
                                    <span className="status-badge" style={{ borderColor: '#c9d9ff', background: '#f0f5ff', color: '#184cff' }}>Verified</span>
                                </div>

                                <div className="summary-grid">

                                    <div className="summary-item">
                                        <span className="summary-icon">
                                            <IconUser size={22} />
                                        </span>
                                        <div>
                                            <p className="summary-label">Role</p>
                                            <h3 className="summary-value">Contract Manager</h3>
                                            <p className="summary-description">Assigned role</p>
                                        </div>
                                    </div>

                                    <div className="summary-item">
                                        <span className="summary-icon">
                                            <IconShieldCheck size={22} />
                                        </span>
                                        <div>
                                            <p className="summary-label">Permissions</p>
                                            <h3 className="summary-value">18 active</h3>
                                            <p className="summary-description">Access permissions</p>
                                        </div>
                                    </div>

                                    <div className="summary-item">
                                        <span className="summary-icon">
                                            <IconSignature size={22} />
                                        </span>
                                        <div>
                                            <p className="summary-label">Signature Library</p>
                                            <h3 className="summary-value">6 signatures</h3>
                                            <p className="summary-description">Personal e-signatures</p>
                                        </div>
                                    </div>

                                    <div className="summary-item">
                                        <span className="summary-icon">
                                            <IconClock size={22} />
                                        </span>
                                        <div>
                                            <p className="summary-label">Last Login</p>
                                            <h3 className="summary-value">May 22, 2025</h3>
                                            <p className="summary-description">Recent account activity</p>
                                        </div>
                                    </div>

                                </div>
                            </section>

                            {/* --- INFO ALERT --- */}
                            <section className="info-alert">
                                <IconInfoCircle size={22} color="#2055ff" />
                                <span>
                                    To update your personal information, click <strong>Edit Profile</strong>.
                                </span>
                            </section>
                        </>
                    ) : (
                        <div style={{ textAlign: 'center', padding: '100px 0', color: '#e11d48' }}>
                            <p>Profile not found or an error occurred.</p>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}

export default ViewProfile;