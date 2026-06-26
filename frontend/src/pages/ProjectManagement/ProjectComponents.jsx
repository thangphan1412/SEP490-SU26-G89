/* eslint-disable react-refresh/only-export-components */
export const projects = [
    ["document", "Digital Contract Rollout", "IT", "Alex Morgan", "Active", "May 01, 2025", "Jul 31, 2025", "$120,000"],
    ["users", "Vendor Summit 2025", "Procurement", "Jamie Lee", "Planning", "Jun 15, 2025", "Aug 15, 2025", "$85,000"],
    ["document", "HR Policy Renewal", "HR", "Taylor Smith", "Active", "Apr 20, 2025", "Jun 30, 2025", "$45,000"],
    ["building", "Office Expansion", "Operations", "Casey Brown", "On Hold", "Mar 01, 2025", "Sep 30, 2025", "$750,000"],
    ["users", "Supplier Onboarding", "Procurement", "Jordan Kim", "Active", "May 10, 2025", "Aug 10, 2025", "$60,000"],
    ["shield", "Compliance Audit 2025", "Legal", "Morgan Lee", "Completed", "Jan 15, 2025", "Mar 31, 2025", "$30,000"],
    ["chart", "Contract Analytics Initiative", "IT", "Riley Johnson", "Planning", "Aug 01, 2025", "Nov 30, 2025", "$95,000"],
];

export function Icon({ name, size = 22, color = "#1f4fff" }) {
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
        plus: <><circle cx="12" cy="12" r="9" /><path d="M12 8v8" /><path d="M8 12h8" /></>,
        search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></>,
        filter: <path d="M4 5h16l-6 7v5l-4 2v-7z" />,
        refresh: <><path d="M20 6v5h-5" /><path d="M4 18v-5h5" /><path d="M18 9a7 7 0 0 0-11.6-2.6L4 9" /><path d="M6 15a7 7 0 0 0 11.6 2.6L20 15" /></>,
        chevron: <path d="m8 10 4 4 4-4" />,
        dots: <><circle cx="6" cy="12" r="1" fill="currentColor" stroke="none" /><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" /><circle cx="18" cy="12" r="1" fill="currentColor" stroke="none" /></>,
        sort: <><path d="m8 9 3-3 3 3" /><path d="m14 15-3 3-3-3" /></>,
        arrowLeft: <path d="m15 18-6-6 6-6" />,
        arrowRight: <path d="m9 18 6-6-6-6" />,
        document: <><path d="M7 3h7l4 4v14H7z" /><path d="M14 3v5h5" /><path d="M10 13h5" /><path d="M10 17h4" /></>,
        users: <><circle cx="9" cy="8" r="3.5" /><path d="M2 20c1.4-3.6 3.7-5.4 7-5.4 3.2 0 5.6 1.8 7 5.4" /><path d="M17 11a3 3 0 1 0-1.2-5.8" /><path d="M18 14.5c1.8.6 3.1 2.2 4 5.5" /></>,
        building: <><path d="M4 21h16" /><path d="M6 21V5h7v16" /><path d="M13 9h5v12" /><path d="M9 9h1" /><path d="M16 13h1" /></>,
        shield: <><path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" /><path d="m9 12 2 2 4-4" /></>,
        chart: <><path d="M5 20V9" /><path d="M12 20V4" /><path d="M19 20v-7" /><path d="M3 20h18" /></>,
        edit: <><path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z" /><path d="m13.5 6.5 4 4" /></>,
        save: <><path d="M5 3h12l2 2v16H5z" /><path d="M8 3v6h8V3" /><path d="M8 21v-7h8v7" /></>,
        calendar: <><path d="M5 4h14v16H5z" /><path d="M8 2v4" /><path d="M16 2v4" /><path d="M5 9h14" /></>,
        dollar: <><circle cx="12" cy="12" r="9" /><path d="M12 6v12" /><path d="M15 8.5c-.8-.7-1.8-1-3-1-1.6 0-2.7.8-2.7 2s1 1.8 2.7 2.2c1.8.4 3 1 3 2.5S13.7 17 12 17c-1.3 0-2.5-.4-3.5-1.2" /></>,
        location: <><path d="M12 21s7-5.2 7-11a7 7 0 0 0-14 0c0 5.8 7 11 7 11Z" /><circle cx="12" cy="10" r="2.5" /></>,
        flag: <><path d="M5 21V4" /><path d="M5 5h11l-1.5 4L16 13H5" /></>,
        link: <><path d="M10 13a5 5 0 0 0 7 0l2-2a5 5 0 0 0-7-7l-1 1" /><path d="M14 11a5 5 0 0 0-7 0l-2 2a5 5 0 0 0 7 7l1-1" /></>,
        task: <><path d="M5 5h14v14H5z" /><path d="m8 12 2.5 2.5L16 9" /></>,
        info: <><circle cx="12" cy="12" r="9" /><path d="M12 11v5" /><path d="M12 8h.01" /></>,
    };

    return <svg {...props}>{paths[name]}</svg>;
}

