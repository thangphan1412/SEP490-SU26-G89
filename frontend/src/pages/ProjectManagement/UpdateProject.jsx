import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { CancelButton, Icon, InfoAlert, PagePanel, PrimaryButton } from "./ProjectComponents.jsx";
import "./UpdateProject.css";

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
    ["Budget Tracking", "$81,600.00", "of $120,000.00", "68%", "dollar", "blue"],
    ["Timeline Health", "61 Days Left", "of 91 Days Total", "67%", "calendar", "green"],
    ["Linked Contracts", "14", "Active Contracts", "View Details →", "document", "purple"],
    ["Team Activity", "8", "Active Members", "View Team →", "users", "orange"],
];

const monitorColors = { blue: "#2450f5", green: "#16a34a", purple: "#7c3aed", orange: "#f97316" };

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
            <label htmlFor={name} className="project-field-label">{label}</label>
            <div className="update-project-input-wrap">
                {icon && <span className="update-project-left-icon"><Icon name={icon} size={18} color="#53617e" /></span>}
                {type === "select" ? (
                    <>
                        <select id={name} name={name} value={project[name]} onChange={handleChange} className={`project-input ${icon ? "update-project-input-with-icon" : ""}`}>
                            {options.map((option) => <option key={option}>{option}</option>)}
                        </select>
                        {name === "status" && <span className="update-project-green-dot" />}
                        {name === "priority" && <span className="update-project-red-dot" />}
                        <span className="update-project-right-icon"><Icon name="chevron" size={18} color="#243452" /></span>
                    </>
                ) : type === "currency" ? (
                    <div className="update-project-currency-wrap"><span className="update-project-currency-symbol">$</span><input id={name} name={name} value={project[name]} onChange={handleChange} className="update-project-currency-input" /></div>
                ) : (
                    <input id={name} name={name} value={project[name]} onChange={handleChange} className={`project-input ${icon ? "update-project-input-with-icon" : ""}`} />
                )}
            </div>
        </div>
    );

    return (
        <PagePanel
            title="Update Project"
            description="Update project details, progress, budget, and closure status."
            action={<div className="project-actions"><CancelButton onClick={() => navigate("/project-management/list")} /><PrimaryButton type="submit"><Icon name="save" size={19} color="#fff" />Save Changes</PrimaryButton></div>}
        >
            <form onSubmit={handleSubmit}>
                <section className="project-card">
                    <h2 className="project-card-title">Project Information</h2>
                    <div className="update-project-form-grid">{fields.map(renderField)}</div>
                    <div className="update-project-progress-group">
                        <label className="project-field-label">Progress Percentage</label>
                        <div className="update-project-progress-row">
                            <span className="update-project-progress-value">{project.progress}%</span>
                            <input type="range" name="progress" min="0" max="100" value={project.progress} onChange={handleChange} className="update-project-range" />
                        </div>
                        <div className="update-project-range-labels"><span>0%</span><span>100%</span></div>
                    </div>
                    <div className="update-project-full-width">
                        <label htmlFor="location" className="project-field-label">Location</label>
                        <div className="update-project-input-wrap"><span className="update-project-left-icon"><Icon name="location" size={18} color="#53617e" /></span><input id="location" name="location" value={project.location} onChange={handleChange} className="project-input update-project-input-with-icon" /></div>
                    </div>
                    <div className="update-project-full-width">
                        <label htmlFor="description" className="project-field-label">Description</label>
                        <textarea id="description" name="description" value={project.description} onChange={handleChange} className="project-textarea" />
                        <div className="update-project-counter">135 / 500</div>
                    </div>
                    <label className="update-project-completed-row">
                        <input type="checkbox" name="completed" checked={project.completed} onChange={handleChange} className="update-project-checkbox" />
                        <span className="update-project-switch-track"><span className="update-project-switch-thumb" /></span>
                        <span><strong>Mark as Completed</strong><small className="update-project-completed-text">Mark this project as completed. This will set the end date to today, change the status to Completed, and stop active tracking.</small></span>
                    </label>
                </section>

                <section className="project-card">
                    <h2 className="project-card-title">Project Monitoring</h2>
                    <div className="update-project-monitor-grid">
                        {monitoringItems.map(([title, value, description, foot, icon, theme]) => (
                            <div key={title} className="update-project-monitor-item">
                                <span className={`project-icon-circle update-project-icon--${theme}`}><Icon name={icon} size={28} color={monitorColors[theme]} /></span>
                                <div className="update-project-monitor-text"><p className="update-project-monitor-title">{title}</p><h3 className="update-project-monitor-value">{value}</h3><p className="update-project-monitor-description">{description}</p>{foot.includes("%") ? <div className="update-project-small-progress"><span className={`update-project-small-progress-fill update-project-progress-fill--${foot.replace("%", "")} update-project-fill--${theme}`} /></div> : <p className={`update-project-monitor-link update-project-link--${theme}`}>{foot}</p>}</div>
                            </div>
                        ))}
                    </div>
                </section>
                <InfoAlert>Please review all changes carefully before saving. Updates will take effect immediately.</InfoAlert>
            </form>
        </PagePanel>
    );
}

export default UpdateProject;
