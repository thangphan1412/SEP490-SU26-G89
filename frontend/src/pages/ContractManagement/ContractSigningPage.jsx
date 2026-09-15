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
import digitalSignatureService from "../../services/signatureService/digitalSignatureService.js";
import { getApiErrorMessage, unwrapApiResponse } from "./contractUtils.js";

import "../../assets/styles/css/layoutStyles/ContractSigning.css";

export default function ContractSigningPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [contract, setContract] = useState(null);
    // =========================
    // ELECTRONIC SIGNATURE
    // =========================
    const [signatures, setSignatures] = useState([]);
    const [selectedId, setSelectedId] = useState("");
    // =========================
    // PDF
    // =========================
    const [pdfUrl, setPdfUrl] = useState("");
    const [pdfBlob, setPdfBlob] = useState(null);

    // =========================
    // KEY INFORMATION
    // =========================
    const [keyInfo, setKeyInfo] = useState(null);

    // =========================
    // SIGNING INPUT
    // =========================
    const [publicKeyCode, setPublicKeyCode] = useState("");
    const [pin, setPin] = useState("");

    const [privateKey, setPrivateKey] = useState("");

    // =========================
    // UI STATE
    // =========================
    const [showPin, setShowPin] = useState(false);
    const [showPrivateKey, setShowPrivateKey] = useState(false);

    const [loading, setLoading] = useState(true);
    const [unlockingKey, setUnlockingKey] = useState(false);
    const [signing, setSigning] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // =========================================================
    // LOAD DATA
    // =========================================================

    useEffect(() => {
        let active = true;
        let objectUrl = "";

        Promise.allSettled([
            contractApi.getContractById(id),
            electronicSignatureService.getAllElectronicSignature(),
            contractApi.exportContractPdf(id),
            digitalSignatureService.getMyPublicKey(),
        ])
            .then(async ([
                             contractResult,
                             signatureResult,
                             pdfResult,
                             keyResult,
                         ]) => {
                if (!active) return;

                const errors = [];

                // =========================
                // CONTRACT
                // =========================
                if (contractResult.status === "fulfilled") {
                    setContract(
                        unwrapApiResponse(contractResult.value)
                    );
                } else {
                    errors.push(
                        await readApiError(
                            contractResult.reason,
                            "Unable to load the contract."
                        )
                    );
                }

                // =========================
                // ELECTRONIC SIGNATURES
                // =========================
                if (signatureResult.status === "fulfilled") {
                    const rows =
                        signatureResult.value?.data?.data || [];

                    const activeRows = rows.filter(
                        (item) => item.status === "ACTIVE"
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

                // =========================
                // PDF
                // =========================
                if (pdfResult.status === "fulfilled") {
                    const pdfData = pdfResult.value.data;

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

                // =========================
                // KEY INFORMATION
                // =========================
                if (keyResult.status === "fulfilled") {
                    setKeyInfo(
                        unwrapApiResponse(
                            keyResult.value
                        )
                    );
                } else {
                    errors.push(
                        await readApiError(
                            keyResult.reason,
                            "Unable to load the digital signing key."
                        )
                    );
                }

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
    // SELECTED ELECTRONIC SIGNATURE
    // =========================================================

    const selectedSignature = useMemo(
        () =>
            signatures.find(
                (item) => item.id === selectedId
            ),
        [signatures, selectedId]
    );

    // =========================================================
    // INPUT: PUBLIC KEY CODE
    // =========================================================

    function handlePublicKeyCodeChange(event) {
        const value =
            event.target.value.replace(/\D/g, "");

        if (value.length <= 6) {
            setPublicKeyCode(value);
        }

        setError("");
        setSuccess("");
    }

    // =========================================================
    // INPUT: PIN
    // =========================================================

    function handlePinChange(event) {
        const value =
            event.target.value.replace(/\D/g, "");

        if (value.length <= 6) {
            setPin(value);
        }

        setError("");
        setSuccess("");
    }

    // =========================================================
    // BASE64
    // =========================================================

    function base64ToArrayBuffer(base64) {
        const binaryString =
            window.atob(base64);

        const bytes =
            new Uint8Array(
                binaryString.length
            );

        for (
            let i = 0;
            i < binaryString.length;
            i++
        ) {
            bytes[i] =
                binaryString.charCodeAt(i);
        }

        return bytes.buffer;
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

        const salt =
            new Uint8Array(
                base64ToArrayBuffer(
                    encryptedData.salt
                )
            );

        const iv =
            new Uint8Array(
                base64ToArrayBuffer(
                    encryptedData.iv
                )
            );

        const encrypted =
            base64ToArrayBuffer(
                encryptedData.encryptedPrivateKey
            );

        // PIN
        const keyMaterial =
            await crypto.subtle.importKey(
                "raw",
                encoder.encode(pinValue),
                "PBKDF2",
                false,
                ["deriveKey"]
            );

        // PBKDF2 -> AES-GCM key
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

        // AES-GCM decrypt
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

        if (!/^\d{6}$/.test(pin)) {
            setError(
                "PIN must contain exactly 6 digits."
            );
            return;
        }

        if (!/^\d{6}$/.test(publicKeyCode)) {
            setError(
                "Public Key Code must contain exactly 6 digits."
            );
            return;
        }

        try {
            setUnlockingKey(true);

            const storageKey =
                `encryptedPrivateKey_${publicKeyCode}`;

            const encryptedRaw =
                localStorage.getItem(
                    storageKey
                );

            if (!encryptedRaw) {
                setError(
                    `Encrypted private key for key code ${publicKeyCode} was not found on this browser.`
                );

                setPrivateKey("");
                setShowPrivateKey(false);

                return;
            }

            let encryptedData;

            try {
                encryptedData =
                    JSON.parse(encryptedRaw);
            } catch {
                setError(
                    "The encrypted private key data is corrupted."
                );

                setPrivateKey("");
                setShowPrivateKey(false);

                return;
            }

            const decryptedPrivateKey =
                await decryptPrivateKey(
                    encryptedData,
                    pin
                );

            if (!decryptedPrivateKey) {
                throw new Error(
                    "Private key is empty."
                );
            }

            setPrivateKey(
                decryptedPrivateKey
            );

            setShowPrivateKey(true);

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
    // IMPORT RSA PRIVATE KEY
    // =========================================================

    async function importPrivateKey(
        privateKeyText
    ) {
        let jwk;

        try {
            jwk =
                JSON.parse(
                    privateKeyText
                );
        } catch {
            throw new Error(
                "Private key format is invalid. The decrypted private key must be a JWK JSON."
            );
        }

        if (
            !jwk ||
            jwk.kty !== "RSA" ||
            !jwk.d ||
            !jwk.n ||
            !jwk.e
        ) {
            throw new Error(
                "Invalid RSA private key."
            );
        }

        return crypto.subtle.importKey(
            "jwk",
            jwk,
            {
                name:
                    "RSASSA-PKCS1-v1_5",
                hash: "SHA-256",
            },
            false,
            ["sign"]
        );
    }

    // =========================================================
    // SIGN PDF
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
         * IMPORTANT:
         *
         * Do NOT hash the PDF first and then
         * pass the hash into crypto.subtle.sign().
         *
         * Web Crypto with SHA-256 already performs
         * SHA-256 internally for RSASSA-PKCS1-v1_5.
         *
         * Therefore:
         *
         * sign(raw PDF bytes)
         *
         * and separately:
         *
         * SHA-256(raw PDF bytes)
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

        // =========================
        // VALIDATION
        // =========================

        if (!selectedId) {
            setError(
                "Please select an electronic signature."
            );
            return;
        }

        if (!pdfBlob) {
            setError(
                "Contract PDF is not available."
            );
            return;
        }

        if (!/^\d{6}$/.test(publicKeyCode)) {
            setError(
                "Public Key Code must contain exactly 6 digits."
            );
            return;
        }

        if (!/^\d{6}$/.test(pin)) {
            setError(
                "PIN must contain exactly 6 digits."
            );
            return;
        }

        if (!privateKey) {
            setError(
                "Please unlock your private key first."
            );
            return;
        }

        try {
            setSigning(true);

            // =========================
            // PDF -> ARRAY BUFFER
            // =========================

            const pdfArrayBuffer =
                await pdfBlob.arrayBuffer();

            // =========================
            // SHA-256 DOCUMENT HASH
            // =========================

            const documentHash =
                await calculateSha256(
                    pdfArrayBuffer
                );

            // =========================
            // RSA SIGN
            // =========================

            const signatureValue =
                await signPdf(
                    privateKey,
                    pdfArrayBuffer
                );

            // =========================
            // REQUEST
            // =========================

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

            console.log(
                "SIGN REQUEST:",
                {
                    ...signRequest,
                    // NEVER log privateKey
                    // NEVER log PIN
                }
            );

            // =========================
            // SEND TO BACKEND
            // =========================

            await contractApi.signContract(
                id,
                signRequest
            );

            // =========================
            // CLEAR PRIVATE KEY
            // =========================

            setPrivateKey("");
            setPin("");
            setShowPrivateKey(false);

            setSuccess(
                "Contract signed successfully."
            );

            // =========================
            // NAVIGATE
            // =========================

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

            {/* =========================================
                HEADER
            ========================================== */}

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

            {/* =========================================
                ERROR
            ========================================== */}

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

            {/* =========================================
                SUCCESS
            ========================================== */}

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

            {/* =========================================
                LOADING
            ========================================== */}

            {loading ? (
                <div className="contract-signing-loading">
                    <Spinner animation="border" />

                    Preparing PDF and signatures...
                </div>
            ) : (
                <div className="contract-signing-workspace">

                    {/* =====================================
                        LEFT PANEL
                    ====================================== */}

                    <aside className="contract-signature-panel">

                        {/* ================================
                            ELECTRONIC SIGNATURE
                        ================================= */}

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

                        {/* =================================
                            DIGITAL SIGNING KEY
                        ================================== */}

                        <div className="contract-signing-key-info">

                            <strong>
                                Digital signing key
                            </strong>

                            <span>
                                {keyInfo?.available
                                    ? `${keyInfo.algorithm} · ${keyInfo.keySize} bit`
                                    : "Signing key information unavailable"}
                            </span>

                            {keyInfo?.publicKeyFingerprint && (
                                <small>
                                    Fingerprint:{" "}
                                    {
                                        keyInfo.publicKeyFingerprint
                                    }
                                </small>
                            )}

                            <small>
                                Your private key is
                                encrypted and stored
                                locally on this browser.
                                It is never sent to the
                                server.
                            </small>

                        </div>

                        {/* =================================
                            PUBLIC KEY CODE
                        ================================== */}

                        <div className="contract-signing-public-key">

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

                        </div>

                        {/* =================================
                            PIN
                        ================================== */}

                        <div className="contract-signing-pin">

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
                                    Your PIN is used only
                                    in the browser to
                                    decrypt your private
                                    key.
                                </Form.Text>

                            </Form.Group>

                        </div>

                        {/* =================================
                            UNLOCK BUTTON
                        ================================== */}

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

                        {/* =================================
                            PRIVATE KEY STATUS
                        ================================== */}

                        <div className="contract-signing-private-key">

                            <Form.Label>
                                Private Key
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
                                    type={
                                        showPrivateKey
                                            ? "text"
                                            : "password"
                                    }
                                    value={
                                        privateKey
                                            ? privateKey
                                            : ""
                                    }
                                    readOnly
                                    placeholder={
                                        "Private key will be unlocked locally"
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
                                The private key is
                                decrypted only in your
                                browser and is never
                                uploaded to the backend.
                            </Form.Text>

                        </div>

                    </aside>

                    {/* =====================================
                        PDF
                    ====================================== */}

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

// =========================================================
// READ API ERROR
// =========================================================

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