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
    END: "END",
    CANCEL: "CANCEL",
    REJECT: "REJECT",
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
        description: "Verify the director's age and confirm the director signature.",
        tone: "primary",
        requiresSignerDateOfBirth: true,
    },
    [CONTRACT_ACTION.SIGN_PARTNER]: {
        label: "Partner sign",
        description: "Verify the partner's age and activate the signed contract.",
        tone: "primary",
        requiresSignerDateOfBirth: true,
    },
    [CONTRACT_ACTION.END]: {
        label: "End contract",
        description: "Close an active contract as completed or expired.",
        tone: "success",
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

export function canManageNewContract(contract, role, actorName) {
    if (!isContractEditable(contract)) {
        return false;
    }

    const normalizedRole = normalizeContractRole(role);
    if (normalizedRole === "ADMIN") {
        return true;
    }

    return Boolean(actorName)
        && Boolean(contract?.contractCreatedBy)
        && actorName.trim().toLowerCase()
            === contract.contractCreatedBy.trim().toLowerCase();
}

export function getContractActionDetails(action) {
    return ACTION_DETAILS[action] || {
        label: formatContractStatus(action),
        description: "",
        tone: "primary",
    };
}

export function getAvailableContractActions(contract, role, actorName) {
    const status = normalizeContractStatus(contract?.contractStatus);
    const normalizedRole = normalizeContractRole(role);
    const isAdmin = normalizedRole === "ADMIN";
    const canManageNew = canManageNewContract(contract, role, actorName);

    if (status === CONTRACT_STATUS.ENDED
        || status === CONTRACT_STATUS.CANCELLED) {
        return [];
    }

    if (status === CONTRACT_STATUS.NEW) {
        if (!canManageNew) {
            return [];
        }

        if (
            isAdmin
            || ["EMPLOYEE", "MANAGER", "CEO", "DIRECTOR"].includes(normalizedRole)
        ) {
            return [CONTRACT_ACTION.SUBMIT, CONTRACT_ACTION.CANCEL];
        }
    }

    if (
        status === CONTRACT_STATUS.PENDING_INTERNAL_APPROVAL
        && (isAdmin || normalizedRole === "MANAGER")
    ) {
        return [CONTRACT_ACTION.APPROVE_INTERNAL, CONTRACT_ACTION.REJECT];
    }

    if (
        status === CONTRACT_STATUS.PENDING_DIRECTOR_SIGNATURE
        && (isAdmin || ["CEO", "DIRECTOR"].includes(normalizedRole))
    ) {
        return [CONTRACT_ACTION.SIGN_DIRECTOR, CONTRACT_ACTION.REJECT];
    }

    if (
        status === CONTRACT_STATUS.PENDING_PARTNER_SIGNATURE
        && (
            isAdmin
            || ["PARTNER", "EXTERNAL", "EXTERNAL_PARTNER"].includes(normalizedRole)
        )
    ) {
        return [CONTRACT_ACTION.SIGN_PARTNER, CONTRACT_ACTION.REJECT];
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
        return [CONTRACT_ACTION.END, CONTRACT_ACTION.CANCEL];
    }

    return [];
}

export function getRoleContractTask(contract, role, actorName) {
    const status = normalizeContractStatus(contract?.contractStatus);
    const normalizedRole = normalizeContractRole(role);
    const actions = getAvailableContractActions(contract, role, actorName);

    if (status === CONTRACT_STATUS.CANCELLED) {
        return { label: "Contract cancelled", status: "CANCELLED" };
    }

    if (status === CONTRACT_STATUS.ENDED) {
        return { label: "Contract completed", status: "COMPLETED" };
    }

    if (normalizedRole === "ADMIN") {
        return actions.length > 0
            ? { label: "Workflow action required", status: "ACTION_REQUIRED" }
            : { label: "Monitor contract", status: "IN_PROGRESS" };
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
            return { label: "Internal review required", status: "ACTION_REQUIRED" };
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
            return { label: "Director signature required", status: "ACTION_REQUIRED" };
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
            return { label: "Partner signature required", status: "ACTION_REQUIRED" };
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
        signerDateOfBirth: form.signerDateOfBirth || null,
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
