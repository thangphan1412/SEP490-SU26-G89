import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
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
import electronicSignatureService from "../../services/signatureService/electronicSignatureService.js";
import ContractForm from "./ContractForm.jsx";
import { signApprovedContractPdf } from "./contractCrypto.js";
import { splitContractPages } from "./contractPageUtils.js";
import {
    CONTRACT_STATUS,
    CONTRACT_PROJECT_ACTION,
    canCreateReplacementContract,
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
    const [searchParameters, setSearchParameters] = useSearchParams();
    const requestedContractId = searchParameters.get("viewContractId");
    const [contracts, setContracts] = useState([]);
    const [projects, setProjects] = useState([]);
    const [contractTypes, setContractTypes] = useState([]);
    const [contractTemplates, setContractTemplates] = useState([]);
    const [projectContext, setProjectContext] = useState(null);
    const [loadingProjectContext, setLoadingProjectContext] = useState(false);
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
        electronicSignatureId: "",
        privateKey: "",
        digitalSignature: "",
    });
    const [electronicSignatures, setElectronicSignatures] = useState([]);
    const [loadingSignatures, setLoadingSignatures] = useState(false);
    const [signingPdfUrl, setSigningPdfUrl] = useState("");
    const [signingPdfError, setSigningPdfError] = useState("");
    const [loadingSigningPdf, setLoadingSigningPdf] = useState(false);
    const [transitionError, setTransitionError] = useState("");
    const [transitioning, setTransitioning] = useState(false);
    const [reloadKey, setReloadKey] = useState(0);
    const currentActor = getCurrentContractActor();

    useEffect(() => {
        const details = getContractActionDetails(
            transitionAction,
            transitionContract
        );
        if (!details.requiresElectronicSignature) {
            return undefined;
        }

        let active = true;
        electronicSignatureService.getAllElectronicSignature()
            .then((response) => {
                if (!active) {
                    return;
                }
                const items = unwrapApiResponse(response);
                const available = (Array.isArray(items) ? items : [])
                    .filter((signature) => signature.status === "ACTIVE");
                setElectronicSignatures(available);
                setTransitionForm((current) => ({
                    ...current,
                    electronicSignatureId: current.electronicSignatureId
                        || available.find((signature) => signature.default)?.id
                        || available[0]?.id
                        || "",
                }));
            })
            .catch((error) => {
                if (active) {
                    setElectronicSignatures([]);
                    setTransitionError(getApiErrorMessage(
                        error,
                        "Unable to load your electronic signatures."
                    ));
                }
            })
            .finally(() => {
                if (active) {
                    setLoadingSignatures(false);
                }
            });

        return () => {
            active = false;
        };
    }, [transitionAction, transitionContract]);

    useEffect(() => {
        const details = getContractActionDetails(
            transitionAction,
            transitionContract
        );
        if (!details.requiresElectronicSignature || !transitionContract?.id) {
            return undefined;
        }

        let active = true;
        let objectUrl = "";

        contractApi.exportContractPdf(transitionContract.id)
            .then((response) => {
                if (!active) {
                    return;
                }
                const pdfBlob = response.data instanceof Blob
                    ? response.data
                    : new Blob([response.data], { type: "application/pdf" });
                objectUrl = URL.createObjectURL(pdfBlob);
                setSigningPdfUrl(objectUrl);
            })
            .catch((error) => {
                if (active) {
                    setSigningPdfError(getApiErrorMessage(
                        error,
                        "Unable to load the CEO-approved PDF."
                    ));
                }
            })
            .finally(() => {
                if (active) {
                    setLoadingSigningPdf(false);
                }
            });

        return () => {
            active = false;
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };
    }, [transitionAction, transitionContract]);

    useEffect(function () {
        if (!requestedContractId) {
            return;
        }

        let active = true;

        // Tải và mở hợp đồng được truyền qua tham số điều hướng nếu component vẫn hoạt động.
        async function openRequestedContract() {
            try {
                const response = await contractApi.getContractById(
                    requestedContractId
                );
                const contract = unwrapApiResponse(response);

                if (active) {
                    setSelectedContract(contract);
                    setModalError("");
                    setModalMode("view");
                }
            } catch (error) {
                if (active) {
                    setErrorMessage(getApiErrorMessage(
                        error,
                        "Unable to load the selected contract."
                    ));
                }
            }
        }

        openRequestedContract();

        return function () {
            active = false;
        };
    }, [requestedContractId]);

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
        let active = true;
        const projectId = contractForm.projectId;
        if (!modalMode || modalMode === "view" || !projectId) {
            return () => {
                active = false;
            };
        }

        const loadContext = async () => {
            setLoadingProjectContext(true);
            try {
                const response = await contractApi.getProjectContext(projectId);
                const payload = unwrapApiResponse(response);
                if (active) {
                    setProjectContext(payload || null);
                }
            } catch (error) {
                if (active) {
                    setProjectContext(null);
                    setModalError(getApiErrorMessage(
                        error,
                        "Unable to load project phases, tasks and members."
                    ));
                }
            } finally {
                if (active) {
                    setLoadingProjectContext(false);
                }
            }
        };
        loadContext();
        return () => {
            active = false;
        };
    }, [contractForm.projectId, modalMode]);

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
        setProjectContext(null);
        setModalError("");
        setModalMode("create");
    };

    const openViewModal = async (contract) => {
        try {
            const response = await contractApi.getContractById(contract.id);
            setSelectedContract(unwrapApiResponse(response));
            setModalError("");
            setModalMode("view");
        } catch (error) {
            setErrorMessage(getApiErrorMessage(
                error,
                "Unable to load the selected contract."
            ));
        }
    };

    const openEditModal = async (contract) => {
        if (!canManageNewContract(
            contract,
            CONTRACT_PROJECT_ACTION.EDIT
        )) {
            setErrorMessage(
                "You do not have permission to edit this NEW contract."
            );
            return;
        }

        try {
            const detailedContract = contract.contractContent != null
                ? contract
                : unwrapApiResponse(
                    await contractApi.getContractById(contract.id)
                );
            setSelectedContract(detailedContract);
            setContractForm(mapContractToForm(detailedContract));
            setProjectContext(null);
            setLoadingProjectContext(false);
            setModalError("");
            setModalMode("edit");
        } catch (error) {
            setErrorMessage(getApiErrorMessage(
                error,
                "Unable to load the contract for editing."
            ));
        }
    };

    const openReplacementModal = (contract) => {
        if (!canCreateReplacementContract(contract)) {
            setErrorMessage(
                "A replacement requires a CANCELLED contract and CREATE_CONTRACTS permission."
            );
            return;
        }

        setSelectedContract(null);
        const replacement = createReplacementContract(contract);
        const selectedType = contractTypes.find(
            (item) => item.id === replacement.contractTypeId
        );
        setContractForm({
            ...replacement,
            workflowDefinition: selectedType?.activeWorkflow || null,
        });
        setProjectContext(null);
        setLoadingProjectContext(false);
        setModalError("");
        setModalMode("create");
    };

    const openTransitionModal = (contract, action) => {
        const isSigning = Boolean(
            getContractActionDetails(action, contract)
                .requiresElectronicSignature
        );
        setElectronicSignatures([]);
        setLoadingSignatures(isSigning);
        setSigningPdfUrl("");
        setSigningPdfError("");
        setLoadingSigningPdf(isSigning);
        setTransitionContract(contract);
        setTransitionAction(action);
        setTransitionForm({
            comment: "",
            electronicSignatureId: "",
            privateKey: "",
            digitalSignature: "",
        });
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
        setSigningPdfUrl("");
        setSigningPdfError("");
        setLoadingSigningPdf(false);

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
        setProjectContext(null);
        setLoadingProjectContext(false);
        setModalError("");

        if (requestedContractId) {
            const updatedParameters = new URLSearchParams(searchParameters);
            updatedParameters.delete("viewContractId");
            setSearchParameters(updatedParameters, { replace: true });
        }
    };

    const handleContractChange = (event) => {
        const { name, value, type, checked } = event.target;
        const nextValue = type === "checkbox" ? checked : value;

        if (name === "projectId") {
            setProjectContext(null);
            setLoadingProjectContext(false);
        }

        setContractForm((current) => {
            if (name === "projectId") {
                return {
                    ...current,
                    projectId: value,
                    phaseId: "",
                    taskId: "",
                    workflowAssignees: [],
                };
            }

            if (name === "phaseId") {
                return {
                    ...current,
                    phaseId: value,
                    taskId: "",
                };
            }

            if (name === "contractTypeId") {
                const selectedType = contractTypes.find(
                    (item) => item.id === value
                );
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
                    workflowDefinition: selectedType?.activeWorkflow || null,
                    workflowAssignees: [],
                };
            }

            if (name.startsWith("workflowAssignee.")) {
                const workflowStepId = name.slice("workflowAssignee.".length);
                const remainingAssignments = (current.workflowAssignees || [])
                    .filter((item) => item.workflowStepId !== workflowStepId);
                return {
                    ...current,
                    workflowAssignees: value
                        ? [
                            ...remainingAssignments,
                            { workflowStepId, userId: value },
                        ]
                        : remainingAssignments,
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

        const validationMessage = validateContract(
            contractForm,
            modalMode !== "edit"
        );
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

    const handlePrivateKeyFileChange = async (event) => {
        const file = event.target.files?.[0];
        if (!file) {
            return;
        }
        try {
            const privateKey = await file.text();
            if (!privateKey.includes("-----BEGIN PRIVATE KEY-----")) {
                throw new Error("The selected file is not a PKCS#8 private key.");
            }
            setTransitionForm((current) => ({
                ...current,
                privateKey: privateKey.trim(),
                digitalSignature: "",
            }));
            setTransitionError("");
        } catch (error) {
            setTransitionError(getApiErrorMessage(
                error,
                "Unable to read the RSA private key file."
            ));
        } finally {
            event.target.value = "";
        }
    };

    const handleTransition = async (event) => {
        event.preventDefault();

        if (transitioning || !transitionContract || !transitionAction) {
            return;
        }

        const actionDetails = getContractActionDetails(
            transitionAction,
            transitionContract
        );
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
        if (actionDetails.requiresElectronicSignature
            && !transitionForm.electronicSignatureId) {
            setTransitionError(
                "Select an active electronic signature before signing."
            );
            return;
        }
        if (actionDetails.requiresDigitalKey
            && !transitionForm.privateKey.trim()) {
            setTransitionError(
                "Provide the RSA private key downloaded when the electronic signature was created."
            );
            return;
        }
        const selectedElectronicSignature = electronicSignatures.find(
            (signature) => signature.id
                === transitionForm.electronicSignatureId
        );
        if (actionDetails.requiresDigitalKey
            && !selectedElectronicSignature?.publicKey) {
            setTransitionError(
                "This electronic signature has no registered RSA key. Open Signature Management and register one first."
            );
            return;
        }

        setTransitioning(true);
        setTransitionError("");

        try {
            let signedTransitionForm = transitionForm;
            if (actionDetails.requiresDigitalKey) {
                const pdfResponse = await contractApi.exportContractPdf(
                    transitionContract.id
                );
                const pdfBlob = pdfResponse.data instanceof Blob
                    ? pdfResponse.data
                    : new Blob(
                        [pdfResponse.data],
                        { type: "application/pdf" }
                    );
                const cryptoProof = await signApprovedContractPdf(
                    await pdfBlob.arrayBuffer(),
                    transitionForm.privateKey,
                    selectedElectronicSignature.publicKey
                );
                signedTransitionForm = {
                    ...transitionForm,
                    ...cryptoProof,
                };
            }

            const response = await contractApi.transitionContract(
                transitionContract.id,
                toTransitionRequest(
                    transitionAction,
                    signedTransitionForm,
                    transitionContract
                )
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
                        the active lifecycle with project-based permissions.
                    </p>
                </div>
                <Button
                    className="contract-primary-button"
                    onClick={openCreateModal}
                    disabled={loadingOptions || projects.length === 0}
                    title={
                        !loadingOptions && projects.length === 0
                            ? "You need CREATE_CONTRACTS permission in a project."
                            : "Create a contract"
                    }
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
                                                    currentActor.actorRole
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
                                                            CONTRACT_PROJECT_ACTION.EDIT
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
                                                            CONTRACT_PROJECT_ACTION.DELETE
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
                projectContext={projectContext}
                loadingOptions={loadingOptions}
                loadingProjectContext={loadingProjectContext}
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
                electronicSignatures={electronicSignatures}
                loadingSignatures={loadingSignatures}
                signingPdfUrl={signingPdfUrl}
                signingPdfError={signingPdfError}
                loadingSigningPdf={loadingSigningPdf}
                error={transitionError}
                submitting={transitioning}
                onChange={(event) => {
                    const { name, value } = event.target;
                    setTransitionForm((current) => ({
                        ...current,
                        [name]: value,
                    }));
                }}
                onPrivateKeyFileChange={handlePrivateKeyFileChange}
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
    projectContext,
    loadingOptions,
    loadingProjectContext,
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
            currentActor.actorRole
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
                        {canCreateReplacementContract(contract) && (
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
                            CONTRACT_PROJECT_ACTION.EDIT
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
                            projectContext={projectContext}
                            loadingProjects={loadingOptions}
                            loadingProjectContext={loadingProjectContext}
                            loadingContractOptions={loadingOptions}
                            creatorReadOnly={Boolean(
                                localStorage.getItem("fullName")
                            )}
                            projectReadOnly={mode === "edit"}
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
        currentActor.actorRole
    );
    const completedPages = splitContractPages(
        contract.renderedContractContent || contract.contractContent
    );

    return (
        <>
            <ContractWorkflow
                status={contract.contractStatus}
                history={contract.statusHistory}
                workflowRuntime={contract.workflowRuntime}
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
                            Only actions allowed by your project permission and
                            role are displayed. The backend validates every
                            transition.
                        </p>
                    </div>
                    <div className="contract-workflow-action-buttons">
                        {availableActions.map((action) => {
                            const details = getContractActionDetails(
                                action,
                                contract
                            );

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
                <DetailItem label="Phase" value={contract.phaseName || "-"} />
                <DetailItem label="Task" value={contract.taskTitle || "-"} />
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
                    label="CEO-approved PDF SHA-256"
                    value={contract.approvedPdfHash || "Not generated"}
                    full
                />
                <DetailItem
                    label="PDF Generated At"
                    value={formatContractDateTime(
                        contract.approvedPdfGeneratedAt
                    )}
                />
                <DetailItem
                    label="Cloudinary PDF"
                    value={contract.approvedPdfUrl ? (
                        <a
                            href={contract.approvedPdfUrl}
                            target="_blank"
                            rel="noreferrer"
                        >
                            Open approved PDF
                        </a>
                    ) : "Not uploaded"}
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
                {contract.workflowRuntime && (
                    <DetailItem
                        label="Workflow Version"
                        value={`${contract.workflowRuntime.workflowName} · V${contract.workflowRuntime.workflowVersionNumber}`}
                        full
                    />
                )}
            </div>
            {contract.workflowRuntime?.steps?.length > 0 && (
                <section className="contract-workflow-assignment-summary">
                    <h3>Workflow responsibilities</h3>
                    {contract.workflowRuntime.steps.map((step) => (
                        <article key={step.id}>
                            <span>{step.stepOrder}</span>
                            <div>
                                <strong>{step.stepName}</strong>
                                <small>
                                    {formatContractStatus(step.actionType)} · {step.requiredRoleCode}
                                </small>
                            </div>
                            <div>
                                <strong>{step.assignedUserName}</strong>
                                <small>{formatContractStatus(step.status)}</small>
                            </div>
                        </article>
                    ))}
                </section>
            )}
            <div className="contract-content-preview">
                <h3>Completed Contract Content</h3>
                <div className="contract-document-pages">
                    {completedPages.map((pageContent, index) => (
                        <section className="contract-document-page" key={index}>
                            <span>Page {index + 1}</span>
                            <pre>{pageContent || "No content on this page."}</pre>
                        </section>
                    ))}
                </div>
            </div>
            <ContractHistory history={contract.statusHistory} />
        </>
    );
}

const CONTRACT_WORKFLOW_STEPS = [
    CONTRACT_STATUS.NEW,
    CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL,
    CONTRACT_STATUS.PENDING_DIRECTOR_APPROVAL,
    CONTRACT_STATUS.PENDING_DIRECTOR_SIGNATURE,
    CONTRACT_STATUS.PENDING_PARTNER_SIGNATURE,
    CONTRACT_STATUS.PENDING_EFFECTIVE,
    CONTRACT_STATUS.ACTIVE,
    CONTRACT_STATUS.ENDED,
];

function ContractWorkflow({ status, history = [], workflowRuntime = null }) {
    const normalizedStatus = normalizeContractStatus(status);
    const runtimeSteps = Array.isArray(workflowRuntime?.steps)
        ? workflowRuntime.steps
        : [];

    if (runtimeSteps.length > 0) {
        return (
            <section className="contract-workflow-progress">
                <div className="contract-workflow-heading">
                    <div>
                        <h3>{workflowRuntime.workflowName}</h3>
                        <p>
                            Contract Type workflow version {workflowRuntime.workflowVersionNumber}.
                            Each step is assigned to one project member.
                        </p>
                    </div>
                    {normalizedStatus === CONTRACT_STATUS.CANCELLED && (
                        <StatusBadge status={CONTRACT_STATUS.CANCELLED} />
                    )}
                </div>
                <div className="contract-workflow-steps">
                    {runtimeSteps.map((step, index) => {
                        const isCompleted = step.status === "COMPLETED";
                        const isCurrent = step.status === "PENDING";
                        return (
                            <div
                                key={step.id}
                                className={[
                                    "contract-workflow-step",
                                    isCompleted ? "completed" : "",
                                    isCurrent ? "current" : "",
                                ].filter(Boolean).join(" ")}
                                title={`Assigned to ${step.assignedUserName || "-"}`}
                            >
                                <span>{isCompleted ? "✓" : index + 1}</span>
                                <small>{step.stepName}</small>
                                <small>{step.assignedUserName || step.requiredRoleCode}</small>
                            </div>
                        );
                    })}
                </div>
            </section>
        );
    }
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
    electronicSignatures,
    loadingSignatures,
    signingPdfUrl,
    signingPdfError,
    loadingSigningPdf,
    error,
    submitting,
    onChange,
    onPrivateKeyFileChange,
    onClose,
    onSubmit,
}) {
    if (!action || !contract) {
        return null;
    }

    const details = getContractActionDetails(action, contract);
    const selectedSignature = electronicSignatures.find(
        (signature) => signature.id === form.electronicSignatureId
    );
    const isSigning = Boolean(details.requiresElectronicSignature);
    const requiredRole = String(
        contract.workflowRuntime?.currentStepRequiredRoleCode || ""
    ).toUpperCase();
    const signatureSide = requiredRole.includes("PARTNER")
        || requiredRole.includes("EXTERNAL")
        ? "partner"
        : "company";

    return (
        <Modal
            show
            onHide={onClose}
            centered
            size={isSigning ? "xl" : undefined}
            dialogClassName={isSigning ? "contract-signing-dialog" : undefined}
            backdrop={submitting ? "static" : true}
            className={`contract-modal${
                isSigning ? " contract-signing-modal" : ""
            }`}
        >
            <form onSubmit={onSubmit} className="contract-transition-form">
                <Modal.Header closeButton>
                    <Modal.Title>{details.label}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {error && <Alert variant="danger">{error}</Alert>}
                    <div className={
                        isSigning ? "contract-signing-layout" : undefined
                    }>
                        <section className="contract-signing-controls">
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

                            {details.verifiesAccountDateOfBirth && (
                                <Alert variant="warning">
                                    The signer must be at least 18 years old. Date
                                    of birth is verified automatically from the
                                    signed-in user account. An invalid signing
                                    attempt is rejected without changing the
                                    contract status.
                                </Alert>
                            )}

                            {details.requiresElectronicSignature && (
                                <div className="contract-signature-picker">
                                    <label
                                        htmlFor="electronicSignatureId"
                                        className="contract-form-label"
                                    >
                                        Electronic signature
                                    </label>
                                    <select
                                        id="electronicSignatureId"
                                        name="electronicSignatureId"
                                        className="form-select"
                                        value={form.electronicSignatureId}
                                        onChange={onChange}
                                        disabled={submitting || loadingSignatures}
                                        required
                                    >
                                        <option value="">
                                            {loadingSignatures
                                                ? "Loading signatures..."
                                                : "Select an active signature"}
                                        </option>
                                        {electronicSignatures.map((signature) => (
                                            <option
                                                key={signature.id}
                                                value={signature.id}
                                            >
                                                {signature.signatureName}
                                                {signature.default
                                                    ? " (Default)"
                                                    : ""}
                                            </option>
                                        ))}
                                    </select>
                                    {!loadingSignatures
                                        && electronicSignatures.length === 0 && (
                                        <Alert
                                            variant="warning"
                                            className="mt-2 mb-0"
                                        >
                                            You do not have an active electronic
                                            signature. Create or activate one in
                                            Signature Management before signing.
                                        </Alert>
                                    )}
                                    {selectedSignature?.imageUrl && (
                                        <div className="contract-signature-preview">
                                            <img
                                                src={selectedSignature.imageUrl}
                                                alt={selectedSignature.signatureName}
                                            />
                                            <div>
                                                <strong>
                                                    {selectedSignature.signatureName}
                                                </strong>
                                                <small>
                                                    The image is shown on the PDF;
                                                    the RSA proof protects its
                                                    SHA-256 content.
                                                </small>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}

                            {details.requiresDigitalKey && (
                                <div className="contract-signing-keys">
                                    <div className="contract-signing-key-header">
                                        <div>
                                            <strong>Registered RSA-2048 key</strong>
                                            <small>
                                                The server verifies with the public
                                                key registered for this signature.
                                            </small>
                                        </div>
                                    </div>

                                    {selectedSignature
                                        && !selectedSignature.publicKey && (
                                        <Alert variant="warning">
                                            This is a legacy signature without an
                                            RSA key. Register a key in Signature
                                            Management before using it.
                                        </Alert>
                                    )}

                                    <label
                                        htmlFor="privateKey"
                                        className="contract-form-label"
                                    >
                                        Private / secret key (PKCS#8 PEM)
                                    </label>
                                    <textarea
                                        id="privateKey"
                                        name="privateKey"
                                        className="form-control contract-key-input contract-private-key-input"
                                        value={form.privateKey}
                                        onChange={onChange}
                                        disabled={submitting}
                                        placeholder="-----BEGIN PRIVATE KEY-----"
                                        autoComplete="off"
                                        spellCheck="false"
                                        required
                                    />
                                    <input
                                        type="file"
                                        className="form-control mt-2"
                                        accept=".pem,.key,text/plain"
                                        onChange={onPrivateKeyFileChange}
                                        disabled={submitting}
                                        aria-label="Load private key file"
                                    />
                                    {form.privateKey && (
                                        <details className="contract-private-key-reveal">
                                            <summary>Show private key</summary>
                                            <pre>{form.privateKey}</pre>
                                        </details>
                                    )}

                                    <label
                                        htmlFor="publicKey"
                                        className="contract-form-label mt-3"
                                    >
                                        Public key (X.509/SPKI PEM)
                                    </label>
                                    <textarea
                                        id="publicKey"
                                        className="form-control contract-key-input"
                                        value={selectedSignature?.publicKey || ""}
                                        placeholder="Select an RSA-enabled signature"
                                        spellCheck="false"
                                        readOnly
                                    />
                                    {selectedSignature?.publicKeyFingerprint && (
                                        <small className="contract-key-fingerprint">
                                            Fingerprint: {
                                                selectedSignature
                                                    .publicKeyFingerprint
                                            }
                                        </small>
                                    )}
                                </div>
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
                        </section>

                        {isSigning && (
                            <aside className="contract-signing-pdf-panel">
                                <div className="contract-signing-pdf-header">
                                    <div>
                                        <strong>CEO-approved PDF</strong>
                                        <small>
                                            SHA-256: {contract.approvedPdfHash
                                                || "Checking..."}
                                        </small>
                                    </div>
                                    <span>Signature preview</span>
                                </div>

                                <div className="contract-signing-pdf-viewer">
                                    {loadingSigningPdf && (
                                        <div className="contract-signing-pdf-state">
                                            <Spinner animation="border" />
                                            <span>Loading approved PDF...</span>
                                        </div>
                                    )}
                                    {signingPdfError && (
                                        <Alert variant="danger" className="m-3">
                                            {signingPdfError}
                                        </Alert>
                                    )}
                                    {!loadingSigningPdf
                                        && !signingPdfError
                                        && signingPdfUrl && (
                                        <>
                                            <iframe
                                                title="CEO-approved contract PDF"
                                                src={`${signingPdfUrl}#toolbar=0&navpanes=0&view=FitH`}
                                            />
                                            {selectedSignature?.imageUrl && (
                                                <div className={
                                                    `contract-pdf-signature-stamp ${
                                                        signatureSide
                                                    }`
                                                }>
                                                    <span>Preview</span>
                                                    <img
                                                        src={selectedSignature.imageUrl}
                                                        alt={
                                                            selectedSignature
                                                                .signatureName
                                                        }
                                                    />
                                                    <small>
                                                        {selectedSignature.signatureName}
                                                    </small>
                                                </div>
                                            )}
                                        </>
                                    )}
                                </div>
                            </aside>
                        )}
                    </div>
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
                        disabled={
                            submitting
                            || (isSigning && !selectedSignature?.publicKey)
                        }
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
