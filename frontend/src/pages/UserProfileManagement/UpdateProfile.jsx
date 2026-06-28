import { useState } from "react";
import { useNavigate } from "react-router-dom";

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

const formFields = [
    {
        label: "Full Name",
        name: "fullName",
        helperText: "Display name used in the system.",
    },
    {
        label: "Email",
        name: "email",
        type: "email",
        helperText: "Work email for system notifications.",
    },
    {
        label: "Phone Number",
        name: "phoneNumber",
        type: "tel",
    },
    {
        label: "Department",
        name: "department",
        as: "select",
        options: ["Legal Department", "Sales Department", "Finance Department"],
    },
    {
        label: "Position",
        name: "position",
        as: "select",
        options: ["Contract Manager", "Legal Specialist", "Department Manager"],
    },
    {
        label: "Employee ID",
        name: "employeeId",
        disabled: true,
    },
    {
        label: "Time Zone",
        name: "timeZone",
        as: "select",
        options: [
            "(GMT+07:00) Bangkok, Hanoi, Jakarta",
            "(GMT+08:00) Singapore, Kuala Lumpur",
            "(GMT+09:00) Tokyo, Seoul",
        ],
    },
    {
        label: "Language",
        name: "language",
        as: "select",
        options: ["English", "Vietnamese"],
    },
];

const usagePreviewItems = [
    {
        title: "Contracts",
        description: "Profile details appear on owned contracts.",
        icon: "document",
    },
    {
        title: "Approvals",
        description: "Name and position show in approval flows.",
        icon: "shield",
    },
    {
        title: "Department",
        description: "Department controls workflow routing.",
        icon: "building",
    },
    {
        title: "Notifications",
        description: "Email and phone receive system alerts.",
        icon: "clock",
    },
];

