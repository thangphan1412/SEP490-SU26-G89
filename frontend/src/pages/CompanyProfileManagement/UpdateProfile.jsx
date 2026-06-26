import { useState } from "react";
import { useNavigate } from "react-router-dom";

const defaultCompanyProfile = {
    companyName: "ABC Holdings Co., Ltd.",
    email: "legal@abcholdings.vn",
    taxCode: "0312345678",
    phone: "+84 28 3822 5678",
    registeredAddress:
        "125 Nguyen Hue Boulevard, Ben Nghe Ward, District 1, Ho Chi Minh City, Vietnam",
    businessRegistrationNumber: "BRN-2025-00981",
    legalRepresentative: "Nguyen Minh An",
    registrationDate: "May 12, 2020",
};

const formFields = [
    {
        label: "Company Name",
        name: "companyName",
        helperText: "The official registered name of your company.",
    },
    {
        label: "Email",
        name: "email",
        type: "email",
    },
    {
        label: "Tax Code (MST)",
        name: "taxCode",
        helperText: "Enter your 10-digit Tax Code as issued by the tax authority.",
    },
    {
        label: "Phone",
        name: "phone",
        type: "tel",
    },
    {
        label: "Registered Address",
        name: "registeredAddress",
        as: "textarea",
        helperText:
            "Enter the full registered address as shown on your business license.",
    },
    {
        label: "Business Registration No.",
        name: "businessRegistrationNumber",
        helperText: "Your company's business registration number.",
    },
    {
        label: "Legal Representative",
        name: "legalRepresentative",
        helperText: "Full name of the legal representative.",
    },
    {
        label: "Registration Date",
        name: "registrationDate",
        icon: "calendar",
        helperText: "Date of business registration.",
    },
];

const automationPreviewItems = [
    {
        title: "Contract Templates",
        description: "Auto-fill company details",
        icon: "document",
    },
    {
        title: "Generated Documents",
        description: "Quotes, agreements, reports",
        icon: "edit",
    },
    {
        title: "Compliance",
        description: "Accurate legal information",
        icon: "shield",
    },
    {
        title: "Digital Signatures",
        description: "Verified company identity",
        icon: "signature",
    },
];

function Icon({ name, size = 22, color = "#1f4fff", strokeWidth = 2.2 }) {
    const commonProps = {
        width: size,
        height: size,
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: color,
        strokeWidth,
        strokeLinecap: "round",
        strokeLinejoin: "round",
        "aria-hidden": true,
    };

    const paths = {
        building: (
            <>
                <path d="M4 21h16" />
                <path d="M6 21V5h7v16" />
                <path d="M13 9h5v12" />
                <path d="M9 9h1" />
                <path d="M9 13h1" />
                <path d="M9 17h1" />
                <path d="M16 13h1" />
                <path d="M16 17h1" />
            </>
        ),
        edit: (
            <>
                <path d="M4 20h4l11-11a2.8 2.8 0 0 0-4-4L4 16z" />
                <path d="m13.5 6.5 4 4" />
            </>
        ),
        save: (
            <>
                <path d="M5 3h12l2 2v16H5z" />
                <path d="M8 3v6h8V3" />
                <path d="M8 21v-7h8v7" />
            </>
        ),
        calendar: (
            <>
                <path d="M5 4h14v16H5z" />
                <path d="M8 2v4" />
                <path d="M16 2v4" />
                <path d="M5 9h14" />
            </>
        ),
        document: (
            <>
                <path d="M7 3h7l4 4v14H7z" />
                <path d="M14 3v5h5" />
                <path d="M10 13h5" />
                <path d="M10 17h4" />
            </>
        ),
        shield: (
            <>
                <path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" />
                <path d="m9 12 2 2 4-4" />
            </>
        ),
        signature: (
            <>
                <path d="M4 18c4-6 5-8 6-8 2 0 0 5 2 5 1.5 0 2.5-3 4-3 1 0 1.5 1 2 2" />
                <path d="M4 21h16" />
            </>
        ),
        info: (
            <>
                <circle cx="12" cy="12" r="9" />
                <path d="M12 11v5" />
                <path d="M12 8h.01" />
            </>
        ),
        check: (
            <>
                <circle cx="12" cy="12" r="9" />
                <path d="m8 12 2.5 2.5L16 9" />
            </>
        ),
    };

    return <svg {...commonProps}>{paths[name]}</svg>;
}

