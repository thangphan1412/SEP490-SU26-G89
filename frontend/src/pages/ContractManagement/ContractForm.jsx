import { formatContractStatus } from "./contractUtils.js";

function ContractForm({
    contract,
    onChange,
    projects = [],
    contractTypes = [],
    contractTemplates = [],
    projectContext = null,
    loadingProjects = false,
    loadingProjectContext = false,
    loadingContractOptions = false,
    creatorReadOnly = false,
}) {
    const containsCurrentProject = projects.some(
        (project) => project.id === contract.projectId
    );
    const filteredTemplates = contractTemplates.filter(
        (template) => template.contractTypeId === contract.contractTypeId
    );
    const phases = Array.isArray(projectContext?.phases)
        ? projectContext.phases
        : [];
    const selectedPhase = phases.find(
        (phase) => phase.id === contract.phaseId
    );
    const tasks = Array.isArray(selectedPhase?.tasks)
        ? selectedPhase.tasks
        : [];
    const selectedContractType = contractTypes.find(
        (item) => item.id === contract.contractTypeId
    );
    const workflow = selectedContractType?.activeWorkflow
        || contract.workflowDefinition
        || null;
    const selectedTemplate = filteredTemplates.find(
        (template) => template.id === contract.contractTemplateId
    );
    const versions = Array.isArray(selectedTemplate?.versions)
        ? selectedTemplate.versions
        : [];
    const selectedVersion = versions.find(
        (version) => version.id === contract.contractTemplateVersionId
    );
    const manualFields = (Array.isArray(selectedVersion?.positions)
        ? selectedVersion.positions
        : []
    ).filter(
        (position, index, positions) =>
            String(position.valueSource || "").toUpperCase() === "MANUAL"
            && positions.findIndex(
                (item) => item.attributeKey === position.attributeKey
            ) === index
    );

    return (
        <div className="contract-form-grid">
                {contract.previousContractId && (
                    <div className="contract-form-full contract-replacement-banner">
                        This is a new replacement for cancelled contract{" "}
                        <strong>
                            {contract.previousContractNumber
                                || contract.previousContractId}
                        </strong>
                        . The cancelled contract remains unchanged for audit.
                    </div>
                )}

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
                    label="Project (optional)"
                    name="projectId"
                    value={contract.projectId}
                    onChange={onChange}
                    disabled={loadingProjects}
                >
                    <option value="">
                        {loadingProjects
                            ? "Loading projects..."
                            : "Standalone contract (no project)"}
                    </option>

                    {contract.projectId && !containsCurrentProject && (
                        <option value={contract.projectId}>
                            {contract.projectName
                                ? `${contract.projectName} (current project)`
                                : `Current project (${contract.projectId})`}
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
                    label="Phase (optional)"
                    name="phaseId"
                    value={contract.phaseId}
                    onChange={onChange}
                    disabled={!contract.projectId
                        || loadingProjectContext}
                >
                    <option value="">
                        {loadingProjectContext
                            ? "Loading phases..."
                            : "No phase selected"}
                    </option>
                    {phases.map((phase) => (
                        <option key={phase.id} value={phase.id}>
                            {phase.title}
                            {phase.status ? ` (${formatContractStatus(phase.status)})` : ""}
                        </option>
                    ))}
                </SelectField>

                <SelectField
                    label="Task (optional)"
                    name="taskId"
                    value={contract.taskId}
                    onChange={onChange}
                    disabled={!contract.phaseId
                        || loadingProjectContext}
                >
                    <option value="">No task selected</option>
                    {tasks.map((task) => (
                        <option key={task.id} value={task.id}>
                            {task.title}
                            {task.status ? ` (${formatContractStatus(task.status)})` : ""}
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

                <ReadOnlyField
                    label="Status"
                    value={formatContractStatus(contract.contractStatus)}
                    hint="Status changes are performed by workflow actions, not by editing."
                />

                <TextField
                    label="Effective Date"
                    name="effectiveDate"
                    type="date"
                    value={contract.effectiveDate}
                    onChange={onChange}
                    icon="calendar"
                    required
                />

                <TextField
                    label="Expiration Date"
                    name="expirationDate"
                    type="date"
                    value={contract.expirationDate}
                    onChange={onChange}
                    icon="calendar"
                    required
                />

                <TextField
                    label="Created By"
                    name="contractCreatedBy"
                    value={contract.contractCreatedBy}
                    onChange={onChange}
                    placeholder="Current user"
                    readOnly={creatorReadOnly}
                />

                {contract.contractTypeId && (
                    <WorkflowAssignments
                        workflow={workflow}
                        projectContext={projectContext}
                        assignments={contract.workflowAssignees}
                        creatorName={contract.contractCreatedBy}
                        onChange={onChange}
                    />
                )}

                {manualFields.length > 0 && (
                    <section className="contract-form-full contract-manual-fields">
                        <div className="contract-manual-fields-heading">
                            <strong>Contract-specific values</strong>
                            <span>
                                These values fill the selected template without
                                changing the reusable version.
                            </span>
                        </div>
                        <div className="contract-manual-fields-grid">
                            {manualFields.map((field) => (
                                <ManualAttributeField
                                    key={field.attributeKey}
                                    field={field}
                                    value={
                                        contract.attributeValues?.[
                                            field.attributeKey
                                        ] || ""
                                    }
                                    onChange={onChange}
                                />
                            ))}
                        </div>
                    </section>
                )}

        </div>
    );
}

function WorkflowAssignments({
    workflow,
    projectContext,
    assignments = [],
    creatorName,
    onChange,
}) {
    const steps = Array.isArray(workflow?.steps) ? workflow.steps : [];
    const members = Array.isArray(projectContext?.members)
        ? projectContext.members
        : [];
    const projectContract = Boolean(projectContext?.projectId);

    if (steps.length === 0) {
        return (
            <div className="contract-form-full contract-replacement-banner">
                This contract type has no active workflow. Configure it from
                List Contract Type before creating a contract.
            </div>
        );
    }

    return (
        <section className="contract-form-full contract-workflow-assignments">
            <div className="contract-manual-fields-heading">
                <strong>
                    {workflow.workflowName} · V{workflow.versionNumber}
                </strong>
                <span>
                    {projectContract
                        ? "Assign a project member matching the configured role, department and project permissions."
                        : "Assign an active system user matching the configured role and department."}
                </span>
            </div>
            <div className="contract-workflow-assignment-list">
                {steps.map((step, index) => {
                    const assignment = assignments.find(
                        (item) => item.workflowStepId === step.id
                    );
                    const requiredPermissions = Array.isArray(
                        step.requiredPermissionCodes
                    ) ? step.requiredPermissionCodes : [];
                    const candidates = members.filter((member) => {
                        const memberRoles = Array.isArray(member.roleCodes)
                            ? member.roleCodes
                            : [member.roleCode];
                        const anyRole = normalizeRole(step.requiredRoleCode) === "ANY";
                        const roleActions = new Set(member.allowedActions || []);
                        const departmentMatches = !step.requiredDepartmentId
                            || member.departmentId === step.requiredDepartmentId;
                        return (anyRole || memberRoles.some((role) =>
                            normalizeRole(role)
                                === normalizeRole(step.requiredRoleCode)
                        )) && departmentMatches && (!projectContract
                            || requiredPermissions.every((permission) =>
                                roleActions.has(permission)
                            ));
                    });

                    return (
                        <article className="contract-workflow-assignment" key={step.id || index}>
                            <span>{step.stepOrder || index + 1}</span>
                            <div>
                                <strong>{step.stepName}</strong>
                                <small>
                                    {formatContractStatus(step.actionType)} · {step.requiredRoleCode === "ANY" ? "Any assigned member" : step.requiredRoleCode}
                                    {step.requiredDepartmentName
                                        ? ` · ${step.requiredDepartmentName}`
                                        : " · Any department"}
                                    {projectContract && requiredPermissions.length > 0
                                        ? ` · ${requiredPermissions.join(", ")}`
                                        : ""}
                                </small>
                            </div>
                            {step.actionType === "CREATE" ? (
                                <div className="contract-readonly-field">
                                    {creatorName || "Current user"}
                                </div>
                            ) : (
                                <select
                                    className="form-select"
                                    value={assignment?.userId || ""}
                                    required
                                    onChange={(event) => onChange({
                                        target: {
                                            name: `workflowAssignee.${step.id}`,
                                            value: event.target.value,
                                            type: "select-one",
                                        },
                                    })}
                                >
                                    <option value="">
                                        {candidates.length === 0
                                            ? projectContract
                                                ? "No eligible project member"
                                                : "No eligible system user"
                                            : "Select responsible user"}
                                    </option>
                                    {candidates.map((member) => (
                                        <option value={member.userId} key={member.userId}>
                                            {member.fullName} ({member.email || member.roleCode})
                                            {member.departmentName
                                                ? ` · ${member.departmentName}`
                                                : ""}
                                        </option>
                                    ))}
                                </select>
                            )}
                        </article>
                    );
                })}
            </div>
        </section>
    );
}

function normalizeRole(value) {
    const normalized = String(value || "")
        .trim()
        .toUpperCase()
        .replaceAll("-", "")
        .replaceAll("_", "")
        .replaceAll(" ", "");
    if (["HOD", "HEADDEPARTMENT", "DEPARTMENTHEAD"].includes(normalized)) {
        return "HEADOFDEPARTMENT";
    }
    return normalized;
}

function ReadOnlyField({ label, value, hint }) {
    return (
        <div>
            <span className="contract-form-label">{label}</span>
            <div className="contract-readonly-field">{value || "-"}</div>
            {hint && <div className="form-text">{hint}</div>}
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
    min,
    step,
    inputMode,
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
                min={min}
                step={step}
                inputMode={inputMode}
                className={`form-control${icon ? " contract-date-input" : ""}`}
            />
        </div>
    );
}

function ManualAttributeField({ field, value, onChange }) {
    const fieldType = String(field.fieldType || "TEXT").toUpperCase();
    const isContractValue = field.attributeKey === "contract_value";

    if (fieldType === "CHECKBOX") {
        return (
            <label className="form-check contract-manual-checkbox">
                <input
                    className="form-check-input"
                    type="checkbox"
                    checked={String(value).toLowerCase() === "true"}
                    onChange={(event) => onChange({
                        target: {
                            name: `attributeValues.${field.attributeKey}`,
                            value: String(event.target.checked),
                            type: "text",
                        },
                    })}
                />
                <span className="form-check-label">
                    {field.fieldLabel || field.attributeKey}
                </span>
            </label>
        );
    }

    return (
        <TextField
            label={
                isContractValue
                    ? `${field.fieldLabel || "Contract Value"} (VND)`
                    : field.fieldLabel || field.attributeKey
            }
            name={`attributeValues.${field.attributeKey}`}
            value={value}
            onChange={onChange}
            type={isContractValue ? "number" : fieldType === "DATE" ? "date" : "text"}
            min={isContractValue ? "0" : undefined}
            step={isContractValue ? "1000" : undefined}
            inputMode={isContractValue ? "decimal" : undefined}
            placeholder={isContractValue ? "Example: 150000000" : "Enter value"}
            required={Boolean(field.required)}
        />
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
