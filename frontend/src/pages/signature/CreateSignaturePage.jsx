import React, { useState } from "react";
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

            if (!validatePin()) {
                return;
            }

            // POST /api/v1/signature/keys/generate
            const response =
                await digitalSignatureService.generateKey();

            console.log(
                "Generate key response:",
                response
            );

            // BaseResponse.data
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

            // Encrypt private key bằng PIN
            const encryptedData =
                await encryptPrivateKey(
                    privateKey,
                    pin
                );

            // Lưu browser
            localStorage.setItem(
                "encryptedPrivateKey",
                JSON.stringify(encryptedData)
            );
            console.log("Encrypted data:", encryptedData);
            // Download backup
            downloadPrivateKeyBackup(encryptedData, keyCode);
            console.log("Download function called");
            setKeyCode(
                keyCode ?? null
            );

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
        try {
            setLoading(true);
            setError("");
            setSuccess("");

            // ==========================================
            // 1. Check RSA key
            // ==========================================

            const keyResponse =
                await digitalSignatureService.getMyPublicKey();

            const keyData =
                keyResponse?.data?.data;

            // Backend chưa có public key
            if (!keyData?.available || !keyData?.publicKey) {
                setError(
                    "Please generate your signing key before saving an electronic signature."
                );
                return;
            }

            // ==========================================
            // 2. Check encrypted private key in browser
            // ==========================================

            const encryptedPrivateKey =
                localStorage.getItem("encryptedPrivateKey");

            if (!encryptedPrivateKey) {
                setError(
                    "Your private key is not configured. Please generate your signing key again."
                );
                return;
            }

            // ==========================================
            // 3. Check signature name
            // ==========================================

            if (!form.electronicSignatureName.trim()) {
                setError(
                    "Please enter an electronic signature name."
                );
                return;
            }

            // ==========================================
            // 4. Check DRAW / UPLOAD
            // ==========================================

            if (activeTab === "upload" && !signatureFile) {
                setError(
                    "Please upload your signature image."
                );
                return;
            }

            // ==========================================
            // 5. Save electronic signature
            // ==========================================

            // phần code save hiện tại của bạn đặt ở đây

        } catch (error) {

            console.error(
                "SAVE SIGNATURE ERROR:",
                error
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
                <div>
                    <label>PIN for Private Key</label>

                    <input
                        type="password"
                        inputMode="numeric"
                        maxLength={6}
                        value={pin}
                        onChange={(e) => {
                            const value = e.target.value.replace(/\D/g, "");
                            setPin(value);
                        }}
                        placeholder="Enter 6-digit PIN"
                    />
                </div>

                <div>
                    <label>Confirm PIN</label>

                    <input
                        type="password"
                        inputMode="numeric"
                        maxLength={6}
                        value={confirmPin}
                        onChange={(e) => {
                            const value = e.target.value.replace(/\D/g, "");
                            setConfirmPin(value);
                        }}
                        placeholder="Confirm 6-digit PIN"
                    />
                </div>
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
