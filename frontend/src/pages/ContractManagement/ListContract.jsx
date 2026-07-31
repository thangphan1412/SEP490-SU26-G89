import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import {
    IconEye,
    IconPencil,
    IconPlus,
    IconRefresh,
    IconSearch,
    IconTrash,
} from "@tabler/icons-react";
import contractApi from "../../services/contractService/contractApi.js";
import contractTypeApi from "../../services/contractTypeService/contractTypeApi.js";
import contractTemplateApi from "../../services/contractTemplateService/contractTemplateApi.js";
import ContractForm from "./ContractForm.jsx";
import {
    createEmptyContract,
    formatContractDate,
    formatContractDateTime,
    formatContractStatus,
    getApiErrorMessage,
    loadProjectOptions,
    mapContractToForm,
    toContractRequest,
    unwrapApiResponse,
    validateContract,
} from "./contractUtils.js";
import "../../assets/styles/css/layoutStyles/ContractWorkspace.css";

const PAGE_SIZE = 8;

function createPageNumbers(currentPage, totalPages) {
    if (totalPages <= 5) {
        return Array.from({ length: totalPages }, (_, index) => index);
    }

    return [...new Set([
        0,
        Math.max(0, currentPage - 1),
        currentPage,
        Math.min(totalPages - 1, currentPage + 1),
        totalPages - 1,
    ])].sort((first, second) => first - second);
}

