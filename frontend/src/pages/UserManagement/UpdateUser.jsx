import { useState } from "react";
import { useNavigate } from "react-router-dom";
import NavDropdown from "react-bootstrap/NavDropdown";

// Import tất cả icon chuẩn từ Tabler Icons
import {
    IconWorld,
    IconDeviceFloppy, // Icon Save
    IconUser,
    IconPhone,
    IconMail,
    IconId, // Icon cho Employee ID
    IconBuilding,
    IconCalendar,
    IconShieldCheck,
    IconBriefcase,
    IconLock,
    IconChevronDown,
    IconUserShield,
    IconClipboardList, // Icon cho Audit
    IconGitMerge, // Icon cho Workflow
    IconInfoCircle,
    IconLoader2 // Icon xoay khi loading
} from "@tabler/icons-react";

// Import CSS
import "../../assets/styles/css/userManagementStyles/UpdateUserPage.css";

// Dữ liệu User mẫu ban đầu (khi chưa có API)
const initialUser = {
    fullName: "Emma Nguyen",
    phoneNumber: "(555) 123-4567",
    email: "emma.nguyen@company.com",
    employeeId: "EMP-00987",
    department: "Legal",
    startDate: "Jun 15, 2023",
    role: "Legal Reviewer",
    status: "Active",
    position: "Senior Legal Counsel",
    accessScope: "Department Level Access",
    deactivateAccount: false,
};

