import { useState } from "react";
import { useNavigate } from "react-router-dom";
import NavDropdown from "react-bootstrap/NavDropdown";

// Import Icon chuẩn từ Tabler Icons
import {
    IconWorld,
    IconDeviceFloppy, // Icon Save
    IconBuilding,
    IconCheck,
    IconCalendar,
    IconFileDescription, // Icon Document
    IconEdit,
    IconShieldCheck,
    IconSignature,
    IconInfoCircle,
    IconLoader2
} from "@tabler/icons-react";

// Import CSS
import "../../assets/styles/css/companyProfileStyles/UpdateCompanyProfilePage.css";

// Dữ liệu mẫu (sẽ thay bằng API lấy dữ liệu thực tế)
const defaultCompanyProfile = {
    companyName: "ABC Holdings Co., Ltd.",
    email: "legal@abcholdings.vn",
    taxCode: "0312345678",
    phone: "+84 28 3822 5678",
    registeredAddress: "125 Nguyen Hue Boulevard, Ben Nghe Ward, District 1, Ho Chi Minh City, Vietnam",
    businessRegistrationNumber: "BRN-2025-00981",
    legalRepresentative: "Nguyen Minh An",
    registrationDate: "May 12, 2020",
};

function UpdateProfile({ initialProfile = defaultCompanyProfile, onSaveProfile, onCancel }) {
    const navigate = useNavigate();
    const [profile, setProfile] = useState(initialProfile);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setProfile((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleCancel = () => {
        if (onCancel) {
            onCancel();
            return;
        }
        navigate("/company-profile/view"); // Đổi đường dẫn về trang view nếu cần
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setIsSubmitting(true);

        try {
            // Giả lập API call mất 1.5s
            await new Promise((resolve) => setTimeout(resolve, 1500));

            // Nếu có hàm onSaveProfile từ cha truyền xuống
            onSaveProfile?.(profile);

            alert("Cập nhật hồ sơ công ty thành công!");
            navigate("/company-profile/view");

        } catch (error) {
            console.error("Lỗi cập nhật:", error);
            alert("Có lỗi xảy ra, vui lòng thử lại!");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="update-profile-page">
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
                    <section className="panel-header">
                        <div>
                            <h1 className="page-title">Update Company Profile</h1>
                            <p className="page-description">
                                Edit your company's legal identity information to keep contracts and documents accurate.
                            </p>
                        </div>
                        <div className="form-actions">
                            <button
                                type="button"
                                onClick={handleCancel}
                                className="btn-cancel"
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
                                        <IconLoader2 size={20} color="#ffffff" className="animate-spin" />
                                        <span>Saving...</span>
                                    </>
                                ) : (
                                    <>
                                        <IconDeviceFloppy size={20} color="#ffffff" />
                                        <span>Save Changes</span>
                                    </>
                                )}
                            </button>
                        </div>
                    </section>

                    {/* --- COMPANY LEGAL INFORMATION FORM --- */}
                    <section className="form-card">
                        <div className="card-header">
                            <div className="card-title-group">
                                <IconBuilding size={32} color="#0f48ff" />
                                <h2 className="card-title">Company Legal Information</h2>
                            </div>
                            <div className="verified-badge">
                                <IconCheck size={18} color="#13a538" strokeWidth={2} />
                                <span>Verified Data</span>
                            </div>
                        </div>

                        <div className="form-grid">

                            {/* Company Name */}
                            <div className="form-group">
                                <label htmlFor="companyName" className="form-label">
                                    Company Name <span className="text-required">*</span>
                                </label>
                                <div className="input-wrapper">
                                    <input id="companyName" name="companyName" type="text" value={profile.companyName} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                </div>
                                <p className="helper-text">The official registered name of your company.</p>
                            </div>

                            {/* Email */}
                            <div className="form-group">
                                <label htmlFor="email" className="form-label">
                                    Email <span className="text-required">*</span>
                                </label>
                                <div className="input-wrapper">
                                    <input id="email" name="email" type="email" value={profile.email} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                </div>
                            </div>

                            {/* Tax Code */}
                            <div className="form-group">
                                <label htmlFor="taxCode" className="form-label">
                                    Tax Code (MST) <span className="text-required">*</span>
                                </label>
                                <div className="input-wrapper">
                                    <input id="taxCode" name="taxCode" type="text" value={profile.taxCode} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                </div>
                                <p className="helper-text">Enter your 10-digit Tax Code as issued by the tax authority.</p>
                            </div>

                            {/* Phone */}
                            <div className="form-group">
                                <label htmlFor="phone" className="form-label">
                                    Phone <span className="text-required">*</span>
                                </label>
                                <div className="input-wrapper">
                                    <input id="phone" name="phone" type="tel" value={profile.phone} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                </div>
                            </div>

                            {/* Registered Address */}
                            <div className="form-group">
                                <label htmlFor="registeredAddress" className="form-label">
                                    Registered Address <span className="text-required">*</span>
                                </label>
                                <div className="input-wrapper">
                                    <textarea id="registeredAddress" name="registeredAddress" value={profile.registeredAddress} onChange={handleChange} rows={2} required disabled={isSubmitting} className="form-input form-textarea" />
                                </div>
                                <p className="helper-text">Enter the full registered address as shown on your business license.</p>
                            </div>

                            {/* Business Registration No */}
                            <div className="form-group">
                                <label htmlFor="businessRegistrationNumber" className="form-label">
                                    Business Registration No. <span className="text-required">*</span>
                                </label>
                                <div className="input-wrapper">
                                    <input id="businessRegistrationNumber" name="businessRegistrationNumber" type="text" value={profile.businessRegistrationNumber} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                </div>
                                <p className="helper-text">Your company's business registration number.</p>
                            </div>

                            {/* Legal Representative */}
                            <div className="form-group">
                                <label htmlFor="legalRepresentative" className="form-label">
                                    Legal Representative <span className="text-required">*</span>
                                </label>
                                <div className="input-wrapper">
                                    <input id="legalRepresentative" name="legalRepresentative" type="text" value={profile.legalRepresentative} onChange={handleChange} required disabled={isSubmitting} className="form-input" />
                                </div>
                                <p className="helper-text">Full name of the legal representative.</p>
                            </div>

                            {/* Registration Date */}
                            <div className="form-group">
                                <label htmlFor="registrationDate" className="form-label">
                                    Registration Date <span className="text-required">*</span>
                                </label>
                                <div className="input-wrapper">
                                    {/* Dùng type="text" kết hợp icon lịch để giao diện giống hệt phiên bản cũ */}
                                    <input id="registrationDate" name="registrationDate" type="text" value={profile.registrationDate} onChange={handleChange} required disabled={isSubmitting} className="form-input has-right-icon" />
                                    <span className="input-icon">
                                        <IconCalendar size={18} />
                                    </span>
                                </div>
                                <p className="helper-text">Date of business registration.</p>
                            </div>

                        </div>
                    </section>

                    {/* --- AUTOMATION PREVIEW --- */}
                    <section className="automation-card">
                        <div className="automation-header">
                            <IconFileDescription size={32} color="#0f48ff" />
                            <div>
                                <h2 className="card-title">Document Automation Preview</h2>
                                <p className="automation-description">
                                    Updated company details will be reflected in contract templates and generated legal documents after saving.
                                </p>
                            </div>
                        </div>

                        <div className="preview-grid">

                            {/* Contract Templates */}
                            <div className="preview-tile">
                                <div className="preview-icon-wrap">
                                    <IconFileDescription size={24} />
                                </div>
                                <div>
                                    <h3 className="preview-title">Contract Templates</h3>
                                    <p className="preview-description">Auto-fill company details</p>
                                </div>
                            </div>

                            {/* Generated Documents */}
                            <div className="preview-tile">
                                <div className="preview-icon-wrap">
                                    <IconEdit size={24} />
                                </div>
                                <div>
                                    <h3 className="preview-title">Generated Documents</h3>
                                    <p className="preview-description">Quotes, agreements, reports</p>
                                </div>
                            </div>

                            {/* Compliance */}
                            <div className="preview-tile">
                                <div className="preview-icon-wrap">
                                    <IconShieldCheck size={24} />
                                </div>
                                <div>
                                    <h3 className="preview-title">Compliance</h3>
                                    <p className="preview-description">Accurate legal information</p>
                                </div>
                            </div>

                            {/* Digital Signatures */}
                            <div className="preview-tile">
                                <div className="preview-icon-wrap">
                                    <IconSignature size={24} />
                                </div>
                                <div>
                                    <h3 className="preview-title">Digital Signatures</h3>
                                    <p className="preview-description">Verified company identity</p>
                                </div>
                            </div>

                        </div>
                    </section>

                    {/* --- INFO ALERT --- */}
                    <section className="info-alert">
                        <IconInfoCircle size={20} color="#2455d9" />
                        <div>
                            <p className="info-title">Review all information carefully before saving changes.</p>
                            <p className="info-description">Unsaved changes will be lost if you leave this page.</p>
                        </div>
                    </section>

                </form>
            </main>
        </div>
    );
}

export default UpdateProfile;