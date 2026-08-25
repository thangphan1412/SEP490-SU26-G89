import contractApi from "../../services/contractService/contractApi.js";

export const CONTRACT_STATUS = Object.freeze({
    NEW: "NEW",
    PENDING_APPROVAL: "PENDING_APPROVAL",
    PENDING_SIGNATURE: "PENDING_SIGNATURE",
    PENDING_INTERNAL_APPROVAL: "PENDING_INTERNAL_APPROVAL",
    PENDING_DIRECTOR_APPROVAL: "PENDING_DIRECTOR_APPROVAL",
    PENDING_DIRECTOR_SIGNATURE: "PENDING_DIRECTOR_SIGNATURE",
    PENDING_PARTNER_SIGNATURE: "PENDING_PARTNER_SIGNATURE",
<<<<<<< HEAD
    PENDING_EFFECTIVE: "PENDING_EFFECTIVE",
=======
    SIGNED: "SIGNED",
>>>>>>> 7d6eb51fe9c660b46d1a1bc0200bcbbc73cf5f51
    ACTIVE: "ACTIVE",
    ENDED: "ENDED",
    CANCELLED: "CANCELLED",
});

export const defaultContractStatuses = Object.values(CONTRACT_STATUS);

export const CONTRACT_ACTION = Object.freeze({
    COMPLETE_STEP: "COMPLETE_STEP",
    SUBMIT: "SUBMIT",
    APPROVE_INTERNAL: "APPROVE_INTERNAL",
    APPROVE_DIRECTOR: "APPROVE_DIRECTOR",
    SIGN_DIRECTOR: "SIGN_DIRECTOR",
    SIGN_PARTNER: "SIGN_PARTNER",
    CANCEL: "CANCEL",
    REJECT: "REJECT",
});

export const CONTRACT_PROJECT_ACTION = Object.freeze({
    VIEW: "VIEW_CONTRACTS",
    CREATE: "CREATE_CONTRACTS",
    EDIT: "EDIT_CONTRACTS",
    DELETE: "DELETE_CONTRACTS",
    SUBMIT: "SUBMIT_CONTRACTS",
    APPROVE: "APPROVE_CONTRACTS",
    SIGN: "SIGN_CONTRACTS",
    CANCEL: "CANCEL_CONTRACTS",
    EXPORT: "EXPORT_CONTRACTS",
});

export const CONTRACT_WORKFLOW = Object.freeze({
    workflowName: "Contract approval and signing workflow",
    versionNumber: 1,
    steps: [
        { id: "contract-step-1", stepOrder: 1, stepName: "Prepare and submit", actionType: "CREATE", requiredRoleCode: "EMPLOYEE", requiredPermissionCodes: ["VIEW_CONTRACTS", "CREATE_CONTRACTS", "SUBMIT_CONTRACTS"] },
        { id: "contract-step-2", stepOrder: 2, stepName: "Head of Department approval", actionType: "APPROVE", requiredRoleCode: "HEADOFDEPARTMENT", requiredPermissionCodes: ["VIEW_CONTRACTS", "APPROVE_CONTRACTS"] },
        { id: "contract-step-3", stepOrder: 3, stepName: "CEO final approval", actionType: "APPROVE", requiredRoleCode: "CEO", requiredPermissionCodes: ["VIEW_CONTRACTS", "APPROVE_CONTRACTS"] },
        { id: "contract-step-4", stepOrder: 4, stepName: "CEO electronic signature", actionType: "SIGN", requiredRoleCode: "CEO", requiredPermissionCodes: ["VIEW_CONTRACTS", "SIGN_CONTRACTS"] },
        { id: "contract-step-5", stepOrder: 5, stepName: "Assigned representative signature", actionType: "SIGN", requiredRoleCode: "ANY", requiredPermissionCodes: ["VIEW_CONTRACTS", "SIGN_CONTRACTS"] },
    ],
});

