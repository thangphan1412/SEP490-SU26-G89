/* eslint-disable react-refresh/only-export-components */
import {
    CancelButton,
    Icon,
    InfoAlert,
    PagePanel,
    PrimaryButton,
    StatusBadge,
} from "../ProjectManagement/ProjectComponents.jsx";

export {
    CancelButton,
    Icon,
    InfoAlert,
    PagePanel,
    PrimaryButton,
};

export const contractStatuses = ["Active", "Pending", "Expired", "Draft"];

export const contractParties = [
    "Acme Corporation",
    "Globex Corporation",
    "Initech",
    "Soylent Corp",
    "Umbrella Corp",
    "Wayne Enterprises",
    "Stark Industries",
    "Hooli",
];

export const contractTypes = [
    "NDA",
    "MSA",
    "SOW",
    "Purchase Agreement",
    "Service Agreement",
    "License Agreement",
    "Partnership Agreement",
    "Data Processing Addendum",
];

export const contracts = [
    {
        id: 1,
        contractNumber: "CON-2025-0001",
        title: "NDA Agreement",
        party: "Acme Corporation",
        type: "NDA",
        status: "Active",
        effectiveDate: "May 01, 2025",
        expirationDate: "May 01, 2026",
        owner: "Alex Morgan",
        project: "Digital Contract Rollout",
        value: "$24,000",
        description: "Non-disclosure agreement for confidential project discussions.",
    },
    {
        id: 2,
        contractNumber: "CON-2025-0002",
        title: "MSA",
        party: "Globex Corporation",
        type: "MSA",
        status: "Pending",
        effectiveDate: "Apr 28, 2025",
        expirationDate: "Apr 28, 2026",
        owner: "Jamie Lee",
        project: "Vendor Summit 2025",
        value: "$86,000",
        description: "Master service agreement for long-term vendor services.",
    },
    {
        id: 3,
        contractNumber: "CON-2025-0003",
        title: "SOW - Phase 1",
        party: "Initech",
        type: "SOW",
        status: "Active",
        effectiveDate: "Apr 30, 2025",
        expirationDate: "Sep 30, 2025",
        owner: "Taylor Smith",
        project: "Contract Analytics Initiative",
        value: "$42,500",
        description: "Statement of work for phase 1 implementation deliverables.",
    },
    {
        id: 4,
        contractNumber: "CON-2025-0004",
        title: "Purchase Agreement",
        party: "Soylent Corp",
        type: "Purchase Agreement",
        status: "Expired",
        effectiveDate: "Jan 15, 2025",
        expirationDate: "Jun 15, 2025",
        owner: "Casey Brown",
        project: "Supplier Onboarding",
        value: "$18,700",
        description: "Purchase terms for contract management equipment.",
    },
    {
        id: 5,
        contractNumber: "CON-2025-0005",
        title: "Service Agreement",
        party: "Umbrella Corp",
        type: "Service Agreement",
        status: "Draft",
        effectiveDate: "-",
        expirationDate: "-",
        owner: "Morgan Lee",
        project: "Compliance Audit 2025",
        value: "$12,000",
        description: "Draft service agreement waiting for internal review.",
    },
    {
        id: 6,
        contractNumber: "CON-2025-0006",
        title: "License Agreement",
        party: "Wayne Enterprises",
        type: "License Agreement",
        status: "Active",
        effectiveDate: "Mar 10, 2025",
        expirationDate: "Mar 10, 2027",
        owner: "Jordan Kim",
        project: "Digital Contract Rollout",
        value: "$96,000",
        description: "Software license agreement for enterprise users.",
    },
    {
        id: 7,
        contractNumber: "CON-2025-0007",
        title: "Partnership Agreement",
        party: "Stark Industries",
        type: "Partnership Agreement",
        status: "Pending",
        effectiveDate: "May 10, 2025",
        expirationDate: "May 10, 2026",
        owner: "Riley Johnson",
        project: "Vendor Summit 2025",
        value: "$64,000",
        description: "Partnership agreement pending final signature.",
    },
    {
        id: 8,
        contractNumber: "CON-2025-0008",
        title: "Data Processing Addendum",
        party: "Hooli",
        type: "Data Processing Addendum",
        status: "Active",
        effectiveDate: "Apr 05, 2025",
        expirationDate: "Apr 05, 2026",
        owner: "Alex Morgan",
        project: "Compliance Audit 2025",
        value: "$8,500",
        description: "Data protection addendum for compliance requirements.",
    },
];

export function ContractStatusBadge({ status }) {
    const styleByStatus = {
        Expired: { background: "#fff1f2", border: "1px solid #fecdd3", color: "#e11d48" },
    };

    return status === "Active" || status === "Draft" ? (
        <StatusBadge status={status} />
    ) : (
        <span style={{ ...styleByStatus[status] }}>{status}</span>
    );
}

export function getContractById(id) {
    return contracts.find((contract) => String(contract.id) === String(id)) || contracts[0];
}
