import {
    Icon,
    styles,
} from "./ContractComponents.jsx";
import { defaultContractStatuses } from "./contractUtils.js";

function ContractForm({
    contract,
    onChange,
    projects,
    loadingProjects = false,
    creatorReadOnly = false,
}) {
    const statusOptions = defaultContractStatuses.includes(contract.contractStatus)
        ? defaultContractStatuses
        : [contract.contractStatus, ...defaultContractStatuses];
    const containsCurrentProject = projects.some(
        (project) => project.id === contract.projectId
    );

    return (
        <section style={styles.card}>
            <h2 style={styles.cardTitle}>Contract Information</h2>

            <div style={localStyles.formGrid}>
                <TextField
                    label="Contract ID"
                    name="contractNumber"
                    value={contract.contractNumber}
                    onChange={onChange}
                    placeholder="CON-2026-0001"
                    required
                />

                <TextField
                    label="Title"
                    name="contractTitle"
                    value={contract.contractTitle}
                    onChange={onChange}
                    placeholder="Enter contract title"
                    required
                />

                <div>
                    <label htmlFor="projectId" style={styles.label}>Project</label>

                    <div style={localStyles.inputWrap}>
                        <select
                            id="projectId"
                            name="projectId"
                            value={contract.projectId}
                            onChange={onChange}
                            style={styles.input}
                            disabled={loadingProjects}
                            required
                        >
                            <option value="">
                                {loadingProjects ? "Loading projects..." : "Select project"}
                            </option>

                            {contract.projectId && !containsCurrentProject && (
                                <option value={contract.projectId}>
                                    Current project ({contract.projectId})
                                </option>
                            )}

                            {projects.map((project) => (
                                <option key={project.id} value={project.id}>
                                    {project.projectCode ? `${project.projectCode} - ` : ""}
                                    {project.projectName || project.id}
                                </option>
                            ))}
                        </select>

                        <span style={localStyles.rightIcon}>
                            <Icon name="chevron" size={18} color="#243452" />
                        </span>
                    </div>
                </div>

                <SelectField
                    label="Status"
                    name="contractStatus"
                    value={contract.contractStatus}
                    onChange={onChange}
                    options={statusOptions}
                />

                <TextField
                    label="Effective Date"
                    name="effectiveDate"
                    type="date"
                    value={contract.effectiveDate}
                    onChange={onChange}
                    icon="calendar"
                />

                <TextField
                    label="Expiration Date"
                    name="expirationDate"
                    type="date"
                    value={contract.expirationDate}
                    onChange={onChange}
                    icon="calendar"
                />

                <TextField
                    label="Created By"
                    name="contractCreatedBy"
                    value={contract.contractCreatedBy}
                    onChange={onChange}
                    placeholder="Current user"
                    readOnly={creatorReadOnly}
                />
            </div>
        </section>
    );
}

function TextField({
    label,
    name,
    value,
    onChange,
    placeholder,
    icon,
    type = "text",
    required = false,
    readOnly = false,
}) {
    return (
        <div>
            <label htmlFor={name} style={styles.label}>{label}</label>

            <div style={localStyles.inputWrap}>
                {icon && (
                    <span style={localStyles.leftIcon}>
                        <Icon name={icon} size={18} color="#53617e" />
                    </span>
                )}

                <input
                    id={name}
                    name={name}
                    type={type}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    required={required}
                    readOnly={readOnly}
                    style={{
                        ...styles.input,
                        paddingLeft: icon ? 42 : 13,
                        background: readOnly ? "#f8fafc" : "#ffffff",
                    }}
                />
            </div>
        </div>
    );
}

function SelectField({ label, name, value, onChange, options }) {
    return (
        <div>
            <label htmlFor={name} style={styles.label}>{label}</label>

            <div style={localStyles.inputWrap}>
                <select
                    id={name}
                    name={name}
                    value={value}
                    onChange={onChange}
                    style={styles.input}
                >
                    {options.map((option) => (
                        <option key={option} value={option}>{option}</option>
                    ))}
                </select>

                <span style={localStyles.rightIcon}>
                    <Icon name="chevron" size={18} color="#243452" />
                </span>
            </div>
        </div>
    );
}

const localStyles = {
    formGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
        columnGap: 30,
        rowGap: 18,
    },
    inputWrap: { position: "relative" },
    leftIcon: {
        position: "absolute",
        left: 12,
        top: "50%",
        transform: "translateY(-50%)",
        display: "flex",
        zIndex: 1,
    },
    rightIcon: {
        position: "absolute",
        right: 12,
        top: "50%",
        transform: "translateY(-50%)",
        display: "flex",
        pointerEvents: "none",
    },
};

export default ContractForm;
