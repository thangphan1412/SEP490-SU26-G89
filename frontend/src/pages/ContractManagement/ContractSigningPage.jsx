import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import {
    IconArrowLeft,
    IconCheck,
    IconFileTypePdf,
    IconEye,
    IconEyeOff,
    IconLock,
} from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";

import contractApi from "../../services/contractService/contractApi.js";
import electronicSignatureService from "../../services/signatureService/electronicSignatureService.js";
import { getApiErrorMessage, unwrapApiResponse } from "./contractUtils.js";

import "../../assets/styles/css/layoutStyles/ContractSigning.css";

export default function ContractSigningPage() {

    const { id } = useParams();
    const navigate = useNavigate();

    // =========================================================
    // CONTRACT
    // =========================================================

    const [contract, setContract] = useState(null);

    // =========================================================
    // ELECTRONIC SIGNATURE
    // =========================================================

    const [signatures, setSignatures] = useState([]);
    const [selectedId, setSelectedId] = useState("");

    // =========================================================
    // PDF
    // =========================================================

    const [pdfUrl, setPdfUrl] = useState("");
    const [pdfBlob, setPdfBlob] = useState(null);

    // =========================================================
    // SIGNING KEY
    // =========================================================

    const [publicKeyCode, setPublicKeyCode] = useState("");
    const [pin, setPin] = useState("");
    const [privateKey, setPrivateKey] = useState("");

    // =========================================================
    // UI
    // =========================================================

    const [showPin, setShowPin] = useState(false);
    const [showPrivateKey, setShowPrivateKey] = useState(false);

    const [loading, setLoading] = useState(true);
    const [unlockingKey, setUnlockingKey] = useState(false);
    const [signing, setSigning] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // =========================================================
    // LOAD CONTRACT / SIGNATURES / PDF
    // =========================================================

    useEffect(() => {

        let active = true;
        let objectUrl = "";

        Promise.allSettled([
            contractApi.getContractById(id),
            electronicSignatureService.getAllElectronicSignature(),
            contractApi.exportContractPdf(id),
        ])
            .then(async ([
                contractResult,
                signatureResult,
                pdfResult,
            ]) => {

                if (!active) {
                    return;
                }

                const errors = [];

                // =====================================================
                // CONTRACT
                // =====================================================

                if (contractResult.status === "fulfilled") {

                    setContract(
                        unwrapApiResponse(
                            contractResult.value
                        )
                    );

                } else {

                    errors.push(
                        await readApiError(
                            contractResult.reason,
                            "Unable to load the contract."
                        )
                    );
                }

                // =====================================================
                // ELECTRONIC SIGNATURES
                // =====================================================

                if (signatureResult.status === "fulfilled") {

                    const rows =
                        signatureResult.value?.data?.data || [];

                    const activeRows =
                        rows.filter(
                            (item) =>
                                item.status === "ACTIVE"
                        );

                    setSignatures(activeRows);

                    setSelectedId(
                        activeRows.find(
                            (item) =>
                                item.default ||
                                item.isDefault
                        )?.id ||
                        activeRows[0]?.id ||
                        ""
                    );

                } else {

                    errors.push(
                        await readApiError(
                            signatureResult.reason,
                            "Unable to load your electronic signatures."
                        )
                    );
                }

                // =====================================================
                // PDF
                // =====================================================

                if (pdfResult.status === "fulfilled") {

                    const pdfData =
                        pdfResult.value.data;

                    const blob =
                        pdfData instanceof Blob
                            ? pdfData
                            : new Blob(
                                [pdfData],
                                {
                                    type: "application/pdf",
                                }
                            );

                    setPdfBlob(blob);

                    objectUrl =
                        URL.createObjectURL(blob);

                    setPdfUrl(objectUrl);

                } else {

                    errors.push(
                        await readApiError(
                            pdfResult.reason,
                            "Unable to load the contract PDF."
                        )
                    );
                }

                // =====================================================
                // ERRORS
                // =====================================================

                if (
                    active &&
                    errors.length > 0
                ) {

                    setError(
                        [...new Set(errors)].join(" ")
                    );
                }

            })
            .finally(() => {

                if (active) {
                    setLoading(false);
                }

            });

        return () => {

            active = false;

            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }

        };

    }, [id]);

    // =========================================================
    // SELECTED SIGNATURE
    // =========================================================

    const selectedSignature = useMemo(
        () =>
            signatures.find(
                (item) =>
                    item.id === selectedId
            ),
        [signatures, selectedId]
    );

    // =========================================================
    // PUBLIC KEY CODE INPUT
    // =========================================================

    function handlePublicKeyCodeChange(event) {

        const value =
            event.target.value
                .replace(/\D/g, "");

        if (value.length <= 6) {
            setPublicKeyCode(value);
        }

        setError("");
        setSuccess("");
    }

    // =========================================================
    // PIN INPUT
    // =========================================================

    function handlePinChange(event) {

        const value =
            event.target.value
                .replace(/\D/g, "");

        if (value.length <= 6) {
            setPin(value);
        }

        setError("");
        setSuccess("");
    }

    // =========================================================
    // BASE64 -> ARRAY BUFFER
    // =========================================================

    // =========================================================
    // ARRAY BUFFER -> BASE64
    // =========================================================

    function bufferToBase64(buffer) {

        const bytes =
            new Uint8Array(buffer);

        let binary = "";

        const chunkSize = 0x8000;

        for (
            let i = 0;
            i < bytes.length;
            i += chunkSize
        ) {

            const chunk =
                bytes.subarray(
                    i,
                    Math.min(
                        i + chunkSize,
                        bytes.length
                    )
                );

            binary += String.fromCharCode(
                ...chunk
            );
        }

        return window.btoa(binary);
    }

    // =========================================================
    // DECRYPT PRIVATE KEY
    // =========================================================

    async function decryptPrivateKey(
        encryptedData,
        pinValue
    ) {

        const encoder =
            new TextEncoder();

        // =====================================================
        // SALT
        // =====================================================

        const salt =
            new Uint8Array(
                base64ToArrayBuffer(
                    encryptedData.salt
                )
            );

        // =====================================================
        // IV
        // =====================================================

        const iv =
            new Uint8Array(
                base64ToArrayBuffer(
                    encryptedData.iv
                )
            );

        // =====================================================
        // ENCRYPTED PRIVATE KEY
        // =====================================================

        const encrypted =
            base64ToArrayBuffer(
                encryptedData.encryptedPrivateKey
            );

        // =====================================================
        // IMPORT PIN
        // =====================================================

        const keyMaterial =
            await crypto.subtle.importKey(
                "raw",
                encoder.encode(pinValue),
                "PBKDF2",
                false,
                ["deriveKey"]
            );

        // =====================================================
        // DERIVE AES KEY
        // =====================================================

        const encryptionKey =
            await crypto.subtle.deriveKey(
                {
                    name: "PBKDF2",
                    salt,
                    iterations:
                        encryptedData.iterations,
                    hash: "SHA-256",
                },
                keyMaterial,
                {
                    name: "AES-GCM",
                    length: 256,
                },
                false,
                ["decrypt"]
            );

        // =====================================================
        // DECRYPT
        // =====================================================

        const decrypted =
            await crypto.subtle.decrypt(
                {
                    name: "AES-GCM",
                    iv,
                },
                encryptionKey,
                encrypted
            );

        return new TextDecoder().decode(
            decrypted
        );
    }

    // =========================================================
    // UNLOCK PRIVATE KEY
    // =========================================================

    async function handleUnlockPrivateKey() {

        setError("");
        setSuccess("");

        // =====================================================
        // VALIDATE KEY CODE
        // =====================================================

        if (!/^\d{6}$/.test(publicKeyCode)) {

            setError(
                "Public Key Code must contain exactly 6 digits."
            );

            return;
        }

        // =====================================================
        // VALIDATE PIN
        // =====================================================

        if (!/^\d{6}$/.test(pin)) {

            setError(
                "PIN must contain exactly 6 digits."
            );

            return;
        }

        try {

            setUnlockingKey(true);

            // =================================================
            // FIND LOCAL STORAGE
            // =================================================

            const storageKey =
                `encryptedPrivateKey_${publicKeyCode}`;

            const encryptedRaw =
                localStorage.getItem(
                    storageKey
                );

            if (!encryptedRaw) {

                setPrivateKey("");
                setShowPrivateKey(false);

                setError(
                    `Encrypted private key for key code ${publicKeyCode} was not found on this browser.`
                );

                return;
            }

            // =================================================
            // PARSE
            // =================================================

            let encryptedData;

            try {

                encryptedData =
                    JSON.parse(
                        encryptedRaw
                    );

            } catch {

                setPrivateKey("");
                setShowPrivateKey(false);

                setError(
                    "The encrypted private key data is corrupted."
                );

                return;
            }

            // =================================================
            // DECRYPT
            // =================================================

            const decryptedPrivateKey =
                await decryptPrivateKey(
                    encryptedData,
                    pin
                );
            console.log("DECRYPTED PRIVATE KEY:", decryptedPrivateKey);
            console.log("PRIVATE KEY LENGTH:", decryptedPrivateKey?.length);
            if (!decryptedPrivateKey) {

                throw new Error(
                    "Private key is empty."
                );
            }

            // =================================================
            // STORE ONLY IN MEMORY
            // =================================================

            setPrivateKey(
                decryptedPrivateKey
            );

            setShowPrivateKey(false);

            setSuccess(
                "Private key unlocked successfully."
            );

        } catch (unlockError) {

            console.error(
                "UNLOCK PRIVATE KEY ERROR:",
                unlockError
            );

            setPrivateKey("");
            setShowPrivateKey(false);

            setError(
                "Invalid PIN or corrupted encrypted private key."
            );

        } finally {

            setUnlockingKey(false);
        }
    }

    // =========================================================
    // SHA-256
    // =========================================================

    async function calculateSha256(
        arrayBuffer
    ) {

        const hashBuffer =
            await crypto.subtle.digest(
                "SHA-256",
                arrayBuffer
            );

        return bufferToBase64(
            hashBuffer
        );
    }

    // =========================================================
    // IMPORT RSA PRIVATE KEY
    // =========================================================

    const base64ToArrayBuffer = (base64) => {
        const binaryString = window.atob(base64);
        const bytes = new Uint8Array(binaryString.length);

        for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }

        return bytes.buffer;
    };

    const importPrivateKey = async (privateKeyText) => {
        try {
            if (!privateKeyText || !privateKeyText.trim()) {
                throw new Error("Private key is empty.");
            }

            const base64Key = privateKeyText.trim();

            console.log("========== IMPORT PRIVATE KEY ==========");
            console.log("Private key length:", base64Key.length);
            console.log(
                "Private key preview:",
                base64Key.substring(0, 30)
            );

            const keyBuffer = base64ToArrayBuffer(base64Key);

            console.log(
                "Private key byte length:",
                keyBuffer.byteLength
            );

            const keyBytes = new Uint8Array(keyBuffer);

            console.log(
                "First 10 bytes:",
                Array.from(keyBytes.slice(0, 10))
            );

            console.log(
                "First 10 bytes HEX:",
                Array.from(keyBytes.slice(0, 10))
                    .map(b => b.toString(16).padStart(2, "0"))
                    .join(" ")
            );

            const cryptoKey = await window.crypto.subtle.importKey(
                "pkcs8",
                keyBuffer,
                {
                    name: "RSASSA-PKCS1-v1_5",
                    hash: "SHA-256"
                },
                false,
                ["sign"]
            );

            console.log("PRIVATE KEY IMPORT SUCCESS");

            return cryptoKey;

        } catch (error) {
            console.error("IMPORT PRIVATE KEY ERROR:", error);

            throw new Error(
                "Invalid RSA private key. The private key must be a valid PKCS#8 RSA key."
            );
        }
    };

    // =========================================================
    // SIGN PDF WITH PRIVATE KEY
    // =========================================================

    async function signPdf(
        privateKeyText,
        pdfArrayBuffer
    ) {

        const cryptoKey =
            await importPrivateKey(
                privateKeyText
            );

        /*
         * IMPORTANT
         *
         * We sign the RAW PDF bytes.
         *
         * Web Crypto will perform SHA-256
         * internally because the algorithm
         * is configured with:
         *
         * RSASSA-PKCS1-v1_5 + SHA-256
         *
         * Do NOT pass documentHash here.
         */

        const signatureBuffer =
            await crypto.subtle.sign(
                {
                    name:
                        "RSASSA-PKCS1-v1_5",
                },
                cryptoKey,
                pdfArrayBuffer
            );

        return bufferToBase64(
            signatureBuffer
        );
    }

    // =========================================================
    // HANDLE SIGN
    // =========================================================

    async function handleSign() {

        if (signing) {
            return;
        }

        setError("");
        setSuccess("");

        // =====================================================
        // VALIDATE SIGNATURE
        // =====================================================

        if (!selectedId) {

            setError(
                "Please select an electronic signature."
            );

            return;
        }

        // =====================================================
        // VALIDATE PDF
        // =====================================================

        if (!pdfBlob) {

            setError(
                "Contract PDF is not available."
            );

            return;
        }

        // =====================================================
        // VALIDATE KEY CODE
        // =====================================================

        if (!/^\d{6}$/.test(publicKeyCode)) {

            setError(
                "Public Key Code must contain exactly 6 digits."
            );

            return;
        }

        // =====================================================
        // VALIDATE PIN
        // =====================================================

        if (!/^\d{6}$/.test(pin)) {

            setError(
                "PIN must contain exactly 6 digits."
            );

            return;
        }

        // =====================================================
        // VALIDATE PRIVATE KEY
        // =====================================================

        if (!privateKey) {

            setError(
                "Please unlock your private key first."
            );

            return;
        }

        try {

            setSigning(true);

            // =================================================
            // PDF -> ARRAY BUFFER
            // =================================================

            const pdfArrayBuffer =
                await pdfBlob.arrayBuffer();

            // =================================================
            // CALCULATE DOCUMENT HASH
            // =================================================

            const documentHash =
                await calculateSha256(
                    pdfArrayBuffer
                );

            // =================================================
            // CREATE DIGITAL SIGNATURE
            // =================================================

            const signatureValue =
                await signPdf(
                    privateKey,
                    pdfArrayBuffer
                );

            // =================================================
            // REQUEST
            // =================================================

            const signRequest = {

                electronicSignatureId:
                    selectedId,

                keyCode:
                    publicKeyCode,

                documentHash:
                    documentHash,

                signatureValue:
                    signatureValue,

                signatureAlgorithm:
                    "RSA",

                hashAlgorithm:
                    "SHA-256",
            };

            // =================================================
            // DEBUG
            // =================================================
            // IMPORTANT:
            // Never log privateKey or PIN.

            console.log(
                "SIGN REQUEST:",
                signRequest
            );

            // =================================================
            // SEND TO BACKEND
            // =================================================

            await contractApi.signContract(
                id,
                signRequest
            );

            // =================================================
            // CLEAR PRIVATE KEY FROM MEMORY
            // =================================================

            setPrivateKey("");
            setPin("");
            setShowPrivateKey(false);

            setSuccess(
                "Contract signed successfully."
            );

            // =================================================
            // NAVIGATE
            // =================================================

            navigate(
                `/contract-management/list?viewContractId=${id}`,
                {
                    replace: true,
                }
            );

        } catch (signError) {

            console.error(
                "SIGN CONTRACT ERROR:",
                signError
            );

            setError(
                getApiErrorMessage(
                    signError,
                    "The contract could not be signed."
                )
            );

        } finally {

            setSigning(false);
        }
    }

    // =========================================================
    // RENDER
    // =========================================================

    return (
        <main className="contract-signing-page">

            {/* =================================================
                HEADER
            ================================================= */}

            <header className="contract-signing-header">

                <Button
                    variant="outline-secondary"
                    onClick={() =>
                        navigate(-1)
                    }
                    disabled={signing}
                >
                    <IconArrowLeft
                        size={18}
                    />

                    Back
                </Button>

                <div>

                    <h1>
                        Review and sign contract
                    </h1>

                    <p>
                        {contract?.contractNumber ||
                            "Contract"}

                        {" · "}

                        {contract?.contractTitle ||
                            ""}
                    </p>

                </div>

                <Button
                    onClick={handleSign}
                    disabled={
                        !selectedId ||
                        !publicKeyCode ||
                        !pin ||
                        !privateKey ||
                        signing ||
                        loading
                    }
                >

                    {signing ? (
                        <Spinner
                            animation="border"
                            size="sm"
                        />
                    ) : (
                        <IconCheck
                            size={18}
                        />
                    )}

                    {signing
                        ? "Signing..."
                        : "Sign contract"}

                </Button>

            </header>

            {/* =================================================
                ERROR
            ================================================= */}

            {error && (
                <Alert
                    variant="danger"
                    dismissible
                    onClose={() =>
                        setError("")
                    }
                >
                    {error}
                </Alert>
            )}

            {/* =================================================
                SUCCESS
            ================================================= */}

            {success && (
                <Alert
                    variant="success"
                    dismissible
                    onClose={() =>
                        setSuccess("")
                    }
                >
                    {success}
                </Alert>
            )}

            {/* =================================================
                LOADING
            ================================================= */}

            {loading ? (

                <div className="contract-signing-loading">

                    <Spinner animation="border" />

                    Preparing PDF and signatures...

                </div>

            ) : (

                <div className="contract-signing-workspace">

                    {/* =========================================
                        LEFT PANEL
                    ========================================== */}

                    <aside className="contract-signature-panel">

                        {/* =====================================
                            ELECTRONIC SIGNATURE
                        ====================================== */}

                        <div className="contract-signing-section-title">

                            <h2>
                                Your electronic signatures
                            </h2>

                            <p>
                                Select the signature
                                that will be linked
                                to this PDF.
                            </p>

                        </div>

                        {signatures.length === 0 ? (

                            <Alert variant="warning">

                                You have no active
                                signature. Create one
                                in Signature Management
                                first.

                            </Alert>

                        ) : (

                            signatures.map(
                                (signature) => (

                                    <label
                                        key={
                                            signature.id
                                        }
                                        className={`contract-signature-choice ${
    selectedId ===
    signature.id
        ? "selected"
        : ""
}`}
                                    >

                                        <input
                                            type="radio"
                                            name="electronicSignature"
                                            checked={
                                                selectedId ===
                                                signature.id
                                            }
                                            onChange={() =>
                                                setSelectedId(
                                                    signature.id
                                                )
                                            }
                                        />

                                        <div className="contract-signature-image-wrap">

                                            {signature.fileUrl ? (

                                                <img
                                                    src={
                                                        signature.fileUrl
                                                    }
                                                    alt={
                                                        signature.signatureName
                                                    }
                                                />

                                            ) : (

                                                <span>
                                                    Signature
                                                    image
                                                </span>

                                            )}

                                        </div>

                                        <strong>
                                            {
                                                signature.signatureName
                                            }
                                        </strong>

                                        <small>

                                            {
                                                signature.type
                                            }

                                            {(signature.default ||
                                                signature.isDefault) &&
                                                " · Default"}

                                        </small>

                                    </label>
                                )
                            )
                        )}

                        {selectedSignature && (

                            <div className="contract-signing-confirmation">

                                <IconCheck
                                    size={18}
                                />

                                Selected:{" "}
                                {
                                    selectedSignature.signatureName
                                }

                            </div>

                        )}

                        {/* =====================================
                            SIGNING KEY INFORMATION
                        ====================================== */}

                        <div className="contract-signing-key-info">

                            <strong>
                                Digital signing key
                            </strong>

                            <span>
                                RSA 2048-bit
                            </span>

                            <small>
                                Your encrypted private
                                key is stored locally
                                on this browser.
                            </small>

                            <small>
                                The PIN and private key
                                are never sent to the
                                backend.
                            </small>

                        </div>

                        {/* =====================================
                            PUBLIC KEY CODE
                        ====================================== */}

                        <Form.Group className="mb-3">

                            <Form.Label
                                htmlFor="public-key-code"
                            >
                                Public Key Code
                            </Form.Label>

                            <Form.Control
                                id="public-key-code"
                                type="text"
                                inputMode="numeric"
                                maxLength={6}
                                value={
                                    publicKeyCode
                                }
                                onChange={
                                    handlePublicKeyCodeChange
                                }
                                placeholder="Enter 6-digit key code"
                            />

                            <Form.Text>
                                Enter the 6-digit code
                                associated with your
                                signing key.
                            </Form.Text>

                        </Form.Group>

                        {/* =====================================
                            PIN
                        ====================================== */}

                        <Form.Group className="mb-3">

                            <Form.Label
                                htmlFor="signing-pin"
                            >
                                PIN
                            </Form.Label>

                            <div
                                style={{
                                    position:
                                        "relative",
                                }}
                            >

                                <Form.Control
                                    id="signing-pin"
                                    type={
                                        showPin
                                            ? "text"
                                            : "password"
                                    }
                                    inputMode="numeric"
                                    maxLength={6}
                                    value={pin}
                                    onChange={
                                        handlePinChange
                                    }
                                    placeholder="Enter 6-digit PIN"
                                    style={{
                                        paddingRight:
                                            "45px",
                                    }}
                                />

                                <Button
                                    variant="link"
                                    type="button"
                                    onClick={() =>
                                        setShowPin(
                                            !showPin
                                        )
                                    }
                                    style={{
                                        position:
                                            "absolute",
                                        right: 0,
                                        top: 0,
                                        height: "100%",
                                    }}
                                    aria-label={
                                        showPin
                                            ? "Hide PIN"
                                            : "Show PIN"
                                    }
                                >

                                    {showPin ? (
                                        <IconEyeOff
                                            size={18}
                                        />
                                    ) : (
                                        <IconEye
                                            size={18}
                                        />
                                    )}

                                </Button>

                            </div>

                            <Form.Text>
                                The PIN is used locally
                                to decrypt your private
                                key.
                            </Form.Text>

                        </Form.Group>

                        {/* =====================================
                            UNLOCK
                        ====================================== */}

                        <Button
                            variant="outline-primary"
                            className="w-100 mb-3"
                            onClick={
                                handleUnlockPrivateKey
                            }
                            disabled={
                                unlockingKey ||
                                !/^\d{6}$/.test(
                                    publicKeyCode
                                ) ||
                                !/^\d{6}$/.test(
                                    pin
                                )
                            }
                        >

                            {unlockingKey ? (

                                <Spinner
                                    animation="border"
                                    size="sm"
                                />

                            ) : (

                                <IconLock
                                    size={18}
                                />

                            )}

                            {unlockingKey
                                ? "Unlocking..."
                                : "Unlock Private Key"}

                        </Button>

                        {/* =====================================
                            PRIVATE KEY STATUS
                        ====================================== */}

                        <div className="contract-signing-private-key">

                            <Form.Label>
                                Private Key Status
                            </Form.Label>

                            <div
                                style={{
                                    display: "flex",
                                    alignItems:
                                        "center",
                                    gap: "8px",
                                }}
                            >

                                <Form.Control
                                    type="text"
                                    readOnly
                                    value={
                                        privateKey
                                            ? showPrivateKey
                                                ? privateKey
                                                : "Private key unlocked"
                                            : ""
                                    }
                                    placeholder={
                                        "Private key is encrypted locally"
                                    }
                                />

                                <Button
                                    variant="outline-secondary"
                                    type="button"
                                    onClick={() =>
                                        setShowPrivateKey(
                                            !showPrivateKey
                                        )
                                    }
                                    disabled={
                                        !privateKey
                                    }
                                >

                                    {showPrivateKey ? (
                                        <IconEyeOff
                                            size={18}
                                        />
                                    ) : (
                                        <IconEye
                                            size={18}
                                        />
                                    )}

                                </Button>

                            </div>

                            <Form.Text>
                                Private key is decrypted
                                only in browser memory
                                and is never uploaded.
                            </Form.Text>

                        </div>

                    </aside>

                    {/* =========================================
                        PDF PANEL
                    ========================================== */}

                    <section className="contract-pdf-panel">

                        <div className="contract-pdf-heading">

                            <IconFileTypePdf
                                size={20}
                            />

                            <strong>
                                Generated contract PDF
                            </strong>

                        </div>

                        {pdfUrl ? (

                            <iframe
                                title="Contract PDF preview"
                                src={pdfUrl}
                            />

                        ) : (

                            <Alert variant="danger">
                                PDF preview is unavailable.
                            </Alert>

                        )}

                    </section>

                </div>
            )}

        </main>
    );
}

// =============================================================
// READ API ERROR
// =============================================================

async function readApiError(
    error,
    fallbackMessage
) {

    const responseData =
        error?.response?.data;

    if (
        responseData instanceof Blob
    ) {

        try {

            const body =
                JSON.parse(
                    await responseData.text()
                );

            return (
                body?.message ||
                fallbackMessage
            );

        } catch {

            return fallbackMessage;
        }
    }

    return getApiErrorMessage(
        error,
        fallbackMessage
    );
}