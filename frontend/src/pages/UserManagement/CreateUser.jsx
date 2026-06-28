import { useState } from "react";
import { useNavigate } from "react-router-dom";

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

const fields = [
    ["Full Name", "fullName", "input", "Enter full name", true],
    ["Email Address", "email", "email", "Enter email address", true],
    ["Initial Password", "initialPassword", "password", "Enter initial password", true],
    ["Confirm Password", "confirmPassword", "password", "Confirm initial password", true],
    ["Department", "department", "select", "Select department", true, ["Legal", "HR", "Finance", "Sales"]],
    ["Role", "role", "select", "Select role", true, ["Contract Manager", "HR Admin", "Approver", "Viewer"]],
    ["Position", "position", "input", "Enter position", false],
    ["Phone Number", "phoneNumber", "phone", "Enter phone number", false],
    ["Employee ID", "employeeId", "input", "Enter employee ID", false],
    ["Start Date", "startDate", "date", "Select start date", true],
    ["Status", "status", "select", "Active", true, ["Active", "Inactive"]],
];

const onboardingItems = [
    ["Role Permissions", "User permissions will be assigned based on the selected role.", "shield", "#eef3ff"],
    ["Department Access", "Access will be limited to data and modules within the selected department.", "building", "#f0edff"],
    ["Email Notification", "A welcome email will be sent with login details and getting started guide.", "mail", "#eafaf0"],
    ["Account Activation", "User account will be activated based on the selected status.", "userCheck", "#fff3e6"],
];

function Icon({ name, size = 22, color = "#1f4fff" }) {
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
        plusUser: (
            <>
                <circle cx="9" cy="8" r="4" />
                <path d="M2 21c1.4-4 3.7-6 7-6 2 0 3.7.7 5 2" />
                <path d="M19 8v6" />
                <path d="M16 11h6" />
            </>
        ),
        eye: (
            <>
                <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12z" />
                <circle cx="12" cy="12" r="3" />
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
        shield: (
            <>
                <path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" />
            </>
        ),
        building: (
            <>
                <path d="M4 21h16" />
                <path d="M6 21V5h7v16" />
                <path d="M13 9h5v12" />
                <path d="M9 9h1" />
                <path d="M16 13h1" />
            </>
        ),
        mail: (
            <>
                <path d="M4 6h16v12H4z" />
                <path d="m4 7 8 6 8-6" />
            </>
        ),
        userCheck: (
            <>
                <circle cx="9" cy="8" r="4" />
                <path d="M2 21c1.4-4 3.7-6 7-6 1.2 0 2.3.3 3.3.8" />
                <path d="m16 17 2 2 4-5" />
            </>
        ),
        info: (
            <>
                <circle cx="12" cy="12" r="9" />
                <path d="M12 11v5" />
                <path d="M12 8h.01" />
            </>
        ),
        chevron: <path d="m8 10 4 4 4-4" />,
    };

    return <svg {...props}>{paths[name]}</svg>;
}