function UpdateUser({ onUpdateUser }) {
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
        setIsSubmitting(true);

        try {
            // Giả lập thời gian gửi API lên Backend mất 1.5 giây
            await new Promise((resolve) => setTimeout(resolve, 1500));

            // Nếu có function truyền từ Parent thì gọi
            onUpdateUser?.(user);

            // Thông báo thành công và điều hướng
            alert("Cập nhật thông tin người dùng thành công!");
            navigate("/user-management/list");

        } catch (error) {
            console.error("Lỗi:", error);
            alert("Có lỗi xảy ra trong quá trình lưu dữ liệu, vui lòng thử lại!");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="update-user-page">
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
                    <div className="panel-header">
                        <div>
                            <h1 className="page-title">Update User</h1>
                            <p className="page-description">
                                Edit employee information, change role, department, or deactivate access.
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
                                        <IconLoader2 size={19} color="#fff" className="animate-spin" />
                                        Saving...
                                    </>
                                ) : (
                                    <>
                                        <IconDeviceFloppy size={19} color="#fff" />
                                        Save Changes
                                    </>
                                )}
                            </button>
                        </div>
                    </div>

                    {/* --- FORM SECTION --- */}
                    <section className="form-card">
                        <h2 className="card-title">User Information</h2>

                        {/* Thay vì dùng Map khó đọc, ta viết rõ từng trường nhập liệu */}
                        <div className="form-grid">

                            {/* Full Name */}
                            <div className="form-group">
                                <label htmlFor="fullName" className="form-label">Full Name</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconUser size={18} color="#64708f" /></span>
                                    <input id="fullName" name="fullName" type="text" value={user.fullName} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon" />
                                </div>
                            </div>

                            {/* Phone Number */}
                            <div className="form-group">
                                <label htmlFor="phoneNumber" className="form-label">Phone Number</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconPhone size={18} color="#64708f" /></span>
                                    <input id="phoneNumber" name="phoneNumber" type="text" value={user.phoneNumber} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon" />
                                </div>
                            </div>

                            {/* Email Address */}
                            <div className="form-group">
                                <label htmlFor="email" className="form-label">Email Address</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconMail size={18} color="#64708f" /></span>
                                    <input id="email" name="email" type="email" value={user.email} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon" />
                                </div>
                            </div>

                            {/* Employee ID */}
                            <div className="form-group">
                                <label htmlFor="employeeId" className="form-label">Employee ID</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconId size={18} color="#64708f" /></span>
                                    <input id="employeeId" name="employeeId" type="text" value={user.employeeId} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon" />
                                </div>
                            </div>

                            {/* Department */}
                            <div className="form-group">
                                <label htmlFor="department" className="form-label">Department</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconBuilding size={18} color="#64708f" /></span>
                                    <select id="department" name="department" value={user.department} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon">
                                        <option value="Legal">Legal</option>
                                        <option value="HR">HR</option>
                                        <option value="Finance">Finance</option>
                                        <option value="Sales">Sales</option>
                                    </select>
                                    <span className="right-icon"><IconChevronDown size={18} color="#243452" /></span>
                                </div>
                            </div>

                            {/* Start Date */}
                            <div className="form-group">
                                <label htmlFor="startDate" className="form-label">Start Date</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconCalendar size={18} color="#64708f" /></span>
                                    <input id="startDate" name="startDate" type="text" value={user.startDate} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon" />
                                </div>
                            </div>

                            {/* Role */}
                            <div className="form-group">
                                <label htmlFor="role" className="form-label">Role</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconShieldCheck size={18} color="#64708f" /></span>
                                    <select id="role" name="role" value={user.role} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon">
                                        <option value="Legal Reviewer">Legal Reviewer</option>
                                        <option value="Contract Manager">Contract Manager</option>
                                        <option value="Approver">Approver</option>
                                        <option value="Viewer">Viewer</option>
                                    </select>
                                    <span className="right-icon"><IconChevronDown size={18} color="#243452" /></span>
                                </div>
                            </div>

                            {/* Status */}
                            <div className="form-group">
                                <label htmlFor="status" className="form-label">Status</label>
                                <div className="input-wrap">
                                    <select id="status" name="status" value={user.status} onChange={handleChange} disabled={isSubmitting} className="form-input">
                                        <option value="Active">Active</option>
                                        <option value="Inactive">Inactive</option>
                                        <option value="Deactivated">Deactivated</option>
                                    </select>
                                    <span className="right-icon"><IconChevronDown size={18} color="#243452" /></span>
                                </div>
                            </div>

                            {/* Position */}
                            <div className="form-group">
                                <label htmlFor="position" className="form-label">Position</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconBriefcase size={18} color="#64708f" /></span>
                                    <input id="position" name="position" type="text" value={user.position} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon" />
                                </div>
                            </div>

                            {/* Access Scope */}
                            <div className="form-group">
                                <label htmlFor="accessScope" className="form-label">Access Scope</label>
                                <div className="input-wrap">
                                    <span className="left-icon"><IconLock size={18} color="#64708f" /></span>
                                    <select id="accessScope" name="accessScope" value={user.accessScope} onChange={handleChange} disabled={isSubmitting} className="form-input has-left-icon">
                                        <option value="Department Level Access">Department Level Access</option>
                                        <option value="Full Access">Full Access</option>
                                        <option value="Limited Access">Limited Access</option>
                                    </select>
                                    <span className="right-icon"><IconChevronDown size={18} color="#243452" /></span>
                                </div>
                            </div>

                        </div>

                        {/* --- DEACTIVATE ACCOUNT TOGGLE --- */}
                        <div className="deactivate-row">
                            <label className="switch-label">
                                <input
                                    type="checkbox"
                                    name="deactivateAccount"
                                    checked={user.deactivateAccount}
                                    onChange={handleChange}
                                    disabled={isSubmitting}
                                    className="toggle-checkbox"
                                />
                                <span className={`toggle-track ${user.deactivateAccount ? 'active' : ''}`}>
                                    <span className="toggle-thumb" />
                                </span>
                            </label>
                            <div>
                                <h3 className="deactivate-title">Deactivate Account</h3>
                                <p className="deactivate-text">
                                    Revoke this user's access to the system. This action can be reversed later if needed.
                                </p>
                            </div>
                        </div>
                    </section>

                    {/* --- ACCESS PREVIEW SECTION --- */}
                    <section className="form-card">
                        <h2 className="card-title">Access Preview</h2>

                        <div className="preview-grid">
                            {/* Current Role */}
                            <div className="preview-tile">
                                <span className="preview-icon"><IconUserShield size={28} color="#1f4fff" /></span>
                                <div>
                                    <h3 className="preview-title">Current Role</h3>
                                    <p className="preview-text">Legal Reviewer</p>
                                    <span className="preview-chip">Approval Level: L2</span>
                                </div>
                            </div>

                            {/* Department Access */}
                            <div className="preview-tile">
                                <span className="preview-icon"><IconBuilding size={28} color="#1f4fff" /></span>
                                <div>
                                    <h3 className="preview-title">Department Access</h3>
                                    <p className="preview-text">Legal Department</p>
                                    <span className="preview-chip">12 Modules</span>
                                </div>
                            </div>

                            {/* Approval Workflow */}
                            <div className="preview-tile">
                                <span className="preview-icon"><IconGitMerge size={28} color="#1f4fff" /></span>
                                <div>
                                    <h3 className="preview-title">Approval Workflow</h3>
                                    <p className="preview-text">Can review & approve</p>
                                    <span className="preview-chip">Level 2 Access</span>
                                </div>
                            </div>

                            {/* Audit Trail */}
                            <div className="preview-tile">
                                <span className="preview-icon"><IconClipboardList size={28} color="#1f4fff" /></span>
                                <div>
                                    <h3 className="preview-title">Audit Trail</h3>
                                    <p className="preview-text">All actions are logged</p>
                                    <span className="preview-chip">Full Visibility</span>
                                </div>
                            </div>
                        </div>

                        <div className="info-alert">
                            <IconInfoCircle size={20} />
                            Please review all changes carefully before saving. Updates will take effect immediately after confirmation.
                        </div>
                    </section>
                </form>
            </main>
        </div>
    );
}

export default UpdateUser;