const ACTION_DETAILS = Object.freeze({
    [CONTRACT_ACTION.COMPLETE_STEP]: {
        label: "Complete current step",
        description: "Complete the workflow step assigned to you.",
        tone: "primary",
    },
    [CONTRACT_ACTION.SUBMIT]: {
        label: "Submit for approval",
        description: "Send this contract to the internal approval stage.",
        tone: "primary",
    },
    [CONTRACT_ACTION.APPROVE_INTERNAL]: {
        label: "Approve internal review",
        description: "Complete internal approval and send the contract to the director.",
        tone: "primary",
    },
    [CONTRACT_ACTION.APPROVE_DIRECTOR]: {
        label: "CEO approve & generate PDF",
        description: "Approve the contract and freeze an unsigned PDF with its SHA-256 hash. Signing remains a separate step.",
        tone: "primary",
    },
    [CONTRACT_ACTION.SIGN_DIRECTOR]: {
        label: "Director sign",
        description: "Verify the director's account age and confirm the director signature.",
        tone: "primary",
        verifiesAccountDateOfBirth: true,
        requiresElectronicSignature: true,
        requiresDigitalKey: true,
    },
    [CONTRACT_ACTION.SIGN_PARTNER]: {
        label: "Partner sign",
        description: "Verify the partner's account age and activate the signed contract.",
        tone: "primary",
        verifiesAccountDateOfBirth: true,
        requiresElectronicSignature: true,
        requiresDigitalKey: true,
    },
    [CONTRACT_ACTION.CANCEL]: {
        label: "Cancel contract",
        description: "Cancel this contract and record the mandatory reason.",
        tone: "danger",
        requiresComment: true,
    },
    [CONTRACT_ACTION.REJECT]: {
        label: "Reject contract",
        description: "Reject this contract. Corrections must be made in a new contract.",
        tone: "danger",
        requiresComment: true,
    },
});

export function unwrapApiResponse(response) {
    return response?.data?.data ?? response?.data ?? response;
}

export function getApiErrorMessage(error, fallbackMessage) {
    return error?.response?.data?.message || error?.message || fallbackMessage;
}

export function getCurrentContractActor() {
    return {
        actorName: localStorage.getItem("fullName") || "",
        actorRole: localStorage.getItem("role") || "",
    };
}

export function createEmptyContract(projectId = "") {
    return {
        projectId,
        phaseId: "",
        taskId: "",
        contractTypeId: "",
        contractTemplateId: "",
        contractTemplateVersionId: "",
        contractNumber: "",
        contractTitle: "",
        contractStatus: CONTRACT_STATUS.NEW,
        effectiveDate: "",
        expirationDate: "",
        contractCreatedBy: localStorage.getItem("fullName") || "",
        contractCreatedAt: null,
        contractContent: "",
        contractLayoutJson: "",
        attributeValues: {},
        saveAsTemplateVersion: false,
        templateVersionName: "",
        templateVersionNote: "",
        previousContractId: "",
        previousContractNumber: "",
        workflowDefinition: CONTRACT_WORKFLOW,
        workflowAssignees: [],
    };
}

export function mapContractToForm(contract) {
    return {
        projectId: contract?.projectId || "",
        phaseId: contract?.phaseId || "",
        taskId: contract?.taskId || "",
        contractTypeId: contract?.contractTypeId || "",
        contractTemplateId: contract?.contractTemplateId || "",
        contractTemplateVersionId: contract?.contractTemplateVersionId || "",
        contractNumber: contract?.contractNumber || "",
        contractTitle: contract?.contractTitle || "",
        contractStatus: normalizeContractStatus(contract?.contractStatus),
        effectiveDate: contract?.effectiveDate || "",
        expirationDate: contract?.expirationDate || "",
        contractCreatedBy: contract?.contractCreatedBy || "",
        contractCreatedAt: contract?.contractCreatedAt || null,
        contractContent: contract?.contractContent || "",
        contractLayoutJson: contract?.contractLayoutJson || "",
        attributeValues: { ...(contract?.attributeValues || {}) },
        saveAsTemplateVersion: false,
        templateVersionName: "",
        templateVersionNote: "",
        previousContractId: contract?.previousContractId || "",
        previousContractNumber: contract?.previousContractNumber || "",
        workflowDefinition: contract?.workflowRuntime
            ? {
                ...contract.workflowRuntime,
                steps: (contract.workflowRuntime.steps || []).map((step) => ({
                    ...step,
                    id: step.workflowStepId,
                })),
            }
            : null,
        workflowAssignees: Array.isArray(contract?.workflowRuntime?.steps)
            ? contract.workflowRuntime.steps.map((step) => ({
                workflowStepId: step.workflowStepId,
                userId: step.assignedUserId,
            })).filter((item) => item.workflowStepId && item.userId)
            : [],
    };
}

