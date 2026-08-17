import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

import PageHeader from "../../components/signature/createSignature/PageHeader.jsx";
import SignatureInformationCard from "../../components/signature/createSignature/SignatureInformationCard.jsx";
import SignatureCanvasCard from "../../components/signature/createSignature/SignatureCanvasCard.jsx";
import DocumentAutomationPreview from "../../components/signature/createSignature/DocumentAutomationPreview.jsx";
import InfoBanner from "../../components/signature/createSignature/InforBanner.jsx";

import electronicSignatureService
    from "../../services/signatureService/electronicSignatureService.js";

function CreateSignaturePage() {

    const navigate = useNavigate();

    const [form, setForm] = useState({
        electronicSignatureName: "",
        electronicSignatureType: "DRAW",
        isDefault: false,
        electronicStatus: "ACTIVE",
    });

    const [activeTab, setActiveTab] = useState("draw");


    const [signatureFile, setSignatureFile] = useState(null);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");


    const handleTypeChange = (type) => {

        setForm((prev) => ({
            ...prev,
            electronicSignatureType: type,
        }));

        setSignatureFile(null);

        if (type === "DRAW") {
            setActiveTab("draw");
        } else {
            setActiveTab("upload");
        }
    };


    const handleSave = async () => {

        setError("");
        setSuccess("");


        if (!form.electronicSignatureName?.trim()) {
            setError("Please enter signature name.");
            return;
        }


        if (!signatureFile) {
            if (form.electronicSignatureType === "DRAW") {
                setError("Please draw your signature first.");
            } else {
                setError("Please upload your signature file.");
            }

            return;
        }

        try {

            setLoading(true);

            const formData = new FormData();


            formData.append(
                "electronicSignatureName",
                form.electronicSignatureName
            );


            formData.append(
                "electronicSignatureType",
                form.electronicSignatureType
            );


            formData.append(
                "isDefault",
                String(form.isDefault)
            );


            formData.append(
                "electronicStatus",
                form.electronicStatus
            );


            formData.append(
                "multipartFile",
                signatureFile
            );

            console.log("===== CREATE SIGNATURE =====");

            for (const [key, value] of formData.entries()) {
                console.log(key, value);
            }


            await electronicSignatureService
                .createElectronicSignature(formData);


            setSuccess("Signature created successfully!");


            setSignatureFile(null);

        } catch (error) {

            console.error(
                "CREATE SIGNATURE ERROR:",
                error
            );

            console.error(
                "RESPONSE:",
                error?.response?.data
            );

            setError(
                error?.response?.data?.message ||
                "Create signature failed."
            );

        } finally {

            setLoading(false);
        }
    };

    const handleCancel = () => {
        navigate("/signatures");
    };

    return (
        <div
            className="min-vh-100"
            style={{
                backgroundColor: "#f8fafc",
                padding: "24px",
            }}
        >

            <div
                className="mx-auto"
                style={{
                    maxWidth: "1100px",
                }}
            >

                <PageHeader
                    onCancel={handleCancel}
                    onSave={handleSave}
                    loading={loading}
                />


                {error && (
                    <div className="alert alert-danger mt-3">
                        {error}
                    </div>
                )}


                {success && (
                    <div className="alert alert-success mt-3">
                        {success}
                    </div>
                )}

                <SignatureInformationCard
                    form={form}
                    setForm={setForm}
                    onTypeChange={handleTypeChange}
                />

                <SignatureCanvasCard
                    activeTab={activeTab}
                    setActiveTab={setActiveTab}
                    signatureType={form.electronicSignatureType}
                    onFileChange={setSignatureFile}
                    onClear={() => {
                        setSignatureFile(null);
                    }}
                />

                <DocumentAutomationPreview />

                <InfoBanner
                    text="Your signature will be available for personal use after saving."
                />

            </div>

        </div>
    );
}

export default CreateSignaturePage;
