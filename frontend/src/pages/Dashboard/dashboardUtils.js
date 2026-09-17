export const DEFAULT_CONTRACT_STATUSES = Object.freeze([
    "NEW",
    "PENDING_APPROVAL",
    "PENDING_INTERNAL_APPROVAL",
    "PENDING_SIGNATURE",
    "PENDING_DIRECTOR_SIGNATURE",
    "PENDING_PARTNER_SIGNATURE",
    "PENDING_EFFECTIVE",
    "SIGNED",
    "ACTIVE",
    "ENDED",
    "CANCELLED",
]);

const STATUS_DETAILS = Object.freeze({
    NEW: { label: "New", color: "#52617e", background: "#eff2f6" },
    PENDING_APPROVAL: {
        label: "Pending Approval",
        color: "#b45309",
        background: "#fff7e6",
    },
    PENDING_INTERNAL_APPROVAL: {
        label: "Pending Internal Approval",
        color: "#b45309",
        background: "#fff7e6",
    },
    PENDING_SIGNATURE: {
        label: "Pending Signature",
        color: "#d97706",
        background: "#fff4e8",
    },
    PENDING_DIRECTOR_SIGNATURE: {
        label: "Pending Director Signature",
        color: "#d97706",
        background: "#fff4e8",
    },
    PENDING_PARTNER_SIGNATURE: {
        label: "Pending Partner Signature",
        color: "#d97706",
        background: "#fff4e8",
    },
    PENDING_EFFECTIVE: {
        label: "Pending Effective Date",
        color: "#6d28d9",
        background: "#f3efff",
    },
    SIGNED: { label: "Signed", color: "#0369a1", background: "#e8f7ff" },
    ACTIVE: { label: "Active", color: "#087f4a", background: "#e9f8ef" },
    ENDED: { label: "Ended", color: "#d92d3d", background: "#ffedef" },
    CANCELLED: {
        label: "Cancelled",
        color: "#64748b",
        background: "#f1f5f9",
    },
    UNKNOWN: { label: "Unknown", color: "#475569", background: "#f1f5f9" },
});

const LEGACY_STATUS_MAP = Object.freeze({
    DRAFT: "NEW",
    PENDING: "PENDING_INTERNAL_APPROVAL",
    REJECTED: "CANCELLED",
    CANCELED: "CANCELLED",
    COMPLETED: "ENDED",
    EXPIRED: "ENDED",
});

export function normalizeDashboardStatus(status) {
    const normalized = String(status || "UNKNOWN")
        .trim()
        .toUpperCase()
        .replaceAll(" ", "_");

    return LEGACY_STATUS_MAP[normalized] || normalized;
}

export function formatDashboardStatus(status) {
    const normalized = normalizeDashboardStatus(status);
    const knownStatus = STATUS_DETAILS[normalized];
    if (knownStatus) {
        return knownStatus.label;
    }

    return normalized
        .toLowerCase()
        .replaceAll("_", " ")
        .replace(/\b\w/g, (character) => character.toUpperCase());
}

export function getDashboardStatusStyle(status) {
    const normalized = normalizeDashboardStatus(status);
    return STATUS_DETAILS[normalized] || STATUS_DETAILS.UNKNOWN;
}

export function buildDonutGradient(data, total) {
    const safeTotal = Number(total) || 0;
    const segments = (data || []).filter((item) => Number(item?.value) > 0);

    if (safeTotal <= 0 || segments.length === 0) {
        return "conic-gradient(#e7ebf3 100%)";
    }

    let start = 0;
    const gradientSegments = segments.map((item) => {
        const end = Math.min(100, start + (Number(item.value) / safeTotal) * 100);
        const segment = `${item.color || "#4d5c74"} ${start.toFixed(2)}% ${end.toFixed(2)}%`;
        start = end;
        return segment;
    });

    return `conic-gradient(${gradientSegments.join(", ")})`;
}

export function unwrapDashboardResponse(response, fallback) {
    return response?.data?.data ?? fallback;
}

export function formatAverageDays(value) {
    const days = Number(value);
    return Number.isFinite(days) ? days.toFixed(1) : "0.0";
}
