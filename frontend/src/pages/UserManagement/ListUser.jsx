import { useNavigate } from "react-router-dom";

const users = [
    ["AM", "Alex Morgan", "alex.morgan@econtract.com", "Legal", "Contract Manager", "Active", "May 22, 2025 09:32 AM"],
    ["EN", "Emma Nguyen", "emma.nguyen@econtract.com", "HR", "HR Admin", "Active", "May 22, 2025 08:45 AM"],
    ["DT", "David Tran", "david.tran@econtract.com", "Finance", "Approver", "Active", "May 21, 2025 04:18 PM"],
    ["SL", "Sophia Le", "sophia.le@econtract.com", "Sales", "Viewer", "Inactive", "May 20, 2025 11:07 AM"],
    ["MP", "Michael Pham", "michael.pham@econtract.com", "Operations", "Department Manager", "Active", "May 19, 2025 03:25 PM"],
    ["LH", "Linda Hoang", "linda.hoang@econtract.com", "HR", "Approver", "Deactivated", "May 18, 2025 10:12 AM"],
    ["KV", "Kevin Vu", "kevin.vu@econtract.com", "Legal", "Viewer", "Inactive", "May 17, 2025 02:50 PM"],
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
        plus: (
            <>
                <circle cx="12" cy="12" r="9" />
                <path d="M12 8v8" />
                <path d="M8 12h8" />
            </>
        ),
        search: (
            <>
                <circle cx="11" cy="11" r="7" />
                <path d="m20 20-3.5-3.5" />
            </>
        ),
        filter: (
            <>
                <path d="M4 5h16l-6 7v5l-4 2v-7z" />
            </>
        ),
        refresh: (
            <>
                <path d="M20 6v5h-5" />
                <path d="M4 18v-5h5" />
                <path d="M18 9a7 7 0 0 0-11.6-2.6L4 9" />
                <path d="M6 15a7 7 0 0 0 11.6 2.6L20 15" />
            </>
        ),
        chevron: <path d="m8 10 4 4 4-4" />,
        dots: (
            <>
                <circle cx="6" cy="12" r="1" fill="currentColor" stroke="none" />
                <circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" />
                <circle cx="18" cy="12" r="1" fill="currentColor" stroke="none" />
            </>
        ),
        sort: (
            <>
                <path d="m8 9 3-3 3 3" />
                <path d="m14 15-3 3-3-3" />
            </>
        ),
        arrowLeft: <path d="m15 18-6-6 6-6" />,
        arrowRight: <path d="m9 18 6-6-6-6" />,
    };

    return <svg {...props}>{paths[name]}</svg>;
}

function StatusBadge({ status }) {
    const styleByStatus = {
        Active: styles.activeBadge,
        Inactive: styles.inactiveBadge,
        Deactivated: styles.deactivatedBadge,
    };

    return <span style={{ ...styles.statusBadge, ...styleByStatus[status] }}>{status}</span>;
}

