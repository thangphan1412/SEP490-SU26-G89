import { useState } from "react";
import { useNavigate } from "react-router-dom";

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

const fields = [
    ["Full Name", "fullName", "input", "user"],
    ["Phone Number", "phoneNumber", "input", "phone"],
    ["Email Address", "email", "email", "mail"],
    ["Employee ID", "employeeId", "input", "badge"],
    ["Department", "department", "select", "building", ["Legal", "HR", "Finance", "Sales"]],
    ["Start Date", "startDate", "input", "calendar"],
    ["Role", "role", "select", "shield", ["Legal Reviewer", "Contract Manager", "Approver", "Viewer"]],
    ["Status", "status", "select", null, ["Active", "Inactive", "Deactivated"]],
    ["Position", "position", "input", "briefcase"],
    ["Access Scope", "accessScope", "select", "lock", ["Department Level Access", "Full Access", "Limited Access"]],
];

const accessItems = [
    ["Current Role", "Legal Reviewer", "Approval Level: L2", "userShield"],
    ["Department Access", "Legal Department", "12 Modules", "building"],
    ["Approval Workflow", "Can review & approve", "Level 2 Access", "workflow"],
    ["Audit Trail", "All actions are logged", "Full Visibility", "audit"],
];

function Icon({ name, size = 22, color = "#1f4fff" }) {
    const props = { width: size, height: size, viewBox: "0 0 24 24", fill: "none", stroke: color, strokeWidth: 2, strokeLinecap: "round", strokeLinejoin: "round", "aria-hidden": true };
    const paths = {
        save: <><path d="M5 3h12l2 2v16H5z" /><path d="M8 3v6h8V3" /><path d="M8 21v-7h8v7" /></>,
        user: <><circle cx="12" cy="8" r="4" /><path d="M4 21c1.6-4 4.2-6 8-6s6.4 2 8 6" /></>,
        phone: <><path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1.9.3 1.7.6 2.5a2 2 0 0 1-.5 2.1L8 9.5a16 16 0 0 0 6.5 6.5l1.2-1.2a2 2 0 0 1 2.1-.5c.8.3 1.6.5 2.5.6a2 2 0 0 1 1.7 2Z" /></>,
        mail: <><path d="M4 6h16v12H4z" /><path d="m4 7 8 6 8-6" /></>,
        badge: <><path d="M8 4h8v4H8z" /><path d="M6 8h12v12H6z" /><path d="M10 13h4" /><path d="M10 16h4" /></>,
        building: <><path d="M4 21h16" /><path d="M6 21V5h7v16" /><path d="M13 9h5v12" /><path d="M9 9h1" /><path d="M16 13h1" /></>,
        calendar: <><path d="M5 4h14v16H5z" /><path d="M8 2v4" /><path d="M16 2v4" /><path d="M5 9h14" /></>,
        shield: <><path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" /><path d="m9 12 2 2 4-4" /></>,
        briefcase: <><path d="M10 6V5a2 2 0 0 1 2-2h0a2 2 0 0 1 2 2v1" /><path d="M4 7h16v12H4z" /><path d="M4 12h16" /></>,
        lock: <><path d="M7 11V8a5 5 0 0 1 10 0v3" /><path d="M5 11h14v10H5z" /></>,
        chevron: <path d="m8 10 4 4 4-4" />,
        userShield: <><circle cx="9" cy="8" r="4" /><path d="M2 21c1.4-4 3.7-6 7-6 1.2 0 2.3.3 3.3.8" /><path d="M18 13 22 15v3c0 2.4-1.7 4-4 5-2.3-1-4-2.6-4-5v-3z" /></>,
        workflow: <><path d="M6 6h5v5H6z" /><path d="M13 13h5v5h-5z" /><path d="M8.5 11v3.5H13" /><path d="M11 8.5h3.5V13" /></>,
        audit: <><path d="M7 3h7l4 4v14H7z" /><path d="M14 3v5h5" /><path d="M10 14h5" /><path d="M10 17h4" /></>,
        info: <><circle cx="12" cy="12" r="9" /><path d="M12 11v5" /><path d="M12 8h.01" /></>,
    };
    return <svg {...props}>{paths[name]}</svg>;
}

