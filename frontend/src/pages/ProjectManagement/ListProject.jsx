import { useNavigate } from "react-router-dom";
import { Icon, PagePanel, PrimaryButton, StatusBadge, projects, styles } from "./ProjectComponents.jsx";

function ListProject() {
    const navigate = useNavigate();

    return (
        <PagePanel
            title="Projects"
            description="Manage projects, events, timelines, ownership, and related contract documents."
            action={
                <PrimaryButton onClick={() => navigate("/project-management/create")}>
                    <Icon name="plus" size={20} color="#ffffff" />
                    New Project
                </PrimaryButton>
            }
        >
            <div style={localStyles.toolbar}>
                <label style={localStyles.searchBox}>
                    <Icon name="search" size={23} color="#3f4d6f" />
                    <input aria-label="Search projects" placeholder="Search projects..." style={localStyles.searchInput} />
                </label>
                {["Department", "Status"].map((label) => (
                    <label key={label} style={localStyles.selectBox}>
                        <span style={localStyles.selectLabel}>{label}</span>
                        <select style={localStyles.select}>
                            <option>All</option>
                        </select>
                        <span style={localStyles.selectIcon}>
                            <Icon name="chevron" size={18} color="#243452" />
                        </span>
                    </label>
                ))}
                <button type="button" style={localStyles.filterButton}>
                    <Icon name="filter" size={20} color="#243452" />
                    Filters
                </button>
                <button type="button" style={localStyles.iconButton}>
                    <Icon name="refresh" size={22} color="#243452" />
                </button>
            </div>

            <div style={localStyles.tableWrap}>
                <table style={localStyles.table}>
                    <thead>
                        <tr>
                            {["Project Name", "Department", "Owner", "Status", "Start Date", "End Date", "Budget", "Actions"].map((header) => (
                                <th key={header} style={localStyles.th}>
                                    <span style={localStyles.thContent}>
                                        {header}
                                        {header !== "Actions" && <Icon name="sort" size={13} color="#243452" />}
                                    </span>
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {projects.map(([icon, name, department, owner, status, startDate, endDate, budget]) => (
                            <tr key={name} style={localStyles.tr}>
                                <td style={localStyles.projectCell}>
                                    <span style={localStyles.avatar}>
                                        <Icon name={icon} size={21} />
                                    </span>
                                    <span style={localStyles.projectName}>{name}</span>
                                </td>
                                <td style={localStyles.td}>{department}</td>
                                <td style={localStyles.td}>{owner}</td>
                                <td style={localStyles.td}><StatusBadge status={status} /></td>
                                <td style={localStyles.td}>{startDate}</td>
                                <td style={localStyles.td}>{endDate}</td>
                                <td style={localStyles.td}>{budget}</td>
                                <td style={localStyles.actionCell}>
                                    <button type="button" style={localStyles.actionButton} onClick={() => navigate("/project-management/view")}>
                                        <Icon name="dots" size={20} color="#111827" />
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div style={localStyles.footer}>
                <span>Showing 1 to 7 of 7 results</span>
                <div style={localStyles.pagination}>
                    <button type="button" style={localStyles.pageButton}><Icon name="arrowLeft" size={18} color="#243452" /></button>
                    <button type="button" style={localStyles.currentPage}>1</button>
                    <button type="button" style={localStyles.pageButton}><Icon name="arrowRight" size={18} color="#243452" /></button>
                    <select style={localStyles.perPage}><option>10 / page</option></select>
                </div>
            </div>
        </PagePanel>
    );
}

const localStyles = {
    toolbar: { display: "grid", gridTemplateColumns: "minmax(360px, 1fr) 166px 148px 126px 54px", gap: 18, alignItems: "center", padding: "26px 32px" },
    searchBox: { height: 55, border: "1px solid #d7dfeb", borderRadius: 7, display: "flex", alignItems: "center", gap: 14, padding: "0 16px" },
    searchInput: { border: 0, outline: "none", flex: 1, color: "#243452", fontSize: 16 },
    selectBox: { height: 58, border: "1px solid #d7dfeb", borderRadius: 7, padding: "8px 14px", position: "relative" },
    selectLabel: { display: "block", color: "#52617f", fontSize: 12, marginBottom: 2 },
    select: { width: "100%", border: 0, outline: "none", appearance: "none", background: "transparent", color: "#111827", fontSize: 15 },
    selectIcon: { position: "absolute", right: 12, bottom: 15, pointerEvents: "none" },
    filterButton: { height: 55, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 10, color: "#243452", fontSize: 16, cursor: "pointer" },
    iconButton: { width: 54, height: 55, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", cursor: "pointer" },
    tableWrap: { margin: "0 28px", border: "1px solid #dfe6f1", borderRadius: 8, overflow: "hidden" },
    table: { width: "100%", borderCollapse: "collapse" },
    th: { height: 72, background: "#fbfcff", borderBottom: "1px solid #e4eaf3", color: "#243452", fontSize: 14, fontWeight: 700, textAlign: "left", padding: "0 18px" },
    thContent: { display: "inline-flex", alignItems: "center", gap: 6 },
    tr: { borderBottom: "1px solid #e8edf4", height: 81 },
    projectCell: { padding: "0 20px", display: "flex", alignItems: "center", gap: 16, fontWeight: 700, color: "#111827", whiteSpace: "nowrap" },
    avatar: { ...styles.iconCircle, width: 39, height: 39 },
    projectName: { whiteSpace: "nowrap" },
    td: { padding: "0 18px", color: "#334260", fontSize: 14, whiteSpace: "nowrap" },
    actionCell: { textAlign: "center" },
    actionButton: { width: 38, height: 42, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", cursor: "pointer", color: "#111827" },
    footer: { height: 86, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 42px", color: "#52617f", fontSize: 14 },
    pagination: { display: "flex", alignItems: "center", gap: 10 },
    pageButton: { width: 38, height: 38, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", cursor: "pointer" },
    currentPage: { width: 38, height: 38, borderRadius: 7, border: "1px solid #1f4fff", background: "#fff", color: "#1f4fff", fontWeight: 800 },
    perPage: { height: 38, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", padding: "0 12px", color: "#111827" },
};

export default ListProject;