function ListContract() {
    const [contracts, setContracts] = useState([]);
    const [projects, setProjects] = useState([]);
    const [contractTypes, setContractTypes] = useState([]);
    const [contractTemplates, setContractTemplates] = useState([]);
    const [searchInput, setSearchInput] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [availableStatuses, setAvailableStatuses] = useState([]);
    const [page, setPage] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [loadingOptions, setLoadingOptions] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [modalMode, setModalMode] = useState(null);
    const [selectedContract, setSelectedContract] = useState(null);
    const [contractForm, setContractForm] = useState(createEmptyContract);
    const [modalError, setModalError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [deletingId, setDeletingId] = useState(null);
    const [reloadKey, setReloadKey] = useState(0);

    const fetchContractOptions = useCallback(async () => {
        const [projectItems, typeResponse, templateResponse] =
            await Promise.all([
                loadProjectOptions(),
                contractTypeApi.getAllContractTypes(),
                contractTemplateApi.getAllContractTemplates(),
            ]);
        const typeItems = unwrapApiResponse(typeResponse);
        const templateItems = unwrapApiResponse(templateResponse);

        return {
            projectItems,
            typeItems: Array.isArray(typeItems) ? typeItems : [],
            templateItems: Array.isArray(templateItems) ? templateItems : [],
        };
    }, []);

    useEffect(() => {
        let active = true;

        const loadOptions = async () => {
            try {
                const options = await fetchContractOptions();

                if (active) {
                    setProjects(options.projectItems);
                    setContractTypes(options.typeItems);
                    setContractTemplates(options.templateItems);
                }
            } catch (error) {
                if (active) {
                    setErrorMessage(
                        getApiErrorMessage(
                            error,
                            "Unable to load contract creation options."
                        )
                    );
                }
            } finally {
                if (active) {
                    setLoadingOptions(false);
                }
            }
        };

        loadOptions();

        return () => {
            active = false;
        };
    }, [fetchContractOptions]);

    useEffect(() => {
        const debounceId = window.setTimeout(() => {
            setSearch(searchInput.trim());
            setPage(0);
        }, 350);

        return () => window.clearTimeout(debounceId);
    }, [searchInput]);

    useEffect(() => {
        let active = true;

        const loadContracts = async () => {
            setLoading(true);
            setErrorMessage("");

            try {
                const response = await contractApi.getAllContracts({
                    search,
                    status,
                    page,
                    sortBy: "contractCreatedAt",
                    sortDirection: "desc",
                });
                const payload = unwrapApiResponse(response);

                if (!active) {
                    return;
                }

                const pageCount = Number(payload?.totalPages) || 0;
                setContracts(Array.isArray(payload?.items) ? payload.items : []);
                setAvailableStatuses(
                    Array.isArray(payload?.availableStatuses)
                        ? payload.availableStatuses.filter(Boolean)
                        : []
                );
                setTotalElements(Number(payload?.totalElements) || 0);
                setTotalPages(pageCount);

                if (pageCount > 0 && page >= pageCount) {
                    setPage(pageCount - 1);
                }
            } catch (error) {
                if (active) {
                    setContracts([]);
                    setTotalElements(0);
                    setTotalPages(0);
                    setErrorMessage(
                        getApiErrorMessage(error, "Unable to load contracts.")
                    );
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        loadContracts();

        return () => {
            active = false;
        };
    }, [page, reloadKey, search, status]);

    const openCreateModal = () => {
        setSelectedContract(null);
        setContractForm(createEmptyContract());
        setModalError("");
        setModalMode("create");
    };

    const openViewModal = (contract) => {
        setSelectedContract(contract);
        setModalError("");
        setModalMode("view");
    };

    const openEditModal = (contract) => {
        setSelectedContract(contract);
        setContractForm(mapContractToForm(contract));
        setModalError("");
        setModalMode("edit");
    };

    const closeModal = () => {
        if (submitting) {
            return;
        }

        setModalMode(null);
        setSelectedContract(null);
        setModalError("");
    };

    const handleContractChange = (event) => {
        const { name, value, type, checked } = event.target;
        const nextValue = type === "checkbox" ? checked : value;

        setContractForm((current) => {
            if (name === "contractTypeId") {
                return {
                    ...current,
                    contractTypeId: value,
                    contractTemplateId: "",
                    contractTemplateVersionId: "",
                    contractContent: "",
                    contractLayoutJson: "",
                    saveAsTemplateVersion: false,
                    templateVersionName: "",
                    templateVersionNote: "",
                };
            }

            if (name === "contractTemplateId") {
                const template = contractTemplates.find(
                    (item) => item.id === value
                );
                const latestVersion = Array.isArray(template?.versions)
                    ? template.versions[0]
                    : null;

                return {
                    ...current,
                    contractTemplateId: value,
                    contractTemplateVersionId: latestVersion?.id || "",
                    contractContent: latestVersion?.templateContent || "",
                    contractLayoutJson: latestVersion?.layoutJson || "",
                    saveAsTemplateVersion: false,
                    templateVersionName: "",
                    templateVersionNote: "",
                };
            }

            if (name === "contractTemplateVersionId") {
                const template = contractTemplates.find(
                    (item) => item.id === current.contractTemplateId
                );
                const version = template?.versions?.find(
                    (item) => item.id === value
                );

                return {
                    ...current,
                    contractTemplateVersionId: value,
                    contractContent: version?.templateContent || "",
                    contractLayoutJson: version?.layoutJson || "",
                };
            }

            return { ...current, [name]: nextValue };
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (submitting) {
            return;
        }

        const validationMessage = validateContract(contractForm);
        if (validationMessage) {
            setModalError(validationMessage);
            return;
        }

        setSubmitting(true);
        setModalError("");

        try {
            if (modalMode === "edit") {
                await contractApi.updateContract(
                    selectedContract.id,
                    toContractRequest(contractForm, false)
                );
            } else {
                await contractApi.createContract(
                    toContractRequest(contractForm, true)
                );
            }
        } catch (error) {
            setModalError(
                getApiErrorMessage(error, "Unable to save the contract.")
            );
            return;
        } finally {
            setSubmitting(false);
        }

        closeModal();
        setReloadKey((current) => current + 1);
        setLoadingOptions(true);

        try {
            const options = await fetchContractOptions();
            setProjects(options.projectItems);
            setContractTypes(options.typeItems);
            setContractTemplates(options.templateItems);
        } catch (error) {
            setErrorMessage(
                getApiErrorMessage(
                    error,
                    "The contract was saved, but the form options could not be refreshed."
                )
            );
        } finally {
            setLoadingOptions(false);
        }
    };

    const handleDelete = async (contract) => {
        const confirmed = window.confirm(
            `Delete contract ${contract.contractNumber || contract.id}?`
        );

        if (!confirmed) {
            return;
        }

        setDeletingId(contract.id);
        setErrorMessage("");

        try {
            await contractApi.deleteContract(contract.id);
            setReloadKey((current) => current + 1);
        } catch (error) {
            setErrorMessage(
                getApiErrorMessage(error, "Unable to delete the contract.")
            );
        } finally {
            setDeletingId(null);
        }
    };

    const clearFilters = () => {
        setSearchInput("");
        setSearch("");
        setStatus("");
        setPage(0);
        setReloadKey((current) => current + 1);
    };

    const firstResult = totalElements === 0 ? 0 : page * PAGE_SIZE + 1;
    const lastResult = Math.min((page + 1) * PAGE_SIZE, totalElements);

    return (
        <div className="contract-workspace">
            <header className="contract-page-header">
                <div>
                    <h1>Contracts</h1>
                    <p>
                        Create, review, update and remove contracts from this
                        list.
                    </p>
                </div>
                <Button
                    className="contract-primary-button"
                    onClick={openCreateModal}
                    disabled={loadingOptions}
                >
                    <IconPlus size={20} />
                    New Contract
                </Button>
            </header>

            <section className="contract-list-card">
                <div className="contract-toolbar">
                    <label className="contract-search-box">
                        <IconSearch size={20} />
                        <input
                            aria-label="Search contracts"
                            placeholder="Search number, title, project, type or template..."
                            value={searchInput}
                            onChange={(event) =>
                                setSearchInput(event.target.value)
                            }
                        />
                    </label>

                    <select
                        aria-label="Filter by contract status"
                        className="form-select contract-filter-select"
                        value={status}
                        onChange={(event) => {
                            setStatus(event.target.value);
                            setPage(0);
                        }}
                    >
                        <option value="">All statuses</option>
                        {availableStatuses.map((statusOption) => (
                            <option key={statusOption} value={statusOption}>
                                {formatContractStatus(statusOption)}
                            </option>
                        ))}
                    </select>

                    <Button
                        variant="outline-secondary"
                        className="contract-refresh-button"
                        onClick={clearFilters}
                        title="Clear filters and refresh"
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
                                <th>Contract Number</th>
                                <th>Title</th>
                                <th>Project</th>
                                <th>Contract Type</th>
                                <th>Template / Version</th>
                                <th>Status</th>
                                <th>Effective Date</th>
                                <th className="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <StateRow colSpan={8}>
                                    <Spinner animation="border" size="sm" />
                                    Loading contracts...
                                </StateRow>
                            ) : contracts.length === 0 ? (
                                <StateRow colSpan={8}>
                                    No contracts found.
                                </StateRow>
                            ) : (
                                contracts.map((contract) => (
                                    <tr key={contract.id}>
                                        <td className="contract-cell-strong">
                                            {contract.contractNumber || "-"}
                                        </td>
                                        <td>{contract.contractTitle || "-"}</td>
                                        <td>{contract.projectName || "-"}</td>
                                        <td>
                                            {contract.contractTypeCode
                                                ? `${contract.contractTypeCode} - `
                                                : ""}
                                            {contract.contractTypeName || "-"}
                                        </td>
                                        <td>
                                            <div className="contract-template-cell">
                                                <span>
                                                    {contract.contractTemplateName ||
                                                        "No template"}
                                                </span>
                                                {contract.contractTemplateVersionNumber && (
                                                    <small>
                                                        V
                                                        {
                                                            contract.contractTemplateVersionNumber
                                                        }{" "}
                                                        ·{" "}
                                                        {
                                                            contract.contractTemplateVersionName
                                                        }
                                                    </small>
                                                )}
                                            </div>
                                        </td>
                                        <td>
                                            <StatusBadge
                                                status={contract.contractStatus}
                                            />
                                        </td>
                                        <td>
                                            {formatContractDate(
                                                contract.effectiveDate
                                            )}
                                        </td>
                                        <td>
                                            <div className="contract-row-actions">
                                                <ActionButton
                                                    label="View"
                                                    icon={IconEye}
                                                    onClick={() =>
                                                        openViewModal(contract)
                                                    }
                                                />
                                                <ActionButton
                                                    label="Edit"
                                                    icon={IconPencil}
                                                    onClick={() =>
                                                        openEditModal(contract)
                                                    }
                                                />
                                                <ActionButton
                                                    label="Delete"
                                                    icon={IconTrash}
                                                    danger
                                                    disabled={
                                                        deletingId === contract.id
                                                    }
                                                    onClick={() =>
                                                        handleDelete(contract)
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
                        Showing {firstResult} to {lastResult} of {totalElements}{" "}
                        results
                    </span>
                    <div className="contract-pagination">
                        <Button
                            variant="outline-secondary"
                            size="sm"
                            disabled={page === 0}
                            onClick={() =>
                                setPage((current) => Math.max(0, current - 1))
                            }
                        >
                            Previous
                        </Button>
                        {createPageNumbers(page, totalPages).map((pageNumber) => (
                            <Button
                                key={pageNumber}
                                variant={
                                    pageNumber === page
                                        ? "primary"
                                        : "outline-secondary"
                                }
                                size="sm"
                                onClick={() => setPage(pageNumber)}
                            >
                                {pageNumber + 1}
                            </Button>
                        ))}
                        <Button
                            variant="outline-secondary"
                            size="sm"
                            disabled={
                                totalPages === 0 || page >= totalPages - 1
                            }
                            onClick={() =>
                                setPage((current) =>
                                    Math.min(totalPages - 1, current + 1)
                                )
                            }
                        >
                            Next
                        </Button>
                    </div>
                </footer>
            </section>

            <ContractModal
                mode={modalMode}
                contract={selectedContract}
                form={contractForm}
                projects={projects}
                contractTypes={contractTypes}
                contractTemplates={contractTemplates}
                loadingOptions={loadingOptions}
                error={modalError}
                submitting={submitting}
                onChange={handleContractChange}
                onClose={closeModal}
                onSubmit={handleSubmit}
                onEdit={() => openEditModal(selectedContract)}
            />
        </div>
    );
}

function ContractModal({
    mode,
    contract,
    form,
    projects,
    contractTypes,
    contractTemplates,
    loadingOptions,
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
        ? "Contract Details"
        : mode === "edit"
          ? "Edit Contract"
          : "Create Contract";

    return (
        <Modal
            show
            onHide={onClose}
            size="xl"
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
                        <ContractDetails contract={contract} />
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="outline-secondary" onClick={onClose}>
                            Close
                        </Button>
                        <Button onClick={onEdit}>
                            <IconPencil size={18} />
                            Edit Contract
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
                        <ContractForm
                            contract={form}
                            onChange={onChange}
                            projects={projects}
                            contractTypes={contractTypes}
                            contractTemplates={contractTemplates}
                            loadingProjects={loadingOptions}
                            loadingContractOptions={loadingOptions}
                            creatorReadOnly={Boolean(
                                localStorage.getItem("fullName")
                            )}
                        />
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
                            {submitting ? "Saving..." : "Save Contract"}
                        </Button>
                    </Modal.Footer>
                </form>
            )}
        </Modal>
    );
}

function ContractDetails({ contract }) {
    if (!contract) {
        return <p>Contract information is unavailable.</p>;
    }

    return (
        <>
            <div className="contract-detail-grid">
                <DetailItem
                    label="Contract Number"
                    value={contract.contractNumber}
                />
                <DetailItem label="Title" value={contract.contractTitle} />
                <DetailItem label="Project" value={contract.projectName} />
                <DetailItem
                    label="Contract Type"
                    value={[
                        contract.contractTypeCode,
                        contract.contractTypeName,
                    ]
                        .filter(Boolean)
                        .join(" - ")}
                />
                <DetailItem
                    label="Template"
                    value={contract.contractTemplateName || "No template"}
                />
                <DetailItem
                    label="Template Version"
                    value={
                        contract.contractTemplateVersionNumber
                            ? `V${contract.contractTemplateVersionNumber} - ${contract.contractTemplateVersionName}`
                            : "No saved version"
                    }
                />
                <DetailItem
                    label="Status"
                    value={<StatusBadge status={contract.contractStatus} />}
                />
                <DetailItem
                    label="Effective Date"
                    value={formatContractDate(contract.effectiveDate)}
                />
                <DetailItem
                    label="Expiration Date"
                    value={formatContractDate(contract.expirationDate)}
                />
                <DetailItem
                    label="Created By"
                    value={contract.contractCreatedBy}
                />
                <DetailItem
                    label="Created At"
                    value={formatContractDateTime(contract.contractCreatedAt)}
                />
            </div>
            <div className="contract-content-preview">
                <h3>Contract Content</h3>
                <pre>{contract.contractContent || "No content."}</pre>
            </div>
        </>
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
    const normalized = formatContractStatus(status);
    const className = normalized.toLowerCase().replaceAll(" ", "-");

    return (
        <span className={`contract-status-badge status-${className}`}>
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

export default ListContract;
