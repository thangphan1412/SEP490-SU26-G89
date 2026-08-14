import { useState } from "react";
import { useNavigate } from "react-router-dom";

import PageHeader from "../../components/signature/createSignature/PageHeader.jsx";
import SignatureInformationCard from "../../components/signature/createSignature/SignatureInformationCard.jsx";
import SignatureCanvasCard from "../../components/signature/createSignature/SignatureCanvasCard.jsx";
import DocumentAutomationPreview from "../../components/signature/createSignature/DocumentAutomationPreview.jsx";
import InfoBanner from "../../components/signature/createSignature/InforBanner.jsx";
import RsaKeyPairCard from "../../components/signature/createSignature/RsaKeyPairCard.jsx";

import electronicSignatureService
    from "../../services/signatureService/electronicSignatureService.js";
import {
    downloadPrivateKeyPem,
    generateRsaSigningKeys,
} from "../ContractManagement/contractCrypto.js";

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
    const [rsaKeys, setRsaKeys] = useState({
        privateKey: "",
        publicKey: "",
    });
    const [generatingKeys, setGeneratingKeys] = useState(false);

    const handleGenerateKeys = async () => {
        setGeneratingKeys(true);
        setError("");
        try {
            setRsaKeys(await generateRsaSigningKeys());
        } catch (error) {
            setError(error?.message || "Cannot generate the RSA key pair.");
        } finally {
            setGeneratingKeys(false);
        }
    };

    const handleDownloadPrivateKey = () => {
        downloadPrivateKeyPem(
            rsaKeys.privateKey,
            form.electronicSignatureName
        );
    };


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

            const keys = rsaKeys.publicKey
                ? rsaKeys
                : await generateRsaSigningKeys();
            if (!rsaKeys.publicKey) {
                setRsaKeys(keys);
            }

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
            formData.append("publicKey", keys.publicKey);


            await electronicSignatureService
                .createElectronicSignature(formData);

            downloadPrivateKeyPem(
                keys.privateKey,
                form.electronicSignatureName
            );
            setSuccess(
                "Signature created. Your RSA private key has been downloaded."
            );


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
        navigate("/signature-management/list");
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

                <RsaKeyPairCard
                    publicKey={rsaKeys.publicKey}
                    privateKeyAvailable={Boolean(rsaKeys.privateKey)}
                    registered={false}
                    generating={generatingKeys}
                    onGenerate={handleGenerateKeys}
                    onDownload={handleDownloadPrivateKey}
                />

                <DocumentAutomationPreview />

                <InfoBanner
                    text="The public key is registered with this signature. Keep the downloaded private key secret; it is required when signing contracts."
                />

            </div>

        </div>
    );
}

export default CreateSignaturePage;
