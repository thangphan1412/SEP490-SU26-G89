import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import {
    IconCirclePlus,
    IconEye,
    IconPencil,
    IconPlus,
    IconRefresh,
    IconSearch,
    IconTrash,
} from "@tabler/icons-react";
import contractTemplateApi from "../../services/contractTemplateService/contractTemplateApi.js";
import contractTypeApi from "../../services/contractTypeService/contractTypeApi.js";
import {
    formatContractDateTime,
    getApiErrorMessage,
    unwrapApiResponse,
} from "../ContractManagement/contractUtils.js";
import TemplatePositionDesigner from "./TemplatePositionDesigner.jsx";
import {
    cloneVersionPositions,
    toPositionRequest,
} from "./templatePositionUtils.js";
import "../../assets/styles/css/layoutStyles/ContractWorkspace.css";

function createEmptyTemplateForm() {
    return {
        contractTypeId: "",
        contractTemplateName: "",
        contractTemplateDescription: "",
        status: "Active",
        createdBy: localStorage.getItem("fullName") || "",
    };
}

function readVersionLayout(version) {
    if (!version?.layoutJson) {
        return {};
    }

    try {
        return JSON.parse(version.layoutJson);
    } catch {
        return {};
    }
}

function createEmptyVersionForm(sourceVersion = null) {
    const savedLayout = readVersionLayout(sourceVersion);
    const sourcePositions = Array.isArray(sourceVersion?.positions)
        && sourceVersion.positions.length > 0
        ? sourceVersion.positions
        : savedLayout.fields;

    return {
        versionName: "",
        templateContent: sourceVersion?.templateContent || "",
        changeNote: "",
        createdBy: localStorage.getItem("fullName") || "",
        pageCount: Number(sourceVersion?.pageCount || savedLayout.pageCount) || 1,
        positions: cloneVersionPositions(sourcePositions),
    };
}

