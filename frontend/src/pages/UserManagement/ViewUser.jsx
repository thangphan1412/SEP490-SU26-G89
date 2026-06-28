import { useNavigate } from "react-router-dom";

const leftDetails = [
    ["user", "Full Name", "Emma Nguyen"],
    ["mail", "Email", "emma.nguyen@econtract.com"],
    ["phone", "Phone Number", "+1 (555) 123-4567"],
    ["badge", "Employee ID", "EMP-001248"],
    ["building", "Department", "Legal Department"],
];

const rightDetails = [
    ["briefcase", "Position", "Senior Legal Counsel"],
    ["shield", "Role", "Contract Manager"],
    ["globe", "Access Scope", "Department Level"],
    ["calendar", "Date Joined", "Jan 15, 2024"],
    ["clock", "Last Login", "May 22, 2025, 09:32 AM"],
    ["check", "Account Status", "Active", "badge"],
];

const summaryItems = [
    ["Assigned Role", "Contract Manager", "Full access to contract lifecycle management and approvals.", "shield"],
    ["Department Access", "Legal Department", "Can view and manage all users and contracts within the Legal Department.", "building"],
    ["Approval Rights", "Up to $250,000", "Authorized to approve contracts and changes up to a value of $250,000.", "clipboard"],
    ["Recent Activity", "32 Activities", "Last activity: Viewed contract CN-2025-0456 on May 22, 2025 09:32 AM.", "clock"],
];

function Icon({ name, size = 22, color = "#1f4fff" }) {
    const props = { width: size, height: size, viewBox: "0 0 24 24", fill: "none", stroke: color, strokeWidth: 2, strokeLinecap: "round", strokeLinejoin: "round", "aria-hidden": true };
    const paths = {
        edit: <><path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z" /><path d="m13.5 6.5 4 4" /></>,
        user: <><circle cx="12" cy="8" r="4" /><path d="M4 21c1.6-4 4.2-6 8-6s6.4 2 8 6" /></>,
        mail: <><path d="M4 6h16v12H4z" /><path d="m4 7 8 6 8-6" /></>,
        phone: <><path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1.9.3 1.7.6 2.5a2 2 0 0 1-.5 2.1L8 9.5a16 16 0 0 0 6.5 6.5l1.2-1.2a2 2 0 0 1 2.1-.5c.8.3 1.6.5 2.5.6a2 2 0 0 1 1.7 2Z" /></>,
        badge: <><path d="M8 4h8v4H8z" /><path d="M6 8h12v12H6z" /><path d="M10 13h4" /><path d="M10 16h4" /></>,
        building: <><path d="M4 21h16" /><path d="M6 21V5h7v16" /><path d="M13 9h5v12" /><path d="M9 9h1" /><path d="M16 13h1" /></>,
        briefcase: <><path d="M10 6V5a2 2 0 0 1 2-2h0a2 2 0 0 1 2 2v1" /><path d="M4 7h16v12H4z" /><path d="M4 12h16" /></>,
        shield: <><path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" /><path d="m9 12 2 2 4-4" /></>,
        globe: <><circle cx="12" cy="12" r="9" /><path d="M3 12h18" /><path d="M12 3c3 3 3 15 0 18" /><path d="M12 3c-3 3-3 15 0 18" /></>,
        calendar: <><path d="M5 4h14v16H5z" /><path d="M8 2v4" /><path d="M16 2v4" /><path d="M5 9h14" /></>,
        clock: <><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 3" /></>,
        check: <><circle cx="12" cy="12" r="9" /><path d="m8 12 2.5 2.5L16 9" /></>,
        clipboard: <><path d="M9 4h6l1 2h3v15H5V6h3z" /><path d="m9 13 2 2 4-4" /></>,
        info: <><circle cx="12" cy="12" r="9" /><path d="M12 11v5" /><path d="M12 8h.01" /></>,
    };
    return <svg {...props}>{paths[name]}</svg>;
}

function DetailRow({ icon, label, value, type }) {
    return (
        <div style={styles.detailRow}>
            <Icon name={icon} size={21} color="#435174" />
            <span style={styles.detailLabel}>{label}</span>
            {type === "badge" ? <span style={styles.activeBadge}>{value}</span> : <span style={styles.detailValue}>{value}</span>}
        </div>
    );
}

