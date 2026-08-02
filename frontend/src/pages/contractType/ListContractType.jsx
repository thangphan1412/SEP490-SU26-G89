import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import {
    IconEye,
    IconPencil,
    IconPlus,
    IconRefresh,
    IconSearch,
    IconTrash,
} from "@tabler/icons-react";
import contractTypeApi from "../../services/contractTypeService/contractTypeApi.js";
import {
    formatContractDateTime,
    getApiErrorMessage,
    unwrapApiResponse,
} from "../ContractManagement/contractUtils.js";
import "../../assets/styles/css/layoutStyles/ContractWorkspace.css";

const EMPTY_FORM = {
    contractTypeCode: "",
    contractTypeName: "",
    description: "",
    validityDays: "",
    category: "Legal",
    status: "Active",
    createdBy: "",
};

function createEmptyForm() {
    return {
        ...EMPTY_FORM,
        createdBy: localStorage.getItem("fullName") || "",
    };
}

function ListContractType() {
    const [contractTypes, setContractTypes] = useState([]);
    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [reloadKey, setReloadKey] = useState(0);
    const [modalMode, setModalMode] = useState(null);
    const [selectedType, setSelectedType] = useState(null);
    const [form, setForm] = useState(createEmptyForm);
    const [modalError, setModalError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [deletingId, setDeletingId] = useState(null);

    useEffect(() => {
        let active = true;

        const loadContractTypes = async () => {
            setLoading(true);
            setErrorMessage("");

            try {
                const response = await contractTypeApi.getAllContractTypes();
                const items = unwrapApiResponse(response);

                if (active) {
                    setContractTypes(Array.isArray(items) ? items : []);
                }
            } catch (error) {
                if (active) {
                    setContractTypes([]);
                    setErrorMessage(
                        getApiErrorMessage(
                            error,
                            "Unable to load contract types."
                        )
                    );
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        loadContractTypes();

        return () => {
            active = false;
        };
    }, [reloadKey]);

    const filteredTypes = useMemo(() => {
        const keyword = search.trim().toLowerCase();
        if (!keyword) {
            return contractTypes;
        }

        return contractTypes.filter((item) =>
            [
                item.contractTypeCode,
                item.contractTypeName,
                item.description,
                item.category,
                item.status,
            ]
                .filter(Boolean)
                .join(" ")
                .toLowerCase()
                .includes(keyword)
        );
    }, [contractTypes, search]);

    const openCreateModal = () => {
        setSelectedType(null);
        setForm(createEmptyForm());
        setModalError("");
        setModalMode("create");
    };

    const openViewModal = (contractType) => {
        setSelectedType(contractType);
        setModalError("");
        setModalMode("view");
    };

    const openEditModal = (contractType) => {
        setSelectedType(contractType);
        setForm({
            contractTypeCode: contractType.contractTypeCode || "",
            contractTypeName: contractType.contractTypeName || "",
            description: contractType.description || "",
            validityDays: contractType.validityDays || "",
            category: contractType.category || "Legal",
            status: contractType.status || "Active",
            createdBy:
                contractType.createdBy ||
                localStorage.getItem("fullName") ||
                "",
        });
        setModalError("");
        setModalMode("edit");
    };

    const closeModal = () => {
        if (!submitting) {
            setModalMode(null);
            setSelectedType(null);
            setModalError("");
        }
    };

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((current) => ({ ...current, [name]: value }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (submitting) {
            return;
        }

        if (!form.contractTypeCode.trim() || !form.contractTypeName.trim()) {
            setModalError("Type code and type name are required.");
            return;
        }

        if (form.validityDays && Number(form.validityDays) <= 0) {
            setModalError("Default validity must be greater than zero.");
            return;
        }

        const request = {
            contractTypeCode: form.contractTypeCode.trim(),
            contractTypeName: form.contractTypeName.trim(),
            description: form.description.trim() || null,
            validityDays: form.validityDays
                ? Number(form.validityDays)
                : null,
            category: form.category.trim() || null,
            status: form.status,
            createdBy: form.createdBy.trim() || null,
        };

        setSubmitting(true);
        setModalError("");

        try {
            if (modalMode === "edit") {
                await contractTypeApi.updateContractType(
                    selectedType.id,
                    request
                );
            } else {
                await contractTypeApi.createContractType(request);
            }

            setModalMode(null);
            setSelectedType(null);
            setReloadKey((current) => current + 1);
        } catch (error) {
            setModalError(
                getApiErrorMessage(error, "Unable to save the contract type.")
            );
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (contractType) => {
        const confirmed = window.confirm(
            `Delete contract type ${contractType.contractTypeCode}?`
        );

        if (!confirmed) {
            return;
        }

        setDeletingId(contractType.id);
        setErrorMessage("");

        try {
            await contractTypeApi.deleteContractType(contractType.id);
            setReloadKey((current) => current + 1);
        } catch (error) {
            setErrorMessage(
                getApiErrorMessage(
                    error,
                    "Unable to delete the contract type."
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
                    <h1>Contract Types</h1>
                    <p>
                        Manage the types used to classify templates and
                        contracts.
                    </p>
                </div>
                <Button
                    className="contract-primary-button"
                    onClick={openCreateModal}
                >
                    <IconPlus size={20} />
                    New Contract Type
                </Button>
            </header>

            <section className="contract-list-card">
                <div className="contract-toolbar">
                    <label className="contract-search-box">
                        <IconSearch size={20} />
                        <input
                            aria-label="Search contract types"
                            placeholder="Search code, name, category or description..."
                            value={search}
                            onChange={(event) => setSearch(event.target.value)}
                        />
                    </label>
                    <Button
                        variant="outline-secondary"
                        className="contract-refresh-button"
                        title="Refresh"
                        onClick={() =>
                            setReloadKey((current) => current + 1)
                        }
                    >
                        <IconRefresh size={20} />
                    </Button>
                </div>

                {errorMessage && (
                    <Alert variant="danger" className="contract-inline-alert">
                        {errorMessage}
                    </Alert>
                )}

                <div className="table-responsive">
                    <table className="table contract-data-table mb-0">
                        <thead>
                            <tr>
                                <th>Type Code</th>
                                <th>Type Name</th>
                                <th>Category</th>
                                <th>Default Validity</th>
                                <th>Templates</th>
                                <th>Contracts</th>
                                <th>Status</th>
                                <th className="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <StateRow colSpan={8}>
                                    <Spinner animation="border" size="sm" />
                                    Loading contract types...
                                </StateRow>
                            ) : filteredTypes.length === 0 ? (
                                <StateRow colSpan={8}>
                                    No contract types found.
                                </StateRow>
                            ) : (
                                filteredTypes.map((contractType) => (
                                    <tr key={contractType.id}>
                                        <td className="contract-cell-strong">
                                            {contractType.contractTypeCode}
                                        </td>
                                        <td>
                                            {contractType.contractTypeName}
                                        </td>
                                        <td>{contractType.category || "-"}</td>
                                        <td>
                                            {contractType.validityDays
                                                ? `${contractType.validityDays} days`
                                                : "-"}
                                        </td>
                                        <td>{contractType.templateCount || 0}</td>
                                        <td>{contractType.contractCount || 0}</td>
                                        <td>
                                            <StatusBadge
                                                status={contractType.status}
                                            />
                                        </td>
                                        <td>
                                            <div className="contract-row-actions">
                                                <ActionButton
                                                    label="View"
                                                    icon={IconEye}
                                                    onClick={() =>
                                                        openViewModal(contractType)
                                                    }
                                                />
                                                <ActionButton
                                                    label="Edit"
                                                    icon={IconPencil}
                                                    onClick={() =>
                                                        openEditModal(contractType)
                                                    }
                                                />
                                                <ActionButton
                                                    label="Delete"
                                                    icon={IconTrash}
                                                    danger
                                                    disabled={
                                                        deletingId ===
                                                        contractType.id
                                                    }
                                                    onClick={() =>
                                                        handleDelete(contractType)
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
                        {filteredTypes.length} of {contractTypes.length} contract
                        types
                    </span>
                </footer>
            </section>

            <ContractTypeModal
                mode={modalMode}
                contractType={selectedType}
                form={form}
                error={modalError}
                submitting={submitting}
                onChange={handleChange}
                onClose={closeModal}
                onSubmit={handleSubmit}
                onEdit={() => openEditModal(selectedType)}
            />
        </div>
    );
}

function ContractTypeModal({
    mode,
    contractType,
    form,
    error,
    submitting,
    onChange,
    onClose,
    onSubmit,
    onEdit,
}) {
    if (!mode) {
        return null;
    }

    const isView = mode === "view";
    const title = isView
        ? "Contract Type Details"
        : mode === "edit"
          ? "Edit Contract Type"
          : "Create Contract Type";

    return (
        <Modal
            show
            onHide={onClose}
            size="lg"
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
                                label="Type Code"
                                value={contractType?.contractTypeCode}
                            />
                            <DetailItem
                                label="Type Name"
                                value={contractType?.contractTypeName}
                            />
                            <DetailItem
                                label="Category"
                                value={contractType?.category}
                            />
                            <DetailItem
                                label="Default Validity"
                                value={
                                    contractType?.validityDays
                                        ? `${contractType.validityDays} days`
                                        : "-"
                                }
                            />
                            <DetailItem
                                label="Status"
                                value={
                                    <StatusBadge
                                        status={contractType?.status}
                                    />
                                }
                            />
                            <DetailItem
                                label="Created By"
                                value={contractType?.createdBy}
                            />
                            <DetailItem
                                label="Created At"
                                value={formatContractDateTime(
                                    contractType?.createdAt
                                )}
                            />
                            <DetailItem
                                label="Updated At"
                                value={formatContractDateTime(
                                    contractType?.updatedAt
                                )}
                            />
                            <DetailItem
                                label="Templates / Contracts"
                                value={`${contractType?.templateCount || 0} / ${contractType?.contractCount || 0}`}
                            />
                            <div className="contract-detail-item contract-detail-full">
                                <span>Description</span>
                                <strong>
                                    {contractType?.description || "-"}
                                </strong>
                            </div>
                        </div>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="outline-secondary" onClick={onClose}>
                            Close
                        </Button>
                        <Button onClick={onEdit}>
                            <IconPencil size={18} />
                            Edit Contract Type
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
                            <FormField
                                label="Type Code"
                                name="contractTypeCode"
                                value={form.contractTypeCode}
                                onChange={onChange}
                                placeholder="e.g. NDA"
                                required
                            />
                            <FormField
                                label="Type Name"
                                name="contractTypeName"
                                value={form.contractTypeName}
                                onChange={onChange}
                                placeholder="e.g. Non-disclosure Agreement"
                                required
                            />
                            <FormField
                                label="Default Validity (days)"
                                name="validityDays"
                                type="number"
                                min="1"
                                value={form.validityDays}
                                onChange={onChange}
                            />
                            <div>
                                <label
                                    htmlFor="category"
                                    className="contract-form-label"
                                >
                                    Category
                                </label>
                                <select
                                    id="category"
                                    name="category"
                                    className="form-select"
                                    value={form.category}
                                    onChange={onChange}
                                >
                                    <option>Legal</option>
                                    <option>Commercial</option>
                                    <option>Human Resources</option>
                                    <option>Procurement</option>
                                    <option>Other</option>
                                </select>
                            </div>
                            <div>
                                <label
                                    htmlFor="status"
                                    className="contract-form-label"
                                >
                                    Status
                                </label>
                                <select
                                    id="status"
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
                                    htmlFor="description"
                                    className="contract-form-label"
                                >
                                    Description
                                </label>
                                <textarea
                                    id="description"
                                    name="description"
                                    className="form-control"
                                    rows={4}
                                    value={form.description}
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
                            {submitting
                                ? "Saving..."
                                : "Save Contract Type"}
                        </Button>
                    </Modal.Footer>
                </form>
            )}
        </Modal>
    );
}

function FormField({
    label,
    name,
    value,
    onChange,
    type = "text",
    placeholder = "",
    required = false,
    readOnly = false,
    min,
}) {
    return (
        <div>
            <label htmlFor={name} className="contract-form-label">
                {label}
            </label>
            <input
                id={name}
                name={name}
                type={type}
                min={min}
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
    danger = false,
    disabled = false,
}) {
    return (
        <button
            type="button"
            className={`contract-action-button${danger ? " danger" : ""}`}
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

export default ListContractType;
