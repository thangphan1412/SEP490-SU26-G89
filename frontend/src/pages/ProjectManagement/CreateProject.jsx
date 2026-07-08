import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { CancelButton, Icon, InfoAlert, PagePanel, PrimaryButton } from "./ProjectComponents.jsx";
import "./CreateProject.css";

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
            <label htmlFor={name} className="project-field-label">{label}</label>
            <div className="create-project-input-wrap">
                {type === "select" ? (
                    <>
                        <select id={name} name={name} value={project[name]} onChange={handleChange} className="project-input">
                            <option value="">{placeholder}</option>
                            {options.map((option) => <option key={option}>{option}</option>)}
                        </select>
                        <span className="create-project-right-icon"><Icon name="chevron" size={18} color="#243452" /></span>
                    </>
                ) : type === "currency" ? (
                    <div className="create-project-currency-wrap">
                        <span className="create-project-currency-symbol">$</span>
                        <input id={name} name={name} value={project[name]} onChange={handleChange} placeholder={placeholder} className="create-project-currency-input" />
                    </div>
                ) : (
                    <>
                        <input id={name} name={name} value={project[name]} onChange={handleChange} placeholder={placeholder} className="project-input" />
                        {type === "date" && <span className="create-project-right-icon"><Icon name="calendar" size={18} color="#53617e" /></span>}
                    </>
                )}
            </div>
        </div>
    );

    return (
        <PagePanel
            title="Create Project"
            description="Set up a new project, event, or initiative and assign ownership."
            action={<div className="project-actions"><CancelButton onClick={() => navigate("/project-management/list")} /><PrimaryButton type="submit"><span>Create Project</span></PrimaryButton></div>}
        >
            <form onSubmit={handleSubmit}>
                <section className="project-card">
                    <h2 className="project-card-title">Project Information</h2>
                    <div className="create-project-form-grid">{formFields.map(renderField)}</div>
                    <div className="create-project-full-width">
                        <label htmlFor="description" className="project-field-label">Description</label>
                        <textarea id="description" name="description" value={project.description} onChange={handleChange} placeholder="Enter project description..." className="project-textarea" />
                        <div className="create-project-counter">0 / 1000</div>
                    </div>
                </section>

                <section className="project-card">
                    <h2 className="project-card-title">Project Scope & Tracking</h2>
                    <div className="create-project-scope-grid">
                        {scopeItems.map(([title, description, icon]) => (
                            <div key={title} className="create-project-scope-item">
                                <span className="project-icon-circle"><Icon name={icon} size={27} /></span>
                                <div><h3 className="create-project-scope-title">{title}</h3><p className="create-project-scope-text">{description}</p></div>
                            </div>
                        ))}
                    </div>
                </section>
                <InfoAlert>Please review the project information before creating the record.</InfoAlert>
            </form>
        </PagePanel>
    );
}

export default CreateProject;