function Icon({ name, size = 22, color = "#2055ff" }) {
    const props = {
        width: size,
        height: size,
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: color,
        strokeWidth: 2,
        strokeLinecap: "round",
        strokeLinejoin: "round",
        "aria-hidden": true,
    };

    const paths = {
        edit: (
            <>
                <path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z" />
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
        building: (
            <>
                <path d="M4 21h16" />
                <path d="M6 21V5h7v16" />
                <path d="M13 9h5v12" />
                <path d="M9 9h1" />
                <path d="M9 13h1" />
                <path d="M16 13h1" />
            </>
        ),
        clock: (
            <>
                <circle cx="12" cy="12" r="9" />
                <path d="M12 7v5l3 3" />
            </>
        ),
        info: (
            <>
                <circle cx="12" cy="12" r="9" />
                <path d="M12 11v5" />
                <path d="M12 8h.01" />
            </>
        ),
    };

    return <svg {...props}>{paths[name]}</svg>;
}

function UpdateProfile({ onSaveProfile }) {
    const navigate = useNavigate();
    const [userProfile, setUserProfile] = useState(initialUserProfile);

    const handleChange = (event) => {
        const { name, value } = event.target;

        setUserProfile((currentProfile) => ({
            ...currentProfile,
            [name]: value,
        }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        onSaveProfile?.(userProfile);
    };

    const renderField = (field) => {
        const fieldProps = {
            id: field.name,
            name: field.name,
            value: userProfile[field.name],
            onChange: handleChange,
            required: !field.disabled,
            disabled: field.disabled,
            style: {
                ...styles.input,
                ...(field.disabled ? styles.disabledInput : {}),
            },
        };

        return (
            <div key={field.name} style={styles.formGroup}>
                <label htmlFor={field.name} style={styles.label}>
                    {field.label}
                    {!field.disabled && <span style={styles.required}> *</span>}
                </label>
                {field.as === "select" ? (
                    <select {...fieldProps}>
                        {field.options.map((option) => (
                            <option key={option} value={option}>
                                {option}
                            </option>
                        ))}
                    </select>
                ) : (
                    <input type={field.type || "text"} {...fieldProps} />
                )}
                {field.helperText && (
                    <p style={styles.helperText}>{field.helperText}</p>
                )}
            </div>
        );
    };

    return (
        <main style={styles.page}>
            <form style={styles.panel} onSubmit={handleSubmit}>
                <div style={styles.header}>
                    <div>
                        <h1 style={styles.pageTitle}>Update Profile</h1>
                        <p style={styles.pageDescription}>
                            Update your personal information and account preferences.
                        </p>
                    </div>
                    <div style={styles.actions}>
                        <button
                            type="button"
                            style={styles.cancelButton}
                            onClick={() => navigate("/user-profile/view")}
                        >
                            Cancel
                        </button>
                        <button type="submit" style={styles.primaryButton}>
                            <Icon name="save" size={18} color="#ffffff" />
                            Save Changes
                        </button>
                    </div>
                </div>

                <section style={styles.card}>
                    <div style={styles.cardHeader}>
                        <div style={styles.cardTitleGroup}>
                            <span style={styles.roundIcon}>
                                <Icon name="edit" size={22} />
                            </span>
                            <h2 style={styles.cardTitle}>Profile Information</h2>
                        </div>
                        <span style={styles.statusBadge}>Verified Profile</span>
                    </div>

                    <div style={styles.profileBody}>
                        <div style={styles.avatarBlock}>
                            <div style={styles.avatarWrap}>
                                <div style={styles.avatar}>AM</div>
                                <div style={styles.avatarBadge}>
                                    <Icon name="document" size={17} color="#53617e" />
                                </div>
                            </div>
                            <button type="button" style={styles.avatarButton}>
                                Change Avatar
                            </button>
                            <p style={styles.avatarHelper}>
                                Used across profile and approvals.
                            </p>
                        </div>

                        <div style={styles.formGrid}>{formFields.map(renderField)}</div>
                    </div>
                </section>

                <section style={styles.card}>
                    <div style={styles.usageHeader}>
                        <span style={styles.roundIcon}>
                            <Icon name="shield" size={22} />
                        </span>
                        <div>
                            <h2 style={styles.cardTitle}>Profile Usage Preview</h2>
                            <p style={styles.cardDescription}>
                                Changes will be reflected across contract templates,
                                approvals, and notifications after saving.
                            </p>
                        </div>
                    </div>

                    <div style={styles.usageGrid}>
                        {usagePreviewItems.map((item) => (
                            <div key={item.title} style={styles.usageItem}>
                                <span style={styles.usageIcon}>
                                    <Icon name={item.icon} size={22} />
                                </span>
                                <div>
                                    <h3 style={styles.usageTitle}>{item.title}</h3>
                                    <p style={styles.usageDescription}>
                                        {item.description}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>

                <section style={styles.infoAlert}>
                    <Icon name="info" size={22} />
                    <span>
                        Review all changes carefully before saving. Updated profile
                        data will apply to future documents.
                    </span>
                </section>
            </form>
        </main>
    );
}

const styles = {
    page: {
        minHeight: "100vh",
        background: "#f6f8fc",
        padding: "24px 32px",
        color: "#111827",
        fontFamily:
            "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
    },
    panel: {
        maxWidth: 1150,
        margin: "0 auto",
        background: "#ffffff",
        border: "1px solid #dce4f0",
        borderRadius: 16,
        boxShadow: "0 8px 24px rgba(31, 41, 55, 0.06)",
        overflow: "hidden",
    },
    header: {
        minHeight: 98,
        borderBottom: "1px solid #e6ebf3",
        padding: "26px 32px",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: 24,
    },
    pageTitle: {
        margin: 0,
        fontSize: 29,
        fontWeight: 800,
        letterSpacing: 0,
    },
    pageDescription: {
        margin: "4px 0 0",
        color: "#51607f",
        fontSize: 16,
    },
    actions: {
        display: "flex",
        gap: 12,
    },
    cancelButton: {
        height: 42,
        minWidth: 96,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        color: "#111827",
        fontSize: 15,
        cursor: "pointer",
    },
    primaryButton: {
        height: 42,
        minWidth: 158,
        border: 0,
        borderRadius: 7,
        background: "#2450f5",
        color: "#ffffff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: 8,
        fontSize: 15,
        cursor: "pointer",
    },
    card: {
        margin: "22px 32px",
        border: "1px solid #d9e2ef",
        borderRadius: 9,
        overflow: "hidden",
        background: "#ffffff",
    },
    cardHeader: {
        minHeight: 96,
        borderBottom: "1px solid #e6ebf3",
        padding: "0 28px 0 38px",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: 24,
    },
    cardTitleGroup: {
        display: "flex",
        alignItems: "center",
        gap: 12,
    },
    roundIcon: {
        width: 42,
        height: 42,
        borderRadius: "50%",
        background: "#eef3ff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        flexShrink: 0,
    },
    cardTitle: {
        margin: 0,
        fontSize: 22,
        fontWeight: 800,
    },
    cardDescription: {
        margin: "2px 0 0",
        color: "#52617f",
        fontSize: 15,
    },
    statusBadge: {
        minWidth: 88,
        minHeight: 35,
        borderRadius: 7,
        border: "1px solid #a8e0bb",
        background: "#eefbf3",
        color: "#108139",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: 14,
        fontWeight: 800,
    },
    profileBody: {
        display: "grid",
        gridTemplateColumns: "230px 1fr",
        gap: 38,
        padding: "16px 38px 26px",
    },
    avatarBlock: {
        borderRight: "1px solid #e3e9f2",
        minHeight: 368,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        paddingRight: 38,
    },
    avatarWrap: {
        position: "relative",
        marginBottom: 32,
    },
    avatar: {
        width: 100,
        height: 100,
        borderRadius: "50%",
        background: "#244ff5",
        color: "#ffffff",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: 36,
        fontWeight: 500,
    },
    avatarBadge: {
        position: "absolute",
        right: -6,
        bottom: 4,
        width: 25,
        height: 25,
        borderRadius: "50%",
        background: "#ffffff",
        border: "1px solid #cad4e4",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
    },
    avatarButton: {
        height: 38,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        color: "#111827",
        fontSize: 15,
        cursor: "pointer",
    },
    avatarHelper: {
        margin: "16px 0 0",
        color: "#74819b",
        fontSize: 13,
    },
    formGrid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        columnGap: 48,
        rowGap: 24,
        alignContent: "center",
    },
    formGroup: {
        minWidth: 0,
    },
    label: {
        display: "block",
        marginBottom: 6,
        color: "#111827",
        fontSize: 14,
    },
    required: {
        color: "#d92828",
    },
    input: {
        width: "100%",
        height: 44,
        border: "1px solid #d5deeb",
        borderRadius: 6,
        padding: "0 14px",
        color: "#111827",
        fontSize: 15,
        background: "#ffffff",
        outline: "none",
    },
    disabledInput: {
        background: "#f6f8fb",
        color: "#52617f",
    },
    helperText: {
        margin: "7px 0 0",
        color: "#74819b",
        fontSize: 13,
    },
    usageHeader: {
        display: "flex",
        alignItems: "center",
        gap: 12,
        padding: "26px 38px 8px",
    },
    usageGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(4, 1fr)",
        gap: 18,
        padding: "6px 22px 22px",
    },
    usageItem: {
        border: "1px solid #d9e2ef",
        borderRadius: 8,
        minHeight: 65,
        padding: "10px 16px",
        display: "flex",
        alignItems: "center",
        gap: 14,
    },
    usageIcon: {
        width: 42,
        height: 42,
        borderRadius: "50%",
        background: "#eef3ff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        flexShrink: 0,
    },
    usageTitle: {
        margin: 0,
        fontSize: 16,
        fontWeight: 800,
    },
    usageDescription: {
        margin: "4px 0 0",
        color: "#52617f",
        fontSize: 13,
        lineHeight: 1.35,
    },
    infoAlert: {
        margin: "8px 32px 20px",
        minHeight: 58,
        borderRadius: 8,
        border: "1px solid #b9ceff",
        background: "#edf4ff",
        color: "#53617e",
        display: "flex",
        alignItems: "center",
        gap: 14,
        padding: "0 22px",
        fontSize: 15,
    },
};

export default UpdateProfile;
