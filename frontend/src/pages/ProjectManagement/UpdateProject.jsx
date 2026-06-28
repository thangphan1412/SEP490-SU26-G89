import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { CancelButton, Icon, InfoAlert, PagePanel, PrimaryButton, styles } from "./ProjectComponents.jsx";

const initialProject = {
    projectName: "Digital Contract Rollout",
    projectCode: "DCR-2025-001",
    department: "IT",
    owner: "Alex Morgan",
    status: "Active",
    priority: "High",
    startDate: "May 01, 2025",
    endDate: "Jul 31, 2025",
    budget: "120,000.00",
    progress: 68,
    location: "New York, USA",
    description: "Enterprise-wide rollout of the digital contract management platform to streamline contract creation, approval, and storage across all departments.",
    completed: false,
};

const fields = [
    ["Project Name", "projectName", "input", null],
    ["Project Code", "projectCode", "input", null],
    ["Responsible Department", "department", "select", "document", ["IT", "Procurement", "HR", "Legal"]],
    ["Project Owner", "owner", "select", "users", ["Alex Morgan", "Jamie Lee", "Taylor Smith"]],
    ["Status", "status", "select", null, ["Active", "Planning", "On Hold", "Completed"]],
    ["Priority", "priority", "select", "flag", ["High", "Medium", "Low"]],
    ["Start Date", "startDate", "input", "calendar"],
    ["End Date", "endDate", "input", "calendar"],
    ["Budget", "budget", "currency", "dollar"],
];

const monitoringItems = [
    ["Budget Tracking", "$81,600.00", "of $120,000.00", "68%", "dollar", "#2450f5"],
    ["Timeline Health", "61 Days Left", "of 91 Days Total", "67%", "calendar", "#16a34a"],
    ["Linked Contracts", "14", "Active Contracts", "View Details →", "document", "#7c3aed"],
    ["Team Activity", "8", "Active Members", "View Team →", "users", "#f97316"],
];

