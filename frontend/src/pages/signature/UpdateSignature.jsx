import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

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
    const [rsaKeys, setRsaKeys] = useState({
        privateKey: "",
        publicKey: "",
    });
    const [keyFingerprint, setKeyFingerprint] = useState("");
    const [keyRegistered, setKeyRegistered] = useState(false);
    const [generatingKeys, setGeneratingKeys] = useState(false);



    useEffect(() => {

        const loadSignature = async () => {

            try {

                setLoading(true);

                const response =
                    await electronicSignatureService
                        .getElectronicSignatureById(id);

                const data = response.data.data;

                setForm({
                    electronicSignatureName:
                        data.electronicSignatureName || "",

                    electronicSignatureType:
                        data.electronicSignatureType || "DRAW",

                    electronicStatus:
                        data.electronicSignatureStatus || "ACTIVE",

                    isDefault:
                        data.isDefault ?? false,
                });
                setRsaKeys({
                    privateKey: "",
                    publicKey: data.publicKey || "",
                });
                setKeyFingerprint(data.publicKeyFingerprint || "");
                setKeyRegistered(Boolean(data.publicKey));

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

    const handleGenerateKeys = async () => {
        if (keyRegistered) {
            return;
        }
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

            const keys = keyRegistered || rsaKeys.publicKey
                ? rsaKeys
                : await generateRsaSigningKeys();
            if (!keyRegistered && !rsaKeys.publicKey) {
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
            if (keys.publicKey) {
                formData.append("publicKey", keys.publicKey);
            }

            await electronicSignatureService
                .updateElectronicSignature(
                    id,
                    formData
                );

            if (!keyRegistered && keys.privateKey) {
                downloadPrivateKeyPem(
                    keys.privateKey,
                    form.electronicSignatureName
                );
                setKeyRegistered(true);
                setSuccess(
                    "Signature updated and RSA key registered. The private key has been downloaded."
                );
            } else {
                setSuccess("Signature updated successfully!");
            }

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
                />


                <SignatureCanvasCard
                    activeTab={activeTab}
                    setActiveTab={setActiveTab}
                    onFileChange={setSignatureFile}
                />

                <RsaKeyPairCard
                    publicKey={rsaKeys.publicKey}
                    fingerprint={keyFingerprint}
                    privateKeyAvailable={Boolean(rsaKeys.privateKey)}
                    registered={keyRegistered}
                    generating={generatingKeys}
                    onGenerate={handleGenerateKeys}
                    onDownload={handleDownloadPrivateKey}
                />


                <DocumentAutomationPreview />


                <InfoBanner
                    text="RSA public keys are immutable after registration. Create a new electronic signature if the private key is lost or compromised."
                />

            </div>

        </div>
    );
}

export default UpdateSignature;
