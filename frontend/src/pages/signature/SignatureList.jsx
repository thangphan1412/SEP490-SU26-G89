import { useState, useEffect } from "react";

import "../../assets/styles/css/signatureStyles/SignaturePage.css";
import SignatureToolbar from "../../components/signature/SignatureToolbar.jsx";
import SignatureTable from "../../components/signature/SignatureTable.jsx";
import electronicSignatureService from "../../services/signatureService/electronicSignatureService.js";
import { useNavigate } from "react-router-dom";

function SignatureList() {
    const [searchTerm, setSearchTerm] = useState("");
    const [typeFilter, setTypeFilter] = useState("All");
    const [statusFilter, setStatusFilter] = useState("All");
    const [electronicSignature, setElectronicSignature] = useState([]);

    const navigate = useNavigate();

    const loadElectronicSignature = async () => {
        try {
            const response =
                await electronicSignatureService.getAllElectronicSignature(
                    searchTerm,
                    typeFilter,
                    statusFilter
                );

            console.log("SIGNATURE DATA:", response.data.data);

            setElectronicSignature(response.data.data || []);
        } catch (error) {
            console.error("LOAD SIGNATURE ERROR:", error);
            setElectronicSignature([]);
        }
    };

    useEffect(() => {
        loadElectronicSignature();
    }, [searchTerm, typeFilter, statusFilter]);

    const handleCreateNew = () => {
        navigate("/signature-management/create-signature");
    };

    const handleRefresh = () => {
        loadElectronicSignature();
    };

    return (
        <div className="signatures-page">
            <div className="page-header">
                <div>
                    <h2>Signatures</h2>
                    <p className="page-subtitle">
                        Manage your personal electronic signatures
                        for approvals, contracts, and internal documents.
                    </p>
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
                <SignatureTable
                    electronicSignatures={electronicSignature}
                />
            </div>
        </div>
    );
}

export default SignatureList;