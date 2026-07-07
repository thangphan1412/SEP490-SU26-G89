import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import NavDropdown from "react-bootstrap/NavDropdown";

// Import các Icon từ Tabler Icons
import {
    IconWorld,
    IconEdit,
    IconCheck,
    IconFileDescription,
    IconFileExport,
    IconShieldCheck,
    IconClock,
    IconInfoCircle,
    IconLoader2
} from "@tabler/icons-react";

// Import CSS
import "../../assets/styles/css/companyProfileStyles/ViewCompanyProfilePage.css";

function ViewProfile({ onEditProfile }) {
    const navigate = useNavigate();

    // State quản lý dữ liệu và loading
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);

    // Mô phỏng gọi API lấy dữ liệu Company Profile
    useEffect(() => {
        const fetchProfile = async () => {
            setLoading(true);
            try {
                // TODO: Gắn API thật (VD: const res = await getCompanyProfile())
                // Mô phỏng thời gian chờ 1 giây
                await new Promise(resolve => setTimeout(resolve, 1000));

                setProfile({
                    companyName: "ABC Holdings Co., Ltd.",
                    email: "legal@abcholdings.vn",
                    taxCode: "0312345678",
                    phone: "+84 28 3822 5678",
                    registeredAddress: "125 Nguyen Hue Boulevard, Ben Nghe Ward, District 1, Ho Chi Minh City, Vietnam",
                    businessRegistrationNumber: "BRN-2025-00981",
                    legalRepresentative: "Nguyen Minh An",
                    registrationDate: "May 12, 2020",
                    lastVerifiedDate: "May 10, 2025",
                    verifiedBy: "Alex Morgan",
                });
            } catch (error) {
                console.error("Lỗi khi tải dữ liệu công ty:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    const handleEditProfile = () => {
        // Truyền data cũ sang hàm callback nếu có
        onEditProfile?.(profile);
        navigate("/company-profile/update");
    };

    return (
        <div className="view-profile-page">
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

                    <section className="panel-header">
                        <div>
                            <h1 className="page-title">Company Profile</h1>
                            <p className="page-description">
                                Manage your company's legal identity information for automatic use in contracts and documents.
                            </p>
                        </div>
                        <button
                            type="button"
                            className="btn-primary"
                            onClick={handleEditProfile}
                            disabled={loading}
                        >
                            <IconEdit size={20} color="#ffffff" />
                            <span>Edit Profile</span>
                        </button>
                    </section>

                    {/* Check Loading State */}
                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '100px 0', color: '#64708f' }}>
                            <IconLoader2 size={42} className="animate-spin" style={{ margin: '0 auto 16px' }} />
                            <p>Loading company profile details...</p>
                        </div>
                    ) : profile ? (
                        <>
                            {/* --- COMPANY LEGAL INFORMATION --- */}
                            <section className="info-card">
                                <div className="card-header">
                                    <h2 className="card-title">Company Legal Information</h2>
                                </div>
                                <div className="detail-grid">

                                    {/* Thay vì Map, viết rõ từng ô dữ liệu */}
                                    <div className="detail-item">
                                        <span className="detail-label">Company Name</span>
                                        <span className="detail-value">{profile.companyName}</span>
                                    </div>

                                    <div className="detail-item">
                                        <span className="detail-label">Email</span>
                                        <span className="detail-value">{profile.email}</span>
                                    </div>

                                    <div className="detail-item">
                                        <span className="detail-label">Tax Code (MST)</span>
                                        <span className="detail-value">{profile.taxCode}</span>
                                    </div>

                                    <div className="detail-item">
                                        <span className="detail-label">Phone</span>
                                        <span className="detail-value">{profile.phone}</span>
                                    </div>

                                    <div className="detail-item">
                                        <span className="detail-label">Registered Address</span>
                                        <span className="detail-value">{profile.registeredAddress}</span>
                                    </div>

                                    <div className="detail-item">
                                        <span className="detail-label">Business Registration No.</span>
                                        <span className="detail-value">{profile.businessRegistrationNumber}</span>
                                    </div>

                                    <div className="detail-item">
                                        <span className="detail-label">Legal Representative</span>
                                        <span className="detail-value">{profile.legalRepresentative}</span>
                                    </div>

                                    <div className="detail-item">
                                        <span className="detail-label">Registration Date</span>
                                        <span className="detail-value">{profile.registrationDate}</span>
                                    </div>

                                </div>
                            </section>

                            {/* --- AUTO-FILL USAGE --- */}
                            <section className="info-card">
                                <div className="card-header">
                                    <h2 className="card-title">Auto-fill Usage</h2>
                                    <div className="verified-badge">
                                        <IconCheck size={16} strokeWidth={3} />
                                        <span>Verified</span>
                                    </div>
                                </div>
                                <div className="usage-body">
                                    <p className="usage-intro">
                                        These details are automatically inserted into contract templates and generated legal documents.
                                    </p>

                                    <div className="usage-grid">

                                        <div className="usage-card">
                                            <span className="usage-icon"><IconFileDescription size={24} /></span>
                                            <h3 className="usage-title">Contract Templates</h3>
                                            <p className="usage-description">Company information is auto-filled in all contract templates.</p>
                                        </div>

                                        <div className="usage-card">
                                            <span className="usage-icon"><IconFileExport size={24} /></span>
                                            <h3 className="usage-title">Generated Documents</h3>
                                            <p className="usage-description">Used in quotes, agreements, reports, and legal documents.</p>
                                        </div>

                                        <div className="usage-card">
                                            <span className="usage-icon"><IconShieldCheck size={24} /></span>
                                            <h3 className="usage-title">Compliance</h3>
                                            <p className="usage-description">Ensures consistent and accurate company information.</p>
                                        </div>

                                        <div className="usage-card">
                                            <span className="usage-icon"><IconClock size={24} /></span>
                                            <h3 className="usage-title">Last Verified</h3>
                                            <p className="usage-description">
                                                Verified on {profile.lastVerifiedDate} by {profile.verifiedBy}.
                                            </p>
                                        </div>

                                    </div>
                                </div>
                            </section>

                            {/* --- INFO ALERT --- */}
                            <div className="info-alert">
                                <IconInfoCircle size={22} color="#184cff" />
                                <span>To update your company information, click <strong>Edit Profile</strong>.</span>
                            </div>
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