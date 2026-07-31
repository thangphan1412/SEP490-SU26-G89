import { defaultContractStatuses } from "./contractUtils.js";

function ContractForm({
    contract,
    onChange,
    projects = [],
    contractTypes = [],
    contractTemplates = [],
    loadingProjects = false,
    loadingContractOptions = false,
    creatorReadOnly = false,
}) {
    const statusOptions = defaultContractStatuses.includes(contract.contractStatus)
        ? defaultContractStatuses
        : [contract.contractStatus, ...defaultContractStatuses];
    const containsCurrentProject = projects.some(
        (project) => project.id === contract.projectId
    );
    const filteredTemplates = contractTemplates.filter(
        (template) => template.contractTypeId === contract.contractTypeId
    );
    const selectedTemplate = filteredTemplates.find(
        (template) => template.id === contract.contractTemplateId
    );
    const versions = Array.isArray(selectedTemplate?.versions)
        ? selectedTemplate.versions
        : [];

    return (
        <div className="contract-form-grid">
                <TextField
                    label="Contract Number"
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

                <SelectField
                    label="Project"
                    name="projectId"
                    value={contract.projectId}
                    onChange={onChange}
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
                </SelectField>

                <SelectField
                    label="Contract Type"
                    name="contractTypeId"
                    value={contract.contractTypeId}
                    onChange={onChange}
                    disabled={loadingContractOptions}
                    required
                >
                    <option value="">
                        {loadingContractOptions
                            ? "Loading contract types..."
                            : "Select contract type"}
                    </option>

                    {contractTypes.map((contractType) => (
                        <option key={contractType.id} value={contractType.id}>
                            {contractType.contractTypeCode
                                ? `${contractType.contractTypeCode} - `
                                : ""}
                            {contractType.contractTypeName}
                        </option>
                    ))}
                </SelectField>

                <SelectField
                    label="Contract Template"
                    name="contractTemplateId"
                    value={contract.contractTemplateId}
                    onChange={onChange}
                    disabled={!contract.contractTypeId || loadingContractOptions}
                >
                    <option value="">Start without a template</option>

                    {filteredTemplates.map((template) => (
                        <option key={template.id} value={template.id}>
                            {template.contractTemplateName}
                        </option>
                    ))}
                </SelectField>

                <SelectField
                    label="Template Version"
                    name="contractTemplateVersionId"
                    value={contract.contractTemplateVersionId}
                    onChange={onChange}
                    disabled={!contract.contractTemplateId || versions.length === 0}
                >
                    <option value="">
                        {versions.length === 0
                            ? "No saved versions"
                            : "Do not link a saved version"}
                    </option>

                    {versions.map((version) => (
                        <option key={version.id} value={version.id}>
                            V{version.versionNumber} - {version.versionName}
                        </option>
                    ))}
                </SelectField>

                <SelectField
                    label="Status"
                    name="contractStatus"
                    value={contract.contractStatus}
                    onChange={onChange}
                >
                    {statusOptions.map((option) => (
                        <option key={option} value={option}>{option}</option>
                    ))}
                </SelectField>

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

                <div className="contract-form-full">
                    <label htmlFor="contractContent" className="contract-form-label">
                        Contract Content
                    </label>
                    <textarea
                        id="contractContent"
                        name="contractContent"
                        value={contract.contractContent}
                        onChange={onChange}
                        className="form-control contract-content-editor"
                        placeholder="Enter or edit the reusable contract content..."
                    />
                    <div className="form-text">
                        Selecting a saved version copies its content here. Editing
                        this field does not overwrite that version.
                    </div>
                </div>

                <details className="contract-form-full contract-layout-details">
                    <summary>Advanced layout data (optional)</summary>
                    <textarea
                        id="contractLayoutJson"
                        name="contractLayoutJson"
                        value={contract.contractLayoutJson}
                        onChange={onChange}
                        className="form-control contract-layout-editor"
                        placeholder='{"fields":[]}'
                    />
                </details>

                {contract.contractTemplateId && (
                    <div className="contract-form-full contract-version-option">
                        <label className="form-check">
                            <input
                                className="form-check-input"
                                type="checkbox"
                                name="saveAsTemplateVersion"
                                checked={contract.saveAsTemplateVersion}
                                onChange={onChange}
                            />
                            <span className="form-check-label">
                                Save the edited content as a new reusable version
                                of this template
                            </span>
                        </label>

                        {contract.saveAsTemplateVersion && (
                            <div className="contract-version-fields">
                                <TextField
                                    label="Version Name"
                                    name="templateVersionName"
                                    value={contract.templateVersionName}
                                    onChange={onChange}
                                    placeholder="Defaults to Version N"
                                />
                                <TextField
                                    label="Change Note"
                                    name="templateVersionNote"
                                    value={contract.templateVersionNote}
                                    onChange={onChange}
                                    placeholder="What changed in this version?"
                                />
                            </div>
                        )}
                    </div>
                )}
        </div>
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
            <label htmlFor={name} className="contract-form-label">{label}</label>

            <input
                id={name}
                name={name}
                type={type}
                value={value}
                onChange={onChange}
                placeholder={placeholder}
                required={required}
                readOnly={readOnly}
                className={`form-control${icon ? " contract-date-input" : ""}`}
            />
        </div>
    );
}

function SelectField({
    label,
    name,
    value,
    onChange,
    children,
    disabled = false,
    required = false,
}) {
    return (
        <div>
            <label htmlFor={name} className="contract-form-label">{label}</label>

            <select
                id={name}
                name={name}
                value={value}
                onChange={onChange}
                className="form-select"
                disabled={disabled}
                required={required}
            >
                {children}
            </select>
        </div>
    );
}

export default ContractForm;
