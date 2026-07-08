import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    ContractStatusBadge,
    Icon,
    PagePanel,
    PrimaryButton,
    contractParties,
    contractStatuses,
    contracts,
} from "./ContractComponents.jsx";

function ListContract() {
    const navigate = useNavigate();
    const [searchTerm, setSearchTerm] = useState("");
    const [statusFilter, setStatusFilter] = useState("All");
    const [partyFilter, setPartyFilter] = useState("All");

    const filteredContracts = useMemo(() => {
        const keyword = searchTerm.trim().toLowerCase();

        return contracts.filter((contract) => {
            const matchesKeyword =
                contract.contractNumber.toLowerCase().includes(keyword) ||
                contract.title.toLowerCase().includes(keyword) ||
                contract.party.toLowerCase().includes(keyword);
            const matchesStatus = statusFilter === "All" || contract.status === statusFilter;
            const matchesParty = partyFilter === "All" || contract.party === partyFilter;

            return matchesKeyword && matchesStatus && matchesParty;
        });
    }, [searchTerm, statusFilter, partyFilter]);

    const resetFilters = () => {
        setSearchTerm("");
        setStatusFilter("All");
        setPartyFilter("All");
    };

    return (
        <PagePanel
            title="Contracts"
            description="View and manage all your contracts."
            action={
                <PrimaryButton onClick={() => navigate("/contract-management/create")}>
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
                        placeholder="Search contracts..."
                        value={searchTerm}
                        onChange={(event) => setSearchTerm(event.target.value)}
                        style={localStyles.searchInput}
                    />
                </label>

                <label style={localStyles.selectBox}>
                    <span style={localStyles.selectLabel}>Status</span>
                    <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)} style={localStyles.select}>
                        <option>All</option>
                        {contractStatuses.map((status) => <option key={status}>{status}</option>)}
                    </select>
                    <span style={localStyles.selectIcon}><Icon name="chevron" size={18} color="#243452" /></span>
                </label>

                <label style={localStyles.selectBox}>
                    <span style={localStyles.selectLabel}>All Parties</span>
                    <select value={partyFilter} onChange={(event) => setPartyFilter(event.target.value)} style={localStyles.select}>
                        <option>All</option>
                        {contractParties.map((party) => <option key={party}>{party}</option>)}
                    </select>
                    <span style={localStyles.selectIcon}><Icon name="chevron" size={18} color="#243452" /></span>
                </label>

                <button type="button" style={localStyles.filterButton}>
                    <Icon name="filter" size={20} color="#243452" />
                    Filters
                </button>
                <button type="button" style={localStyles.iconButton} onClick={resetFilters}>
                    <Icon name="refresh" size={22} color="#243452" />
                </button>
            </div>

            <div style={localStyles.tableWrap}>
                <table style={localStyles.table}>
                    <thead>
                        <tr>
                            {["Contract ID", "Title", "Party / Parties", "Status", "Effective Date", "Actions"].map((header) => (
                                <th key={header} style={localStyles.th}>
                                    <span style={localStyles.thContent}>
                                        {header}
                                        {header !== "Actions" && <Icon name="sort" size={13} color="#243452" />}
                                    </span>
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {filteredContracts.map((contract) => (
                            <tr key={contract.id} style={localStyles.tr}>
                                <td style={localStyles.tdStrong}>{contract.contractNumber}</td>
                                <td style={localStyles.tdStrong}>{contract.title}</td>
                                <td style={localStyles.td}>{contract.party}</td>
                                <td style={localStyles.td}><ContractStatusBadge status={contract.status} /></td>
                                <td style={localStyles.td}>{contract.effectiveDate}</td>
                                <td style={localStyles.actionCell}>
                                    <button
                                        type="button"
                                        title="View contract"
                                        style={localStyles.actionButton}
                                        onClick={() => navigate(`/contract-management/view/${contract.id}`)}
                                    >
                                        <Icon name="dots" size={20} color="#111827" />
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div style={localStyles.footer}>
                <span>Showing {filteredContracts.length ? 1 : 0} to {filteredContracts.length} of {filteredContracts.length} results</span>
                <div style={localStyles.pagination}>
                    <button type="button" style={localStyles.pageButton}><Icon name="arrowLeft" size={18} color="#243452" /></button>
                    <button type="button" style={localStyles.currentPage}>1</button>
                    <button type="button" style={localStyles.pageButton}>2</button>
                    <button type="button" style={localStyles.pageButton}>3</button>
                    <button type="button" style={localStyles.pageButton}><Icon name="arrowRight" size={18} color="#243452" /></button>
                </div>
            </div>
        </PagePanel>
    );
}

const localStyles = {
    toolbar: { display: "flex", flexWrap: "wrap", gap: 16, alignItems: "center", padding: "24px 32px" },
    searchBox: { flex: "1 1 280px", minWidth: 0, height: 55, border: "1px solid #d7dfeb", borderRadius: 7, display: "flex", alignItems: "center", gap: 14, padding: "0 16px", boxSizing: "border-box" },
    searchInput: { border: 0, outline: "none", flex: 1, minWidth: 0, color: "#243452", fontSize: 16 },
    selectBox: { flex: "0 1 180px", minWidth: 150, height: 58, border: "1px solid #d7dfeb", borderRadius: 7, padding: "8px 14px", position: "relative", boxSizing: "border-box" },
    selectLabel: { display: "block", color: "#52617f", fontSize: 12, marginBottom: 2 },
    select: { width: "100%", border: 0, outline: "none", appearance: "none", background: "transparent", color: "#111827", fontSize: 15 },
    selectIcon: { position: "absolute", right: 12, bottom: 15, pointerEvents: "none" },
    filterButton: { flex: "0 0 auto", height: 55, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 10, color: "#243452", fontSize: 16, cursor: "pointer", padding: "0 18px" },
    iconButton: { flex: "0 0 54px", width: 54, height: 55, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", cursor: "pointer" },
    tableWrap: { margin: "0 28px", border: "1px solid #dfe6f1", borderRadius: 8, overflowX: "auto", overflowY: "hidden" },
    table: { width: "100%", minWidth: 920, borderCollapse: "collapse" },
    th: { height: 66, background: "#fbfcff", borderBottom: "1px solid #e4eaf3", color: "#243452", fontSize: 14, fontWeight: 700, textAlign: "left", padding: "0 18px" },
    thContent: { display: "inline-flex", alignItems: "center", gap: 6 },
    tr: { borderBottom: "1px solid #e8edf4", height: 62 },
    td: { padding: "0 18px", color: "#334260", fontSize: 14, whiteSpace: "nowrap" },
    tdStrong: { padding: "0 18px", color: "#18243a", fontSize: 14, fontWeight: 700, whiteSpace: "nowrap" },
    actionCell: { textAlign: "center" },
    actionButton: { width: 38, height: 38, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", cursor: "pointer", color: "#111827" },
    footer: { minHeight: 82, display: "flex", alignItems: "center", justifyContent: "space-between", gap: 16, flexWrap: "wrap", padding: "18px 42px", color: "#52617f", fontSize: 14, boxSizing: "border-box" },
    pagination: { display: "flex", alignItems: "center", gap: 10 },
    pageButton: { width: 38, height: 38, borderRadius: 7, border: "1px solid #d7dfeb", background: "#fff", cursor: "pointer" },
    currentPage: { width: 38, height: 38, borderRadius: 7, border: "1px solid #1f4fff", background: "#1f4fff", color: "#fff", fontWeight: 800 },
};

export default ListContract;
