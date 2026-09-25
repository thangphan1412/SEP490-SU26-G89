import { useState } from "react";
import { useNavigate } from "react-router-dom";

import PageHeader from "../../components/signature/createSignature/PageHeader.jsx";
import SignatureInformationCard from "../../components/signature/createSignature/SignatureInformationCard.jsx";
import SignatureCanvasCard from "../../components/signature/createSignature/SignatureCanvasCard.jsx";
import DocumentAutomationPreview from "../../components/signature/createSignature/DocumentAutomationPreview.jsx";
import InfoBanner from "../../components/signature/createSignature/InforBanner.jsx";

import electronicSignatureService
    from "../../services/signatureService/electronicSignatureService.js";
import SigningKeyCard from "../../components/signature/SigningKeyCard.jsx";
import digitalSignatureService from "../../services/signatureService/digitalSignatureService.js";
import "../../assets/styles/css/signature/SignatureKeyFields.css";

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
    const [keyStatus, setKeyStatus] = useState("NOT_CONFIGURED");
    const [keyCode, setKeyCode] = useState(null);
    const [publicKey, setPublicKey] = useState(null);
    const [keyLoading, setKeyLoading] = useState(false);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState("");
    const [pin, setPin] = useState("");
    const [confirmPin, setConfirmPin] = useState("");
    const [error, setError] = useState("");
    const [generatedKey, setGeneratedKey] = useState(null);
    const downloadPrivateKeyBackup = (encryptedData, keyCode) => {
        const blob = new Blob(
            [JSON.stringify(encryptedData, null, 2)],
            { type: "application/json" }
        );

        const url = URL.createObjectURL(blob);

        const a = document.createElement("a");
        a.href = url;
        a.download = `private-key-${keyCode}.enc`;
        document.body.appendChild(a);
        a.click();

        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    };
    const handleGenerateKey = async () => {
        try {
            setKeyLoading(true);
            setError("");
            setSuccess("");

            // ==========================================
            // 1. Validate PIN
            // ==========================================

            if (!validatePin()) {
                return;
            }

            // ==========================================
            // 2. Generate RSA key
            // ==========================================

            const response =
                await digitalSignatureService.generateKey();


            const keyData =
                response?.data?.data;

            if (!keyData) {
                throw new Error(
                    "Backend did not return key information."
                );
            }

            const {
                keyCode,
                publicKey,
                privateKey,
                certificate
            } = keyData;

            if (!privateKey) {
                throw new Error(
                    "Private key was not returned."
                );
            }

            if (!publicKey) {
                throw new Error(
                    "Public key was not returned."
                );
            }

            if (!keyCode) {
                throw new Error(
                    "Key code was not returned."
                );
            }
            if (!certificate) {
                throw new Error(
                    "Certificate was not returned."
                );
            }

            // ==========================================
            // 3. Encrypt private key using PIN
            // ==========================================

            const encryptedData =
                await encryptPrivateKey(
                    privateKey,
                    pin
                );

            // ==========================================
            // 4. Store encrypted private key locally
            // ==========================================

            localStorage.setItem(
                `encryptedPrivateKey_${keyCode}`,
                JSON.stringify(encryptedData)
            );

            // ==========================================
            // 5. Download backup
            // ==========================================

            downloadPrivateKeyBackup(
                encryptedData,
                keyCode
            );

            // ==========================================
            // 6. Store generated key in React state
            // ==========================================

            setGeneratedKey({
                keyCode,
                publicKey,
                certificate
            });
            console.log("GENERATED KEY:", {
                keyCode,
                publicKey,
                certificate
            });
            setKeyCode(keyCode);
            setPublicKey(publicKey);
            setKeyStatus("ACTIVE");

            setSuccess(
                "Signing key generated successfully. " +
                "Private key has been encrypted and backup downloaded."
            );

        } catch (error) {

            console.error(
                "GENERATE KEY ERROR:",
                error
            );

            console.error(
                "RESPONSE:",
                error?.response?.data
            );

            setError(
                error?.response?.data?.message ||
                error?.message ||
                "Failed to generate signing key."
            );

        } finally {
            setKeyLoading(false);
        }
    };
    const encryptPrivateKey = async (
        privateKey,
        pin
    ) => {
        const encoder = new TextEncoder();

        const privateKeyBytes =
            encoder.encode(privateKey);

        const salt =
            crypto.getRandomValues(
                new Uint8Array(16)
            );

        const iv =
            crypto.getRandomValues(
                new Uint8Array(12)
            );

        const keyMaterial =
            await crypto.subtle.importKey(
                "raw",
                encoder.encode(pin),
                "PBKDF2",
                false,
                ["deriveKey"]
            );

        const encryptionKey =
            await crypto.subtle.deriveKey(
                {
                    name: "PBKDF2",
                    salt: salt,
                    iterations: 600000,
                    hash: "SHA-256",
                },
                keyMaterial,
                {
                    name: "AES-GCM",
                    length: 256,
                },
                false,
                ["encrypt"]
            );

        const encrypted =
            await crypto.subtle.encrypt(
                {
                    name: "AES-GCM",
                    iv: iv,
                },
                encryptionKey,
                privateKeyBytes
            );

        return {
            version: 1,
            keyAlgorithm: "RSA",
            keySize: 2048,
            encryption: "AES-GCM",
            kdf: "PBKDF2",
            iterations: 600000,
            salt: arrayBufferToBase64(salt),
            iv: arrayBufferToBase64(iv),
            encryptedPrivateKey:
                arrayBufferToBase64(encrypted),
        };
    };
    const arrayBufferToBase64 = (buffer) => {
        const bytes = new Uint8Array(buffer);

        let binary = "";

        bytes.forEach((byte) => {
            binary += String.fromCharCode(byte);
        });

        return window.btoa(binary);
    };
    const validatePin = () => {
        if (!/^\d{6}$/.test(pin)) {
            setError("PIN must contain exactly 6 digits.");
            return false;
        }

        if (pin !== confirmPin) {
            setError("PIN confirmation does not match.");
            return false;
        }

        setError("");
        return true;
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
        if (loading || keyLoading) return;
        try {
            setLoading(true);
            setError("");
            setSuccess("");

            // ==========================================
            // 1. Check generated key
            // ==========================================

            if (
                !generatedKey?.keyCode ||
                !generatedKey?.publicKey
            ) {
                setError(
                    "Please generate your signing key before saving."
                );
                return;
            }

            // ==========================================
            // 2. Check encrypted private key
            // ==========================================

            const encryptedPrivateKey =
                localStorage.getItem(
                    `encryptedPrivateKey_${generatedKey.keyCode}`
                );

            if (!encryptedPrivateKey) {
                setError(
                    "Encrypted private key was not found. " +
                    "Please generate your signing key again."
                );
                return;
            }

            // ==========================================
            // 3. Validate signature name
            // ==========================================

            if (!form.electronicSignatureName.trim()) {
                setError(
                    "Please enter an electronic signature name."
                );
                return;
            }

            // ==========================================
            // 4. Validate signature image
            // ==========================================

            if (!signatureFile) {
                setError(
                    "Please upload or draw your signature."
                );
                return;
            }

            // ==========================================
            // 5. Create FormData
            // ==========================================

            const formData = new FormData();

            // Electronic Signature
            formData.append(
                "electronicSignatureName",
                form.electronicSignatureName
            );

            formData.append(
                "electronicSignatureType",
                form.electronicSignatureType
            );

            formData.append(
                "default",
                String(form.isDefault)
            );

            formData.append(
                "electronicStatus",
                form.electronicStatus
            );

            // Signing Key
            formData.append(
                "publicKey",
                generatedKey.publicKey
            );

            formData.append(
                "keyCode",
                generatedKey.keyCode
            );
            formData.append(
                "certificate",
                generatedKey.certificate
            );

            // Signature image
            formData.append(
                "multipartFile",
                signatureFile
            );

            // ==========================================
            // 6. Send ONE request
            // ==========================================

            console.log(
                "Saving electronic signature..."
            );

            const response =
                await electronicSignatureService
                    .createElectronicSignature(formData);

            console.log(
                "CREATE SIGNATURE RESPONSE:",
                response
            );

            // ==========================================
            // 7. Success
            // ==========================================

            setSuccess(
                "Electronic signature and signing key saved successfully."
            );

            setTimeout(() => {
                navigate("/signature-management/list");
            }, 1000);

        } catch (error) {

            console.error(
                "SAVE SIGNATURE ERROR:",
                error
            );

            console.error(
                "RESPONSE:",
                error?.response?.data
            );

            setError(
                error?.response?.data?.message ||
                error?.message ||
                "Failed to save electronic signature."
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
                    loading={loading || keyLoading}
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
                <section className="signature-key-fields">
                    <h2>Protect your private key</h2>
                    <p>Create a 6-digit PIN to encrypt your private key and its backup.</p>
                    <div className="signature-key-grid">
                    <label className="signature-key-field">PIN for Private Key

                    <input
                        type="password"
                        className="form-control"
                        autoComplete="new-password"
                        disabled={keyLoading || keyStatus === "ACTIVE"}
                        inputMode="numeric"
                        maxLength={6}
                        value={pin}
                        onChange={(e) => {
                            const value = e.target.value.replace(/\D/g, "");
                            setPin(value);
                        }}
                        placeholder="Enter 6-digit PIN"
                    />
                    </label>

                    <label className="signature-key-field">Confirm PIN

                    <input
                        type="password"
                        className="form-control"
                        autoComplete="new-password"
                        disabled={keyLoading || keyStatus === "ACTIVE"}
                        inputMode="numeric"
                        maxLength={6}
                        value={confirmPin}
                        onChange={(e) => {
                            const value = e.target.value.replace(/\D/g, "");
                            setConfirmPin(value);
                        }}
                        placeholder="Confirm 6-digit PIN"
                    />
                    </label>
                    </div>
                </section>
                <SigningKeyCard
                    keyStatus={keyStatus}
                    keyCode={keyCode}
                    publicKey={publicKey}
                    onGenerateKey={handleGenerateKey}
                    loading={keyLoading}
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
