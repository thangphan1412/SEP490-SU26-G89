import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { CancelButton, Icon, InfoAlert, PagePanel, PrimaryButton, styles } from "./ProjectComponents.jsx";

const initialProject = {
    projectName: "",
    projectCode: "",
    department: "",
    owner: "",
    status: "Planning",
    priority: "High",
    startDate: "May 22, 2025",
    endDate: "Aug 22, 2025",
    budget: "",
    location: "",
    description: "",
};

const formFields = [
    ["Project Name", "projectName", "input", "Enter project name"],
    ["Project Code", "projectCode", "input", "Enter project code"],
    ["Responsible Department", "department", "select", "Select department", ["IT", "Procurement", "HR", "Legal", "Operations"]],
    ["Project Owner", "owner", "select", "Select owner", ["Alex Morgan", "Jamie Lee", "Taylor Smith"]],
    ["Status", "status", "select", "Planning", ["Planning", "Active", "On Hold"]],
    ["Priority", "priority", "select", "High", ["High", "Medium", "Low"]],
    ["Start Date", "startDate", "date", "May 22, 2025"],
    ["End Date", "endDate", "date", "Aug 22, 2025"],
    ["Budget", "budget", "currency", "Enter budget amount"],
    ["Location / Event Venue", "location", "input", "Enter location or venue"],
];

const scopeItems = [
    ["Contract Linkage", "Link related contracts and documents to this project.", "link"],
    ["Team Assignment", "Assign internal and external team members.", "users"],
    ["Timeline Setup", "Define key milestones and project schedule.", "calendar"],
    ["Approval Flow", "Configure approval workflow and authorization.", "shield"],
];

function CreateProject({ onCreateProject }) {
    const navigate = useNavigate();
    const [project, setProject] = useState(initialProject);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setProject((currentProject) => ({ ...currentProject, [name]: value }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        onCreateProject?.(project);
    };

    const renderField = ([label, name, type, placeholder, options]) => (
        <div key={name}>
            <label htmlFor={name} style={styles.label}>{label}</label>
            <div style={localStyles.inputWrap}>
                {type === "select" ? (
                    <>
                        <select id={name} name={name} value={project[name]} onChange={handleChange} style={styles.input}>
                            <option value="">{placeholder}</option>
                            {options.map((option) => <option key={option}>{option}</option>)}
                        </select>
                        <span style={localStyles.rightIcon}><Icon name="chevron" size={18} color="#243452" /></span>
                    </>
                ) : type === "currency" ? (
                    <div style={localStyles.currencyWrap}>
                        <span style={localStyles.currencySymbol}>$</span>
                        <input id={name} name={name} value={project[name]} onChange={handleChange} placeholder={placeholder} style={localStyles.currencyInput} />
                    </div>
                ) : (
                    <>
                        <input id={name} name={name} value={project[name]} onChange={handleChange} placeholder={placeholder} style={styles.input} />
                        {type === "date" && <span style={localStyles.rightIcon}><Icon name="calendar" size={18} color="#53617e" /></span>}
                    </>
                )}
            </div>
        </div>
    );

    return (
        <PagePanel
            title="Create Project"
            description="Set up a new project, event, or initiative and assign ownership."
            action={<div style={styles.actions}><CancelButton onClick={() => navigate("/project-management/list")} /><PrimaryButton type="submit"><span>Create Project</span></PrimaryButton></div>}
        >
            <form onSubmit={handleSubmit}>
                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Project Information</h2>
                    <div style={localStyles.formGrid}>{formFields.map(renderField)}</div>
                    <div style={localStyles.fullWidth}>
                        <label htmlFor="description" style={styles.label}>Description</label>
                        <textarea id="description" name="description" value={project.description} onChange={handleChange} placeholder="Enter project description..." style={styles.textarea} />
                        <div style={localStyles.counter}>0 / 1000</div>
                    </div>
                </section>

                <section style={styles.card}>
                    <h2 style={styles.cardTitle}>Project Scope & Tracking</h2>
                    <div style={localStyles.scopeGrid}>
                        {scopeItems.map(([title, description, icon]) => (
                            <div key={title} style={localStyles.scopeItem}>
                                <span style={styles.iconCircle}><Icon name={icon} size={27} /></span>
                                <div><h3 style={localStyles.scopeTitle}>{title}</h3><p style={localStyles.scopeText}>{description}</p></div>
                            </div>
                        ))}
                    </div>
                </section>
                <InfoAlert>Please review the project information before creating the record.</InfoAlert>
            </form>
        </PagePanel>
    );
}

const localStyles = {
    formGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: 54, rowGap: 18 },
    inputWrap: { position: "relative" },
    rightIcon: { position: "absolute", right: 12, top: "50%", transform: "translateY(-50%)", display: "flex", pointerEvents: "none" },
    currencyWrap: { height: 38, border: "1px solid #d5deeb", borderRadius: 6, display: "grid", gridTemplateColumns: "32px 1fr", overflow: "hidden" },
    currencySymbol: { display: "flex", alignItems: "center", justifyContent: "center", borderRight: "1px solid #e3e9f2", color: "#243452" },
    currencyInput: { border: 0, outline: "none", padding: "0 13px", color: "#243452", fontSize: 14 },
    fullWidth: { marginTop: 18 },
    counter: { textAlign: "right", color: "#52617f", fontSize: 13, marginTop: 5 },
    scopeGrid: { display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 22 },
    scopeItem: { border: "1px solid #d9e2ef", borderRadius: 8, padding: "16px", minHeight: 90, display: "flex", alignItems: "center", gap: 16 },
    scopeTitle: { margin: "0 0 5px", fontSize: 15, fontWeight: 800 },
    scopeText: { margin: 0, color: "#52617f", fontSize: 13, lineHeight: 1.45 },
};

export default CreateProject;
