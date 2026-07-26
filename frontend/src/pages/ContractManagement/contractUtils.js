import { listProjects } from "../../services/projectService/projectApi.js";

export const defaultContractStatuses = [
    "Draft",
    "Pending",
    "Active",
    "Expired",
];

export function unwrapApiResponse(response) {
    return response?.data?.data ?? response?.data;
}

export function getApiErrorMessage(error, fallbackMessage) {
    return error?.response?.data?.message || error?.message || fallbackMessage;
}

export function createEmptyContract(projectId = "") {
    return {
        projectId,
        contractNumber: "",
        contractTitle: "",
        contractStatus: "Draft",
        effectiveDate: "",
        expirationDate: "",
        contractCreatedBy: localStorage.getItem("fullName") || "",
        contractCreatedAt: null,
    };
}

export function mapContractToForm(contract) {
    return {
        projectId: contract?.projectId || "",
        contractNumber: contract?.contractNumber || "",
        contractTitle: contract?.contractTitle || "",
        contractStatus: contract?.contractStatus || "Draft",
        effectiveDate: contract?.effectiveDate || "",
        expirationDate: contract?.expirationDate || "",
        contractCreatedBy: contract?.contractCreatedBy || "",
        contractCreatedAt: contract?.contractCreatedAt || null,
    };
}

export function toContractRequest(contract, isCreating = false) {
    return {
        projectId: contract.projectId || null,
        contractNumber: contract.contractNumber.trim(),
        contractTitle: contract.contractTitle.trim(),
        contractStatus: contract.contractStatus,
        effectiveDate: contract.effectiveDate || null,
        expirationDate: contract.expirationDate || null,
        contractCreatedBy: contract.contractCreatedBy.trim() || null,
        contractCreatedAt: isCreating
            ? new Date().toISOString().slice(0, 19)
            : contract.contractCreatedAt,
    };
}

export function validateContract(contract) {
    if (!contract.projectId) {
        return "Please select a project.";
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
    const requestParams = {
        search: "",
        status: "",
        page: 0,
        sortBy: "projectName",
        sortDirection: "asc",
    };
    const firstResponse = await listProjects(requestParams);
    const firstPayload = unwrapApiResponse(firstResponse);
    const firstItems = Array.isArray(firstPayload?.items) ? firstPayload.items : [];
    const totalPages = Number(firstPayload?.totalPages) || 0;

    if (totalPages <= 1) {
        return firstItems;
    }

    const remainingResponses = await Promise.all(
        Array.from({ length: totalPages - 1 }, (_, index) =>
            listProjects({ ...requestParams, page: index + 1 })
        )
    );

    return remainingResponses.reduce((projects, response) => {
        const payload = unwrapApiResponse(response);
        const items = Array.isArray(payload?.items) ? payload.items : [];
        return projects.concat(items);
    }, firstItems);
}
