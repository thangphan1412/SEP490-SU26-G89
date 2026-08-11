import contractApi from "../../services/contractService/contractApi.js";

export const CONTRACT_STATUS = Object.freeze({
    NEW: "NEW",
    PENDING_INTERNAL_APPROVAL: "PENDING_INTERNAL_APPROVAL",
    PENDING_DIRECTOR_SIGNATURE: "PENDING_DIRECTOR_SIGNATURE",
    PENDING_PARTNER_SIGNATURE: "PENDING_PARTNER_SIGNATURE",
    ACTIVE: "ACTIVE",
    ENDED: "ENDED",
    CANCELLED: "CANCELLED",
});

export const defaultContractStatuses = Object.values(CONTRACT_STATUS);

export const CONTRACT_ACTION = Object.freeze({
    SUBMIT: "SUBMIT",
    APPROVE_INTERNAL: "APPROVE_INTERNAL",
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

const ACTION_DETAILS = Object.freeze({
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
    [CONTRACT_ACTION.SIGN_DIRECTOR]: {
        label: "Director sign",
        description: "Verify the director's account age and confirm the director signature.",
        tone: "primary",
        verifiesAccountDateOfBirth: true,
    },
    [CONTRACT_ACTION.SIGN_PARTNER]: {
        label: "Partner sign",
        description: "Verify the partner's account age and activate the signed contract.",
        tone: "primary",
        verifiesAccountDateOfBirth: true,
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
    };
}

export function mapContractToForm(contract) {
    return {
        projectId: contract?.projectId || "",
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
    };
}

export function toContractRequest(contract, isCreating = false) {
    const actor = getCurrentContractActor();

    return {
        projectId: contract.projectId || null,
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
    };
}

export function validateContract(contract) {
    const actor = getCurrentContractActor();

    if (!actor.actorName || !actor.actorRole) {
        return "Your signed-in name and role are required to manage contracts.";
    }

    if (!contract.projectId) {
        return "Please select a project.";
    }

    if (!contract.contractTypeId) {
        return "Please select a contract type.";
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
        || [CONTRACT_STATUS.ACTIVE, CONTRACT_STATUS.ENDED].includes(status);

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
    return String(role || "")
        .trim()
        .toUpperCase()
        .replaceAll("-", "_")
        .replaceAll(" ", "_");
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

    return fullScope || Boolean(contract?.currentUserAccess?.currentUserOwner);
}

export function canCreateReplacementContract(contract) {
    return normalizeContractStatus(contract?.contractStatus)
        === CONTRACT_STATUS.CANCELLED
        && hasContractProjectAction(
            contract,
            CONTRACT_PROJECT_ACTION.CREATE
        );
}

export function getContractActionDetails(action) {
    return ACTION_DETAILS[action] || {
        label: formatContractStatus(action),
        description: "",
        tone: "primary",
    };
}

export function getAvailableContractActions(contract, role) {
    const status = normalizeContractStatus(contract?.contractStatus);
    const normalizedRole = normalizeContractRole(role);
    const isAdmin = normalizedRole === "ADMIN";

    if (status === CONTRACT_STATUS.ENDED
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
        && (isAdmin || normalizedRole === "MANAGER")
    ) {
        return canUseContractProjectAction(
            contract,
            CONTRACT_PROJECT_ACTION.APPROVE
        )
            ? [CONTRACT_ACTION.APPROVE_INTERNAL, CONTRACT_ACTION.REJECT]
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
        status === CONTRACT_STATUS.ACTIVE
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

    if (status === CONTRACT_STATUS.CANCELLED) {
        return { label: "Contract cancelled", status: "CANCELLED" };
    }

    if (status === CONTRACT_STATUS.ENDED) {
        return { label: "Contract completed", status: "COMPLETED" };
    }

    if (actions.length > 0) {
        const actionTaskByStatus = {
            [CONTRACT_STATUS.NEW]: "Prepare and submit",
            [CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL]:
                "Internal review required",
            [CONTRACT_STATUS.PENDING_DIRECTOR_SIGNATURE]:
                "Director signature required",
            [CONTRACT_STATUS.PENDING_PARTNER_SIGNATURE]:
                "Partner signature required",
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

    if (normalizedRole === "MANAGER") {
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

export function toTransitionRequest(action, form) {
    const actor = getCurrentContractActor();

    return {
        action,
        actorName: actor.actorName,
        actorRole: actor.actorRole,
        comment: form.comment.trim() || null,
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
