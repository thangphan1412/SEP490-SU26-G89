import { useMemo, useState } from "react";
import { IconPlus, IconTrash } from "@tabler/icons-react";
import { formatContractStatus } from "./contractUtils.js";
import {
    joinContractPages,
    splitContractPages,
} from "./contractPageUtils.js";

const MAX_CONTRACT_PAGE_COUNT = 50;

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
    projectReadOnly = false,
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
    const workflow = projectReadOnly
        ? contract.workflowDefinition
        : selectedContractType?.activeWorkflow || contract.workflowDefinition;
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
                    disabled={loadingContractOptions || projectReadOnly}
                    required
                >
                    <option value="">
                        {loadingContractOptions
                            ? "Loading contract types..."
                            : "Select contract type to load its workflow"}
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
                    label="Project"
                    name="projectId"
                    value={contract.projectId}
                    onChange={onChange}
                    disabled={loadingProjects || projectReadOnly}
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
                    label="Phase"
                    name="phaseId"
                    value={contract.phaseId}
                    onChange={onChange}
                    disabled={!contract.projectId
                        || loadingProjectContext
                        || projectReadOnly}
                    required
                >
                    <option value="">
                        {loadingProjectContext
                            ? "Loading phases..."
                            : "Select phase"}
                    </option>
                    {phases.map((phase) => (
                        <option key={phase.id} value={phase.id}>
                            {phase.title}
                            {phase.status ? ` (${formatContractStatus(phase.status)})` : ""}
                        </option>
                    ))}
                </SelectField>

                <SelectField
                    label="Task"
                    name="taskId"
                    value={contract.taskId}
                    onChange={onChange}
                    disabled={!contract.phaseId
                        || loadingProjectContext
                        || projectReadOnly}
                    required
                >
                    <option value="">Select task</option>
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
                        readOnly={projectReadOnly}
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

                <div className="contract-form-full">
                    <PagedContractContentEditor
                        value={contract.contractContent}
                        onChange={onChange}
                    />
                    <div className="form-text">
                        Selecting a saved version copies its content here. Editing
                        a page does not overwrite that version. Each page tab is
                        exported as a separate PDF page.
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

function WorkflowAssignments({
    workflow,
    projectContext,
    assignments = [],
    creatorName,
    onChange,
    readOnly,
}) {
    const steps = Array.isArray(workflow?.steps) ? workflow.steps : [];
    const members = Array.isArray(projectContext?.members)
        ? projectContext.members
        : [];

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
                    Assign the exact project member responsible for each step.
                    The backend also verifies their role and project permissions.
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
                        return memberRoles.some((role) =>
                            normalizeRole(role)
                                === normalizeRole(step.requiredRoleCode)
                        ) && requiredPermissions.every((permission) =>
                            (member.allowedActions || []).includes(permission)
                        );
                    });

                    return (
                        <article className="contract-workflow-assignment" key={step.id || index}>
                            <span>{step.stepOrder || index + 1}</span>
                            <div>
                                <strong>{step.stepName}</strong>
                                <small>
                                    {formatContractStatus(step.actionType)} · {step.requiredRoleCode}
                                    {requiredPermissions.length > 0
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
                                    disabled={readOnly}
                                    required={!readOnly}
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
                                            ? "No eligible project member"
                                            : "Select responsible member"}
                                    </option>
                                    {candidates.map((member) => (
                                        <option value={member.userId} key={member.userId}>
                                            {member.fullName} ({member.email || member.roleCode})
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
        .replaceAll("-", "_")
        .replaceAll(" ", "_");
    const compact = normalized.replaceAll("_", "");

    if (["ADMIN", "ADMINISTRATOR"].includes(compact)) {
        return "ADMIN";
    }
    if (["MANAGER", "HEADOFDEPARTMENT", "DEPARTMENTHEAD"].includes(compact)) {
        return "HEAD_OF_DEPARTMENT";
    }
    if ([
        "PARTNER",
        "EXTERNAL",
        "EXTERNALPARTNER",
        "EXTERNALPARTNERS",
        "EXTERNALPARNER",
        "EXTERNALPARNERS",
    ].includes(compact)) {
        return "EXTERNAL_PARTNER";
    }
    return normalized;
}

function PagedContractContentEditor({ value, onChange }) {
    const [currentPage, setCurrentPage] = useState(1);
    const pages = useMemo(() => splitContractPages(value), [value]);
    const activePage = Math.min(currentPage, pages.length);

    const emitPages = (nextPages) => {
        onChange({
            target: {
                name: "contractContent",
                value: joinContractPages(nextPages),
            },
        });
    };

    const updateCurrentPage = (nextContent) => {
        const nextPages = [...pages];
        nextPages[activePage - 1] = nextContent;
        emitPages(nextPages);
    };

    const addPage = () => {
        if (pages.length >= MAX_CONTRACT_PAGE_COUNT) {
            return;
        }
        const nextPages = [...pages, ""];
        emitPages(nextPages);
        setCurrentPage(nextPages.length);
    };

    const removePage = () => {
        if (pages.length === 1) {
            return;
        }
        if (
            pages[activePage - 1].trim()
            && !window.confirm(`Remove Contract Page ${activePage}?`)
        ) {
            return;
        }

        const nextPages = pages.filter(
            (_page, index) => index !== activePage - 1
        );
        emitPages(nextPages);
        setCurrentPage(Math.min(activePage, nextPages.length));
    };

    return (
        <section className="contract-paged-content-editor">
            <div className="contract-paged-content-heading">
                <div>
                    <span className="contract-form-label">Contract Content</span>
                    <small>
                        Edit one logical contract page at a time.
                    </small>
                </div>
                <strong>{pages.length} page(s)</strong>
            </div>
            <div className="template-page-navigation">
                <div className="template-page-tabs" role="tablist">
                    {pages.map((_page, index) => {
                        const pageNumber = index + 1;
                        return (
                            <button
                                type="button"
                                role="tab"
                                aria-selected={activePage === pageNumber}
                                className={
                                    activePage === pageNumber ? "active" : ""
                                }
                                key={pageNumber}
                                onClick={() => setCurrentPage(pageNumber)}
                            >
                                Page {pageNumber}
                            </button>
                        );
                    })}
                </div>
                <div className="template-page-actions">
                    <button
                        type="button"
                        onClick={addPage}
                        disabled={pages.length >= MAX_CONTRACT_PAGE_COUNT}
                    >
                        <IconPlus size={16} />
                        Add page
                    </button>
                    <button
                        type="button"
                        className="danger"
                        onClick={removePage}
                        disabled={pages.length === 1}
                    >
                        <IconTrash size={16} />
                        Remove page
                    </button>
                </div>
            </div>
            <textarea
                id="contractContent"
                name="contractContent"
                value={pages[activePage - 1] || ""}
                onChange={(event) => updateCurrentPage(event.target.value)}
                className="form-control contract-content-editor"
                placeholder={`Enter or edit Contract Page ${activePage}...`}
            />
        </section>
    );
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