function UpdateProfile({
                           initialProfile = defaultCompanyProfile,
                           onSaveProfile,
                           onCancel,
                       }) {
    const navigate = useNavigate();
    const [companyProfile, setCompanyProfile] = useState(initialProfile);

    const handleChange = (event) => {
        const { name, value } = event.target;

        setCompanyProfile((currentProfile) => ({
            ...currentProfile,
            [name]: value,
        }));
    };

    const handleCancel = () => {
        if (onCancel) {
            onCancel();
            return;
        }

        navigate("/company-profile/view");
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        onSaveProfile?.(companyProfile);
    };

    const renderField = (field) => {
        const fieldValue = companyProfile[field.name];

        return (
            <div key={field.name} style={styles.formGroup}>
                <label htmlFor={field.name} style={styles.label}>
                    {field.label} <span style={styles.required}>*</span>
                </label>
                <div style={styles.inputWrapper}>
                    {field.as === "textarea" ? (
                        <textarea
                            id={field.name}
                            name={field.name}
                            value={fieldValue}
                            onChange={handleChange}
                            rows={2}
                            required
                            style={{ ...styles.input, ...styles.textarea }}
                        />
                    ) : (
                        <input
                            id={field.name}
                            name={field.name}
                            type={field.type || "text"}
                            value={fieldValue}
                            onChange={handleChange}
                            required
                            style={{
                                ...styles.input,
                                paddingRight: field.icon ? 48 : 18,
                            }}
                        />
                    )}
                    {field.icon && (
                        <span style={styles.inputIcon}>
                            <Icon name={field.icon} size={18} color="#64708f" />
                        </span>
                    )}
                </div>
                {field.helperText && (
                    <p style={styles.helperText}>{field.helperText}</p>
                )}
            </div>
        );
    };

    return (
        <form onSubmit={handleSubmit} style={styles.panel}>
            {/* Header của vùng nội dung chính */}
            <section style={styles.pageHeader}>
                <div>
                    <h1 style={styles.pageTitle}>Update Company Profile</h1>
                    <p style={styles.pageDescription}>
                        Edit your company&apos;s legal identity information
                        to keep contracts and documents accurate.
                    </p>
                </div>
                <div style={styles.actionGroup}>
                    <button
                        type="button"
                        onClick={handleCancel}
                        style={styles.cancelButton}
                    >
                        Cancel
                    </button>
                    <button type="submit" style={styles.saveButton}>
                        <Icon name="save" size={20} color="#ffffff" />
                        <span>Save Changes</span>
                    </button>
                </div>
            </section>

            {/* Khối Form thông tin chi tiết */}
            <section style={styles.card}>
                <div style={styles.cardHeader}>
                    <div style={styles.cardTitleGroup}>
                        <Icon name="building" size={32} color="#0f48ff" />
                        <h2 style={styles.cardTitle}>
                            Company Legal Information
                        </h2>
                    </div>
                    <div style={styles.verifiedBadge}>
                        <Icon
                            name="check"
                            size={18}
                            color="#13a538"
                            strokeWidth={2}
                        />
                        <span>Verified Data</span>
                    </div>
                </div>

                <div style={styles.formGrid}>
                    {formFields.map(renderField)}
                </div>
            </section>

            {/* Khối Xem trước tài liệu tự động điền */}
            <section style={styles.automationCard}>
                <div style={styles.automationHeader}>
                    <Icon name="document" size={32} color="#0f48ff" />
                    <div>
                        <h2 style={styles.cardTitle}>
                            Document Automation Preview
                        </h2>
                        <p style={styles.automationDescription}>
                            Updated company details will be reflected in
                            contract templates and generated legal documents
                            after saving.
                        </p>
                    </div>
                </div>

                <div style={styles.previewGrid}>
                    {automationPreviewItems.map((item) => (
                        <div key={item.title} style={styles.previewTile}>
                            <div style={styles.previewIconWrap}>
                                <Icon
                                    name={item.icon}
                                    size={24}
                                    color="#0f48ff"
                                />
                            </div>
                            <div>
                                <h3 style={styles.previewTitle}>
                                    {item.title}
                                </h3>
                                <p style={styles.previewDescription}>
                                    {item.description}
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            {/* Alert thông báo cảnh báo ở cuối */}
            <section style={styles.infoAlert}>
                <Icon name="info" size={20} color="#2455d9" />
                <div>
                    <p style={styles.infoTitle}>
                        Review all information carefully before saving
                        changes.
                    </p>
                    <p style={styles.infoDescription}>
                        Unsaved changes will be lost if you leave this page.
                    </p>
                </div>
            </section>
        </form>
    );
}

const styles = {
    panel: {
        background: "#ffffff",
        border: "1px solid #e4e9f2",
        borderRadius: 14,
        boxShadow: "0 8px 24px rgba(22, 32, 61, 0.06)",
        overflow: "hidden",
        width: "100%",
        fontFamily: "Inter, ui-sans-serif, system-ui, sans-serif",
    },
    pageHeader: {
        minHeight: 104,
        borderBottom: "1px solid #e5ebf4",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: 24,
        padding: "24px 30px",
    },
    pageTitle: {
        margin: 0,
        fontSize: 26,
        fontWeight: 800,
        letterSpacing: 0,
        color: "#111b3d",
    },
    pageDescription: {
        margin: "8px 0 0",
        color: "#64708f",
        fontSize: 15,
    },
    actionGroup: {
        display: "flex",
        gap: 14,
        alignItems: "center",
    },
    cancelButton: {
        height: 44,
        minWidth: 94,
        borderRadius: 7,
        border: "1px solid #d7deea",
        background: "#ffffff",
        color: "#111b3d",
        fontSize: 15,
        fontWeight: 700,
        cursor: "pointer",
    },
    saveButton: {
        height: 44,
        minWidth: 172,
        borderRadius: 7,
        border: "1px solid #174aff",
        background: "linear-gradient(135deg, #184cff, #1f47df)",
        color: "#ffffff",
        fontSize: 15,
        fontWeight: 700,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: 10,
        cursor: "pointer",
        boxShadow: "0 10px 20px rgba(31, 79, 255, 0.22)",
    },
    card: {
        margin: "24px 30px 22px",
        border: "1px solid #dfe6f1",
        borderRadius: 10,
        padding: "24px 24px 16px",
    },
    cardHeader: {
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        marginBottom: 24,
    },
    cardTitleGroup: {
        display: "flex",
        alignItems: "center",
        gap: 16,
    },
    cardTitle: {
        margin: 0,
        fontSize: 18,
        fontWeight: 800,
        color: "#142046",
    },
    verifiedBadge: {
        display: "inline-flex",
        alignItems: "center",
        gap: 8,
        minHeight: 30,
        padding: "0 13px",
        borderRadius: 6,
        border: "1px solid #a8e5bb",
        background: "#ecfff2",
        color: "#149335",
        fontSize: 13,
        fontWeight: 700,
    },
    formGrid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        columnGap: 42,
        rowGap: 22,
    },
    formGroup: {
        minWidth: 0,
    },
    label: {
        display: "block",
        marginBottom: 7,
        color: "#54617f",
        fontSize: 13,
        fontWeight: 700,
    },
    required: {
        color: "#e9584f",
    },
    inputWrapper: {
        position: "relative",
    },
    input: {
        width: "100%",
        minHeight: 42,
        border: "1px solid #d4dce9",
        borderRadius: 7,
        padding: "9px 14px",
        color: "#111b3d",
        fontSize: 15,
        fontWeight: 600,
        outline: "none",
        background: "#ffffff",
        boxShadow: "inset 0 1px 2px rgba(21, 31, 54, 0.03)",
    },
    textarea: {
        resize: "vertical",
        lineHeight: 1.35,
    },
    inputIcon: {
        position: "absolute",
        right: 15,
        top: "50%",
        transform: "translateY(-50%)",
        display: "flex",
        alignItems: "center",
    },
    helperText: {
        margin: "8px 0 0",
        color: "#667392",
        fontSize: 12,
        lineHeight: 1.4,
    },
    automationCard: {
        margin: "0 30px 22px",
        border: "1px solid #dfe6f1",
        borderRadius: 10,
        padding: "22px 24px 20px",
    },
    automationHeader: {
        display: "flex",
        alignItems: "flex-start",
        gap: 16,
        marginBottom: 18,
    },
    automationDescription: {
        margin: "6px 0 0",
        color: "#667392",
        fontSize: 14,
    },
    previewGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(4, 1fr)",
        gap: 28,
    },
    previewTile: {
        border: "1px solid #dfe6f1",
        borderRadius: 8,
        minHeight: 66,
        padding: "12px 14px",
        display: "flex",
        alignItems: "center",
        gap: 14,
        background: "#ffffff",
    },
    previewIconWrap: {
        width: 42,
        height: 42,
        borderRadius: "50%",
        background: "#eef3ff",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        flexShrink: 0,
    },
    previewTitle: {
        margin: "0 0 5px",
        color: "#111b3d",
        fontSize: 13,
        fontWeight: 800,
    },
    previewDescription: {
        margin: 0,
        color: "#64708f",
        fontSize: 12,
    },
    infoAlert: {
        margin: "0 24px 16px",
        border: "1px solid #c9d9ff",
        borderRadius: 8,
        background: "#f0f5ff",
        minHeight: 64,
        display: "flex",
        alignItems: "center",
        gap: 14,
        padding: "12px 18px",
    },
    infoTitle: {
        margin: "0 0 5px",
        color: "#163d93",
        fontSize: 13,
        fontWeight: 800,
    },
    infoDescription: {
        margin: 0,
        color: "#596987",
        fontSize: 13,
    },
};

export default UpdateProfile;