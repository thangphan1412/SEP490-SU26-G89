/* eslint-disable react-refresh/only-export-components */
export const contractStatuses = ["Active", "Pending", "Expired", "Draft"];

export const contractParties = [
    "Acme Corporation",
    "Globex Corporation",
    "Initech",
    "Soylent Corp",
    "Umbrella Corp",
    "Wayne Enterprises",
    "Stark Industries",
    "Hooli",
];

export const contractTypes = [
    "NDA",
    "MSA",
    "SOW",
    "Purchase Agreement",
    "Service Agreement",
    "License Agreement",
    "Partnership Agreement",
    "Data Processing Addendum",
];

export const contracts = [
    {
        id: 1,
        contractNumber: "CON-2025-0001",
        title: "NDA Agreement",
        party: "Acme Corporation",
        type: "NDA",
        status: "Active",
        effectiveDate: "May 01, 2025",
        expirationDate: "May 01, 2026",
        owner: "Alex Morgan",
        project: "Digital Contract Rollout",
        value: "$24,000",
        description: "Non-disclosure agreement for confidential project discussions.",
    },
    {
        id: 2,
        contractNumber: "CON-2025-0002",
        title: "MSA",
        party: "Globex Corporation",
        type: "MSA",
        status: "Pending",
        effectiveDate: "Apr 28, 2025",
        expirationDate: "Apr 28, 2026",
        owner: "Jamie Lee",
        project: "Vendor Summit 2025",
        value: "$86,000",
        description: "Master service agreement for long-term vendor services.",
    },
    {
        id: 3,
        contractNumber: "CON-2025-0003",
        title: "SOW - Phase 1",
        party: "Initech",
        type: "SOW",
        status: "Active",
        effectiveDate: "Apr 30, 2025",
        expirationDate: "Sep 30, 2025",
        owner: "Taylor Smith",
        project: "Contract Analytics Initiative",
        value: "$42,500",
        description: "Statement of work for phase 1 implementation deliverables.",
    },
    {
        id: 4,
        contractNumber: "CON-2025-0004",
        title: "Purchase Agreement",
        party: "Soylent Corp",
        type: "Purchase Agreement",
        status: "Expired",
        effectiveDate: "Jan 15, 2025",
        expirationDate: "Jun 15, 2025",
        owner: "Casey Brown",
        project: "Supplier Onboarding",
        value: "$18,700",
        description: "Purchase terms for contract management equipment.",
    },
    {
        id: 5,
        contractNumber: "CON-2025-0005",
        title: "Service Agreement",
        party: "Umbrella Corp",
        type: "Service Agreement",
        status: "Draft",
        effectiveDate: "-",
        expirationDate: "-",
        owner: "Morgan Lee",
        project: "Compliance Audit 2025",
        value: "$12,000",
        description: "Draft service agreement waiting for internal review.",
    },
    {
        id: 6,
        contractNumber: "CON-2025-0006",
        title: "License Agreement",
        party: "Wayne Enterprises",
        type: "License Agreement",
        status: "Active",
        effectiveDate: "Mar 10, 2025",
        expirationDate: "Mar 10, 2027",
        owner: "Jordan Kim",
        project: "Digital Contract Rollout",
        value: "$96,000",
        description: "Software license agreement for enterprise users.",
    },
    {
        id: 7,
        contractNumber: "CON-2025-0007",
        title: "Partnership Agreement",
        party: "Stark Industries",
        type: "Partnership Agreement",
        status: "Pending",
        effectiveDate: "May 10, 2025",
        expirationDate: "May 10, 2026",
        owner: "Riley Johnson",
        project: "Vendor Summit 2025",
        value: "$64,000",
        description: "Partnership agreement pending final signature.",
    },
    {
        id: 8,
        contractNumber: "CON-2025-0008",
        title: "Data Processing Addendum",
        party: "Hooli",
        type: "Data Processing Addendum",
        status: "Active",
        effectiveDate: "Apr 05, 2025",
        expirationDate: "Apr 05, 2026",
        owner: "Alex Morgan",
        project: "Compliance Audit 2025",
        value: "$8,500",
        description: "Data protection addendum for compliance requirements.",
    },
];

