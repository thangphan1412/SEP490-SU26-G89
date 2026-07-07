import { useState } from "react";
import { useNavigate } from "react-router-dom";
import NavDropdown from "react-bootstrap/NavDropdown";

// Import Icon chuẩn từ Tabler Icons
import {
    IconWorld,
    IconDeviceFloppy, // Icon Save
    IconEdit,
    IconFileDescription, // Icon Document
    IconShieldCheck,
    IconBuilding,
    IconClock,
    IconInfoCircle,
    IconLoader2,
    IconChevronDown
} from "@tabler/icons-react";

// Import CSS
import "../../assets/styles/css/userProfileStyles/UpdateUserProfilePage.css";

const initialUserProfile = {
    fullName: "Alex Morgan",
    email: "alex.morgan@econtract.com",
    phoneNumber: "+84 28 3822 5678",
    department: "Legal Department",
    position: "Contract Manager",
    employeeId: "EMP-00023",
    timeZone: "(GMT+07:00) Bangkok, Hanoi, Jakarta",
    language: "English",
};

function UpdateProfile({ onSaveProfile }) {
    const navigate = useNavigate();
    const [userProfile, setUserProfile] = useState(initialUserProfile);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setUserProfile((currentProfile) => ({
            ...currentProfile,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setIsSubmitting(true);

        try {
            // Giả lập API lưu profile mất 1.5 giây
            await new Promise((resolve) => setTimeout(resolve, 1500));

            // Gọi callback nếu được truyền từ props
            onSaveProfile?.(userProfile);

            alert("Cập nhật thông tin cá nhân thành công!");
            navigate("/user-profile/view");

        } catch (error) {
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra, vui lòng thử lại!");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="update-user-profile-page">
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
                <form className="form-panel" onSubmit={handleSubmit}>

                    {/* Panel Header */}
                    <div className="panel-header">
                        <div>
                            <h1 className="page-title">Update Profile</h1>
                            <p className="page-description">
                                Update your personal information and account preferences.
                            </p>
                        </div>
                        <div className="form-actions">
                            <button
                                type="button"
                                className="btn-cancel"
                                onClick={() => navigate("/user-profile/view")}
                                disabled={isSubmitting}
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="btn-primary"
                                disabled={isSubmitting}
                                style={{ opacity: isSubmitting ? 0.7 : 1, cursor: isSubmitting ? "not-allowed" : "pointer" }}
                            >
                                {isSubmitting ? (
                                    <>
                                        <IconLoader2 size={19} color="#ffffff" className="animate-spin" />
                                        Saving...
                                    </>
                                ) : (
                                    <>
                                        <IconDeviceFloppy size={19} color="#ffffff" />
                                        Save Changes
                                    </>
                                )}
                            </button>
                        </div>
                    </div>

                    {/* --- PROFILE INFORMATION FORM --- */}
                    <section className="form-card">
                        <div className="card-header">
                            <div className="card-title-group">
                                <span className="round-icon">
                                    <IconEdit size={22} />
                                </span>
                                <h2 className="card-title">Profile Information</h2>
                            </div>
                            <span className="status-badge">Verified Profile</span>
                        </div>

                        <div className="profile-body">

                            {/* Avatar Section */}
                            <div className="avatar-block">
                                <div className="avatar-wrap">
                                    {/* Lấy 2 chữ cái đầu của tên làm Avatar */}
                                    <div className="avatar">
                                        {userProfile.fullName ? userProfile.fullName.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase() : 'U'}
                                    </div>
                                    <div className="avatar-badge">
                                        <IconFileDescription size={15} />
                                    </div>
                                </div>
                                <button type="button" className="btn-avatar">
                                    Change Avatar
                                </button>
                                <p className="avatar-helper">
                                    Used across profile and approvals.
                                </p>
                            </div>

                            {/* Form Input Section */}
                            <div className="form-grid">

                                {/* Full Name */}
                                <div className="form-group">
                                    <label htmlFor="fullName" className="form-label">
                                        Full Name <span className="text-required">*</span>
                                    </label>
                                    <input id="fullName" name="fullName" type="text" value={userProfile.fullName} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                    <p className="helper-text">Display name used in the system.</p>
                                </div>

                                {/* Email */}
                                <div className="form-group">
                                    <label htmlFor="email" className="form-label">
                                        Email <span className="text-required">*</span>
                                    </label>
                                    <input id="email" name="email" type="email" value={userProfile.email} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                    <p className="helper-text">Work email for system notifications.</p>
                                </div>

                                {/* Phone Number */}
                                <div className="form-group">
                                    <label htmlFor="phoneNumber" className="form-label">
                                        Phone Number <span className="text-required">*</span>
                                    </label>
                                    <input id="phoneNumber" name="phoneNumber" type="tel" value={userProfile.phoneNumber} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                </div>

                                {/* Department */}
                                <div className="form-group">
                                    <label htmlFor="department" className="form-label">
                                        Department <span className="text-required">*</span>
                                    </label>
                                    <div className="input-wrap">
                                        <select id="department" name="department" value={userProfile.department} onChange={handleChange} required disabled={isSubmitting} className="form-input">
                                            <option value="Legal Department">Legal Department</option>
                                            <option value="Sales Department">Sales Department</option>
                                            <option value="Finance Department">Finance Department</option>
                                        </select>
                                        <span className="right-icon"><IconChevronDown size={18} /></span>
                                    </div>
                                </div>

                                {/* Position */}
                                <div className="form-group">
                                    <label htmlFor="position" className="form-label">
                                        Position <span className="text-required">*</span>
                                    </label>
                                    <div className="input-wrap">
                                        <select id="position" name="position" value={userProfile.position} onChange={handleChange} required disabled={isSubmitting} className="form-input">
                                            <option value="Contract Manager">Contract Manager</option>
                                            <option value="Legal Specialist">Legal Specialist</option>
                                            <option value="Department Manager">Department Manager</option>
                                        </select>
                                        <span className="right-icon"><IconChevronDown size={18} /></span>
                                    </div>
                                </div>

                                {/* Employee ID */}
                                <div className="form-group">
                                    <label htmlFor="employeeId" className="form-label">Employee ID</label>
                                    <input id="employeeId" name="employeeId" type="text" value={userProfile.employeeId} disabled className="form-input" />
                                </div>

                                {/* Time Zone */}
                                <div className="form-group">
                                    <label htmlFor="timeZone" className="form-label">
                                        Time Zone <span className="text-required">*</span>
                                    </label>
                                    <div className="input-wrap">
                                        <select id="timeZone" name="timeZone" value={userProfile.timeZone} onChange={handleChange} required disabled={isSubmitting} className="form-input">
                                            <option value="(GMT+07:00) Bangkok, Hanoi, Jakarta">(GMT+07:00) Bangkok, Hanoi, Jakarta</option>
                                            <option value="(GMT+08:00) Singapore, Kuala Lumpur">(GMT+08:00) Singapore, Kuala Lumpur</option>
                                            <option value="(GMT+09:00) Tokyo, Seoul">(GMT+09:00) Tokyo, Seoul</option>
                                        </select>
                                        <span className="right-icon"><IconChevronDown size={18} /></span>
                                    </div>
                                </div>

                                {/* Language */}
                                <div className="form-group">
                                    <label htmlFor="language" className="form-label">
                                        Language <span className="text-required">*</span>
                                    </label>
                                    <div className="input-wrap">
                                        <select id="language" name="language" value={userProfile.language} onChange={handleChange} required disabled={isSubmitting} className="form-input">
                                            <option value="English">English</option>
                                            <option value="Vietnamese">Vietnamese</option>
                                        </select>
                                        <span className="right-icon"><IconChevronDown size={18} /></span>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </section>

                    {/* --- USAGE PREVIEW --- */}
                    <section className="form-card">
                        <div className="usage-header">
                            <span className="round-icon">
                                <IconShieldCheck size={22} />
                            </span>
                            <div>
                                <h2 className="card-title">Profile Usage Preview</h2>
                                <p className="card-description">
                                    Changes will be reflected across contract templates, approvals, and notifications after saving.
                                </p>
                            </div>
                        </div>

                        <div className="usage-grid">

                            <div className="usage-item">
                                <span className="usage-icon">
                                    <IconFileDescription size={22} />
                                </span>
                                <div>
                                    <h3 className="usage-title">Contracts</h3>
                                    <p className="usage-description">Profile details appear on owned contracts.</p>
                                </div>
                            </div>

                            <div className="usage-item">
                                <span className="usage-icon">
                                    <IconShieldCheck size={22} />
                                </span>
                                <div>
                                    <h3 className="usage-title">Approvals</h3>
                                    <p className="usage-description">Name and position show in approval flows.</p>
                                </div>
                            </div>

                            <div className="usage-item">
                                <span className="usage-icon">
                                    <IconBuilding size={22} />
                                </span>
                                <div>
                                    <h3 className="usage-title">Department</h3>
                                    <p className="usage-description">Department controls workflow routing.</p>
                                </div>
                            </div>

                            <div className="usage-item">
                                <span className="usage-icon">
                                    <IconClock size={22} />
                                </span>
                                <div>
                                    <h3 className="usage-title">Notifications</h3>
                                    <p className="usage-description">Email and phone receive system alerts.</p>
                                </div>
                            </div>

                        </div>
                    </section>

                    {/* --- INFO ALERT --- */}
                    <section className="info-alert">
                        <IconInfoCircle size={22} color="#2055ff" />
                        <span>
                            Review all changes carefully before saving. Updated profile data will apply to future documents.
                        </span>
                    </section>

                </form>
            </main>
        </div>
    );
}

export default UpdateProfile;