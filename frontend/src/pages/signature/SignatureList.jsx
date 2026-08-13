import {useState, useMemo, useEffect} from "react"

import "../../assets/styles/css/signatureStyles/SignaturePage.css"
import SignatureToolbar from "../../components/signature/SignatureToolbar.jsx";
import SignatureTable from "../../components/signature/SignatureTable.jsx";
import SignaturePagination from "../../components/signature/SignaturePagination.jsx";
import electronicSignatureService from "../../services/signatureService/electronicSignatureService.js"


function SignatureList() {
    const [searchTerm, setSearchTerm] = useState("")
    const [typeFilter, setTypeFilter] = useState("All")
    const [statusFilter, setStatusFilter] = useState("All")
    const [currentPage, setCurrentPage] = useState(1)
    const [pageSize, setPageSize] = useState(10)

    const [electronicSignature, setElectronicSignature] = useState([]);
    const loadElectronicSignature = async () => {
        try {
            const response =
                await electronicSignatureService
                    .getAllElectronicSignature();

            console.log("SIGNATURE DATA:", response.data.data);

            setElectronicSignature(response.data.data);

        } catch (error) {
            console.error(error);
            setElectronicSignature([]);
        }
    };
    useEffect(() => {
        loadElectronicSignature();
    }, []);
    const filteredSignatures = useMemo(() => {
        return electronicSignature.filter((sig) => {

            const name = sig.signatureName || "";
            const type = sig.type || "";
            const status = sig.status || "";

            const matchesSearch =
                name
                    .toLowerCase()
                    .includes(
                        searchTerm.toLowerCase()
                    );

            const matchesType =
                typeFilter === "All" ||
                type === typeFilter;

            const matchesStatus =
                statusFilter === "All" ||
                status === statusFilter;


            return (
                matchesSearch &&
                matchesType &&
                matchesStatus
            );
        });
    }, [electronicSignature,
        searchTerm,
        typeFilter,
        statusFilter]);

    const totalPages = Math.max(1, Math.ceil(filteredSignatures.length / pageSize))

    const handleCreateNew = () => {
        // TODO: điều hướng tới trang tạo signature hoặc mở modal
    }

    const handleRefresh = () => {
        loadElectronicSignature();
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