function UpdateProject({ onUpdateProject }) {
    const navigate = useNavigate();
    const [project, setProject] = useState(initialProject);

    const handleChange = (event) => {
        const { name, value, checked, type } = event.target;
        setProject((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        onUpdateProject?.(project);
    };

    const renderField = ([label, name, type, icon, options]) => (
        <div key={name}>
            <label htmlFor={name} style={styles.label}>{label}</label>
            <div style={localStyles.inputWrap}>
                {icon && <span style={localStyles.leftIcon}><Icon name={icon} size={18} color="#53617e" /></span>}
                {type === "select" ? (
                    <>
                        <select id={name} name={name} value={project[name]} onChange={handleChange} style={{ ...styles.input, paddingLeft: icon ? 42 : 14 }}>
                            {options.map((option) => <option key={option}>{option}</option>)}
                        </select>
                        {name === "status" && <span style={localStyles.greenDot} />}
                        {name === "priority" && <span style={localStyles.redDot} />}
                        <span style={localStyles.rightIcon}><Icon name="chevron" size={18} color="#243452" /></span>
                    </>
                ) : type === "currency" ? (
                    <div style={localStyles.currencyWrap}><span style={localStyles.currencySymbol}>$</span><input id={name} name={name} value={project[name]} onChange={handleChange} style={localStyles.currencyInput} /></div>
                ) : (
                    <input id={name} name={name} value={project[name]} onChange={handleChange} style={{ ...styles.input, paddingLeft: icon ? 42 : 14 }} />
                )}
            </div>
        </div>
    );

    return (
        <PagePanel
            title="Update Project"
            description="Update project details, progress, budget, and closure status."
            action={<div style={styles.actions}><CancelButton onClick={() => navigate("/project-management/list")} /><PrimaryButton type="submit"><Icon name="save" size={19} color="#fff" />Save Changes</PrimaryButton></div>}
        >
            <form onSubmit={handleSubmit}>
                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Project Information</h2>
                    <div style={localStyles.formGrid}>{fields.map(renderField)}</div>
                    <div style={localStyles.progressGroup}>
                        <label style={styles.label}>Progress Percentage</label>
                        <div style={localStyles.progressInputRow}>
                            <span style={localStyles.progressValue}>{project.progress}%</span>
                            <input type="range" name="progress" min="0" max="100" value={project.progress} onChange={handleChange} style={localStyles.range} />
                        </div>
                        <div style={localStyles.rangeLabels}><span>0%</span><span>100%</span></div>
                    </div>
                    <div style={localStyles.fullWidth}>
                        <label htmlFor="location" style={styles.label}>Location</label>
                        <div style={localStyles.inputWrap}><span style={localStyles.leftIcon}><Icon name="location" size={18} color="#53617e" /></span><input id="location" name="location" value={project.location} onChange={handleChange} style={{ ...styles.input, paddingLeft: 42 }} /></div>
                    </div>
                    <div style={localStyles.fullWidth}>
                        <label htmlFor="description" style={styles.label}>Description</label>
                        <textarea id="description" name="description" value={project.description} onChange={handleChange} style={styles.textarea} />
                        <div style={localStyles.counter}>135 / 500</div>
                    </div>
                    <label style={localStyles.completedRow}>
                        <input type="checkbox" name="completed" checked={project.completed} onChange={handleChange} style={localStyles.checkbox} />
                        <span style={localStyles.switchTrack}><span style={localStyles.switchThumb} /></span>
                        <span><strong>Mark as Completed</strong><small style={localStyles.completedText}>Mark this project as completed. This will set the end date to today, change the status to Completed, and stop active tracking.</small></span>
                    </label>
                </section>

                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Project Monitoring</h2>
                    <div style={localStyles.monitorGrid}>
                        {monitoringItems.map(([title, value, description, foot, icon, color]) => (
                            <div key={title} style={localStyles.monitorItem}>
                                <span style={{ ...styles.iconCircle, background: `${color}18` }}><Icon name={icon} size={28} color={color} /></span>
                                <div style={localStyles.monitorText}><p style={localStyles.monitorTitle}>{title}</p><h3 style={localStyles.monitorValue}>{value}</h3><p style={localStyles.monitorDescription}>{description}</p>{foot.includes("%") ? <div style={localStyles.smallProgress}><span style={{ ...localStyles.smallProgressFill, width: foot, background: color }} /></div> : <p style={{ ...localStyles.monitorLink, color }}>{foot}</p>}</div>
                            </div>
                        ))}
                    </div>
                </section>
                <InfoAlert>Please review all changes carefully before saving. Updates will take effect immediately.</InfoAlert>
            </form>
        </PagePanel>
    );
}

const localStyles = {
    formGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: 26, rowGap: 16 },
    inputWrap: { position: "relative" },
    leftIcon: { position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", display: "flex", zIndex: 1 },
    rightIcon: { position: "absolute", right: 12, top: "50%", transform: "translateY(-50%)", display: "flex", pointerEvents: "none" },
    greenDot: { position: "absolute", left: 15, top: "50%", transform: "translateY(-50%)", width: 9, height: 9, borderRadius: "50%", background: "#22c55e" },
    redDot: { position: "absolute", left: 37, top: "50%", transform: "translateY(-50%)", width: 9, height: 9, borderRadius: "50%", background: "#ef4444" },
    currencyWrap: { height: 38, border: "1px solid #d5deeb", borderRadius: 6, display: "grid", gridTemplateColumns: "32px 1fr", overflow: "hidden" },
    currencySymbol: { display: "flex", alignItems: "center", justifyContent: "center", borderRight: "1px solid #e3e9f2", color: "#243452" },
    currencyInput: { border: 0, outline: "none", padding: "0 13px", color: "#243452", fontSize: 14 },
    progressGroup: { gridColumn: "2", gridRow: "5" },
    progressInputRow: { display: "grid", gridTemplateColumns: "58px 1fr", gap: 12, alignItems: "center" },
    progressValue: { height: 38, border: "1px solid #d5deeb", borderRadius: 6, display: "flex", alignItems: "center", justifyContent: "center" },
    range: { width: "100%", accentColor: "#2450f5" },
    rangeLabels: { display: "flex", justifyContent: "space-between", color: "#52617f", fontSize: 12, marginLeft: 70 },
    fullWidth: { gridColumn: "1 / -1", marginTop: 14 },
    counter: { textAlign: "right", color: "#52617f", fontSize: 12, marginTop: 4 },
    completedRow: { marginTop: 18, borderTop: "1px solid #e8edf4", paddingTop: 16, display: "flex", alignItems: "center", gap: 16 },
    checkbox: { display: "none" },
    switchTrack: { width: 47, height: 29, borderRadius: 20, background: "#d8dee9", padding: 3, display: "inline-flex", alignItems: "center" },
    switchThumb: { width: 23, height: 23, borderRadius: "50%", background: "#fff", boxShadow: "0 1px 4px rgba(0,0,0,.18)" },
    completedText: { display: "block", marginTop: 4, color: "#52617f", fontSize: 12, maxWidth: 490 },
    monitorGrid: { display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 18 },
    monitorItem: { border: "1px solid #d9e2ef", borderRadius: 8, padding: "16px", minHeight: 116, display: "flex", gap: 16 },
    monitorText: { minWidth: 0 },
    monitorTitle: { margin: 0, color: "#52617f", fontSize: 12, fontWeight: 700 },
    monitorValue: { margin: "5px 0", fontSize: 20, fontWeight: 800 },
    monitorDescription: { margin: 0, color: "#52617f", fontSize: 12 },
    smallProgress: { marginTop: 14, height: 5, borderRadius: 6, background: "#e9eef8", overflow: "hidden" },
    smallProgressFill: { display: "block", height: "100%", borderRadius: 6 },
    monitorLink: { margin: "12px 0 0", fontSize: 12, fontWeight: 800 },
};

export default UpdateProject;
