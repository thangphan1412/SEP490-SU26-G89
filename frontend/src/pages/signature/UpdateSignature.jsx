import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import PageHeader from "../../components/signature/createSignature/PageHeader.jsx";
import SignatureInformationCard from "../../components/signature/createSignature/SignatureInformationCard.jsx";
import SignatureCanvasCard from "../../components/signature/createSignature/SignatureCanvasCard.jsx";
import DocumentAutomationPreview from "../../components/signature/createSignature/DocumentAutomationPreview.jsx";
import InfoBanner from "../../components/signature/createSignature/InforBanner.jsx";

import electronicSignatureService
    from "../../services/signatureService/electronicSignatureService.js";
import PropTypes from "prop-types";
import SignatureKeyVerification from "../../components/signature/SignatureKeyVerification.jsx";

function SignatureCanvas({ signatureUrl }) {
    if (!signatureUrl) return null;

    return (
        <div className="mt-3 text-center">
            <img
                src={signatureUrl}
                alt="Signature"
                style={{
                    maxWidth: "300px",
                    border: "1px solid #e5e7eb",
                    borderRadius: "6px",
                    padding: "10px",
                    backgroundColor: "#fff",
                }}
            />
        </div>
    );
}

SignatureCanvas.propTypes = {
    mode: PropTypes.string,

    signatureUrl: PropTypes.any
};
function UpdateSignature() {

    const { id } = useParams();
    const navigate = useNavigate();
    const [electronicSignature, setElectronicSignature] = useState(null);
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
    const [keyProof, setKeyProof] = useState(null);
    const [verificationAttempt, setVerificationAttempt] = useState(0);



    useEffect(() => {

        const loadSignature = async () => {

            try {

                setLoading(true);

                const response =
                    await electronicSignatureService
                        .getElectronicSignatureById(id);
                setElectronicSignature(response.data.data);
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
                        data.electronicSignatureStatus || data.electronicStatus || "ACTIVE",

                    isDefault:
                        data.default ?? data.isDefault ?? false,
                });
                setActiveTab(data.electronicSignatureType === "UPLOADED" ? "upload" : "draw");

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
        if (saving) return;
        if (!keyProof || keyProof.signatureId !== id || Date.parse(keyProof.expiresAt) <= Date.now()) {
            setError("Verify your public and private keys before saving changes.");
            return;
        }

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
            formData.append("verificationChallengeId", keyProof.challengeId);
            formData.append("verificationSignature", keyProof.signature);

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
                "default",
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
            navigate(`/signature-management/detail/${id}`, { replace: true });

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
            setKeyProof(null);
            setVerificationAttempt((attempt) => attempt + 1);

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
                    title="Update Signature"
                    description="Update your signature after verifying ownership of your signing keys."
                    onCancel={() =>
                        navigate("/signature-management/list")
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
                    onTypeChange={(type) => {
                        setForm((current) => ({ ...current, electronicSignatureType: type }));
                        setActiveTab(type === "DRAW" ? "draw" : "upload");
                        setSignatureFile(null);
                    }}
                />


                <SignatureCanvas
                    mode="view"
                    signatureUrl={electronicSignature?.signatureUrl}
                />
                <SignatureCanvasCard
                    signatureType={form.electronicSignatureType}
                    activeTab={activeTab}
                    setActiveTab={setActiveTab}
                    onFileChange={setSignatureFile}
                />

                <SignatureKeyVerification key={`${id}:${verificationAttempt}`} signatureId={id}
                    onVerified={setKeyProof} saving={saving} />


                <DocumentAutomationPreview />


                <InfoBanner
                    text="Your signature information can be updated at any time."
                />

            </div>

        </div>
    );
}

export default UpdateSignature;