export function createReplacementContract(contract) {
    return {
        ...mapContractToForm(contract),
        contractNumber: "",
        contractStatus: CONTRACT_STATUS.NEW,
        contractCreatedBy: localStorage.getItem("fullName") || "",
        contractCreatedAt: null,
        saveAsTemplateVersion: false,
        templateVersionName: "",
        templateVersionNote: "",
        previousContractId: contract?.id || "",
        previousContractNumber: contract?.contractNumber || "",
        workflowDefinition: null,
        workflowAssignees: [],
    };
}

export function toContractRequest(contract, isCreating = false) {
    const actor = getCurrentContractActor();

    return {
        projectId: contract.projectId || null,
        taskId: contract.taskId || null,
        contractTypeId: contract.contractTypeId || null,
        contractTemplateId: contract.contractTemplateId || null,
        contractTemplateVersionId:
            contract.contractTemplateVersionId || null,
        contractNumber: contract.contractNumber.trim(),
        contractTitle: contract.contractTitle.trim(),
        contractStatus: normalizeContractStatus(contract.contractStatus),
        effectiveDate: contract.effectiveDate || null,
        expirationDate: contract.expirationDate || null,
        contractCreatedBy: contract.contractCreatedBy.trim() || null,
        contractCreatedAt: isCreating
            ? null
            : contract.contractCreatedAt,
        contractContent: contract.contractContent.trim() || null,
        contractLayoutJson: contract.contractLayoutJson.trim() || null,
        saveAsTemplateVersion: Boolean(contract.saveAsTemplateVersion),
        templateVersionName: contract.templateVersionName.trim() || null,
        templateVersionNote: contract.templateVersionNote.trim() || null,
        previousContractId: contract.previousContractId || null,
        actorName: actor.actorName,
        actorRole: actor.actorRole,
        attributeValues: Object.fromEntries(
            Object.entries(contract.attributeValues || {})
                .map(([key, value]) => [key, String(value ?? "").trim()])
                .filter(([, value]) => value !== "")
        ),
        workflowAssignees: (contract.workflowAssignees || []).map(
            (assignment) => ({
                workflowStepId: assignment.workflowStepId,
                stepOrder: contract.workflowDefinition?.steps?.find(
                    (step) => step.id === assignment.workflowStepId
                )?.stepOrder || assignment.stepOrder,
                userId: assignment.userId,
            })
        ),
    };
}

export function validateContract(contract, isCreating = true) {
    const actor = getCurrentContractActor();

    if (!actor.actorName || !actor.actorRole) {
        return "Your signed-in name and role are required to manage contracts.";
    }

    if (!contract.projectId) {
        return "Please select a project.";
    }

    if (!contract.taskId) {
        return "Please select a task from a project phase.";
    }

    if (!contract.contractTypeId) {
        return "Please select a contract type.";
    }

    if (isCreating) {
        const workflowSteps = contract.workflowDefinition?.steps;
        if (!Array.isArray(workflowSteps) || workflowSteps.length < 2) {
            return "The selected contract type does not have an active workflow.";
        }
        const requiredAssigneeSteps = workflowSteps.filter(
            (step) => step.actionType !== "CREATE"
        );
        const assignedStepIds = new Set(
            (contract.workflowAssignees || [])
                .filter((assignment) => assignment.userId)
                .map((assignment) => assignment.workflowStepId)
        );
        if (requiredAssigneeSteps.some(
            (step) => !assignedStepIds.has(step.id)
        )) {
            return "Please assign a project member to every workflow step.";
        }
    }

    if (!contract.contractNumber.trim() || !contract.contractTitle.trim()) {
        return "Contract number and title are required.";
    }

    if (!contract.contractCreatedBy.trim()) {
        return "Contract creator is required.";
    }

    if (
        normalizeContractRole(actor.actorRole) !== "ADMIN"
        && contract.contractCreatedBy.trim().toLowerCase()
            !== actor.actorName.trim().toLowerCase()
    ) {
        return "The signed-in user must be the contract creator.";
    }

    if (!contract.effectiveDate || !contract.expirationDate) {
        return "Effective date and expiration date are required.";
    }

    if (contract.effectiveDate > contract.expirationDate) {
        return "Expiration date must be on or after the effective date.";
    }

    const contractValue = contract.attributeValues?.contract_value;
    if (
        contractValue !== undefined
        && contractValue !== ""
        && Number(contractValue) <= 0
    ) {
        return "Contract value must be greater than zero.";
    }

    if (contract.saveAsTemplateVersion && !contract.contractTemplateId) {
        return "Please select a template before saving a reusable version.";
    }

    if (
        contract.saveAsTemplateVersion &&
        !contract.contractContent.trim()
    ) {
        return "Contract content is required to save a template version.";
    }

    return "";
}

