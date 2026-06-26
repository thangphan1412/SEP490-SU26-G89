import { useNavigate } from "react-router-dom";
import { Icon, InfoAlert, PagePanel, PrimaryButton, StatusBadge, styles } from "./ProjectComponents.jsx";

const overviewLeft = [
    ["document", "Project Name", "Digital Contract Rollout"],
    ["building", "Department", "IT"],
    ["users", "Project Owner", "Alex Morgan"],
    ["dollar", "Status", "Active", "badge"],
    ["flag", "Priority", "High", "priority"],
];

const overviewMiddle = [
    ["calendar", "Start Date", "May 01, 2025"],
    ["calendar", "End Date", "Jul 31, 2025"],
    ["dollar", "Budget", "$120,000"],
    ["location", "Location", "New York, NY, USA"],
];

const metricItems = [
    ["Milestones", "8", "Total Milestones", "flag"],
    ["Team Members", "6", "Active Members", "users"],
    ["Open Tasks", "12", "Tasks Remaining", "task"],
    ["Spent Budget", "$81,600", "68% of Budget", "dollar"],
];

const documents = [
    ["document", "Master Service Agreement", "Contract", "Signed", "Alex Morgan", "May 10, 2025"],
    ["document", "NDA with Supplier A", "Contract", "In Review", "Morgan Lee", "May 08, 2025"],
    ["document", "Procurement Policy Update", "Document", "Approved", "Taylor Smith", "May 06, 2025"],
    ["document", "Implementation Checklist", "Document", "Active", "Jamie Lee", "May 05, 2025"],
    ["document", "Vendor Onboarding Contract", "Contract", "Draft", "Jordan Kim", "May 03, 2025"],
    ["document", "Rollout Timeline", "Document", "Completed", "Casey Brown", "May 01, 2025"],
];

function DetailRow({ icon, label, value, type }) {
    return (
        <div style={localStyles.detailRow}>
            <Icon name={icon} size={20} />
            <span style={localStyles.detailLabel}>{label}</span>
            {type === "badge" ? <StatusBadge status={value} /> : type === "priority" ? <StatusBadge status="On Hold" /> : <span style={localStyles.detailValue}>{value}</span>}
        </div>
    );
}

