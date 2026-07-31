import contractApi from "../../services/contractService/contractApi.js";

export const defaultContractStatuses = [
    "Draft",
    "Pending",
    "Active",
    "Rejected",
    "Completed",
    "Expired",
];

export function unwrapApiResponse(response) {
    return response?.data?.data ?? response?.data ?? response;
}

export function getApiErrorMessage(error, fallbackMessage) {
    return error?.response?.data?.message || error?.message || fallbackMessage;
}

export function createEmptyContract(projectId = "") {
    return {
        projectId,
        contractTypeId: "",
        contractTemplateId: "",
        contractTemplateVersionId: "",
        contractNumber: "",
        contractTitle: "",
        contractStatus: "Draft",
        effectiveDate: "",
        expirationDate: "",
        contractCreatedBy: localStorage.getItem("fullName") || "",
        contractCreatedAt: null,
        contractContent: "",
        contractLayoutJson: "",
        saveAsTemplateVersion: false,
        templateVersionName: "",
        templateVersionNote: "",
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
        contractStatus: contract?.contractStatus || "Draft",
        effectiveDate: contract?.effectiveDate || "",
        expirationDate: contract?.expirationDate || "",
        contractCreatedBy: contract?.contractCreatedBy || "",
        contractCreatedAt: contract?.contractCreatedAt || null,
        contractContent: contract?.contractContent || "",
        contractLayoutJson: contract?.contractLayoutJson || "",
        saveAsTemplateVersion: false,
        templateVersionName: "",
        templateVersionNote: "",
    };
}

export function toContractRequest(contract, isCreating = false) {
    return {
        projectId: contract.projectId || null,
        contractTypeId: contract.contractTypeId || null,
        contractTemplateId: contract.contractTemplateId || null,
        contractTemplateVersionId:
            contract.contractTemplateVersionId || null,
        contractNumber: contract.contractNumber.trim(),
        contractTitle: contract.contractTitle.trim(),
        contractStatus: contract.contractStatus,
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
    };
}

export function validateContract(contract) {
    if (!contract.projectId) {
        return "Please select a project.";
    }

    if (!contract.contractTypeId) {
        return "Please select a contract type.";
    }

    if (!contract.contractNumber.trim() || !contract.contractTitle.trim()) {
        return "Contract ID and title are required.";
    }

    if (
        contract.effectiveDate &&
        contract.expirationDate &&
        contract.effectiveDate > contract.expirationDate
    ) {
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

export function formatContractStatus(status) {
    const normalizedStatus = String(status || "Unknown")
        .trim()
        .toLowerCase()
        .replaceAll("_", " ");

    return normalizedStatus.replace(/\b\w/g, (character) => character.toUpperCase());
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