export function getContractById(id) {
    return contracts.find((contract) => String(contract.id) === String(id)) || contracts[0];
}

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
        plus: <><path d="M12 5v14" /><path d="M5 12h14" /></>,
        search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></>,
        filter: <><path d="M4 5h16" /><path d="M7 12h10" /><path d="M10 19h4" /></>,
        refresh: <><path d="M20 6v5h-5" /><path d="M4 18v-5h5" /><path d="M18 9a7 7 0 0 0-11.6-2.6L4 9" /><path d="M6 15a7 7 0 0 0 11.6 2.6L20 15" /></>,
        chevron: <path d="m8 10 4 4 4-4" />,
        dots: <><circle cx="12" cy="5" r="1" fill="currentColor" stroke="none" /><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" /><circle cx="12" cy="19" r="1" fill="currentColor" stroke="none" /></>,
        sort: <><path d="m8 9 3-3 3 3" /><path d="m14 15-3 3-3-3" /></>,
        arrowLeft: <path d="m15 18-6-6 6-6" />,
        arrowRight: <path d="m9 18 6-6-6-6" />,
        document: <><path d="M7 3h7l4 4v14H7z" /><path d="M14 3v5h5" /><path d="M10 13h5" /><path d="M10 17h4" /></>,
        users: <><circle cx="9" cy="8" r="3.5" /><path d="M2 20c1.4-3.6 3.7-5.4 7-5.4 3.2 0 5.6 1.8 7 5.4" /><path d="M17 11a3 3 0 1 0-1.2-5.8" /><path d="M18 14.5c1.8.6 3.1 2.2 4 5.5" /></>,
        shield: <><path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" /><path d="m9 12 2 2 4-4" /></>,
        edit: <><path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z" /><path d="m13.5 6.5 4 4" /></>,
        save: <><path d="M5 3h12l2 2v16H5z" /><path d="M8 3v6h8V3" /><path d="M8 21v-7h8v7" /></>,
        calendar: <><path d="M5 4h14v16H5z" /><path d="M8 2v4" /><path d="M16 2v4" /><path d="M5 9h14" /></>,
        dollar: <><circle cx="12" cy="12" r="9" /><path d="M12 6v12" /><path d="M15 8.5c-.8-.7-1.8-1-3-1-1.6 0-2.7.8-2.7 2s1 1.8 2.7 2.2c1.8.4 3 1 3 2.5S13.7 17 12 17c-1.3 0-2.5-.4-3.5-1.2" /></>,
        link: <><path d="M10 13a5 5 0 0 0 7 0l2-2a5 5 0 0 0-7-7l-1 1" /><path d="M14 11a5 5 0 0 0-7 0l-2 2a5 5 0 0 0 7 7l1-1" /></>,
        task: <><path d="M5 5h14v14H5z" /><path d="m8 12 2.5 2.5L16 9" /></>,
        info: <><circle cx="12" cy="12" r="9" /><path d="M12 11v5" /><path d="M12 8h.01" /></>,
    };

    return <svg {...props}>{paths[name]}</svg>;
}

export function PagePanel({ title, description, action, children }) {
    return (
        <main style={styles.page}>
            <section style={styles.panel}>
                <div style={styles.header}>
                    <div style={styles.titleBlock}>
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
            <span>{children}</span>
        </div>
    );
}

export function ContractStatusBadge({ status }) {
    const styleByStatus = {
        Active: styles.activeBadge,
        Pending: styles.pendingBadge,
        Expired: styles.expiredBadge,
        Draft: styles.draftBadge,
    };

    return (
        <span style={{ ...styles.badge, ...styleByStatus[status] }}>
            {status}
        </span>
    );
}

export const styles = {
    page: {
        minHeight: "100vh",
        background: "#f6f8fc",
        padding: "20px",
        color: "#111827",
        fontFamily: "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
        overflowX: "auto",
    },
    panel: {
        width: "min(1180px, calc(100vw - 40px))",
        margin: "0 auto",
        background: "#ffffff",
        border: "1px solid #dce4f0",
        borderRadius: 8,
        boxShadow: "0 10px 26px rgba(31, 41, 55, 0.06)",
        overflow: "hidden",
    },
    header: {
        minHeight: 102,
        borderBottom: "1px solid #e6ebf3",
        padding: "24px 28px",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: 20,
        flexWrap: "wrap",
    },
    titleBlock: { minWidth: 260 },
    pageTitle: { margin: 0, fontSize: 28, fontWeight: 800, letterSpacing: 0 },
    pageDescription: { margin: "7px 0 0", color: "#51607f", fontSize: 15, lineHeight: 1.45 },
    primaryButton: {
        minHeight: 46,
        minWidth: 148,
        border: 0,
        borderRadius: 7,
        background: "#2450f5",
        color: "#ffffff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: 9,
        padding: "0 18px",
        fontSize: 15,
        fontWeight: 800,
        cursor: "pointer",
        whiteSpace: "nowrap",
    },
    cancelButton: {
        minHeight: 43,
        minWidth: 86,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        color: "#111827",
        padding: "0 16px",
        fontSize: 15,
        fontWeight: 700,
        cursor: "pointer",
    },
    actions: { display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" },
    card: {
        margin: "22px 28px",
        border: "1px solid #d9e2ef",
        borderRadius: 8,
        padding: "20px",
        background: "#ffffff",
    },
    cardTitle: { margin: "0 0 18px", fontSize: 18, fontWeight: 800 },
    label: { display: "block", marginBottom: 7, color: "#243452", fontSize: 13, fontWeight: 800 },
    input: {
        width: "100%",
        height: 40,
        border: "1px solid #d5deeb",
        borderRadius: 7,
        padding: "0 13px",
        color: "#243452",
        fontSize: 14,
        outline: "none",
        appearance: "none",
        background: "#ffffff",
        boxSizing: "border-box",
    },
    textarea: {
        width: "100%",
        minHeight: 90,
        border: "1px solid #d5deeb",
        borderRadius: 7,
        padding: "12px 13px",
        color: "#243452",
        fontSize: 14,
        outline: "none",
        resize: "vertical",
        boxSizing: "border-box",
    },
    badge: {
        minHeight: 30,
        minWidth: 66,
        borderRadius: 6,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "0 10px",
        fontSize: 13,
        fontWeight: 800,
        boxSizing: "border-box",
    },
    activeBadge: { background: "#ecfff2", border: "1px solid #a8e0bb", color: "#108139" },
    pendingBadge: { background: "#fff7ed", border: "1px solid #fdba74", color: "#c2410c" },
    expiredBadge: { background: "#fff1f2", border: "1px solid #fecdd3", color: "#e11d48" },
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
        margin: "0 28px 24px",
        minHeight: 44,
        borderRadius: 7,
        border: "1px solid #b9ceff",
        background: "#edf4ff",
        color: "#2450f5",
        display: "flex",
        alignItems: "center",
        gap: 12,
        padding: "10px 16px",
        fontSize: 14,
        boxSizing: "border-box",
    },
};