export function normalizeContractStatus(status) {
    const normalized = String(status || CONTRACT_STATUS.NEW)
        .trim()
        .toUpperCase()
        .replaceAll(" ", "_");

    const legacyStatusMap = {
        DRAFT: CONTRACT_STATUS.NEW,
        PENDING: CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL,
        REJECTED: CONTRACT_STATUS.CANCELLED,
        COMPLETED: CONTRACT_STATUS.ENDED,
        EXPIRED: CONTRACT_STATUS.ENDED,
    };

    return legacyStatusMap[normalized] || normalized;
}

export function canExportContractPdf(contract) {
    const status = normalizeContractStatus(contract?.contractStatus);

    const completed = Boolean(contract?.pdfAvailable)
<<<<<<< HEAD
        || [
            CONTRACT_STATUS.PENDING_EFFECTIVE,
            CONTRACT_STATUS.ACTIVE,
            CONTRACT_STATUS.ENDED,
        ].includes(status);
=======
        || [CONTRACT_STATUS.SIGNED, CONTRACT_STATUS.ACTIVE, CONTRACT_STATUS.ENDED].includes(status);
>>>>>>> 7d6eb51fe9c660b46d1a1bc0200bcbbc73cf5f51

    return completed && canUseContractProjectAction(
        contract,
        CONTRACT_PROJECT_ACTION.EXPORT
    );
}

export function formatContractStatus(status) {
    const normalizedStatus = normalizeContractStatus(status)
        .toLowerCase()
        .replaceAll("_", " ");

    return normalizedStatus.replace(/\b\w/g, (character) => character.toUpperCase());
}

