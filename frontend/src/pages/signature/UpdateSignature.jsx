import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import PageHeader from "../../components/signature/createSignature/PageHeader.jsx";
import SignatureInformationCard from "../../components/signature/createSignature/SignatureInformationCard.jsx";
import SignatureCanvasCard from "../../components/signature/createSignature/SignatureCanvasCard.jsx";
import DocumentAutomationPreview from "../../components/signature/createSignature/DocumentAutomationPreview.jsx";
import InfoBanner from "../../components/signature/createSignature/InforBanner.jsx";

import electronicSignatureService
    from "../../services/signatureService/electronicSignatureService.js";

function UpdateSignature() {

    const { id } = useParams();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        electronicSignatureName: "",
        electronicSignatureType: "DRAW",
        electronicStatus: "ACTIVE",
        isDefault: false,
    });

    const [activeTab, setActiveTab] = useState("draw");

    const [signatureFile, setSignatureFile] = useState(null);

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");



    useEffect(() => {

        const loadSignature = async () => {

            try {

                setLoading(true);

                const response =
                    await electronicSignatureService
                        .getElectronicSignatureById(id);

                console.log(
                    "DETAIL:",
                    response.data
                );

                const data = response.data.data;

                setForm({
                    electronicSignatureName:
                        data.electronicSignatureName || "",

                    electronicSignatureType:
                        data.electronicSignatureType || "DRAW",

                    electronicStatus:
                        data.electronicStatus || "ACTIVE",

                    isDefault:
                        data.isDefault ?? false,
                });

            } catch (error) {

                console.error(error);

                setError(
                    error?.response?.data?.message ||
                    "Cannot load signature."
                );

            } finally {

                setLoading(false);

            }
        };

        if (id) {
            loadSignature();
        }

    }, [id]);



    const handleSave = async () => {

        if (
            !form.electronicSignatureName ||
            !form.electronicSignatureName.trim()
        ) {
            setError("Please enter signature name.");
            return;
        }

        try {

            setSaving(true);
            setError("");
            setSuccess("");

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
                "electronicStatus",
                form.electronicStatus
            );

            formData.append(
                "isDefault",
                String(form.isDefault)
            );

            if (signatureFile) {

                formData.append(
                    "multipartFile",
                    signatureFile
                );

            }

            await electronicSignatureService
                .updateElectronicSignature(
                    id,
                    formData
                );

            setSuccess(
                "Signature updated successfully!"
            );

        } catch (error) {

            console.error(
                "UPDATE SIGNATURE ERROR:",
                error
            );

            setError(
                error?.response?.data?.message ||
                "Update signature failed."
            );

        } finally {

            setSaving(false);

        }
    };


    if (loading) {
        return (
            <div className="p-4">
                Loading signature...
            </div>
        );
    }


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
                    onCancel={() =>
                        navigate("/signatures")
                    }
                    onSave={handleSave}
                    loading={saving}
                />


                {error && (
                    <div className="alert alert-danger">
                        {error}
                    </div>
                )}


                {success && (
                    <div className="alert alert-success">
                        {success}
                    </div>
                )}


                <SignatureInformationCard
                    form={form}
                    setForm={setForm}
                />


                <SignatureCanvasCard
                    activeTab={activeTab}
                    setActiveTab={setActiveTab}
                    onFileChange={setSignatureFile}
                />


                <DocumentAutomationPreview />


                <InfoBanner
                    text="Your signature information can be updated at any time."
                />

            </div>

        </div>
    );
}

export default UpdateSignature;