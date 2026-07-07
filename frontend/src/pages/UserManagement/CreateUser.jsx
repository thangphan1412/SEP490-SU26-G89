import { useState } from "react";
import { useNavigate } from "react-router-dom";
import NavDropdown from "react-bootstrap/NavDropdown";

// Import các icon cần thiết
import {
    IconWorld,
    IconChevronDown,
    IconEye,
    IconCalendar,
    IconUserPlus,
    IconShieldCheck,
    IconBuilding,
    IconMail,
    IconUserCheck,
    IconInfoCircle,
    IconLoader2
} from "@tabler/icons-react";

import "../../assets/styles/css/userManagementStyles/CreateUserPage.css";

const initialUser = {
    fullName: "",
    email: "",
    initialPassword: "",
    confirmPassword: "",
    department: "",
    role: "",
    position: "",
    phoneNumber: "",
    employeeId: "",
    startDate: "",
    status: "Active",
    sendWelcomeEmail: true,
};

function CreateUser() {
    const navigate = useNavigate();
    const [user, setUser] = useState(initialUser);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (event) => {
        const { name, value, checked, type } = event.target;
        setUser((currentUser) => ({
            ...currentUser,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (user.initialPassword !== user.confirmPassword) {
            alert("Mật khẩu xác nhận không khớp!");
            return;
        }

        setIsSubmitting(true);

        try {
            // Giả lập API call 1.5s
            await new Promise((resolve) => setTimeout(resolve, 1500));

            alert("Tạo tài khoản thành công!");
            navigate("/user-management/list");

        } catch (error) {
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra, vui lòng thử lại!");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="create-user-page">
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

            <main>
                <form className="form-panel" onSubmit={handleSubmit}>
                    <div className="panel-header">
                        <div>
                            <h1 className="page-title">Create User</h1>
                            <p className="page-description">
                                Add a new employee account and assign access permissions.
                            </p>
                        </div>
                        <div className="form-actions">
                            <button
                                type="button"
                                className="btn-cancel"
                                onClick={() => navigate("/user-management/list")}
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
                                        <IconLoader2 size={21} color="#ffffff" className="animate-spin" />
                                        Creating...
                                    </>
                                ) : (
                                    <>
                                        <IconUserPlus size={21} color="#ffffff" />
                                        Create User
                                    </>
                                )}
                            </button>
                        </div>
                    </div>

                    <section className="form-card">
                        <h2 className="card-title">User Information</h2>

                        <div className="form-grid">

                            {/* Full Name */}
                            <div className="form-group">
                                <label htmlFor="fullName" className="form-label">
                                    Full Name <span className="text-required"> *</span>
                                </label>
                                <div className="input-wrap">
                                    <input id="fullName" name="fullName" type="text" value={user.fullName} onChange={handleChange} placeholder="Enter full name" required disabled={isSubmitting} className="form-input" />
                                </div>
                            </div>

                            {/* Email */}
                            <div className="form-group">
                                <label htmlFor="email" className="form-label">
                                    Email Address <span className="text-required"> *</span>
                                </label>
                                <div className="input-wrap">
                                    <input id="email" name="email" type="email" value={user.email} onChange={handleChange} placeholder="Enter email address" required disabled={isSubmitting} className="form-input" />
                                </div>
                            </div>

                            {/* Password */}
                            <div className="form-group">
                                <label htmlFor="initialPassword" className="form-label">
                                    Initial Password <span className="text-required"> *</span>
                                </label>
                                <div className="input-wrap">
                                    <input id="initialPassword" name="initialPassword" type="password" value={user.initialPassword} onChange={handleChange} placeholder="Enter initial password" required disabled={isSubmitting} className="form-input" />
                                    <span className="right-icon"><IconEye size={18} color="#53617e" /></span>
                                </div>
                            </div>

                            {/* Confirm Password */}
                            <div className="form-group">
                                <label htmlFor="confirmPassword" className="form-label">
                                    Confirm Password <span className="text-required"> *</span>
                                </label>
                                <div className="input-wrap">
                                    <input id="confirmPassword" name="confirmPassword" type="password" value={user.confirmPassword} onChange={handleChange} placeholder="Confirm initial password" required disabled={isSubmitting} className="form-input" />
                                    <span className="right-icon"><IconEye size={18} color="#53617e" /></span>
                                </div>
                            </div>

                            {/* Department */}
                            <div className="form-group">
                                <label htmlFor="department" className="form-label">
                                    Department <span className="text-required"> *</span>
                                </label>
                                <div className="input-wrap">
                                    <select id="department" name="department" value={user.department} onChange={handleChange} required disabled={isSubmitting} className="form-input">
                                        <option value="">Select department</option>
                                        <option value="Legal">Legal</option>
                                        <option value="HR">HR</option>
                                        <option value="Finance">Finance</option>
                                        <option value="Sales">Sales</option>
                                    </select>
                                    <span className="right-icon"><IconChevronDown size={18} color="#243452" /></span>
                                </div>
                            </div>

                            {/* Role */}
                            <div className="form-group">
                                <label htmlFor="role" className="form-label">
                                    Role <span className="text-required"> *</span>
                                </label>
                                <div className="input-wrap">
                                    <select id="role" name="role" value={user.role} onChange={handleChange} required disabled={isSubmitting} className="form-input">
                                        <option value="">Select role</option>
                                        <option value="Contract Manager">Contract Manager</option>
                                        <option value="HR Admin">HR Admin</option>
                                        <option value="Approver">Approver</option>
                                        <option value="Viewer">Viewer</option>
                                    </select>
                                    <span className="right-icon"><IconChevronDown size={18} color="#243452" /></span>
                                </div>
                            </div>

                            {/* Position */}
                            <div className="form-group">
                                <label htmlFor="position" className="form-label">Position</label>
                                <div className="input-wrap">
                                    <input id="position" name="position" type="text" value={user.position} onChange={handleChange} placeholder="Enter position" disabled={isSubmitting} className="form-input" />
                                </div>
                            </div>

                            {/* Phone Number */}
                            <div className="form-group">
                                <label htmlFor="phoneNumber" className="form-label">Phone Number</label>
                                <div className="input-wrap">
                                    <div className="phone-input">
                                        <span className="phone-country">🇺🇸</span>
                                        <span className="phone-code">+1</span>
                                        <input id="phoneNumber" name="phoneNumber" type="text" value={user.phoneNumber} onChange={handleChange} placeholder="Enter phone number" disabled={isSubmitting} className="phone-field" />
                                    </div>
                                </div>
                            </div>

                            {/* Employee ID */}
                            <div className="form-group">
                                <label htmlFor="employeeId" className="form-label">Employee ID</label>
                                <div className="input-wrap">
                                    <input id="employeeId" name="employeeId" type="text" value={user.employeeId} onChange={handleChange} placeholder="Enter employee ID" disabled={isSubmitting} className="form-input" />
                                </div>
                            </div>

                            {/* Start Date */}
                            <div className="form-group">
                                <label htmlFor="startDate" className="form-label">
                                    Start Date <span className="text-required"> *</span>
                                </label>
                                <div className="input-wrap">
                                    <input id="startDate" name="startDate" type="date" value={user.startDate} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                </div>
                            </div>

                            {/* Status */}
                            <div className="form-group">
                                <label htmlFor="status" className="form-label">
                                    Status <span className="text-required"> *</span>
                                </label>
                                <div className="input-wrap">
                                    <select id="status" name="status" value={user.status} onChange={handleChange} required disabled={isSubmitting} className="form-input">
                                        <option value="Active">Active</option>
                                        <option value="Inactive">Inactive</option>
                                    </select>
                                    <span className="right-icon"><IconChevronDown size={18} color="#243452" /></span>
                                </div>
                            </div>

                        </div>

                        {/* Toggle Email */}
                        <label className="toggle-row">
                            <input type="checkbox" name="sendWelcomeEmail" checked={user.sendWelcomeEmail} onChange={handleChange} disabled={isSubmitting} className="toggle-checkbox" />
                            <span className="toggle-track"><span className="toggle-thumb" /></span>
                            <span>
                                <strong>Send welcome email</strong>
                                <small className="toggle-help">Send an email invitation with login details to the new user</small>
                            </span>
                        </label>
                    </section>

                    <section className="form-card">
                        <h2 className="card-title">Access & Onboarding</h2>

                        <div className="preview-grid">
                            <div className="preview-tile">
                                <span className="preview-icon" style={{ background: "#eef3ff" }}>
                                    <IconShieldCheck size={27} color="#1f4fff" />
                                </span>
                                <div>
                                    <h3 className="preview-title">Role Permissions</h3>
                                    <p className="preview-text">User permissions will be assigned based on the selected role.</p>
                                </div>
                            </div>

                            <div className="preview-tile">
                                <span className="preview-icon" style={{ background: "#f0edff" }}>
                                    <IconBuilding size={27} color="#1f4fff" />
                                </span>
                                <div>
                                    <h3 className="preview-title">Department Access</h3>
                                    <p className="preview-text">Access will be limited to data and modules within the selected department.</p>
                                </div>
                            </div>

                            <div className="preview-tile">
                                <span className="preview-icon" style={{ background: "#eafaf0" }}>
                                    <IconMail size={27} color="#16a34a" />
                                </span>
                                <div>
                                    <h3 className="preview-title">Email Notification</h3>
                                    <p className="preview-text">A welcome email will be sent with login details and getting started guide.</p>
                                </div>
                            </div>

                            <div className="preview-tile">
                                <span className="preview-icon" style={{ background: "#fff3e6" }}>
                                    <IconUserCheck size={27} color="#f97316" />
                                </span>
                                <div>
                                    <h3 className="preview-title">Account Activation</h3>
                                    <p className="preview-text">User account will be activated based on the selected status.</p>
                                </div>
                            </div>
                        </div>

                        <div className="info-alert">
                            <IconInfoCircle size={20} />
                            Please review the information above before creating the account. You can edit details after the account is created.
                        </div>
                    </section>
                </form>
            </main>
        </div>
    );
}

export default CreateUser;