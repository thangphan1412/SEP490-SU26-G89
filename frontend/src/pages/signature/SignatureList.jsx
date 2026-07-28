import { useState, useMemo } from "react"

import "../../assets/styles/css/signatureStyles/SignaturePage.css"
import SignatureToolbar from "../../components/signature/SignatureToolbar.jsx";
import SignatureTable from "../../components/signature/SignatureTable.jsx";
import SignaturePagination from "../../components/signature/SignaturePagination.jsx";

// TODO: thay bằng data thật từ API
const mockSignatures = [
    { id: 1, name: "Default Work Signature", type: "Drawn", usedIn: "Contracts", status: "Active", updatedAt: "May 22, 2025", avatarText: "❦", avatarColor: "#2563eb" },
    { id: 2, name: "Formal Approval", type: "Uploaded", usedIn: "Approvals", status: "Active", updatedAt: "May 21, 2025", avatarText: "Am", avatarColor: "#374151" },
    { id: 3, name: "Internal Memo Sign", type: "Typed", usedIn: "Memos", status: "Active", updatedAt: "May 20, 2025", avatarText: "Am", avatarColor: "#374151" },
    { id: 4, name: "Short Initials", type: "Drawn", usedIn: "Quick Sign", status: "Inactive", updatedAt: "May 18, 2025", avatarText: "AM", avatarColor: "#2563eb" },
    { id: 5, name: "HR Document Signature", type: "Uploaded", usedIn: "HR Forms", status: "Active", updatedAt: "May 17, 2025", avatarText: "Am", avatarColor: "#374151" },
    { id: 6, name: "Procurement Approval", type: "Drawn", usedIn: "Purchase Requests", status: "Active", updatedAt: "May 15, 2025", avatarText: "❦", avatarColor: "#2563eb" },
]

function SignatureList() {
    const [searchTerm, setSearchTerm] = useState("")
    const [typeFilter, setTypeFilter] = useState("All")
    const [statusFilter, setStatusFilter] = useState("All")
    const [currentPage, setCurrentPage] = useState(1)
    const [pageSize, setPageSize] = useState(10)

    const filteredSignatures = useMemo(() => {
        return mockSignatures.filter((sig) => {
            const matchesSearch = sig.name.toLowerCase().includes(searchTerm.toLowerCase())
            const matchesType = typeFilter === "All" || sig.type === typeFilter
            const matchesStatus = statusFilter === "All" || sig.status === statusFilter
            return matchesSearch && matchesType && matchesStatus
        })
    }, [searchTerm, typeFilter, statusFilter])

    const totalPages = Math.max(1, Math.ceil(filteredSignatures.length / pageSize))

    const handleCreateNew = () => {
        // TODO: điều hướng tới trang tạo signature hoặc mở modal
    }

    const handleRefresh = () => {
        // TODO: gọi lại API
    }

    return (
        <div className="signatures-page">
            <div className="page-header">
                <div>
                    <h2>Signatures</h2>
                    <p className="page-subtitle">Manage your personal electronic signatures for approvals, contracts, and internal documents.</p>
                </div>
            </div>

            <SignatureToolbar
                searchTerm={searchTerm}
                onSearchChange={setSearchTerm}
                typeFilter={typeFilter}
                onTypeChange={setTypeFilter}
                statusFilter={statusFilter}
                onStatusChange={setStatusFilter}
                onRefresh={handleRefresh}
                onCreateNew={handleCreateNew}
            />

            <div className="signature-table-wrapper">
                <SignatureTable signatures={filteredSignatures} />
            </div>

            <SignaturePagination
                currentPage={currentPage}
                totalPages={totalPages}
                totalResults={filteredSignatures.length}
                pageSize={pageSize}
                onPageChange={setCurrentPage}
                onPageSizeChange={setPageSize}
            />
        </div>
    )
}

export default SignatureList