function CreateUser({ onCreateUser }) {
    const navigate = useNavigate();
    const [user, setUser] = useState(initialUser);

    const handleChange = (event) => {
        const { name, value, checked, type } = event.target;
        setUser((currentUser) => ({
            ...currentUser,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        onCreateUser?.(user);
    };

    const renderField = ([label, name, type, placeholder, required, options]) => {
        return (
            <div key={name} style={styles.formGroup}>
                <label htmlFor={name} style={styles.label}>
                    {label}
                    {required && <span style={styles.required}> *</span>}
                </label>
                <div style={styles.inputWrap}>
                    {type === "select" ? (
                        <>
                            <select
                                id={name}
                                name={name}
                                value={user[name]}
                                onChange={handleChange}
                                required={required}
                                style={styles.input}
                            >
                                <option value="">{placeholder}</option>
                                {options.map((option) => (
                                    <option key={option} value={option}>
                                        {option}
                                    </option>
                                ))}
                            </select>
                            <span style={styles.rightIcon}>
                                <Icon name="chevron" size={18} color="#243452" />
                            </span>
                        </>
                    ) : type === "phone" ? (
                        <div style={styles.phoneInput}>
                            <span style={styles.country}>🇺🇸</span>
                            <span style={styles.phoneCode}>+1</span>
                            <input
                                id={name}
                                name={name}
                                value={user[name]}
                                onChange={handleChange}
                                placeholder={placeholder}
                                style={styles.phoneField}
                            />
                        </div>
                    ) : (
                        <>
                            <input
                                id={name}
                                name={name}
                                type={type === "date" ? "text" : type}
                                value={user[name]}
                                onChange={handleChange}
                                placeholder={placeholder}
                                required={required}
                                style={styles.input}
                            />
                            {type === "password" && (
                                <span style={styles.rightIcon}>
                                    <Icon name="eye" size={18} color="#53617e" />
                                </span>
                            )}
                            {type === "date" && (
                                <span style={styles.rightIcon}>
                                    <Icon name="calendar" size={18} color="#53617e" />
                                </span>
                            )}
                        </>
                    )}
                </div>
            </div>
        );
    };

    return (
        <main style={styles.page}>
            <form style={styles.panel} onSubmit={handleSubmit}>
                <div style={styles.header}>
                    <div>
                        <h1 style={styles.pageTitle}>Create User</h1>
                        <p style={styles.pageDescription}>
                            Add a new employee account and assign access permissions.
                        </p>
                    </div>
                    <div style={styles.actions}>
                        <button type="button" style={styles.cancelButton} onClick={() => navigate("/user-management/list")}>
                            Cancel
                        </button>
                        <button type="submit" style={styles.primaryButton}>
                            <Icon name="plusUser" size={21} color="#ffffff" />
                            Create User
                        </button>
                    </div>
                </div>

                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>User Information</h2>
                    <div style={styles.formGrid}>{fields.map(renderField)}</div>
                    <label style={styles.toggleRow}>
                        <input
                            type="checkbox"
                            name="sendWelcomeEmail"
                            checked={user.sendWelcomeEmail}
                            onChange={handleChange}
                            style={styles.checkbox}
                        />
                        <span style={styles.toggleTrack}>
                            <span style={styles.toggleThumb} />
                        </span>
                        <span>
                            <strong>Send welcome email</strong>
                            <small style={styles.toggleHelp}>
                                Send an email invitation with login details to the new user
                            </small>
                        </span>
                    </label>
                </section>

                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Access & Onboarding</h2>
                    <div style={styles.previewGrid}>
                        {onboardingItems.map(([title, description, icon, color]) => (
                            <div key={title} style={styles.previewTile}>
                                <span style={{ ...styles.previewIcon, background: color }}>
                                    <Icon name={icon} size={27} color={icon === "mail" ? "#16a34a" : icon === "userCheck" ? "#f97316" : "#1f4fff"} />
                                </span>
                                <div>
                                    <h3 style={styles.previewTitle}>{title}</h3>
                                    <p style={styles.previewText}>{description}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                    <div style={styles.infoAlert}>
                        <Icon name="info" size={20} />
                        Please review the information above before creating the account. You can edit details after the account is created.
                    </div>
                </section>
            </form>
        </main>
    );
}

const styles = {
    page: { minHeight: "100vh", background: "#f6f8fc", padding: "22px 32px", color: "#111827", fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif" },
    panel: { maxWidth: 1150, margin: "0 auto", background: "#ffffff", border: "1px solid #dce4f0", borderRadius: 16, boxShadow: "0 8px 24px rgba(31,41,55,.06)", overflow: "hidden" },
    header: { minHeight: 98, borderBottom: "1px solid #e6ebf3", padding: "24px 34px", display: "flex", alignItems: "center", justifyContent: "space-between", gap: 24 },
    pageTitle: { margin: 0, fontSize: 29, fontWeight: 800, letterSpacing: 0 },
    pageDescription: { margin: "6px 0 0", color: "#51607f", fontSize: 16 },
    actions: { display: "flex", gap: 12 },
    cancelButton: { height: 43, minWidth: 84, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", color: "#111827", fontSize: 15, cursor: "pointer" },
    primaryButton: { height: 43, minWidth: 162, border: 0, borderRadius: 7, background: "#2450f5", color: "#fff", display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 9, fontSize: 15, fontWeight: 700, cursor: "pointer" },
    card: { margin: "22px 32px", border: "1px solid #d9e2ef", borderRadius: 9, padding: "20px", background: "#fff" },
    cardTitle: { margin: "0 0 18px", fontSize: 18, fontWeight: 800 },
    formGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: 42, rowGap: 17 },
    formGroup: { minWidth: 0 },
    label: { display: "block", marginBottom: 6, color: "#172342", fontSize: 14, fontWeight: 600 },
    required: { color: "#dc2626" },
    inputWrap: { position: "relative" },
    input: { width: "100%", height: 38, border: "1px solid #d5deeb", borderRadius: 6, padding: "0 13px", color: "#111827", fontSize: 14, outline: "none", appearance: "none", background: "#fff" },
    rightIcon: { position: "absolute", right: 12, top: "50%", transform: "translateY(-50%)", display: "flex" },
    phoneInput: { height: 38, border: "1px solid #d5deeb", borderRadius: 6, display: "grid", gridTemplateColumns: "48px 50px 1fr", overflow: "hidden" },
    country: { display: "flex", alignItems: "center", justifyContent: "center", borderRight: "1px solid #e3e9f2" },
    phoneCode: { display: "flex", alignItems: "center", justifyContent: "center", borderRight: "1px solid #e3e9f2", color: "#243452" },
    phoneField: { border: 0, outline: "none", padding: "0 12px", fontSize: 14 },
    toggleRow: { marginTop: 18, display: "flex", alignItems: "center", gap: 12, color: "#111827", fontSize: 14 },
    checkbox: { display: "none" },
    toggleTrack: { width: 38, height: 21, background: "#2450f5", borderRadius: 20, padding: 2, display: "inline-flex", justifyContent: "flex-end" },
    toggleThumb: { width: 17, height: 17, background: "#fff", borderRadius: "50%" },
    toggleHelp: { display: "block", marginTop: 6, color: "#65728f", fontSize: 13 },
    previewGrid: { display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 18 },
    previewTile: { border: "1px solid #d9e2ef", borderRadius: 8, padding: "16px", minHeight: 112, display: "flex", alignItems: "center", gap: 16 },
    previewIcon: { width: 52, height: 52, borderRadius: "50%", display: "inline-flex", alignItems: "center", justifyContent: "center", flexShrink: 0 },
    previewTitle: { margin: "0 0 5px", fontSize: 15, fontWeight: 800 },
    previewText: { margin: 0, color: "#52617f", fontSize: 13, lineHeight: 1.45 },
    infoAlert: { marginTop: 18, minHeight: 43, borderRadius: 7, border: "1px solid #b9ceff", background: "#edf4ff", color: "#2450f5", display: "flex", alignItems: "center", gap: 12, padding: "0 16px", fontSize: 14 },
};

export default CreateUser;