function ViewProject() {
    const navigate = useNavigate();

    return (
        <PagePanel
            title="Project Details"
            description="Review project information, progress, and related contract documents."
            action={<PrimaryButton onClick={() => navigate("/project-management/update")}><Icon name="edit" size={20} color="#ffffff" />Edit Project</PrimaryButton>}
        >
            <section style={styles.card}>
                <h2 style={styles.cardTitle}>Project Overview</h2>
                <div style={localStyles.overviewGrid}>
                    <div style={localStyles.detailColumn}>{overviewLeft.map((item) => <DetailRow key={item[1]} icon={item[0]} label={item[1]} value={item[2]} type={item[3]} />)}</div>
                    <div style={localStyles.detailColumn}>{overviewMiddle.map((item) => <DetailRow key={item[1]} icon={item[0]} label={item[1]} value={item[2]} />)}</div>
                    <div style={localStyles.progressColumn}>
                        <p style={localStyles.progressTitle}>Progress</p>
                        <div style={localStyles.progressRow}><span style={localStyles.progressBar}><span style={localStyles.progressFill} /></span><strong>68%</strong></div>
                        <p style={localStyles.descriptionTitle}>Description</p>
                        <p style={localStyles.descriptionText}>Company-wide initiative to implement and standardize digital contract management processes across all departments. This project includes system rollout, policy updates, training, and vendor onboarding.</p>
                    </div>
                </div>
                <div style={localStyles.metricGrid}>
                    {metricItems.map(([label, value, description, icon]) => (
                        <div key={label} style={localStyles.metricItem}>
                            <span style={styles.iconCircle}><Icon name={icon} size={28} /></span>
                            <div><p style={localStyles.metricLabel}>{label}</p><h3 style={localStyles.metricValue}>{value}</h3><p style={localStyles.metricDescription}>{description}</p></div>
                        </div>
                    ))}
                </div>
            </section>

            <section style={styles.card}>
                <h2 style={{ ...styles.cardTitle, marginBottom: 4 }}>Related Contracts & Documents</h2>
                <p style={localStyles.subText}>All contracts and documents associated with this project.</p>
                <div style={localStyles.tableWrap}>
                    <table style={localStyles.table}>
                        <thead>
                            <tr>{["Document Name", "Type", "Status", "Owner", "Last Updated", "Actions"].map((h) => <th key={h} style={localStyles.th}>{h}</th>)}</tr>
                        </thead>
                        <tbody>
                            {documents.map(([icon, name, type, status, owner, updated]) => (
                                <tr key={name} style={localStyles.tr}>
                                    <td style={localStyles.nameCell}><Icon name={icon} size={22} />{name}</td>
                                    <td style={localStyles.td}>{type}</td>
                                    <td style={localStyles.td}><StatusBadge status={status} /></td>
                                    <td style={localStyles.td}>{owner}</td>
                                    <td style={localStyles.td}>{updated}</td>
                                    <td style={localStyles.td}><button type="button" style={localStyles.actionButton}><Icon name="dots" size={18} color="#111827" /></button></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </section>
            <InfoAlert>To make changes to this project, click <strong>Edit Project</strong>.</InfoAlert>
        </PagePanel>
    );
}

const localStyles = {
    overviewGrid: { display: "grid", gridTemplateColumns: "1fr 1fr 1.15fr", gap: 28 },
    detailColumn: { borderRight: "1px solid #e3e9f2", paddingRight: 28 },
    detailRow: { minHeight: 38, display: "grid", gridTemplateColumns: "28px 130px 1fr", alignItems: "center", gap: 10, color: "#243452", fontSize: 13 },
    detailLabel: { color: "#52617f", fontWeight: 700 },
    detailValue: { color: "#243452", fontWeight: 600 },
    progressColumn: { paddingLeft: 4 },
    progressTitle: { margin: "0 0 12px", color: "#52617f", fontWeight: 700 },
    progressRow: { display: "grid", gridTemplateColumns: "1fr 42px", alignItems: "center", gap: 14, marginBottom: 24 },
    progressBar: { height: 8, borderRadius: 8, background: "#e9eef8", overflow: "hidden" },
    progressFill: { display: "block", width: "68%", height: "100%", background: "#2450f5", borderRadius: 8 },
    descriptionTitle: { margin: "0 0 6px", color: "#52617f", fontWeight: 700 },
    descriptionText: { margin: 0, color: "#52617f", fontSize: 13, lineHeight: 1.55 },
    metricGrid: { display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 18, marginTop: 28 },
    metricItem: { border: "1px solid #d9e2ef", borderRadius: 8, padding: "18px", display: "flex", alignItems: "center", gap: 16 },
    metricLabel: { margin: 0, color: "#52617f", fontSize: 12 },
    metricValue: { margin: "4px 0", fontSize: 24, fontWeight: 800 },
    metricDescription: { margin: 0, color: "#52617f", fontSize: 12 },
    subText: { margin: "0 0 18px", color: "#52617f", fontSize: 14 },
    tableWrap: { border: "1px solid #dfe6f1", borderRadius: 8, overflow: "hidden" },
    table: { width: "100%", borderCollapse: "collapse" },
    th: { height: 46, background: "#fbfcff", borderBottom: "1px solid #e4eaf3", color: "#243452", fontSize: 13, textAlign: "left", padding: "0 18px" },
    tr: { borderBottom: "1px solid #e8edf4", height: 47 },
    nameCell: { padding: "0 18px", display: "flex", alignItems: "center", gap: 12, fontWeight: 700, fontSize: 13 },
    td: { padding: "0 18px", color: "#334260", fontSize: 13 },
    actionButton: { width: 32, height: 30, borderRadius: 6, border: "1px solid #d7dfeb", background: "#fff", color: "#111827" },
};

export default ViewProject;
