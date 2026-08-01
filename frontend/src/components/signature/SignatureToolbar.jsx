import { IconSearch, IconFilter, IconRefresh, IconPlus } from "@tabler/icons-react"

function SignatureToolbar({ searchTerm, onSearchChange, typeFilter, onTypeChange, statusFilter, onStatusChange, onRefresh, onCreateNew }) {
    return (
        <div className="signature-toolbar">
            <div className="search-box">
                <IconSearch size={18} className="search-icon" />
                <input
                    type="text"
                    placeholder="Search signatures..."
                    value={searchTerm}
                    onChange={(e) => onSearchChange(e.target.value)}
                />
            </div>

            <select className="filter-select" value={typeFilter} onChange={(e) => onTypeChange(e.target.value)}>
                <option value="All">Type: All</option>
                <option value="Drawn">Drawn</option>
                <option value="Uploaded">Uploaded</option>
                <option value="Typed">Typed</option>
            </select>

            <select className="filter-select" value={statusFilter} onChange={(e) => onStatusChange(e.target.value)}>
                <option value="All">Status: All</option>
                <option value="Active">Active</option>
                <option value="Inactive">Inactive</option>
            </select>

            <button className="filters-btn">
                <IconFilter size={16} /> Filters
            </button>

            <button className="refresh-btn" onClick={onRefresh}>
                <IconRefresh size={18} />
            </button>

            <button className="new-signature-btn" onClick={onCreateNew}>
                <IconPlus size={18} /> New Signature
            </button>
        </div>
    )
}

export default SignatureToolbar