function UpdateUser({ onUpdateUser }) {
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
        onUpdateUser?.(user);
    };

    const renderField = ([label, name, type, icon, options]) => (
        <div key={name} style={styles.formGroup}>
            <label htmlFor={name} style={styles.label}>{label}</label>
            <div style={styles.inputWrap}>
                {icon && <span style={styles.leftIcon}><Icon name={icon} size={18} color="#64708f" /></span>}
                {type === "select" ? (
                    <>
                        <select id={name} name={name} value={user[name]} onChange={handleChange} style={{ ...styles.input, paddingLeft: icon ? 42 : 14 }}>
                            {options.map((option) => <option key={option}>{option}</option>)}
                        </select>
                        {name === "status" && <span style={styles.statusPill}>Active</span>}
                        <span style={styles.rightIcon}><Icon name="chevron" size={18} color="#243452" /></span>
                    </>
                ) : (
                    <input id={name} name={name} type={type} value={user[name]} onChange={handleChange} style={{ ...styles.input, paddingLeft: icon ? 42 : 14 }} />
                )}
            </div>
        </div>
    );

    return (
        <main style={styles.page}>
            <form style={styles.panel} onSubmit={handleSubmit}>
                <div style={styles.header}>
                    <div>
                        <h1 style={styles.pageTitle}>Update User</h1>
                        <p style={styles.pageDescription}>Edit employee information, change role, department, or deactivate access.</p>
                    </div>
                    <div style={styles.actions}>
                        <button type="button" style={styles.cancelButton} onClick={() => navigate("/user-management/list")}>Cancel</button>
                        <button type="submit" style={styles.primaryButton}><Icon name="save" size={19} color="#fff" />Save Changes</button>
                    </div>
                </div>

                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>User Information</h2>
                    <div style={styles.formGrid}>{fields.map(renderField)}</div>
                    <div style={styles.deactivateRow}>
                        <label style={styles.switch}>
                            <input type="checkbox" name="deactivateAccount" checked={user.deactivateAccount} onChange={handleChange} style={styles.checkbox} />
                            <span style={styles.switchTrack}><span style={styles.switchThumb} /></span>
                        </label>
                        <div>
                            <h3 style={styles.deactivateTitle}>Deactivate Account</h3>
                            <p style={styles.deactivateText}>Revoke this user's access to the system. This action can be reversed later if needed.</p>
                        </div>
                    </div>
                </section>

                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Access Preview</h2>
                    <div style={styles.previewGrid}>
                        {accessItems.map(([title, value, chip, icon]) => (
                            <div key={title} style={styles.previewTile}>
                                <span style={styles.previewIcon}><Icon name={icon} size={28} /></span>
                                <div>
                                    <h3 style={styles.previewTitle}>{title}</h3>
                                    <p style={styles.previewText}>{value}</p>
                                    <span style={styles.previewChip}>{chip}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                    <div style={styles.infoAlert}>
                        <Icon name="info" size={20} />
                        Please review all changes carefully before saving. Updates will take effect immediately after confirmation.
                    </div>
                </section>
            </form>
        </main>
    );
}

const styles = {
    page: { minHeight: "100vh", background: "#f6f8fc", padding: "22px 32px", color: "#111827", fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif" },
    panel: { maxWidth: 1160, margin: "0 auto", background: "#fff", border: "1px solid #dce4f0", borderRadius: 16, boxShadow: "0 8px 24px rgba(31,41,55,.06)", overflow: "hidden" },
    header: { minHeight: 104, borderBottom: "1px solid #e6ebf3", padding: "26px 34px", display: "flex", alignItems: "center", justifyContent: "space-between", gap: 24 },
    pageTitle: { margin: 0, fontSize: 26, fontWeight: 800 },
    pageDescription: { margin: "6px 0 0", color: "#51607f", fontSize: 15 },
    actions: { display: "flex", gap: 12 },
    cancelButton: { height: 43, minWidth: 78, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", color: "#111827", fontSize: 15, cursor: "pointer" },
    primaryButton: { height: 43, minWidth: 156, border: 0, borderRadius: 7, background: "#2450f5", color: "#fff", display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 9, fontSize: 15, fontWeight: 700, cursor: "pointer" },
    card: { margin: "24px 30px 10px", border: "1px solid #d9e2ef", borderRadius: 9, padding: "20px", background: "#fff" },
    cardTitle: { margin: "0 0 20px", fontSize: 18, fontWeight: 800 },
    formGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: 34, rowGap: 18 },
    formGroup: { minWidth: 0 },
    label: { display: "block", marginBottom: 8, color: "#243452", fontSize: 14 },
    inputWrap: { position: "relative" },
    input: { width: "100%", height: 39, border: "1px solid #d5deeb", borderRadius: 7, padding: "0 14px", color: "#243452", fontSize: 14, outline: "none", appearance: "none", background: "#fff" },
    leftIcon: { position: "absolute", left: 13, top: "50%", transform: "translateY(-50%)", display: "flex", zIndex: 1 },
    rightIcon: { position: "absolute", right: 12, top: "50%", transform: "translateY(-50%)", display: "flex", pointerEvents: "none" },
    statusPill: { position: "absolute", left: 13, top: "50%", transform: "translateY(-50%)", minHeight: 24, borderRadius: 5, border: "1px solid #a8e0bb", background: "#ecfff2", color: "#108139", display: "inline-flex", alignItems: "center", padding: "0 10px", fontSize: 12, fontWeight: 800 },
    deactivateRow: { marginTop: 18, borderTop: "1px solid #e8edf4", paddingTop: 22, display: "flex", alignItems: "center", gap: 18 },
    switch: { display: "inline-flex" },
    checkbox: { display: "none" },
    switchTrack: { width: 47, height: 29, borderRadius: 20, background: "#d8dee9", padding: 3, display: "inline-flex", alignItems: "center" },
    switchThumb: { width: 23, height: 23, borderRadius: "50%", background: "#fff", boxShadow: "0 1px 4px rgba(0,0,0,.18)" },
    deactivateTitle: { margin: 0, fontSize: 15, fontWeight: 800 },
    deactivateText: { margin: "4px 0 0", color: "#65728f", fontSize: 13 },
    previewGrid: { display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 18 },
    previewTile: { border: "1px solid #d9e2ef", borderRadius: 8, minHeight: 112, padding: "16px", display: "flex", alignItems: "center", gap: 16 },
    previewIcon: { width: 52, height: 52, borderRadius: "50%", background: "#eef3ff", display: "inline-flex", alignItems: "center", justifyContent: "center", flexShrink: 0 },
    previewTitle: { margin: "0 0 6px", fontSize: 15, fontWeight: 800 },
    previewText: { margin: "0 0 8px", color: "#52617f", fontSize: 13 },
    previewChip: { border: "1px solid #d5deeb", background: "#f8fafc", borderRadius: 5, padding: "3px 10px", color: "#334260", fontSize: 12 },
    infoAlert: { marginTop: 24, minHeight: 43, borderRadius: 7, border: "1px solid #b9ceff", background: "#edf4ff", color: "#2450f5", display: "flex", alignItems: "center", gap: 12, padding: "0 16px", fontSize: 14 },
};

export default UpdateUser;