function ViewUser() {
    const navigate = useNavigate();

    return (
        <main style={styles.page}>
            <section style={styles.panel}>
                <div style={styles.header}>
                    <div>
                        <h1 style={styles.pageTitle}>User Details</h1>
                        <p style={styles.pageDescription}>Review employee information, role permissions, and department assignment.</p>
                    </div>
                    <button type="button" style={styles.primaryButton} onClick={() => navigate("/user-management/update")}>
                        <Icon name="edit" size={20} color="#fff" />
                        Edit User
                    </button>
                </div>

                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Employee Information</h2>
                    <div style={styles.employeeGrid}>
                        <div style={styles.photoColumn}>
                            <div style={styles.photoPlaceholder}>
                                <div style={styles.photoFace} />
                            </div>
                            <h3 style={styles.employeeName}>Emma Nguyen</h3>
                            <span style={styles.activeBadge}>● Active</span>
                        </div>
                        <div style={styles.detailsColumn}>{leftDetails.map((item) => <DetailRow key={item[1]} icon={item[0]} label={item[1]} value={item[2]} />)}</div>
                        <div style={styles.detailsColumn}>{rightDetails.map((item) => <DetailRow key={item[1]} icon={item[0]} label={item[1]} value={item[2]} type={item[3]} />)}</div>
                    </div>
                </section>

                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Role & Access Summary</h2>
                    <div style={styles.summaryGrid}>
                        {summaryItems.map(([title, value, description, icon]) => (
                            <div key={title} style={styles.summaryItem}>
                                <span style={styles.summaryIcon}><Icon name={icon} size={25} /></span>
                                <p style={styles.summaryTitle}>{title}</p>
                                <h3 style={styles.summaryValue}>{value}</h3>
                                <p style={styles.summaryText}>{description}</p>
                            </div>
                        ))}
                    </div>
                </section>

                <section style={styles.infoAlert}>
                    <Icon name="info" size={22} />
                    <span>To make changes to this user, click <strong style={{ color: "#1f4fff" }}>Edit User</strong>.</span>
                </section>
            </section>
        </main>
    );
}

const styles = {
    page: { minHeight: "100vh", background: "#f6f8fc", padding: "24px 32px", color: "#111827", fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif" },
    panel: { maxWidth: 1150, margin: "0 auto", background: "#fff", border: "1px solid #dce4f0", borderRadius: 16, boxShadow: "0 8px 24px rgba(31,41,55,.06)", overflow: "hidden" },
    header: { minHeight: 104, borderBottom: "1px solid #e6ebf3", padding: "26px 36px", display: "flex", alignItems: "center", justifyContent: "space-between" },
    pageTitle: { margin: 0, fontSize: 29, fontWeight: 800 },
    pageDescription: { margin: "6px 0 0", color: "#51607f", fontSize: 16 },
    primaryButton: { height: 47, minWidth: 132, border: 0, borderRadius: 7, background: "#2450f5", color: "#fff", display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 10, fontSize: 16, fontWeight: 700, cursor: "pointer" },
    card: { margin: "28px 32px 20px", border: "1px solid #d9e2ef", borderRadius: 9, padding: "22px 24px", background: "#fff" },
    cardTitle: { margin: "0 0 24px", fontSize: 18, fontWeight: 800 },
    employeeGrid: { display: "grid", gridTemplateColumns: "230px 1fr 1.15fr", gap: 28 },
    photoColumn: { borderRight: "1px solid #e3e9f2", minHeight: 295, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" },
    photoPlaceholder: { width: 150, height: 150, borderRadius: "50%", background: "linear-gradient(#f7fafc,#e9eef6)", border: "1px solid #d7dfeb", display: "flex", alignItems: "center", justifyContent: "center", overflow: "hidden" },
    photoFace: { width: 82, height: 112, borderRadius: "45% 45% 35% 35%", background: "linear-gradient(160deg,#1f2937 0 35%,#f4c7a1 36% 67%,#ffffff 68%)" },
    employeeName: { margin: "20px 0 14px", fontSize: 21, fontWeight: 800 },
    detailsColumn: { borderRight: "1px solid #e3e9f2", paddingRight: 28 },
    detailRow: { minHeight: 56, borderBottom: "1px solid #e8edf4", display: "grid", gridTemplateColumns: "28px 110px 1fr", alignItems: "center", gap: 10, color: "#243452", fontSize: 14 },
    detailLabel: { color: "#52617f", fontWeight: 700 },
    detailValue: { color: "#243452" },
    activeBadge: { minHeight: 31, minWidth: 58, borderRadius: 6, border: "1px solid #a8e0bb", background: "#ecfff2", color: "#108139", display: "inline-flex", alignItems: "center", justifyContent: "center", padding: "0 10px", fontSize: 13, fontWeight: 800 },
    summaryGrid: { display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 16 },
    summaryItem: { minHeight: 198, border: "1px solid #d9e2ef", borderRadius: 9, padding: "24px 18px" },
    summaryIcon: { width: 52, height: 52, borderRadius: "50%", background: "#eef3ff", display: "inline-flex", alignItems: "center", justifyContent: "center", marginBottom: 18 },
    summaryTitle: { margin: "0 0 18px", color: "#243452", fontSize: 15 },
    summaryValue: { margin: "0 0 12px", fontSize: 18, fontWeight: 800 },
    summaryText: { margin: 0, color: "#52617f", fontSize: 14, lineHeight: 1.5 },
    infoAlert: { margin: "0 32px 26px", minHeight: 55, borderRadius: 7, border: "1px solid #b9ceff", background: "#edf4ff", color: "#52617f", display: "flex", alignItems: "center", gap: 14, padding: "0 22px", fontSize: 15 },
};

export default ViewUser;