export function StatusBadge({ status }) {
    const styleByStatus = {
        Active: styles.activeBadge,
        Planning: styles.planningBadge,
        "On Hold": styles.holdBadge,
        Completed: styles.completedBadge,
        Signed: styles.activeBadge,
        "In Review": styles.holdBadge,
        Approved: styles.activeBadge,
        Draft: styles.draftBadge,
    };

    return (
        <span style={{ ...styles.badge, ...styleByStatus[status] }}>
            {status}
        </span>
    );
}

export function PagePanel({ title, description, action, children }) {
    return (
        <main style={styles.page}>
            <section style={styles.panel}>
                <div style={styles.header}>
                    <div>
                        <h1 style={styles.pageTitle}>{title}</h1>
                        <p style={styles.pageDescription}>{description}</p>
                    </div>
                    {action}
                </div>
                {children}
            </section>
        </main>
    );
}

export function PrimaryButton({ children, onClick, type = "button" }) {
    return (
        <button type={type} style={styles.primaryButton} onClick={onClick}>
            {children}
        </button>
    );
}

export function CancelButton({ children = "Cancel", onClick }) {
    return (
        <button type="button" style={styles.cancelButton} onClick={onClick}>
            {children}
        </button>
    );
}

export function InfoAlert({ children }) {
    return (
        <div style={styles.infoAlert}>
            <Icon name="info" size={20} />
            {children}
        </div>
    );
}

export const styles = {
    page: {
        minHeight: "100vh",
        background: "#f6f8fc",
        padding: "22px 32px",
        color: "#111827",
        fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
    },
    panel: {
        maxWidth: 1160,
        margin: "0 auto",
        background: "#ffffff",
        border: "1px solid #dce4f0",
        borderRadius: 16,
        boxShadow: "0 8px 24px rgba(31, 41, 55, 0.06)",
        overflow: "hidden",
    },
    header: {
        minHeight: 104,
        borderBottom: "1px solid #e6ebf3",
        padding: "26px 34px",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: 24,
    },
    pageTitle: { margin: 0, fontSize: 29, fontWeight: 800, letterSpacing: 0 },
    pageDescription: { margin: "6px 0 0", color: "#51607f", fontSize: 16 },
    primaryButton: {
        height: 47,
        minWidth: 142,
        border: 0,
        borderRadius: 7,
        background: "#2450f5",
        color: "#ffffff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: 10,
        fontSize: 16,
        fontWeight: 700,
        cursor: "pointer",
    },
    cancelButton: {
        height: 43,
        minWidth: 84,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        color: "#111827",
        fontSize: 15,
        cursor: "pointer",
    },
    actions: { display: "flex", gap: 12 },
    card: {
        margin: "20px 28px",
        border: "1px solid #d9e2ef",
        borderRadius: 9,
        padding: "20px",
        background: "#ffffff",
    },
    cardTitle: { margin: "0 0 18px", fontSize: 18, fontWeight: 800 },
    label: { display: "block", marginBottom: 7, color: "#243452", fontSize: 13, fontWeight: 700 },
    input: {
        width: "100%",
        height: 38,
        border: "1px solid #d5deeb",
        borderRadius: 6,
        padding: "0 13px",
        color: "#243452",
        fontSize: 14,
        outline: "none",
        appearance: "none",
        background: "#ffffff",
    },
    textarea: {
        width: "100%",
        minHeight: 78,
        border: "1px solid #d5deeb",
        borderRadius: 6,
        padding: "12px 13px",
        color: "#243452",
        fontSize: 14,
        outline: "none",
        resize: "vertical",
    },
    badge: {
        minHeight: 31,
        minWidth: 62,
        borderRadius: 6,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "0 10px",
        fontSize: 13,
        fontWeight: 800,
    },
    activeBadge: { background: "#ecfff2", border: "1px solid #a8e0bb", color: "#108139" },
    planningBadge: { background: "#eef5ff", border: "1px solid #b6cffc", color: "#1f4fff" },
    holdBadge: { background: "#fff7ed", border: "1px solid #fdba74", color: "#ea580c" },
    completedBadge: { background: "#f5edff", border: "1px solid #cba6ff", color: "#7c3aed" },
    draftBadge: { background: "#f8fafc", border: "1px solid #cfd8e5", color: "#42516d" },
    iconCircle: {
        width: 42,
        height: 42,
        borderRadius: "50%",
        background: "#eef3ff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        flexShrink: 0,
    },
    infoAlert: {
        margin: "0 28px 22px",
        minHeight: 43,
        borderRadius: 7,
        border: "1px solid #b9ceff",
        background: "#edf4ff",
        color: "#2450f5",
        display: "flex",
        alignItems: "center",
        gap: 12,
        padding: "0 16px",
        fontSize: 14,
    },
};
