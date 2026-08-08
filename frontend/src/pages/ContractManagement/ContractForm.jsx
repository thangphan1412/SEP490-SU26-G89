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
    loadingProjects = false,
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
