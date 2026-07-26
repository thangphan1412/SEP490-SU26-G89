import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import contractApi from "../../services/contractService/contractApi.js";
import {
    ContractStatusBadge,
    Icon,
    PagePanel,
    PrimaryButton,
} from "./ContractComponents.jsx";
import {
    formatContractDate,
    formatContractStatus,
    getApiErrorMessage,
    unwrapApiResponse,
} from "./contractUtils.js";

const PAGE_SIZE = 8;

function createPageNumbers(currentPage, totalPages) {
    if (totalPages <= 5) {
        return Array.from({ length: totalPages }, (_, index) => index);
    }

    const candidates = new Set([
        0,
        totalPages - 1,
        currentPage - 1,
        currentPage,
        currentPage + 1,
    ]);
    const visiblePages = [...candidates]
        .filter((pageNumber) => pageNumber >= 0 && pageNumber < totalPages)
        .sort((first, second) => first - second);
    const pageNumbers = [];

    visiblePages.forEach((pageNumber, index) => {
        if (index > 0 && pageNumber - visiblePages[index - 1] > 1) {
            pageNumbers.push(`ellipsis-${pageNumber}`);
        }

        pageNumbers.push(pageNumber);
    });

    return pageNumbers;
}

function ListContract() {
    const navigate = useNavigate();
    const [contracts, setContracts] = useState([]);
    const [searchInput, setSearchInput] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [availableStatuses, setAvailableStatuses] = useState([]);
    const [page, setPage] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        const debounceId = window.setTimeout(() => {
            setSearch(searchInput.trim());
            setPage(0);
        }, 400);

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

                const items = Array.isArray(payload?.items) ? payload.items : [];
                const statuses = Array.isArray(payload?.availableStatuses)
                    ? payload.availableStatuses.filter(Boolean)
                    : [];
                const pageCount = Number(payload?.totalPages) || 0;

                setContracts(items);
                setAvailableStatuses(statuses);
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

    const clearFilters = () => {
        setSearchInput("");
        setSearch("");
        setStatus("");
        setPage(0);
        setReloadKey((currentKey) => currentKey + 1);
    };

    const pageNumbers = createPageNumbers(page, totalPages);
    const firstResult = totalElements === 0 ? 0 : page * PAGE_SIZE + 1;
    const lastResult = Math.min((page + 1) * PAGE_SIZE, totalElements);

    return (
        <PagePanel
            title="Contracts"
            description="View and manage all your contracts."
            action={
                <PrimaryButton
                    onClick={() => navigate("/contract-management/create")}
                >
                    <Icon name="plus" size={20} color="#ffffff" />
                    Create Contract
                </PrimaryButton>
            }
        >
            <div style={localStyles.toolbar}>
                <label style={localStyles.searchBox}>
                    <Icon name="search" size={23} color="#3f4d6f" />

                    <input
                        aria-label="Search contracts"
                        placeholder="Search by number, title, creator, or project..."
                        value={searchInput}
                        onChange={(event) => setSearchInput(event.target.value)}
                        style={localStyles.searchInput}
                    />
                </label>

                <label style={localStyles.selectBox}>
                    <span style={localStyles.selectLabel}>Status</span>

                    <select
                        value={status}
                        onChange={(event) => {
                            setStatus(event.target.value);
                            setPage(0);
                        }}
                        style={localStyles.select}
                    >
                        <option value="">All statuses</option>

                        {availableStatuses.map((statusOption) => (
                            <option key={statusOption} value={statusOption}>
                                {formatContractStatus(statusOption)}
                            </option>
                        ))}
                    </select>

                    <span style={localStyles.selectIcon}>
                        <Icon name="chevron" size={18} color="#243452" />
                    </span>
                </label>

                <button
                    type="button"
                    style={localStyles.iconButton}
                    onClick={clearFilters}
                    title="Refresh and clear filters"
                >
                    <Icon name="refresh" size={22} color="#243452" />
                </button>
            </div>

            {errorMessage && (
                <div role="alert" style={localStyles.errorAlert}>
                    {errorMessage}
                </div>
            )}

            <div style={localStyles.tableWrap}>
                <table style={localStyles.table}>
                    <thead>
                        <tr>
                            {[
                                "Contract ID",
                                "Title",
                                "Project",
                                "Status",
                                "Effective Date",
                                "Actions",
                            ].map((header) => (
                                <th key={header} style={localStyles.th}>
                                    {header}
                                </th>
                            ))}
                        </tr>
                    </thead>

                    <tbody>
                        {loading ? (
                            <tr>
                                <td colSpan={6} style={localStyles.stateCell}>
                                    Loading contracts...
                                </td>
                            </tr>
                        ) : contracts.length === 0 ? (
                            <tr>
                                <td colSpan={6} style={localStyles.stateCell}>
                                    No contracts found.
                                </td>
                            </tr>
                        ) : (
                            contracts.map((contract) => (
                                <tr key={contract.id} style={localStyles.tr}>
                                    <td style={localStyles.tdStrong}>
                                        {contract.contractNumber || "-"}
                                    </td>
                                    <td style={localStyles.tdStrong}>
                                        {contract.contractTitle || "-"}
                                    </td>
                                    <td style={localStyles.td}>
                                        {contract.projectName || "Not linked"}
                                    </td>
                                    <td style={localStyles.td}>
                                        <ContractStatusBadge
                                            status={formatContractStatus(
                                                contract.contractStatus
                                            )}
                                        />
                                    </td>
                                    <td style={localStyles.td}>
                                        {formatContractDate(contract.effectiveDate)}
                                    </td>
                                    <td style={localStyles.actionCell}>
                                        <button
                                            type="button"
                                            title="View contract"
                                            style={localStyles.actionButton}
                                            onClick={() =>
                                                navigate(
                                                    `/contract-management/view/${contract.id}`
                                                )
                                            }
                                        >
                                            View
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            <div style={localStyles.footer}>
                <span>
                    Showing {firstResult} to {lastResult} of {totalElements} results
                </span>

                <div style={localStyles.pagination}>
                    <button
                        type="button"
                        style={localStyles.pageButton}
                        onClick={() =>
                            setPage((currentPage) => Math.max(0, currentPage - 1))
                        }
                        disabled={page === 0}
                    >
                        <Icon name="arrowLeft" size={18} color="#243452" />
                    </button>

                    {pageNumbers.map((pageNumber) =>
                        typeof pageNumber === "number" ? (
                            <button
                                key={pageNumber}
                                type="button"
                                style={
                                    pageNumber === page
                                        ? localStyles.currentPage
                                        : localStyles.pageButton
                                }
                                onClick={() => setPage(pageNumber)}
                            >
                                {pageNumber + 1}
                            </button>
                        ) : (
                            <span key={pageNumber}>…</span>
                        )
                    )}

                    <button
                        type="button"
                        style={localStyles.pageButton}
                        onClick={() =>
                            setPage((currentPage) =>
                                Math.min(totalPages - 1, currentPage + 1)
                            )
                        }
                        disabled={totalPages === 0 || page >= totalPages - 1}
                    >
                        <Icon name="arrowRight" size={18} color="#243452" />
                    </button>
                </div>
            </div>
        </PagePanel>
    );
}

const localStyles = {
    toolbar: {
        display: "flex",
        flexWrap: "wrap",
        gap: 16,
        alignItems: "center",
        padding: "24px 32px",
    },
    searchBox: {
        flex: "1 1 280px",
        minWidth: 0,
        height: 55,
        border: "1px solid #d7dfeb",
        borderRadius: 7,
        display: "flex",
        alignItems: "center",
        gap: 14,
        padding: "0 16px",
        boxSizing: "border-box",
    },
    searchInput: {
        border: 0,
        outline: "none",
        flex: 1,
        minWidth: 0,
        color: "#243452",
        fontSize: 16,
    },
    selectBox: {
        flex: "0 1 210px",
        minWidth: 170,
        height: 58,
        border: "1px solid #d7dfeb",
        borderRadius: 7,
        padding: "8px 14px",
        position: "relative",
        boxSizing: "border-box",
    },
    selectLabel: {
        display: "block",
        color: "#52617f",
        fontSize: 12,
        marginBottom: 2,
    },
    select: {
        width: "100%",
        border: 0,
        outline: "none",
        appearance: "none",
        background: "transparent",
        color: "#111827",
        fontSize: 15,
    },
    selectIcon: {
        position: "absolute",
        right: 12,
        bottom: 15,
        pointerEvents: "none",
    },
    iconButton: {
        flex: "0 0 54px",
        width: 54,
        height: 55,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#fff",
        cursor: "pointer",
    },
    errorAlert: {
        margin: "0 28px 18px",
        border: "1px solid #fecaca",
        background: "#fef2f2",
        color: "#b91c1c",
        borderRadius: 7,
        padding: "12px 16px",
    },
    tableWrap: {
        margin: "0 28px",
        border: "1px solid #dfe6f1",
        borderRadius: 8,
        overflowX: "auto",
        overflowY: "hidden",
    },
    table: { width: "100%", minWidth: 920, borderCollapse: "collapse" },
    th: {
        height: 66,
        background: "#fbfcff",
        borderBottom: "1px solid #e4eaf3",
        color: "#243452",
        fontSize: 14,
        fontWeight: 700,
        textAlign: "left",
        padding: "0 18px",
    },
    tr: { borderBottom: "1px solid #e8edf4", height: 62 },
    td: {
        padding: "0 18px",
        color: "#334260",
        fontSize: 14,
        whiteSpace: "nowrap",
    },
    tdStrong: {
        padding: "0 18px",
        color: "#18243a",
        fontSize: 14,
        fontWeight: 700,
        whiteSpace: "nowrap",
    },
    stateCell: {
        height: 120,
        textAlign: "center",
        color: "#52617f",
        fontSize: 14,
    },
    actionCell: { padding: "0 18px" },
    actionButton: {
        minWidth: 58,
        height: 36,
        borderRadius: 7,
        border: "1px solid #c7d2fe",
        background: "#eef2ff",
        cursor: "pointer",
        color: "#3730a3",
        fontWeight: 700,
    },
    footer: {
        minHeight: 82,
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: 16,
        flexWrap: "wrap",
        padding: "18px 42px",
        color: "#52617f",
        fontSize: 14,
        boxSizing: "border-box",
    },
    pagination: { display: "flex", alignItems: "center", gap: 10 },
    pageButton: {
        minWidth: 38,
        height: 38,
        borderRadius: 7,
        border: "1px solid #d7dfeb",
        background: "#fff",
        cursor: "pointer",
    },
    currentPage: {
        minWidth: 38,
        height: 38,
        borderRadius: 7,
        border: "1px solid #1f4fff",
        background: "#1f4fff",
        color: "#fff",
        fontWeight: 800,
    },
};

export default ListContract;