function ListUser() {
    const navigate = useNavigate();

    return (
        <main style={styles.page}>
            <section style={styles.panel}>
                <div style={styles.header}>
                    <div>
                        <h1 style={styles.pageTitle}>Users</h1>
                        <p style={styles.pageDescription}>
                            Manage employee accounts, roles, departments, and access status.
                        </p>
                    </div>
                    <button
                        type="button"
                        style={styles.primaryButton}
                        onClick={() => navigate("/user-management/create")}
                    >
                        <Icon name="plus" size={20} color="#ffffff" />
                        New User
                    </button>
                </div>

                <div style={styles.toolbar}>
                    <label style={styles.searchBox}>
                        <Icon name="search" size={23} color="#3f4d6f" />
                        <input
                            aria-label="Search users"
                            placeholder="Search users..."
                            style={styles.searchInput}
                        />
                    </label>
                    {["Department", "Role", "Status"].map((label) => (
                        <label key={label} style={styles.selectBox}>
                            <span style={styles.selectLabel}>{label}</span>
                            <select style={styles.select}>
                                <option>All</option>
                            </select>
                            <span style={styles.selectIcon}>
                                <Icon name="chevron" size={18} color="#243452" />
                            </span>
                        </label>
                    ))}
                    <button type="button" style={styles.filterButton}>
                        <Icon name="filter" size={20} color="#243452" />
                        Filters
                    </button>
                    <button type="button" style={styles.iconButton}>
                        <Icon name="refresh" size={22} color="#243452" />
                    </button>
                </div>

                <div style={styles.tableWrap}>
                    <table style={styles.table}>
                        <thead>
                            <tr>
                                {["User Name", "Email", "Department", "Role", "Status", "Last Active", "Actions"].map((header) => (
                                    <th key={header} style={styles.th}>
                                        <span style={styles.thContent}>
                                            {header}
                                            {header !== "Actions" && <Icon name="sort" size={13} color="#243452" />}
                                        </span>
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(([initials, name, email, department, role, status, lastActive]) => (
                                <tr key={email} style={styles.tr}>
                                    <td style={styles.nameCell}>
                                        <span style={styles.avatar}>{initials}</span>
                                        <span style={styles.userName}>{name}</span>
                                    </td>
                                    <td style={styles.td}>{email}</td>
                                    <td style={styles.td}>{department}</td>
                                    <td style={styles.td}>{role}</td>
                                    <td style={styles.td}>
                                        <StatusBadge status={status} />
                                    </td>
                                    <td style={styles.td}>{lastActive}</td>
                                    <td style={styles.actionCell}>
                                        <button
                                            type="button"
                                            style={styles.actionButton}
                                            onClick={() => navigate("/user-management/view")}
                                        >
                                            <Icon name="dots" size={20} color="#111827" />
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div style={styles.footer}>
                    <span>Showing 1 to 7 of 7 results</span>
                    <div style={styles.pagination}>
                        <button type="button" style={styles.pageButton}>
                            <Icon name="arrowLeft" size={18} color="#243452" />
                        </button>
                        <button type="button" style={styles.currentPage}>1</button>
                        <button type="button" style={styles.pageButton}>
                            <Icon name="arrowRight" size={18} color="#243452" />
                        </button>
                        <select style={styles.perPage}>
                            <option>10 / page</option>
                        </select>
                    </div>
                </div>
            </section>
        </main>
    );
}

const styles = {
    page: {
        minHeight: "100vh",
        background: "#f6f8fc",
        padding: "28px 32px",
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
        minHeight: 126,
        borderBottom: "1px solid #e6ebf3",
        padding: "34px 36px",
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
        margin: "6px 0 0",
        color: "#51607f",
        fontSize: 16,
    },
    primaryButton: {
        height: 47,
        minWidth: 138,
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
    toolbar: {
        display: "grid",
        gridTemplateColumns: "minmax(320px, 1fr) 150px 162px 134px 104px 54px",
        gap: 18,
        alignItems: "center",
        padding: "28px 32px",
    },
    searchBox: {
        height: 61,
        border: "1px solid #d7dfeb",
        borderRadius: 7,
        display: "flex",
        alignItems: "center",
        gap: 14,
        padding: "0 14px",
    },
    searchInput: {
        border: 0,
        outline: "none",
        flex: 1,
        color: "#243452",
        fontSize: 15,
    },
    selectBox: {
        height: 62,
        border: "1px solid #d7dfeb",
        borderRadius: 7,
        padding: "9px 14px",
        position: "relative",
    },
    selectLabel: {
        display: "block",
        color: "#52617f",
        fontSize: 13,
        marginBottom: 2,
    },
    select: {
        width: "100%",
        border: 0,
        outline: "none",
        appearance: "none",
        background: "transparent",
        color: "#111827",
        fontSize: 16,
    },
    selectIcon: {
        position: "absolute",
        right: 12,
        bottom: 15,
        pointerEvents: "none",
    },
    filterButton: {
        height: 50,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: 10,
        color: "#243452",
        fontSize: 15,
        cursor: "pointer",
    },
    iconButton: {
        width: 54,
        height: 50,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        cursor: "pointer",
    },
    tableWrap: {
        margin: "0 30px",
        border: "1px solid #dfe6f1",
        borderRadius: 8,
        overflow: "hidden",
    },
    table: {
        width: "100%",
        borderCollapse: "collapse",
    },
    th: {
        height: 62,
        background: "#fbfcff",
        borderBottom: "1px solid #e4eaf3",
        color: "#243452",
        fontSize: 14,
        fontWeight: 700,
        textAlign: "left",
        padding: "0 18px",
    },
    thContent: {
        display: "inline-flex",
        alignItems: "center",
        gap: 6,
    },
    tr: {
        borderBottom: "1px solid #e8edf4",
        height: 76,
    },
    nameCell: {
        padding: "0 20px",
        display: "flex",
        alignItems: "center",
        gap: 16,
        color: "#111827",
        fontWeight: 700,
    },
    avatar: {
        width: 43,
        height: 43,
        borderRadius: "50%",
        background: "#eef3ff",
        color: "#164dff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        fontWeight: 800,
    },
    userName: {
        whiteSpace: "nowrap",
    },
    td: {
        padding: "0 18px",
        color: "#334260",
        fontSize: 14,
        whiteSpace: "nowrap",
    },
    statusBadge: {
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        minHeight: 34,
        minWidth: 56,
        borderRadius: 6,
        padding: "0 10px",
        fontSize: 13,
        fontWeight: 800,
    },
    activeBadge: {
        background: "#ecfff2",
        border: "1px solid #a8e0bb",
        color: "#108139",
    },
    inactiveBadge: {
        background: "#f8fafc",
        border: "1px solid #cfd8e5",
        color: "#42516d",
    },
    deactivatedBadge: {
        background: "#fff1f3",
        border: "1px solid #ffb9c6",
        color: "#e11d48",
    },
    actionCell: {
        textAlign: "center",
    },
    actionButton: {
        width: 38,
        height: 42,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        cursor: "pointer",
        color: "#111827",
    },
    footer: {
        height: 86,
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "0 42px",
        color: "#52617f",
        fontSize: 14,
    },
    pagination: {
        display: "flex",
        alignItems: "center",
        gap: 10,
    },
    pageButton: {
        width: 38,
        height: 38,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        cursor: "pointer",
    },
    currentPage: {
        width: 38,
        height: 38,
        borderRadius: 7,
        border: "1px solid #1f4fff",
        background: "#ffffff",
        color: "#1f4fff",
        fontWeight: 800,
    },
    perPage: {
        height: 38,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#ffffff",
        padding: "0 12px",
        color: "#111827",
    },
};

export default ListUser;
