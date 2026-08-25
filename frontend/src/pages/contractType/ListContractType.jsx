import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Modal, Spinner } from "react-bootstrap";
import {
    IconArrowDown,
    IconArrowUp,
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
    formatContractStatus,
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
    workflowName: "Contract approval workflow",
};

let workflowStepKey = 0;

function createWorkflowStep(overrides = {}) {
    workflowStepKey += 1;
    return {
        clientKey: `workflow-step-${workflowStepKey}`,
        stepOrder: 1,
        stepName: "",
        actionType: "APPROVE",
        requiredRoleCode: "",
        required: true,
        canReject: true,
        ...overrides,
    };
}

function createDefaultWorkflowSteps() {
    return [
        createWorkflowStep({
            stepOrder: 1,
            stepName: "Create contract",
            actionType: "CREATE",
            requiredRoleCode: "",
            canReject: false,
        }),
        createWorkflowStep({
            stepOrder: 2,
            stepName: "Review contract",
            actionType: "APPROVE",
            requiredRoleCode: "",
        }),
        createWorkflowStep({
            stepOrder: 3,
            stepName: "Sign contract",
            actionType: "SIGN",
            requiredRoleCode: "",
        }),
    ];
}

function createEmptyForm() {
    return {
        ...EMPTY_FORM,
        createdBy: localStorage.getItem("fullName") || "",
        workflowSteps: createDefaultWorkflowSteps(),
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
    const [workflowOptions, setWorkflowOptions] = useState({
        actionTypes: ["CREATE", "APPROVE", "SIGN"],
        roles: [],
    });

    useEffect(() => {
        let active = true;

        const loadContractTypes = async () => {
            setLoading(true);
            setErrorMessage("");

            try {
                const [typeResponse, workflowResponse] = await Promise.all([
                    contractTypeApi.getAllContractTypes(),
                    contractTypeApi.getWorkflowOptions(),
                ]);
                const items = unwrapApiResponse(typeResponse);
                const workflowPayload = unwrapApiResponse(workflowResponse);

                if (active) {
                    setContractTypes(Array.isArray(items) ? items : []);
                    setWorkflowOptions({
                        actionTypes: Array.isArray(workflowPayload?.actionTypes)
                            ? workflowPayload.actionTypes
                            : ["CREATE", "APPROVE", "SIGN"],
                        roles: Array.isArray(workflowPayload?.roles)
                            ? workflowPayload.roles
                            : [],
                    });
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
            workflowName:
                contractType.activeWorkflow?.workflowName
                || `${contractType.contractTypeName || "Contract"} workflow`,
            workflowSteps: Array.isArray(contractType.activeWorkflow?.steps)
                ? contractType.activeWorkflow.steps.map((step) =>
                    createWorkflowStep({
                        stepOrder: step.stepOrder,
                        stepName: step.stepName || "",
                        actionType: step.actionType || "APPROVE",
                        requiredRoleCode: step.requiredRoleCode || "",
                        required: step.required !== false,
                        canReject: Boolean(step.canReject),
                    }))
                : createDefaultWorkflowSteps(),
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

    const updateWorkflowStep = (clientKey, field, value) => {
        setForm((current) => ({
            ...current,
            workflowSteps: current.workflowSteps.map((step) =>
                step.clientKey === clientKey
                    ? {
                        ...step,
                        [field]: value,
                        ...(field === "actionType" && value === "CREATE"
                            ? { canReject: false }
                            : {}),
                    }
                    : step
            ),
        }));
    };

    const addWorkflowStep = () => {
        setForm((current) => ({
            ...current,
            workflowSteps: [
                ...current.workflowSteps,
                createWorkflowStep({
                    stepOrder: current.workflowSteps.length + 1,
                    stepName: "New workflow step",
                    requiredRoleCode: workflowOptions.roles[0]?.roleCode || "",
                }),
            ],
        }));
    };

    const removeWorkflowStep = (clientKey) => {
        setForm((current) => ({
            ...current,
            workflowSteps: current.workflowSteps
                .filter((step) => step.clientKey !== clientKey)
                .map((step, index) => ({ ...step, stepOrder: index + 1 })),
        }));
    };

    const moveWorkflowStep = (clientKey, direction) => {
        setForm((current) => {
            const index = current.workflowSteps.findIndex(
                (step) => step.clientKey === clientKey
            );
            const targetIndex = index + direction;
            if (index <= 0 || targetIndex <= 0
                || targetIndex >= current.workflowSteps.length) {
                return current;
            }
            const workflowSteps = [...current.workflowSteps];
            [workflowSteps[index], workflowSteps[targetIndex]] = [
                workflowSteps[targetIndex],
                workflowSteps[index],
            ];
            return {
                ...current,
                workflowSteps: workflowSteps.map((step, stepIndex) => ({
                    ...step,
                    stepOrder: stepIndex + 1,
                })),
            };
        });
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

        if (!form.workflowName.trim() || form.workflowSteps.length < 2) {
            setModalError(
                "Workflow name and at least two workflow steps are required."
            );
            return;
        }
        if (form.workflowSteps[0].actionType !== "CREATE"
            || form.workflowSteps.slice(1).some(
                (step) => step.actionType === "CREATE"
            )) {
            setModalError(
                "The workflow must start with exactly one CREATE step."
            );
            return;
        }
        if (form.workflowSteps.some(
            (step) => !step.stepName.trim() || !step.requiredRoleCode
        )) {
            setModalError("Every workflow step needs a name and a role.");
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
            workflowName: form.workflowName.trim(),
            workflowSteps: form.workflowSteps.map((step, index) => ({
                stepOrder: index + 1,
                stepName: step.stepName.trim(),
                actionType: step.actionType,
                requiredRoleCode: step.requiredRoleCode,
                required: true,
                canReject: step.actionType !== "CREATE"
                    && Boolean(step.canReject),
            })),
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
                workflowOptions={workflowOptions}
                onChange={handleChange}
                onStepChange={updateWorkflowStep}
                onAddStep={addWorkflowStep}
                onRemoveStep={removeWorkflowStep}
                onMoveStep={moveWorkflowStep}
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
    workflowOptions,
    onChange,
    onStepChange,
    onAddStep,
    onRemoveStep,
    onMoveStep,
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
                            <div className="contract-detail-item contract-detail-full">
                                <span>Active Workflow</span>
                                <strong>
                                    {contractType?.activeWorkflow
                                        ? `${contractType.activeWorkflow.workflowName} (V${contractType.activeWorkflow.versionNumber})`
                                        : "Not configured"}
                                </strong>
                                <WorkflowPreview
                                    workflow={contractType?.activeWorkflow}
                                />
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
                        <WorkflowEditor
                            form={form}
                            workflowOptions={workflowOptions}
                            onChange={onChange}
                            onStepChange={onStepChange}
                            onAddStep={onAddStep}
                            onRemoveStep={onRemoveStep}
                            onMoveStep={onMoveStep}
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

function WorkflowEditor({
    form,
    workflowOptions,
    onChange,
    onStepChange,
    onAddStep,
    onRemoveStep,
    onMoveStep,
}) {
    const actionTypes = workflowOptions?.actionTypes || [];
    const roles = workflowOptions?.roles || [];

    return (
        <section className="contract-type-workflow-editor">
            <div className="contract-type-workflow-heading">
                <div>
                    <h3>Contract workflow</h3>
                    <p>
                        Set the ordered roles that create, approve and sign this
                        contract type. Saving a changed workflow creates a new
                        immutable version.
                    </p>
                </div>
                <Button type="button" variant="outline-primary" onClick={onAddStep}>
                    <IconPlus size={18} />
                    Add step
                </Button>
            </div>
            <FormField
                label="Workflow Name"
                name="workflowName"
                value={form.workflowName}
                onChange={onChange}
                placeholder="Example: Employment contract workflow"
                required
            />
            <div className="contract-type-workflow-steps">
                {form.workflowSteps.map((step, index) => {
                    const roleExists = roles.some(
                        (role) => role.roleCode === step.requiredRoleCode
                    );
                    return (
                        <article
                            className="contract-type-workflow-step"
                            key={step.clientKey}
                        >
                            <div className="contract-type-workflow-order">
                                <strong>{index + 1}</strong>
                                <div>
                                    <button
                                        type="button"
                                        title="Move up"
                                        disabled={index <= 1}
                                        onClick={() => onMoveStep(step.clientKey, -1)}
                                    >
                                        <IconArrowUp size={16} />
                                    </button>
                                    <button
                                        type="button"
                                        title="Move down"
                                        disabled={index === 0
                                            || index === form.workflowSteps.length - 1}
                                        onClick={() => onMoveStep(step.clientKey, 1)}
                                    >
                                        <IconArrowDown size={16} />
                                    </button>
                                </div>
                            </div>
                            <div>
                                <label className="contract-form-label">Step name</label>
                                <input
                                    className="form-control"
                                    value={step.stepName}
                                    onChange={(event) => onStepChange(
                                        step.clientKey,
                                        "stepName",
                                        event.target.value
                                    )}
                                    required
                                />
                            </div>
                            <div>
                                <label className="contract-form-label">Action</label>
                                <select
                                    className="form-select"
                                    value={step.actionType}
                                    disabled={index === 0}
                                    onChange={(event) => onStepChange(
                                        step.clientKey,
                                        "actionType",
                                        event.target.value
                                    )}
                                >
                                    {(index === 0
                                        ? ["CREATE"]
                                        : actionTypes.filter(
                                            (action) => action !== "CREATE"
                                        )
                                    ).map((action) => (
                                        <option value={action} key={action}>
                                            {formatContractStatus(action)}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="contract-form-label">Required role</label>
                                <select
                                    className="form-select"
                                    value={step.requiredRoleCode}
                                    onChange={(event) => onStepChange(
                                        step.clientKey,
                                        "requiredRoleCode",
                                        event.target.value
                                    )}
                                    required
                                >
                                    <option value="">Select role</option>
                                    {!roleExists && step.requiredRoleCode && (
                                        <option value={step.requiredRoleCode}>
                                            {step.requiredRoleCode}
                                        </option>
                                    )}
                                    {roles.map((role) => (
                                        <option value={role.roleCode} key={role.id || role.roleCode}>
                                            {role.roleName} ({role.roleCode})
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <label className="form-check contract-type-workflow-reject">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    checked={Boolean(step.canReject)}
                                    disabled={index === 0}
                                    onChange={(event) => onStepChange(
                                        step.clientKey,
                                        "canReject",
                                        event.target.checked
                                    )}
                                />
                                <span className="form-check-label">Can reject</span>
                            </label>
                            <button
                                type="button"
                                className="contract-action-button danger"
                                title="Remove step"
                                disabled={index === 0 || form.workflowSteps.length <= 2}
                                onClick={() => onRemoveStep(step.clientKey)}
                            >
                                <IconTrash size={17} />
                            </button>
                        </article>
                    );
                })}
            </div>
        </section>
    );
}

function WorkflowPreview({ workflow }) {
    const steps = Array.isArray(workflow?.steps) ? workflow.steps : [];
    if (steps.length === 0) {
        return null;
    }

    return (
        <div className="contract-type-workflow-preview">
            {steps.map((step) => (
                <div key={step.id || step.stepOrder}>
                    <span>{step.stepOrder}</span>
                    <div>
                        <strong>{step.stepName}</strong>
                        <small>
                            {formatContractStatus(step.actionType)} · {step.requiredRoleCode}
                            {step.canReject ? " · can reject" : ""}
                        </small>
                    </div>
                </div>
            ))}
        </div>
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
