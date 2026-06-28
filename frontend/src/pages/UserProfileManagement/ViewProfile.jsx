import { useNavigate } from "react-router-dom";

const userProfile = {
    fullName: "Alex Morgan",
    email: "alex.morgan@econtract.com",
    phoneNumber: "+84 28 3822 5678",
    department: "Legal Department",
    position: "Contract Manager",
    employeeId: "EMP-00023",
    dateJoined: "March 15, 2023",
    timeZone: "(GMT+07:00) Bangkok, Hanoi, Jakarta",
    language: "English",
    accountStatus: "Active",
    defaultSignature: "Default Work Signature",
    lastUpdated: "May 22, 2025",
};

const profileDetails = [
    ["Full Name", userProfile.fullName],
    ["Email", userProfile.email],
    ["Phone Number", userProfile.phoneNumber],
    ["Department", userProfile.department],
    ["Position", userProfile.position],
    ["Employee ID", userProfile.employeeId],
    ["Date Joined", userProfile.dateJoined],
    ["Time Zone", userProfile.timeZone],
    ["Language", userProfile.language],
    ["Account Status", userProfile.accountStatus, "success"],
    ["Default Signature", userProfile.defaultSignature],
    ["Last Updated", userProfile.lastUpdated],
];

const accessSummaryItems = [
    {
        label: "Role",
        value: "Contract Manager",
        description: "Assigned role",
        icon: "user",
    },
    {
        label: "Permissions",
        value: "18 active",
        description: "Access permissions",
        icon: "shield",
    },
    {
        label: "Signature Library",
        value: "6 signatures",
        description: "Personal e-signatures",
        icon: "edit",
    },
    {
        label: "Last Login",
        value: "May 22, 2025",
        description: "Recent account activity",
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
        user: (
            <>
                <circle cx="12" cy="8" r="4" />
                <path d="M4 21c1.6-4 4.2-6 8-6s6.4 2 8 6" />
            </>
        ),
        edit: (
            <>
                <path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z" />
                <path d="m13.5 6.5 4 4" />
            </>
        ),
        shield: (
            <>
                <path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" />
                <path d="m9 12 2 2 4-4" />
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

function AvatarBlock() {
    return (
        <div style={styles.avatarBlock}>
            <div style={styles.avatarWrap}>
                <div style={styles.avatar}>AM</div>
                <div style={styles.avatarBadge}>
                    <Icon name="edit" size={17} color="#53617e" />
                </div>
            </div>
            <h3 style={styles.avatarName}>{userProfile.fullName}</h3>
            <p style={styles.avatarRole}>{userProfile.position}</p>
        </div>
    );
}

function ViewProfile() {
    const navigate = useNavigate();

    return (
        <main style={styles.page}>
            <section style={styles.panel}>
                <div style={styles.header}>
                    <div>
                        <h1 style={styles.pageTitle}>My Profile</h1>
                        <p style={styles.pageDescription}>
                            View your personal information and account details.
                        </p>
                    </div>
                    <button
                        type="button"
                        style={styles.primaryButton}
                        onClick={() => navigate("/user-profile/update")}
                    >
                        <Icon name="edit" size={19} color="#ffffff" />
                        Edit Profile
                    </button>
                </div>

                <section style={styles.card}>
                    <div style={styles.cardHeader}>
                        <div style={styles.cardTitleGroup}>
                            <span style={styles.roundIcon}>
                                <Icon name="user" size={22} />
                            </span>
                            <h2 style={styles.cardTitle}>Personal Information</h2>
                        </div>
                        <span style={styles.statusBadge}>Active</span>
                    </div>

                    <div style={styles.profileBody}>
                        <AvatarBlock />
                        <div style={styles.detailGrid}>
                            {profileDetails.map(([label, value, tone]) => (
                                <div key={label} style={styles.detailRow}>
                                    <span style={styles.detailLabel}>{label}</span>
                                    <span style={styles.colon}>:</span>
                                    <span
                                        style={{
                                            ...styles.detailValue,
                                            ...(tone === "success"
                                                ? styles.successText
                                                : {}),
                                        }}
                                    >
                                        {value}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                </section>

                <section style={styles.card}>
                    <div style={styles.cardHeader}>
                        <div style={styles.cardTitleGroup}>
                            <span style={styles.roundIcon}>
                                <Icon name="shield" size={22} />
                            </span>
                            <div>
                                <h2 style={styles.cardTitle}>
                                    Account & Access Summary
                                </h2>
                                <p style={styles.cardDescription}>
                                    Your profile is used across contracts, approvals,
                                    and internal workflows.
                                </p>
                            </div>
                        </div>
                        <span style={styles.statusBadge}>Verified</span>
                    </div>

                    <div style={styles.summaryGrid}>
                        {accessSummaryItems.map((item) => (
                            <div key={item.label} style={styles.summaryItem}>
                                <span style={styles.summaryIcon}>
                                    <Icon name={item.icon} size={22} />
                                </span>
                                <div>
                                    <p style={styles.summaryLabel}>{item.label}</p>
                                    <h3 style={styles.summaryValue}>{item.value}</h3>
                                    <p style={styles.summaryDescription}>
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
                        To update your personal information, click Edit Profile.
                    </span>
                </section>
            </section>
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
    primaryButton: {
        height: 43,
        minWidth: 166,
        border: 0,
        borderRadius: 7,
        background: "#2450f5",
        color: "#ffffff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: 10,
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
        minWidth: 86,
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
        gridTemplateColumns: "245px 1fr",
        gap: 38,
        padding: "16px 38px 28px",
    },
    avatarBlock: {
        borderRight: "1px solid #e3e9f2",
        minHeight: 276,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        paddingRight: 38,
    },
    avatarWrap: {
        position: "relative",
        marginBottom: 16,
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
        boxShadow: "0 0 0 4px #edf2ff",
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
    avatarName: {
        margin: 0,
        fontSize: 17,
        fontWeight: 800,
    },
    avatarRole: {
        margin: "7px 0 0",
        color: "#53617e",
        fontSize: 13,
    },
    detailGrid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        columnGap: 50,
        rowGap: 24,
        alignContent: "center",
        minWidth: 0,
    },
    detailRow: {
        display: "grid",
        gridTemplateColumns: "140px 18px minmax(180px, 1fr)",
        alignItems: "center",
        fontSize: 15,
        minWidth: 0,
    },
    detailLabel: {
        color: "#52617f",
    },
    colon: {
        color: "#52617f",
        textAlign: "center",
    },
    detailValue: {
        color: "#111827",
        whiteSpace: "nowrap",
    },
    successText: {
        color: "#13833b",
        fontWeight: 700,
    },
    summaryGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(4, 1fr)",
        gap: 18,
        padding: "18px 22px 32px",
    },
    summaryItem: {
        border: "1px solid #d9e2ef",
        borderRadius: 8,
        minHeight: 92,
        padding: "16px",
        display: "flex",
        alignItems: "center",
        gap: 16,
    },
    summaryIcon: {
        width: 42,
        height: 42,
        borderRadius: "50%",
        background: "#eef3ff",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        flexShrink: 0,
    },
    summaryLabel: {
        margin: 0,
        color: "#52617f",
        fontSize: 13,
    },
    summaryValue: {
        margin: "4px 0",
        fontSize: 21,
        fontWeight: 800,
        whiteSpace: "nowrap",
    },
    summaryDescription: {
        margin: 0,
        color: "#6b7894",
        fontSize: 12,
    },
    infoAlert: {
        margin: "8px 32px 28px",
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

export default ViewProfile;
