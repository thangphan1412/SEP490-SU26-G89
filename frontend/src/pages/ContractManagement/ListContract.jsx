import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import {
    IconCopy,
    IconEye,
    IconFileTypePdf,
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
    CONTRACT_STATUS,
    canExportContractPdf,
    canManageNewContract,
    createEmptyContract,
    createReplacementContract,
    formatContractDate,
    formatContractDateTime,
    formatContractMoney,
    formatContractStatus,
    getAvailableContractActions,
    getApiErrorMessage,
    getContractActionDetails,
    getCurrentContractActor,
    getRoleContractTask,
    loadProjectOptions,
    mapContractToForm,
    normalizeContractStatus,
    toContractRequest,
    toTransitionRequest,
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
    const [exportingPdfId, setExportingPdfId] = useState(null);
    const [transitionAction, setTransitionAction] = useState(null);
    const [transitionContract, setTransitionContract] = useState(null);
    const [transitionForm, setTransitionForm] = useState({
        comment: "",
        signerDateOfBirth: "",
    });
    const [transitionError, setTransitionError] = useState("");
    const [transitioning, setTransitioning] = useState(false);
    const [reloadKey, setReloadKey] = useState(0);
    const currentActor = getCurrentContractActor();

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
        if (!canManageNewContract(
            contract,
            currentActor.actorRole,
            currentActor.actorName
        )) {
            setErrorMessage(
                "Only the creator or an Admin can edit a contract while it is NEW."
            );
            return;
        }

        setSelectedContract(contract);
        setContractForm(mapContractToForm(contract));
        setModalError("");
        setModalMode("edit");
    };

    const openReplacementModal = (contract) => {
        if (
            normalizeContractStatus(contract?.contractStatus)
            !== CONTRACT_STATUS.CANCELLED
        ) {
            setErrorMessage(
                "Only a CANCELLED contract can be used to create a replacement."
            );
            return;
        }

        setSelectedContract(null);
        setContractForm(createReplacementContract(contract));
        setModalError("");
        setModalMode("create");
    };

    const openTransitionModal = (contract, action) => {
        setTransitionContract(contract);
        setTransitionAction(action);
        setTransitionForm({ comment: "", signerDateOfBirth: "" });
        setTransitionError("");
        setSelectedContract(null);
        setModalMode(null);
    };

    const closeTransitionModal = () => {
        if (transitioning) {
            return;
        }

        const contract = transitionContract;
        setTransitionAction(null);
        setTransitionContract(null);
        setTransitionError("");

        if (contract) {
            setSelectedContract(contract);
            setModalMode("view");
        }
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
                    attributeValues: {},
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
                    attributeValues: {},
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
                    attributeValues: {},
                };
            }

            if (name.startsWith("attributeValues.")) {
                const attributeKey = name.slice("attributeValues.".length);
                return {
                    ...current,
                    attributeValues: {
                        ...(current.attributeValues || {}),
                        [attributeKey]: nextValue,
                    },
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

    const handleTransition = async (event) => {
        event.preventDefault();

        if (transitioning || !transitionContract || !transitionAction) {
            return;
        }

        const actionDetails = getContractActionDetails(transitionAction);
        if (!currentActor.actorName || !currentActor.actorRole) {
            setTransitionError(
                "Your signed-in name and role are required to perform workflow actions."
            );
            return;
        }

        if (actionDetails.requiresComment && !transitionForm.comment.trim()) {
            setTransitionError("A reason is required for this action.");
            return;
        }

        if (
            actionDetails.requiresSignerDateOfBirth
            && !transitionForm.signerDateOfBirth
        ) {
            setTransitionError(
                "Signer date of birth is required for the legal age check."
            );
            return;
        }

        setTransitioning(true);
        setTransitionError("");

        try {
            const response = await contractApi.transitionContract(
                transitionContract.id,
                toTransitionRequest(transitionAction, transitionForm)
            );
            const updatedContract = unwrapApiResponse(response);

            setTransitionAction(null);
            setTransitionContract(null);
            setSelectedContract(updatedContract);
            setModalMode("view");
            setReloadKey((current) => current + 1);
        } catch (error) {
            setTransitionError(
                getApiErrorMessage(
                    error,
                    "Unable to update the contract workflow."
                )
            );
        } finally {
            setTransitioning(false);
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
            await contractApi.deleteContract(contract.id, currentActor);
            setReloadKey((current) => current + 1);
        } catch (error) {
            setErrorMessage(
                getApiErrorMessage(error, "Unable to delete the contract.")
            );
        } finally {
            setDeletingId(null);
        }
    };

    const handleExportPdf = async (contract) => {
        if (!contract?.id || exportingPdfId) {
            return;
        }

        setExportingPdfId(contract.id);
        setModalError("");

        try {
            const response = await contractApi.exportContractPdf(contract.id);
            const pdfBlob = response.data instanceof Blob
                ? response.data
                : new Blob([response.data], { type: "application/pdf" });
            const pdfUrl = URL.createObjectURL(pdfBlob);
            const previewWindow = window.open(pdfUrl, "_blank");

            if (previewWindow) {
                previewWindow.opener = null;
            } else {
                const link = document.createElement("a");
                link.href = pdfUrl;
                link.download = `contract-${contract.contractNumber || contract.id}.pdf`;
                document.body.appendChild(link);
                link.click();
                link.remove();
            }

            window.setTimeout(() => URL.revokeObjectURL(pdfUrl), 60_000);
        } catch (error) {
            setModalError(
                getApiErrorMessage(error, "Unable to export the completed PDF.")
            );
        } finally {
            setExportingPdfId(null);
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
                        Manage contracts from NEW through approval, signing and
                        the active lifecycle with role-based actions.
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
                                <th>Your Task</th>
                                <th>Effective Date</th>
                                <th className="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <StateRow colSpan={9}>
                                    <Spinner animation="border" size="sm" />
                                    Loading contracts...
                                </StateRow>
                            ) : contracts.length === 0 ? (
                                <StateRow colSpan={9}>
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
                                            <TaskBadge
                                                task={getRoleContractTask(
                                                    contract,
                                                    currentActor.actorRole,
                                                    currentActor.actorName
                                                )}
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
                                                {canExportContractPdf(contract) && (
                                                    <ActionButton
                                                        label="Export PDF"
                                                        icon={IconFileTypePdf}
                                                        disabled={
                                                            exportingPdfId
                                                            === contract.id
                                                        }
                                                        onClick={() =>
                                                            handleExportPdf(contract)
                                                        }
                                                    />
                                                )}
                                                <ActionButton
                                                    label="Edit"
                                                    icon={IconPencil}
                                                    disabled={
                                                        !canManageNewContract(
                                                            contract,
                                                            currentActor.actorRole,
                                                            currentActor.actorName
                                                        )
                                                    }
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
                                                        || !canManageNewContract(
                                                            contract,
                                                            currentActor.actorRole,
                                                            currentActor.actorName
                                                        )
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
                onCreateReplacement={() =>
                    openReplacementModal(selectedContract)
                }
                onTransition={(action) =>
                    openTransitionModal(selectedContract, action)
                }
                onExportPdf={() => handleExportPdf(selectedContract)}
                exportingPdf={
                    exportingPdfId != null
                    && exportingPdfId === selectedContract?.id
                }
                currentActor={currentActor}
            />

            <ContractTransitionModal
                action={transitionAction}
                contract={transitionContract}
                form={transitionForm}
                error={transitionError}
                submitting={transitioning}
                onChange={(event) => {
                    const { name, value } = event.target;
                    setTransitionForm((current) => ({
                        ...current,
                        [name]: value,
                    }));
                }}
                onClose={closeTransitionModal}
                onSubmit={handleTransition}
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
    onCreateReplacement,
    onTransition,
    onExportPdf,
    exportingPdf,
    currentActor,
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
    const availableActions = isView
        ? getAvailableContractActions(
            contract,
            currentActor.actorRole,
            currentActor.actorName
        )
        : [];

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
                        {error && <Alert variant="danger">{error}</Alert>}
                        <ContractDetails
                            contract={contract}
                            currentActor={currentActor}
                            availableActions={availableActions}
                            onTransition={onTransition}
                        />
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="outline-secondary" onClick={onClose}>
                            Close
                        </Button>
                        {normalizeContractStatus(contract?.contractStatus)
                            === CONTRACT_STATUS.CANCELLED && (
                            <Button
                                variant="outline-primary"
                                onClick={onCreateReplacement}
                            >
                                <IconCopy size={18} />
                                Create Replacement
                            </Button>
                        )}
                        {canExportContractPdf(contract) && (
                            <Button
                                variant="success"
                                onClick={onExportPdf}
                                disabled={exportingPdf}
                            >
                                {exportingPdf ? (
                                    <Spinner animation="border" size="sm" />
                                ) : (
                                    <IconFileTypePdf size={18} />
                                )}
                                {exportingPdf ? "Exporting..." : "Export PDF"}
                            </Button>
                        )}
                        {canManageNewContract(
                            contract,
                            currentActor.actorRole,
                            currentActor.actorName
                        ) && (
                            <Button onClick={onEdit}>
                                <IconPencil size={18} />
                                Edit Contract
                            </Button>
                        )}
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

function ContractDetails({
    contract,
    currentActor,
    availableActions,
    onTransition,
}) {
    if (!contract) {
        return <p>Contract information is unavailable.</p>;
    }

    const currentTask = getRoleContractTask(
        contract,
        currentActor.actorRole,
        currentActor.actorName
    );

    return (
        <>
            <ContractWorkflow
                status={contract.contractStatus}
                history={contract.statusHistory}
            />

            <div className="contract-current-task">
                <div>
                    <span>Your role-based task</span>
                    <strong>{currentTask.label}</strong>
                </div>
                <TaskBadge task={currentTask} />
            </div>

            {availableActions.length > 0 && (
                <section className="contract-workflow-actions">
                    <div>
                        <h3>Available workflow actions</h3>
                        <p>
                            Only valid actions for {currentActor.actorRole} are
                            displayed. The backend validates every transition.
                        </p>
                    </div>
                    <div className="contract-workflow-action-buttons">
                        {availableActions.map((action) => {
                            const details = getContractActionDetails(action);

                            return (
                                <Button
                                    key={action}
                                    variant={details.tone}
                                    onClick={() => onTransition(action)}
                                >
                                    {details.label}
                                </Button>
                            );
                        })}
                    </div>
                </section>
            )}

            <div className="contract-detail-grid">
                <DetailItem
                    label="Contract Number"
                    value={contract.contractNumber}
                />
                <DetailItem label="Title" value={contract.contractTitle} />
                <DetailItem
                    label="Contract Value"
                    value={formatContractMoney(
                        contract.attributeValues?.contract_value
                    )}
                />
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
                <DetailItem
                    label="Director Signed By"
                    value={contract.directorSignerName || "Not signed"}
                />
                <DetailItem
                    label="Director Signed At"
                    value={formatContractDateTime(contract.directorSignedAt)}
                />
                <DetailItem
                    label="Partner Signed By"
                    value={contract.partnerSignerName || "Not signed"}
                />
                <DetailItem
                    label="Partner Signed At"
                    value={formatContractDateTime(contract.partnerSignedAt)}
                />
                <DetailItem
                    label="Status Updated At"
                    value={formatContractDateTime(
                        contract.contractStatusUpdatedAt
                    )}
                />
                <DetailItem
                    label="Ended At"
                    value={formatContractDateTime(contract.contractEndedAt)}
                />
                <DetailItem
                    label="Replaces Contract"
                    value={contract.previousContractNumber || "-"}
                />
                {contract.contractCancellationReason && (
                    <DetailItem
                        label="Cancellation / Rejection Reason"
                        value={contract.contractCancellationReason}
                        full
                    />
                )}
            </div>
            <div className="contract-content-preview">
                <h3>Completed Contract Content</h3>
                <pre>{contract.renderedContractContent
                    || contract.contractContent
                    || "No content."}</pre>
            </div>
            <ContractHistory history={contract.statusHistory} />
        </>
    );
}

const CONTRACT_WORKFLOW_STEPS = [
    CONTRACT_STATUS.NEW,
    CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL,
    CONTRACT_STATUS.PENDING_DIRECTOR_SIGNATURE,
    CONTRACT_STATUS.PENDING_PARTNER_SIGNATURE,
    CONTRACT_STATUS.ACTIVE,
    CONTRACT_STATUS.ENDED,
];

function ContractWorkflow({ status, history = [] }) {
    const normalizedStatus = normalizeContractStatus(status);
    const cancelledFromStatus = normalizedStatus === CONTRACT_STATUS.CANCELLED
        ? normalizeContractStatus(history?.[0]?.fromStatus)
        : null;
    const progressStatus = cancelledFromStatus || normalizedStatus;
    const currentIndex = CONTRACT_WORKFLOW_STEPS.indexOf(progressStatus);

    return (
        <section className="contract-workflow-progress">
            <div className="contract-workflow-heading">
                <div>
                    <h3>Contract lifecycle</h3>
                    <p>Status can move only through the validated workflow.</p>
                </div>
                {normalizedStatus === CONTRACT_STATUS.CANCELLED && (
                    <StatusBadge status={CONTRACT_STATUS.CANCELLED} />
                )}
            </div>
            <div className="contract-workflow-steps">
                {CONTRACT_WORKFLOW_STEPS.map((step, index) => {
                    const isCompleted = currentIndex > index
                        || normalizedStatus === CONTRACT_STATUS.ENDED;
                    const isCurrent = normalizedStatus !== CONTRACT_STATUS.CANCELLED
                        && currentIndex === index;

                    return (
                        <div
                            key={step}
                            className={[
                                "contract-workflow-step",
                                isCompleted ? "completed" : "",
                                isCurrent ? "current" : "",
                            ].filter(Boolean).join(" ")}
                        >
                            <span>{isCompleted ? "✓" : index + 1}</span>
                            <small>{formatContractStatus(step)}</small>
                        </div>
                    );
                })}
            </div>
        </section>
    );
}

function ContractHistory({ history }) {
    const items = Array.isArray(history) ? history : [];

    return (
        <section className="contract-status-history">
            <div className="contract-status-history-heading">
                <h3>Status history</h3>
                <span>{items.length} event{items.length === 1 ? "" : "s"}</span>
            </div>

            {items.length === 0 ? (
                <p className="contract-history-empty">
                    No lifecycle events have been recorded.
                </p>
            ) : (
                <div className="contract-history-list">
                    {items.map((item) => (
                        <article key={item.id} className="contract-history-item">
                            <div className="contract-history-marker" />
                            <div>
                                <div className="contract-history-title">
                                    <strong>{formatContractStatus(item.action)}</strong>
                                    <span>
                                        {item.fromStatus
                                            ? `${formatContractStatus(item.fromStatus)} → `
                                            : ""}
                                        {formatContractStatus(item.toStatus)}
                                    </span>
                                </div>
                                <p>
                                    {item.actorName || "System"}
                                    {item.actorRole ? ` · ${item.actorRole}` : ""}
                                    {` · ${formatContractDateTime(item.changedAt)}`}
                                </p>
                                {item.comment && <blockquote>{item.comment}</blockquote>}
                                {item.signerAgeVerified === true && (
                                    <small className="contract-age-check valid">
                                        Legal age check passed (18+)
                                    </small>
                                )}
                                {item.signerAgeVerified === false && (
                                    <small className="contract-age-check invalid">
                                        Legal age check failed; contract cancelled
                                    </small>
                                )}
                            </div>
                        </article>
                    ))}
                </div>
            )}
        </section>
    );
}

function DetailItem({ label, value, full = false }) {
    return (
        <div className={`contract-detail-item${full ? " contract-detail-full" : ""}`}>
            <span>{label}</span>
            <strong>{value || "-"}</strong>
        </div>
    );
}

function TaskBadge({ task }) {
    const normalizedStatus = String(task?.status || "READ_ONLY")
        .toLowerCase()
        .replaceAll("_", "-");

    return (
        <span className={`contract-task-badge task-${normalizedStatus}`}>
            {task?.label || "Read only"}
        </span>
    );
}

function ContractTransitionModal({
    action,
    contract,
    form,
    error,
    submitting,
    onChange,
    onClose,
    onSubmit,
}) {
    if (!action || !contract) {
        return null;
    }

    const details = getContractActionDetails(action);

    return (
        <Modal
            show
            onHide={onClose}
            centered
            backdrop={submitting ? "static" : true}
            className="contract-modal"
        >
            <form onSubmit={onSubmit}>
                <Modal.Header closeButton>
                    <Modal.Title>{details.label}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {error && <Alert variant="danger">{error}</Alert>}

                    <div className="contract-transition-summary">
                        <strong>{contract.contractNumber}</strong>
                        <span>{contract.contractTitle}</span>
                        <small>
                            {formatContractStatus(contract.contractStatus)}
                        </small>
                    </div>

                    <p className="contract-transition-description">
                        {details.description}
                    </p>

                    {details.requiresSignerDateOfBirth && (
                        <>
                            <Alert variant="warning">
                                The birth date is used only for the 18+ check and
                                is not stored. If the signer is under 18, this
                                contract will automatically become CANCELLED.
                            </Alert>
                            <label
                                htmlFor="signerDateOfBirth"
                                className="contract-form-label"
                            >
                                Signer Date of Birth
                            </label>
                            <input
                                id="signerDateOfBirth"
                                name="signerDateOfBirth"
                                type="date"
                                className="form-control"
                                value={form.signerDateOfBirth}
                                onChange={onChange}
                                required
                            />
                        </>
                    )}

                    <label
                        htmlFor="transitionComment"
                        className="contract-form-label mt-3"
                    >
                        {details.requiresComment
                            ? "Reason / Required corrections"
                            : "Workflow note (optional)"}
                    </label>
                    <textarea
                        id="transitionComment"
                        name="comment"
                        className="form-control contract-transition-comment"
                        value={form.comment}
                        onChange={onChange}
                        placeholder={
                            details.requiresComment
                                ? "Explain the reason and clauses that must be corrected..."
                                : "Add a note to the status history..."
                        }
                        required={Boolean(details.requiresComment)}
                    />
                </Modal.Body>
                <Modal.Footer>
                    <Button
                        variant="outline-secondary"
                        onClick={onClose}
                        disabled={submitting}
                    >
                        Back
                    </Button>
                    <Button
                        type="submit"
                        variant={details.tone}
                        disabled={submitting}
                    >
                        {submitting && <Spinner animation="border" size="sm" />}
                        {submitting ? "Processing..." : details.label}
                    </Button>
                </Modal.Footer>
            </form>
        </Modal>
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