function ListContractTemplate() {
    const [contractTypes, setContractTypes] = useState([]);
    const [templates, setTemplates] = useState([]);
    const [search, setSearch] = useState("");
    const [typeFilter, setTypeFilter] = useState("");
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const [modalMode, setModalMode] = useState(null);
    const [selectedTemplate, setSelectedTemplate] = useState(null);
    const [templateForm, setTemplateForm] = useState(
        createEmptyTemplateForm
    );
    const [versionModalOpen, setVersionModalOpen] = useState(false);
    const [versionForm, setVersionForm] = useState(createEmptyVersionForm);
    const [modalError, setModalError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [deletingId, setDeletingId] = useState(null);

    useEffect(() => {
        let active = true;

        const loadData = async () => {
            setLoading(true);
            setErrorMessage("");

            try {
                const [typeResponse, templateResponse] = await Promise.all([
                    contractTypeApi.getAllContractTypes(),
                    contractTemplateApi.getAllContractTemplates(),
                ]);
                const typeItems = unwrapApiResponse(typeResponse);
                const templateItems = unwrapApiResponse(templateResponse);

                if (active) {
                    setContractTypes(
                        Array.isArray(typeItems) ? typeItems : []
                    );
                    setTemplates(
                        Array.isArray(templateItems) ? templateItems : []
                    );
                }
            } catch (error) {
                if (active) {
                    setContractTypes([]);
                    setTemplates([]);
                    setErrorMessage(
                        getApiErrorMessage(
                            error,
                            "Unable to load contract templates."
                        )
                    );
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        loadData();

        return () => {
            active = false;
        };
    }, [reloadKey]);

    const filteredTemplates = useMemo(() => {
        const keyword = search.trim().toLowerCase();

        return templates.filter((template) => {
            const matchesType =
                !typeFilter || template.contractTypeId === typeFilter;
            const matchesSearch =
                !keyword ||
                [
                    template.contractTemplateName,
                    template.contractTemplateDescription,
                    template.contractTypeCode,
                    template.contractTypeName,
                    template.status,
                ]
                    .filter(Boolean)
                    .join(" ")
                    .toLowerCase()
                    .includes(keyword);

            return matchesType && matchesSearch;
        });
    }, [search, templates, typeFilter]);

    const openCreateModal = () => {
        setSelectedTemplate(null);
        setTemplateForm(createEmptyTemplateForm());
        setModalError("");
        setModalMode("create");
    };

    const openViewModal = (template) => {
        setSelectedTemplate(template);
        setModalError("");
        setModalMode("view");
    };

    const openEditModal = (template) => {
        setSelectedTemplate(template);
        setTemplateForm({
            contractTypeId: template.contractTypeId || "",
            contractTemplateName: template.contractTemplateName || "",
            contractTemplateDescription:
                template.contractTemplateDescription || "",
            status: template.status || "Active",
            createdBy:
                template.createdBy ||
                localStorage.getItem("fullName") ||
                "",
        });
        setModalError("");
        setModalMode("edit");
    };

    const openVersionModal = (template) => {
        const latestVersion = Array.isArray(template?.versions)
            ? template.versions[0]
            : null;
        setModalMode(null);
        setSelectedTemplate(template);
        setVersionForm(createEmptyVersionForm(latestVersion));
        setModalError("");
        setVersionModalOpen(true);
    };

    const closeTemplateModal = () => {
        if (!submitting) {
            setModalMode(null);
            setSelectedTemplate(null);
            setModalError("");
        }
    };

    const closeVersionModal = () => {
        if (!submitting) {
            setVersionModalOpen(false);
            setSelectedTemplate(null);
            setModalError("");
        }
    };

    const handleTemplateChange = (event) => {
        const { name, value } = event.target;
        setTemplateForm((current) => ({ ...current, [name]: value }));
    };

    const handleVersionChange = (event) => {
        const { name, value } = event.target;
        setVersionForm((current) => ({ ...current, [name]: value }));
    };

    const handleLayoutChange = ({ pageCount, positions }) => {
        setVersionForm((current) => ({
            ...current,
            pageCount,
            positions,
        }));
    };

    const handleTemplateSubmit = async (event) => {
        event.preventDefault();

        if (submitting) {
            return;
        }

        if (
            !templateForm.contractTypeId ||
            !templateForm.contractTemplateName.trim()
        ) {
            setModalError("Contract type and template name are required.");
            return;
        }

        const request = {
            contractTypeId: templateForm.contractTypeId,
            contractTemplateName:
                templateForm.contractTemplateName.trim(),
            contractTemplateDescription:
                templateForm.contractTemplateDescription.trim() || null,
            status: templateForm.status,
            createdBy: templateForm.createdBy.trim() || null,
        };

        setSubmitting(true);
        setModalError("");

        try {
            if (modalMode === "edit") {
                await contractTemplateApi.updateContractTemplate(
                    selectedTemplate.id,
                    request
                );
            } else {
                await contractTemplateApi.createContractTemplate(request);
            }

            setModalMode(null);
            setSelectedTemplate(null);
            setReloadKey((current) => current + 1);
        } catch (error) {
            setModalError(
                getApiErrorMessage(
                    error,
                    "Unable to save the contract template."
                )
            );
        } finally {
            setSubmitting(false);
        }
    };

    const handleVersionSubmit = async (event) => {
        event.preventDefault();

        if (submitting || !selectedTemplate) {
            return;
        }

        if (!versionForm.templateContent.trim()) {
            setModalError("Template content is required.");
            return;
        }

        const invalidPosition = versionForm.positions.find(
            (position) =>
                !position.fieldLabel.trim()
                || !/^[a-z][a-z0-9_]{1,79}$/.test(
                    position.attributeKey.trim().toLowerCase()
                )
        );
        if (invalidPosition) {
            setModalError(
                "Every layout field needs a label and a lowercase attribute key."
            );
            return;
        }

        setSubmitting(true);
        setModalError("");

        try {
            await contractTemplateApi.createContractTemplateVersion(
                selectedTemplate.id,
                {
                    versionName: versionForm.versionName.trim() || null,
                    templateContent: versionForm.templateContent.trim(),
                    layoutJson: null,
                    changeNote: versionForm.changeNote.trim() || null,
                    createdBy: versionForm.createdBy.trim() || null,
                    pageCount: versionForm.pageCount,
                    positions: versionForm.positions.map(toPositionRequest),
                }
            );

            setVersionModalOpen(false);
            setSelectedTemplate(null);
            setReloadKey((current) => current + 1);
        } catch (error) {
            setModalError(
                getApiErrorMessage(
                    error,
                    "Unable to create the template version."
                )
            );
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (template) => {
        const confirmed = window.confirm(
            `Delete contract template ${template.contractTemplateName}?`
        );

        if (!confirmed) {
            return;
        }

        setDeletingId(template.id);
        setErrorMessage("");

        try {
            await contractTemplateApi.deleteContractTemplate(template.id);
            setReloadKey((current) => current + 1);
        } catch (error) {
            setErrorMessage(
                getApiErrorMessage(
                    error,
                    "Unable to delete the contract template."
                )
            );
        } finally {
            setDeletingId(null);
        }
    };

    return (
        <div className="contract-workspace">
            <header className="contract-page-header">
                <div>
                    <h1>Contract Templates</h1>
                    <p>
                        Manage template parents and create immutable reusable
                        versions.
                    </p>
                </div>
                <Button
                    className="contract-primary-button"
                    onClick={openCreateModal}
                    disabled={contractTypes.length === 0}
                >
                    <IconPlus size={20} />
                    New Contract Template
                </Button>
            </header>

            <section className="contract-list-card">
                <div className="contract-toolbar">
                    <label className="contract-search-box">
                        <IconSearch size={20} />
                        <input
                            aria-label="Search contract templates"
                            placeholder="Search template name, type or description..."
                            value={search}
                            onChange={(event) => setSearch(event.target.value)}
                        />
                    </label>

                    <select
                        aria-label="Filter by contract type"
                        className="form-select contract-filter-select"
                        value={typeFilter}
                        onChange={(event) =>
                            setTypeFilter(event.target.value)
                        }
                    >
                        <option value="">All contract types</option>
                        {contractTypes.map((contractType) => (
                            <option
                                key={contractType.id}
                                value={contractType.id}
                            >
                                {contractType.contractTypeCode} -{" "}
                                {contractType.contractTypeName}
                            </option>
                        ))}
                    </select>

                    <Button
                        variant="outline-secondary"
                        className="contract-refresh-button"
                        title="Clear filters and refresh"
                        onClick={() => {
                            setSearch("");
                            setTypeFilter("");
                            setReloadKey((current) => current + 1);
                        }}
                    >
                        <IconRefresh size={20} />
                    </Button>
                </div>

                {contractTypes.length === 0 && !loading && !errorMessage && (
                    <Alert variant="info" className="contract-inline-alert">
                        Create a Contract Type first, then create templates for
                        that type.
                    </Alert>
                )}

                {errorMessage && (
                    <Alert variant="danger" className="contract-inline-alert">
                        {errorMessage}
                    </Alert>
                )}

                <div className="table-responsive">
                    <table className="table contract-data-table mb-0">
                        <thead>
                            <tr>
                                <th>Template Name</th>
                                <th>Contract Type</th>
                                <th>Latest Version</th>
                                <th>Total Versions</th>
                                <th>Contracts</th>
                                <th>Status</th>
                                <th>Updated At</th>
                                <th className="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <StateRow colSpan={8}>
                                    <Spinner animation="border" size="sm" />
                                    Loading contract templates...
                                </StateRow>
                            ) : filteredTemplates.length === 0 ? (
                                <StateRow colSpan={8}>
                                    No contract templates found.
                                </StateRow>
                            ) : (
                                filteredTemplates.map((template) => (
                                    <tr key={template.id}>
                                        <td className="contract-cell-strong">
                                            {template.contractTemplateName}
                                        </td>
                                        <td>
                                            {template.contractTypeCode
                                                ? `${template.contractTypeCode} - `
                                                : ""}
                                            {template.contractTypeName || "-"}
                                        </td>
                                        <td>
                                            {template.latestVersion
                                                ? `V${template.latestVersion}`
                                                : "No version"}
                                        </td>
                                        <td>
                                            {template.versions?.length || 0}
                                        </td>
                                        <td>{template.contractCount || 0}</td>
                                        <td>
                                            <StatusBadge
                                                status={template.status}
                                            />
                                        </td>
                                        <td>
                                            {formatContractDateTime(
                                                template.updatedAt
                                            )}
                                        </td>
                                        <td>
                                            <div className="contract-row-actions">
                                                <ActionButton
                                                    label="View versions"
                                                    icon={IconEye}
                                                    onClick={() =>
                                                        openViewModal(template)
                                                    }
                                                />
                                                <ActionButton
                                                    label="Edit template"
                                                    icon={IconPencil}
                                                    onClick={() =>
                                                        openEditModal(template)
                                                    }
                                                />
                                                <ActionButton
                                                    label="New version"
                                                    icon={IconCirclePlus}
                                                    primary
                                                    onClick={() =>
                                                        openVersionModal(template)
                                                    }
                                                />
                                                <ActionButton
                                                    label="Delete template"
                                                    icon={IconTrash}
                                                    danger
                                                    disabled={
                                                        deletingId === template.id
                                                    }
                                                    onClick={() =>
                                                        handleDelete(template)
                                                    }
                                                />
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                <footer className="contract-list-footer">
                    <span>
                        {filteredTemplates.length} of {templates.length} contract
                        templates
                    </span>
                    <span className="contract-version-rule">
                        Saved versions are read-only.
                    </span>
                </footer>
            </section>

            <TemplateModal
                mode={modalMode}
                template={selectedTemplate}
                form={templateForm}
                contractTypes={contractTypes}
                error={modalError}
                submitting={submitting}
                onChange={handleTemplateChange}
                onClose={closeTemplateModal}
                onSubmit={handleTemplateSubmit}
                onEdit={() => openEditModal(selectedTemplate)}
                onNewVersion={() => openVersionModal(selectedTemplate)}
            />

            <VersionModal
                show={versionModalOpen}
                template={selectedTemplate}
                form={versionForm}
                error={modalError}
                submitting={submitting}
                onChange={handleVersionChange}
                onLayoutChange={handleLayoutChange}
                onClose={closeVersionModal}
                onSubmit={handleVersionSubmit}
            />
        </div>
    );
}

function TemplateModal({
    mode,
    template,
    form,
    contractTypes,
    error,
    submitting,
    onChange,
    onClose,
    onSubmit,
    onEdit,
    onNewVersion,
}) {
    if (!mode) {
        return null;
    }

    const isView = mode === "view";
    const title = isView
        ? "Template Details & Versions"
        : mode === "edit"
          ? "Edit Contract Template"
          : "Create Contract Template";

    return (
        <Modal
            show
            onHide={onClose}
            size={isView ? "xl" : "lg"}
            centered
            backdrop={submitting ? "static" : true}
            className="contract-modal"
        >
            {isView ? (
                <>
                    <Modal.Header closeButton>
                        <Modal.Title>{title}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <div className="contract-detail-grid">
                            <DetailItem
                                label="Template Name"
                                value={template?.contractTemplateName}
                            />
                            <DetailItem
                                label="Contract Type"
                                value={[
                                    template?.contractTypeCode,
                                    template?.contractTypeName,
                                ]
                                    .filter(Boolean)
                                    .join(" - ")}
                            />
                            <DetailItem
                                label="Status"
                                value={
                                    <StatusBadge status={template?.status} />
                                }
                            />
                            <DetailItem
                                label="Created By"
                                value={template?.createdBy}
                            />
                            <DetailItem
                                label="Created At"
                                value={formatContractDateTime(
                                    template?.createdAt
                                )}
                            />
                            <DetailItem
                                label="Updated At"
                                value={formatContractDateTime(
                                    template?.updatedAt
                                )}
                            />
                            <div className="contract-detail-item contract-detail-full">
                                <span>Description</span>
                                <strong>
                                    {template?.contractTemplateDescription ||
                                        "-"}
                                </strong>
                            </div>
                        </div>

                        <div className="contract-version-history">
                            <div className="contract-version-history-heading">
                                <div>
                                    <h3>Version History</h3>
                                    <p>
                                        Versions are immutable. A change always
                                        creates the next version number.
                                    </p>
                                </div>
                                <Button onClick={onNewVersion}>
                                    <IconCirclePlus size={18} />
                                    New Version
                                </Button>
                            </div>

                            {template?.versions?.length ? (
                                template.versions.map((version) => (
                                    <details
                                        className="contract-version-item"
                                        key={version.id}
                                    >
                                        <summary>
                                            <span>
                                                V{version.versionNumber} -{" "}
                                                {version.versionName}
                                            </span>
                                            <small>
                                                {formatContractDateTime(
                                                    version.createdAt
                                                )}
                                            </small>
                                        </summary>
                                        <div className="contract-version-meta">
                                            <span>
                                                Created by{" "}
                                                {version.createdBy || "-"}
                                            </span>
                                            <span>
                                                {version.changeNote ||
                                                    "No change note"}
                                            </span>
                                        </div>
                                        <div className="contract-version-position-summary">
                                            <span>
                                                {version.pageCount || 1} page(s)
                                            </span>
                                            <span>
                                                {version.positions?.length || 0}{" "}
                                                positioned field(s)
                                            </span>
                                            <span>Normalized coordinates</span>
                                        </div>
                                        <pre>
                                            {version.templateContent ||
                                                "No content."}
                                        </pre>
                                        {version.layoutJson && (
                                            <details className="contract-layout-preview">
                                                <summary>Layout data</summary>
                                                <pre>{version.layoutJson}</pre>
                                            </details>
                                        )}
                                    </details>
                                ))
                            ) : (
                                <div className="contract-version-empty">
                                    No version has been saved for this template.
                                </div>
                            )}
                        </div>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="outline-secondary" onClick={onClose}>
                            Close
                        </Button>
                        <Button onClick={onEdit}>
                            <IconPencil size={18} />
                            Edit Template Metadata
                        </Button>
                    </Modal.Footer>
                </>
            ) : (
                <form onSubmit={onSubmit}>
                    <Modal.Header closeButton>
                        <Modal.Title>{title}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {error && <Alert variant="danger">{error}</Alert>}
                        <div className="contract-form-grid">
                            <div>
                                <label
                                    htmlFor="contractTypeId"
                                    className="contract-form-label"
                                >
                                    Contract Type
                                </label>
                                <select
                                    id="contractTypeId"
                                    name="contractTypeId"
                                    className="form-select"
                                    value={form.contractTypeId}
                                    onChange={onChange}
                                    required
                                >
                                    <option value="">
                                        Select contract type
                                    </option>
                                    {contractTypes.map((contractType) => (
                                        <option
                                            key={contractType.id}
                                            value={contractType.id}
                                        >
                                            {contractType.contractTypeCode} -{" "}
                                            {contractType.contractTypeName}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <FormField
                                label="Template Name"
                                name="contractTemplateName"
                                value={form.contractTemplateName}
                                onChange={onChange}
                                placeholder="e.g. Standard NDA"
                                required
                            />
                            <div>
                                <label
                                    htmlFor="templateStatus"
                                    className="contract-form-label"
                                >
                                    Status
                                </label>
                                <select
                                    id="templateStatus"
                                    name="status"
                                    className="form-select"
                                    value={form.status}
                                    onChange={onChange}
                                >
                                    <option>Active</option>
                                    <option>Inactive</option>
                                </select>
                            </div>
                            <FormField
                                label="Created By"
                                name="createdBy"
                                value={form.createdBy}
                                onChange={onChange}
                                readOnly={Boolean(
                                    localStorage.getItem("fullName")
                                )}
                            />
                            <div className="contract-form-full">
                                <label
                                    htmlFor="contractTemplateDescription"
                                    className="contract-form-label"
                                >
                                    Description
                                </label>
                                <textarea
                                    id="contractTemplateDescription"
                                    name="contractTemplateDescription"
                                    className="form-control"
                                    rows={4}
                                    value={form.contractTemplateDescription}
                                    onChange={onChange}
                                />
                            </div>
                        </div>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button
                            variant="outline-secondary"
                            onClick={onClose}
                            disabled={submitting}
                        >
                            Cancel
                        </Button>
                        <Button type="submit" disabled={submitting}>
                            {submitting && (
                                <Spinner animation="border" size="sm" />
                            )}
                            {submitting ? "Saving..." : "Save Template"}
                        </Button>
                    </Modal.Footer>
                </form>
            )}
        </Modal>
    );
}

function VersionModal({
    show,
    template,
    form,
    error,
    submitting,
    onChange,
    onLayoutChange,
    onClose,
    onSubmit,
}) {
    return (
        <Modal
            show={show}
            onHide={onClose}
            size="xl"
            centered
            backdrop={submitting ? "static" : true}
            className="contract-modal"
        >
            <form onSubmit={onSubmit}>
                <Modal.Header closeButton>
                    <Modal.Title>
                        New Version · {template?.contractTemplateName}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Alert variant="info">
                        This creates V{(template?.latestVersion || 0) + 1}. Existing
                        versions remain unchanged. The latest version content and
                        positions are copied as the starting point.
                    </Alert>
                    {error && <Alert variant="danger">{error}</Alert>}
                    <div className="contract-form-grid">
                        <FormField
                            label="Version Name"
                            name="versionName"
                            value={form.versionName}
                            onChange={onChange}
                            placeholder="Defaults to Version N"
                        />
                        <FormField
                            label="Created By"
                            name="createdBy"
                            value={form.createdBy}
                            onChange={onChange}
                            readOnly={Boolean(localStorage.getItem("fullName"))}
                        />
                        <div className="contract-form-full">
                            <label
                                htmlFor="changeNote"
                                className="contract-form-label"
                            >
                                Change Note
                            </label>
                            <textarea
                                id="changeNote"
                                name="changeNote"
                                className="form-control"
                                rows={2}
                                value={form.changeNote}
                                onChange={onChange}
                                placeholder="Describe what changed in this version."
                            />
                        </div>
                        <div className="contract-form-full">
                            <label
                                htmlFor="templateContent"
                                className="contract-form-label"
                            >
                                Template Content
                            </label>
                            <textarea
                                id="templateContent"
                                name="templateContent"
                                className="form-control contract-content-editor"
                                value={form.templateContent}
                                onChange={onChange}
                                required
                            />
                        </div>
                        <div className="contract-form-full">
                            <TemplatePositionDesigner
                                content={form.templateContent}
                                pageCount={form.pageCount}
                                positions={form.positions}
                                onChange={onLayoutChange}
                            />
                        </div>
                    </div>
                </Modal.Body>
                <Modal.Footer>
                    <Button
                        variant="outline-secondary"
                        onClick={onClose}
                        disabled={submitting}
                    >
                        Cancel
                    </Button>
                    <Button type="submit" disabled={submitting}>
                        {submitting && (
                            <Spinner animation="border" size="sm" />
                        )}
                        {submitting ? "Saving..." : "Create New Version"}
                    </Button>
                </Modal.Footer>
            </form>
        </Modal>
    );
}

function FormField({
    label,
    name,
    value,
    onChange,
    placeholder = "",
    required = false,
    readOnly = false,
}) {
    return (
        <div>
            <label htmlFor={name} className="contract-form-label">
                {label}
            </label>
            <input
                id={name}
                name={name}
                className="form-control"
                value={value}
                onChange={onChange}
                placeholder={placeholder}
                required={required}
                readOnly={readOnly}
            />
        </div>
    );
}

function DetailItem({ label, value }) {
    return (
        <div className="contract-detail-item">
            <span>{label}</span>
            <strong>{value || "-"}</strong>
        </div>
    );
}

function StatusBadge({ status }) {
    const normalized = status || "Inactive";
    return (
        <span
            className={`contract-status-badge ${
                normalized === "Active"
                    ? "status-active"
                    : "status-inactive"
            }`}
        >
            {normalized}
        </span>
    );
}

function ActionButton({
    label,
    icon: ActionIcon,
    onClick,
    primary = false,
    danger = false,
    disabled = false,
}) {
    const classNames = [
        "contract-action-button",
        primary ? "primary" : "",
        danger ? "danger" : "",
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <button
            type="button"
            className={classNames}
            onClick={onClick}
            disabled={disabled}
            title={label}
            aria-label={label}
        >
            <ActionIcon size={17} />
        </button>
    );
}

function StateRow({ colSpan, children }) {
    return (
        <tr>
            <td colSpan={colSpan} className="contract-table-state">
                <div>{children}</div>
            </td>
        </tr>
    );
}

export default ListContractTemplate;