export function normalizeContractRole(role) {
    const normalized = String(role || "")
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

export function isContractEditable(contract) {
    return normalizeContractStatus(contract?.contractStatus)
        === CONTRACT_STATUS.NEW;
}

export function canManageNewContract(
    contract,
    actionCode = CONTRACT_PROJECT_ACTION.EDIT
) {
    if (!isContractEditable(contract)) {
        return false;
    }

    return canUseContractProjectAction(contract, actionCode);
}

export function hasContractProjectAction(contract, actionCode) {
    const requiredAction = normalizeProjectAction(actionCode);
    const allowedActions = contract?.currentUserAccess?.allowedActions;

    return Array.isArray(allowedActions)
        && allowedActions.some(
            (allowedAction) => normalizeProjectAction(allowedAction)
                === requiredAction
        );
}

export function canUseContractProjectAction(contract, actionCode) {
    if (!hasContractProjectAction(contract, actionCode)) {
        return false;
    }

    const requiredAction = normalizeProjectAction(actionCode);
    const fullScopeActions = contract?.currentUserAccess?.fullScopeActions;
    const fullScope = Array.isArray(fullScopeActions)
        && fullScopeActions.some(
            (fullScopeAction) => normalizeProjectAction(fullScopeAction)
                === requiredAction
        );

    if (fullScope || Boolean(contract?.currentUserAccess?.currentUserOwner)) {
        return true;
    }

    return [
        CONTRACT_PROJECT_ACTION.VIEW,
        CONTRACT_PROJECT_ACTION.EXPORT,
    ].includes(requiredAction)
        && Boolean(
            contract?.currentUserAccess?.currentUserWorkflowParticipant
        );
}

export function canCreateReplacementContract(contract) {
    return normalizeContractStatus(contract?.contractStatus)
        === CONTRACT_STATUS.CANCELLED
        && hasContractProjectAction(
            contract,
            CONTRACT_PROJECT_ACTION.CREATE
        );
}

export function getContractActionDetails(action, contract = null) {
    if (action === CONTRACT_ACTION.COMPLETE_STEP
        && contract?.workflowRuntime) {
        const stepName = contract.workflowRuntime.currentStepName
            || "current workflow step";
        const actionType = String(
            contract.workflowRuntime.currentStepActionType || ""
        ).trim().toUpperCase();
        const actionDetails = {
            CREATE: {
                label: "Submit contract",
                description: `Complete “${stepName}” and send the contract to the next step.`,
            },
            APPROVE: {
                label: "Approve",
                description: `Approve “${stepName}” and continue the workflow.`,
            },
            APPROVE_AND_GENERATE_PDF: {
                label: "CEO approve & generate PDF",
                description: `Approve “${stepName}”, freeze the PDF and continue to the separate signing step.`,
            },
            SIGN: {
                label: "Sign contract",
                description: `Sign at “${stepName}” and continue the workflow.`,
                verifiesAccountDateOfBirth: true,
                requiresElectronicSignature: true,
                requiresDigitalKey: true,
            },
<<<<<<< HEAD
            APPROVE_AND_SIGN: {
                label: "CEO approve & generate PDF",
                description: `Approve “${stepName}”, freeze the PDF and continue to the separate signing step.`,
            },
=======
>>>>>>> 7d6eb51fe9c660b46d1a1bc0200bcbbc73cf5f51
        }[actionType] || {};
        return {
            ...ACTION_DETAILS[action],
            ...actionDetails,
        };
    }
    return ACTION_DETAILS[action] || {
        label: formatContractStatus(action),
        description: "",
        tone: "primary",
    };
}

export function getAvailableContractActions(contract, role) {
    if (contract?.workflowRuntime) {
        return Array.isArray(contract.workflowRuntime.availableActions)
            ? contract.workflowRuntime.availableActions
            : [];
    }
    const status = normalizeContractStatus(contract?.contractStatus);
    const normalizedRole = normalizeContractRole(role);
    const isAdmin = normalizedRole === "ADMIN";

    if (status === CONTRACT_STATUS.SIGNED
        || status === CONTRACT_STATUS.ENDED
        || status === CONTRACT_STATUS.CANCELLED) {
        return [];
    }

    if (status === CONTRACT_STATUS.NEW) {
        if (
            isAdmin
            || ["EMPLOYEE", "MANAGER", "CEO", "DIRECTOR"].includes(normalizedRole)
        ) {
            return [
                canUseContractProjectAction(
                    contract,
                    CONTRACT_PROJECT_ACTION.SUBMIT
                ) && CONTRACT_ACTION.SUBMIT,
                canUseContractProjectAction(
                    contract,
                    CONTRACT_PROJECT_ACTION.CANCEL
                ) && CONTRACT_ACTION.CANCEL,
            ].filter(Boolean);
        }
    }

    if (
        status === CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL
        && (
            isAdmin
            || [
                "MANAGER",
                "HEAD_OF_DEPARTMENT",
                "HEADOFDEPARTMENT",
                "ACCOUNTANT",
            ].includes(normalizedRole)
        )
    ) {
        return canUseContractProjectAction(
            contract,
            CONTRACT_PROJECT_ACTION.APPROVE
        )
            ? [CONTRACT_ACTION.APPROVE_INTERNAL, CONTRACT_ACTION.REJECT]
            : [];
    }

    if (
        status === CONTRACT_STATUS.PENDING_DIRECTOR_APPROVAL
        && (isAdmin || ["CEO", "DIRECTOR"].includes(normalizedRole))
    ) {
        return canUseContractProjectAction(
            contract,
            CONTRACT_PROJECT_ACTION.APPROVE
        )
            ? [CONTRACT_ACTION.APPROVE_DIRECTOR, CONTRACT_ACTION.REJECT]
            : [];
    }

    if (
        status === CONTRACT_STATUS.PENDING_DIRECTOR_SIGNATURE
        && (isAdmin || ["CEO", "DIRECTOR"].includes(normalizedRole))
    ) {
        return canUseContractProjectAction(
            contract,
            CONTRACT_PROJECT_ACTION.SIGN
        )
            ? [CONTRACT_ACTION.SIGN_DIRECTOR, CONTRACT_ACTION.REJECT]
            : [];
    }

    if (
        status === CONTRACT_STATUS.PENDING_PARTNER_SIGNATURE
        && (
            isAdmin
            || ["PARTNER", "EXTERNAL", "EXTERNAL_PARTNER"].includes(normalizedRole)
        )
    ) {
        return canUseContractProjectAction(
            contract,
            CONTRACT_PROJECT_ACTION.SIGN
        )
            ? [CONTRACT_ACTION.SIGN_PARTNER, CONTRACT_ACTION.REJECT]
            : [];
    }

    if (
        [CONTRACT_STATUS.PENDING_EFFECTIVE, CONTRACT_STATUS.ACTIVE].includes(status)
        && (
            isAdmin
            || ["CEO", "DIRECTOR", "PARTNER", "EXTERNAL_PARTNER"].includes(
                normalizedRole
            )
        )
    ) {
        return canUseContractProjectAction(
            contract,
            CONTRACT_PROJECT_ACTION.CANCEL
        )
            ? [CONTRACT_ACTION.CANCEL]
            : [];
    }

    return [];
}

export function getRoleContractTask(contract, role) {
    const status = normalizeContractStatus(contract?.contractStatus);
    const normalizedRole = normalizeContractRole(role);
    const actions = getAvailableContractActions(contract, role);

    if (contract?.workflowRuntime) {
        if (status === CONTRACT_STATUS.CANCELLED) {
            return { label: "Contract cancelled", status: "CANCELLED" };
        }
        if (status === CONTRACT_STATUS.ENDED) {
            return { label: "Contract completed", status: "COMPLETED" };
        }
        if (status === CONTRACT_STATUS.SIGNED) {
            return { label: "Contract signed", status: "COMPLETED" };
        }
        const runtime = contract.workflowRuntime;
        if (!runtime.currentStepId) {
            if (status === CONTRACT_STATUS.PENDING_EFFECTIVE) {
                return {
                    label: "Waiting for effective date",
                    status: "WAITING",
                };
            }
            return status === CONTRACT_STATUS.ACTIVE
                ? { label: "Monitor active contract", status: "IN_PROGRESS" }
                : { label: "Workflow completed", status: "COMPLETED" };
        }
        const assignedToCurrentUser = runtime.steps?.some(
            (step) => step.id === runtime.currentStepId
                && step.currentUserAssigned
        );
        if (assignedToCurrentUser) {
            return {
                label: runtime.currentStepName || "Workflow action required",
                status: actions.length > 0 ? "ACTION_REQUIRED" : "READ_ONLY",
            };
        }
        return {
            label: runtime.currentAssignedUserName
                ? `Waiting for ${runtime.currentAssignedUserName}`
                : `Waiting for ${runtime.currentStepName || "next step"}`,
            status: "WAITING",
        };
    }

    if (status === CONTRACT_STATUS.CANCELLED) {
        return { label: "Contract cancelled", status: "CANCELLED" };
    }

    if (status === CONTRACT_STATUS.ENDED) {
        return { label: "Contract completed", status: "COMPLETED" };
    }
    if (status === CONTRACT_STATUS.PENDING_EFFECTIVE) {
        return { label: "Waiting for effective date", status: "WAITING" };
    }

    if (status === CONTRACT_STATUS.SIGNED) {
        return { label: "Contract signed", status: "COMPLETED" };
    }

    if (actions.length > 0) {
        const actionTaskByStatus = {
            [CONTRACT_STATUS.NEW]: "Prepare and submit",
            [CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL]:
                "Internal review required",
            [CONTRACT_STATUS.PENDING_DIRECTOR_APPROVAL]:
                "CEO approval and PDF generation required",
            [CONTRACT_STATUS.PENDING_DIRECTOR_SIGNATURE]:
                "Director signature required",
            [CONTRACT_STATUS.PENDING_PARTNER_SIGNATURE]:
                "Partner signature required",
            [CONTRACT_STATUS.PENDING_EFFECTIVE]: "Waiting for effective date",
            [CONTRACT_STATUS.ACTIVE]: "Monitor active contract",
        };

        return {
            label: actionTaskByStatus[status] || "Workflow action required",
            status: "ACTION_REQUIRED",
        };
    }

    if (normalizedRole === "ADMIN") {
        return { label: "Monitor contract", status: "IN_PROGRESS" };
    }

    if (normalizedRole === "EMPLOYEE") {
        return status === CONTRACT_STATUS.NEW
            ? {
                label: actions.length > 0 ? "Prepare and submit" : "Read only",
                status: actions.length > 0 ? "ACTION_REQUIRED" : "READ_ONLY",
            }
            : { label: "Creation task completed", status: "COMPLETED" };
    }

    if ([
        "MANAGER",
        "HEAD_OF_DEPARTMENT",
        "HEADOFDEPARTMENT",
        "ACCOUNTANT",
    ].includes(normalizedRole)) {
        if (status === CONTRACT_STATUS.NEW) {
            return { label: "Waiting for submission", status: "WAITING" };
        }
        if (status === CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL) {
            return actions.length > 0
                ? { label: "Internal review required", status: "ACTION_REQUIRED" }
                : { label: "No approval permission", status: "READ_ONLY" };
        }
        return { label: "Internal review completed", status: "COMPLETED" };
    }

    if (["CEO", "DIRECTOR"].includes(normalizedRole)) {
        if (
            [CONTRACT_STATUS.NEW, CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL]
                .includes(status)
        ) {
            return { label: "Waiting for internal approval", status: "WAITING" };
        }
        if (status === CONTRACT_STATUS.PENDING_DIRECTOR_APPROVAL) {
            return actions.length > 0
                ? {
                    label: "Approve and generate PDF",
                    status: "ACTION_REQUIRED",
                }
                : { label: "No approval permission", status: "READ_ONLY" };
        }
        if (status === CONTRACT_STATUS.PENDING_DIRECTOR_SIGNATURE) {
            return actions.length > 0
                ? { label: "Director signature required", status: "ACTION_REQUIRED" }
                : { label: "No signature permission", status: "READ_ONLY" };
        }
        return { label: "Monitor contract lifecycle", status: "IN_PROGRESS" };
    }

    if (["PARTNER", "EXTERNAL", "EXTERNAL_PARTNER"].includes(normalizedRole)) {
        if (
            [
                CONTRACT_STATUS.NEW,
                CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL,
                CONTRACT_STATUS.PENDING_DIRECTOR_APPROVAL,
                CONTRACT_STATUS.PENDING_DIRECTOR_SIGNATURE,
            ].includes(status)
        ) {
            return { label: "Waiting for company signature", status: "WAITING" };
        }
        if (status === CONTRACT_STATUS.PENDING_PARTNER_SIGNATURE) {
            return actions.length > 0
                ? { label: "Partner signature required", status: "ACTION_REQUIRED" }
                : { label: "No signature permission", status: "READ_ONLY" };
        }
        return { label: "Monitor active contract", status: "IN_PROGRESS" };
    }

    return { label: "Read only", status: "READ_ONLY" };
}

export function toTransitionRequest(action, form, contract = null) {
    const actor = getCurrentContractActor();

    return {
        action,
        actorName: actor.actorName,
        actorRole: actor.actorRole,
        comment: form.comment.trim() || null,
        workflowStepId: contract?.workflowRuntime?.currentStepId || null,
        electronicSignatureId: form.electronicSignatureId || null,
        digitalSignature: form.digitalSignature || null,
    };
}

export function formatContractDate(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(`${value}T00:00:00`);

    return Number.isNaN(date.getTime())
        ? value
        : new Intl.DateTimeFormat("vi-VN").format(date);
}

export function formatContractMoney(value) {
    if (value === null || value === undefined || value === "") {
        return "-";
    }

    const amount = Number(value);
    if (!Number.isFinite(amount)) {
        return String(value);
    }

    return `${new Intl.NumberFormat("vi-VN", {
        maximumFractionDigits: 2,
    }).format(amount)} VNĐ`;
}

export function formatContractDateTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    return Number.isNaN(date.getTime())
        ? value
        : new Intl.DateTimeFormat("vi-VN", {
            dateStyle: "medium",
            timeStyle: "short",
        }).format(date);
}

export async function loadProjectOptions() {
    const response = await contractApi.getProjectOptions();
    const payload = unwrapApiResponse(response);

    return Array.isArray(payload) ? payload : [];
}

function normalizeProjectAction(value) {
    return String(value || "").trim().toUpperCase();
}
