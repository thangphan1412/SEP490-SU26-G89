import { useEffect, useRef, useMemo, useState } from "react";

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

import {
    getApiErrorMessage,
    unwrapApiResponse,
} from "./contractUtils.js";

import "../../assets/styles/css/layoutStyles/ContractSigning.css";

import { Document, Page, pdfjs } from "react-pdf";

pdfjs.GlobalWorkerOptions.workerSrc =
    `//unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

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
    const [pdfNumPages, setPdfNumPages] = useState(0);
    const [selectedPage, setSelectedPage] = useState(1);

    const [signaturePosition, setSignaturePosition] = useState({
        x: 50,
        y: 50,
        width: 180,
        height: 70,
    });

    const [draggingSignature, setDraggingSignature] = useState(false);

    const pdfContainerRef = useRef(null);
    const dragStartRef = useRef(null);
    function handlePdfLoadSuccess({ numPages }) {
        setPdfNumPages(numPages);

        if (selectedPage > numPages) {
            setSelectedPage(1);
        }
    }
    function handleSignatureMouseDown(event) {
        event.preventDefault();

        if (!pdfContainerRef.current) {
            return;
        }

        const rect =
            pdfContainerRef.current.getBoundingClientRect();

        dragStartRef.current = {
            mouseX: event.clientX,
            mouseY: event.clientY,
            signatureX: signaturePosition.x,
            signatureY: signaturePosition.y,
            containerWidth: rect.width,
            containerHeight: rect.height,
        };

        setDraggingSignature(true);
    }
    useEffect(() => {
        function handleMouseMove(event) {
            if (
                !draggingSignature ||
                !dragStartRef.current
            ) {
                return;
            }

            const start = dragStartRef.current;

            const deltaX =
                event.clientX - start.mouseX;

            const deltaY =
                event.clientY - start.mouseY;

            let newX =
                start.signatureX + deltaX;

            let newY =
                start.signatureY + deltaY;

            newX = Math.max(
                0,
                Math.min(
                    newX,
                    start.containerWidth -
                    signaturePosition.width
                )
            );

            newY = Math.max(
                0,
                Math.min(
                    newY,
                    start.containerHeight -
                    signaturePosition.height
                )
            );

            setSignaturePosition((previous) => ({
                ...previous,
                x: newX,
                y: newY,
            }));
        }

        function handleMouseUp() {
            setDraggingSignature(false);
            dragStartRef.current = null;
        }

        if (draggingSignature) {
            window.addEventListener(
                "mousemove",
                handleMouseMove
            );

            window.addEventListener(
                "mouseup",
                handleMouseUp
            );
        }

        return () => {
            window.removeEventListener(
                "mousemove",
                handleMouseMove
            );

            window.removeEventListener(
                "mouseup",
                handleMouseUp
            );
        };
    }, [
        draggingSignature,
        signaturePosition.width,
        signaturePosition.height,
    ]);
    // =========================================================
    // LOAD CONTRACT / SIGNATURES / PDF
    // =========================================================

    useEffect(() => {

        let active = true;
        let objectUrl = "";

        async function loadData() {

            try {

                const results =
                    await Promise.allSettled([
                        contractApi.getContractById(id),
                        electronicSignatureService
                            .getAllElectronicSignature(),
                        contractApi.exportContractPdf(id),
                    ]);

                if (!active) {
                    return;
                }

                const [
                    contractResult,
                    signatureResult,
                    pdfResult,
                ] = results;

                const errors = [];

                // =================================================
                // CONTRACT
                // =================================================

                if (
                    contractResult.status ===
                    "fulfilled"
                ) {

                    const contractData =
                        unwrapApiResponse(
                            contractResult.value
                        );

                    setContract(
                        contractData
                    );

                } else {

                    errors.push(
                        await readApiError(
                            contractResult.reason,
                            "Unable to load the contract."
                        )
                    );
                }

                // =================================================
                // ELECTRONIC SIGNATURES
                // =================================================

                if (
                    signatureResult.status ===
                    "fulfilled"
                ) {

                    const rows =
                        signatureResult.value
                            ?.data
                            ?.data || [];

                    const activeRows =
                        rows.filter(
                            (item) =>
                                item?.status ===
                                "ACTIVE"
                        );

                    setSignatures(
                        activeRows
                    );

                    const defaultSignature =
                        activeRows.find(
                            (item) =>
                                item?.default ||
                                item?.isDefault
                        );

                    setSelectedId(
                        defaultSignature?.id ||
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

                // =================================================
                // PDF
                // =================================================

                if (
                    pdfResult.status ===
                    "fulfilled"
                ) {

                    const pdfData =
                        pdfResult.value?.data;

                    if (!pdfData) {

                        throw new Error(
                            "Contract PDF is empty."
                        );
                    }

                    const blob =
                        pdfData instanceof Blob
                            ? pdfData
                            : new Blob(
                                [pdfData],
                                {
                                    type:
                                        "application/pdf",
                                }
                            );

                    setPdfBlob(
                        blob
                    );

                    objectUrl =
                        URL.createObjectURL(
                            blob
                        );

                    setPdfUrl(
                        objectUrl
                    );

                } else {

                    errors.push(
                        await readApiError(
                            pdfResult.reason,
                            "Unable to load the contract PDF."
                        )
                    );
                }

                // =================================================
                // ERRORS
                // =================================================

                if (
                    errors.length > 0
                ) {

                    setError(
                        [
                            ...new Set(
                                errors
                            ),
                        ].join(" ")
                    );
                }

            } catch (loadError) {

                if (!active) {
                    return;
                }

                setError(
                    getApiErrorMessage(
                        loadError,
                        "Unable to load contract signing data."
                    )
                );

            } finally {

                if (active) {
                    setLoading(false);
                }
            }
        }

        loadData();

        return () => {

            active = false;

            if (objectUrl) {
                URL.revokeObjectURL(
                    objectUrl
                );
            }
        };

    }, [id]);

    // =========================================================
    // SELECTED SIGNATURE
    // =========================================================

    const selectedSignature =
        useMemo(
            () =>
                signatures.find(
                    (item) =>
                        item?.id ===
                        selectedId
                ),
            [
                signatures,
                selectedId,
            ]
        );

    // =========================================================
    // PUBLIC KEY CODE INPUT
    // =========================================================

    function handlePublicKeyCodeChange(
        event
    ) {

        const value =
            event.target.value
                .replace(/\D/g, "");

        if (
            value.length <= 6
        ) {

            setPublicKeyCode(
                value
            );
        }

        // Key code changed.
        // The currently unlocked private key
        // must no longer be trusted.

        setPrivateKey("");
        setShowPrivateKey(false);

        setError("");
        setSuccess("");
    }

    // =========================================================
    // PIN INPUT
    // =========================================================

    function handlePinChange(
        event
    ) {

        const value =
            event.target.value
                .replace(/\D/g, "");

        if (
            value.length <= 6
        ) {

            setPin(
                value
            );
        }

        // PIN changed.
        // Clear the currently unlocked private key.

        setPrivateKey("");
        setShowPrivateKey(false);

        setError("");
        setSuccess("");
    }

    // =========================================================
    // ARRAY BUFFER -> BASE64
    // =========================================================

    function bufferToBase64(
        buffer
    ) {

        const bytes =
            new Uint8Array(
                buffer
            );

        let binary = "";

        const chunkSize =
            0x8000;

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

            binary +=
                String.fromCharCode(
                    ...chunk
                );
        }

        return window.btoa(
            binary
        );
    }

    // =========================================================
    // BASE64 -> ARRAY BUFFER
    // =========================================================

    function base64ToArrayBuffer(
        base64
    ) {

        if (
            !base64 ||
            typeof base64 !== "string"
        ) {

            throw new Error(
                "Invalid Base64 data."
            );
        }

        const normalized =
            base64
                .trim()
                .replace(/\s/g, "");

        if (!normalized) {

            throw new Error(
                "Base64 data is empty."
            );
        }

        const binaryString =
            window.atob(
                normalized
            );

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

        if (
            !encryptedData
        ) {

            throw new Error(
                "Encrypted private key data is missing."
            );
        }

        if (
            !encryptedData.salt ||
            !encryptedData.iv ||
            !encryptedData.encryptedPrivateKey
        ) {

            throw new Error(
                "Encrypted private key data is incomplete."
            );
        }

        if (
            !Number.isInteger(
                encryptedData.iterations
            ) ||
            encryptedData.iterations <= 0
        ) {

            throw new Error(
                "Invalid PBKDF2 iteration count."
            );
        }

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

        // AES-GCM normally uses a 12-byte IV.

        if (
            iv.byteLength !== 12
        ) {

            throw new Error(
                "Invalid AES-GCM IV."
            );
        }

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
                encoder.encode(
                    pinValue
                ),
                "PBKDF2",
                false,
                [
                    "deriveKey",
                ]
            );

        // =====================================================
        // DERIVE AES KEY
        // =====================================================

        const encryptionKey =
            await crypto.subtle.deriveKey(
                {
                    name:
                        "PBKDF2",
                    salt,
                    iterations:
                    encryptedData.iterations,
                    hash:
                        "SHA-256",
                },
                keyMaterial,
                {
                    name:
                        "AES-GCM",
                    length:
                        256,
                },
                false,
                [
                    "decrypt",
                ]
            );

        // =====================================================
        // DECRYPT
        // =====================================================

        const decrypted =
            await crypto.subtle.decrypt(
                {
                    name:
                        "AES-GCM",
                    iv,
                },
                encryptionKey,
                encrypted
            );

        const privateKeyText =
            new TextDecoder()
                .decode(
                    decrypted
                )
                .trim();

        if (
            !privateKeyText
        ) {

            throw new Error(
                "Decrypted private key is empty."
            );
        }

        return privateKeyText;
    }

    // =========================================================
    // UNLOCK PRIVATE KEY
    // =========================================================

    async function handleUnlockPrivateKey() {

        if (
            unlockingKey
        ) {
            return;
        }

        setError("");
        setSuccess("");

        // =====================================================
        // VALIDATE KEY CODE
        // =====================================================

        if (
            !/^\d{6}$/.test(
                publicKeyCode
            )
        ) {

            setPrivateKey("");

            setError(
                "Public Key Code must contain exactly 6 digits."
            );

            return;
        }

        // =====================================================
        // VALIDATE PIN
        // =====================================================

        if (
            !/^\d{6}$/.test(
                pin
            )
        ) {

            setPrivateKey("");

            setError(
                "PIN must contain exactly 6 digits."
            );

            return;
        }

        try {

            setUnlockingKey(
                true
            );

            // =================================================
            // FIND LOCAL STORAGE
            // =================================================

            const storageKey =
                `encryptedPrivateKey_${publicKeyCode}`;

            const encryptedRaw =
                localStorage.getItem(
                    storageKey
                );

            if (
                !encryptedRaw
            ) {

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

            // =================================================
            // VALIDATE PKCS#8 IMMEDIATELY
            // =================================================

            await importPrivateKey(
                decryptedPrivateKey
            );

            // =================================================
            // STORE ONLY IN MEMORY
            // =================================================

            setPrivateKey(
                decryptedPrivateKey
            );

            setShowPrivateKey(
                false
            );

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

            setUnlockingKey(
                false
            );
        }
    }

    // =========================================================
    // IMPORT RSA PRIVATE KEY
    // =========================================================

    async function importPrivateKey(
        privateKeyText
    ) {

        try {

            if (
                !privateKeyText ||
                !privateKeyText.trim()
            ) {

                throw new Error(
                    "Private key is empty."
                );
            }

            const base64Key =
                privateKeyText
                    .trim();

            const keyBuffer =
                base64ToArrayBuffer(
                    base64Key
                );

            if (
                keyBuffer.byteLength === 0
            ) {

                throw new Error(
                    "Private key has no data."
                );
            }

            // =================================================
            // IMPORT PKCS#8 RSA PRIVATE KEY
            // =================================================

            const cryptoKey =
                await window.crypto.subtle.importKey(
                    "pkcs8",
                    keyBuffer,
                    {
                        name:
                            "RSASSA-PKCS1-v1_5",
                        hash:
                            "SHA-256",
                    },
                    false,
                    [
                        "sign",
                    ]
                );

            return cryptoKey;

        } catch (error) {

            console.error(
                "IMPORT PRIVATE KEY ERROR:",
                error
            );

            throw new Error(
                "Invalid RSA private key. The private key must be a valid PKCS#8 RSA key."
            );
        }
    }

    // =========================================================
    // SIGN BACKEND CONTENT WITH PRIVATE KEY
    // =========================================================

    async function signContent(
        privateKeyText,
        contentToSignBase64
    ) {

        if (
            !privateKeyText
        ) {

            throw new Error(
                "Private key is not available."
            );
        }

        if (
            !contentToSignBase64
        ) {

            throw new Error(
                "Signing content is empty."
            );
        }

        // =====================================================
        // IMPORT PRIVATE KEY
        // =====================================================

        const cryptoKey =
            await importPrivateKey(
                privateKeyText
            );

        // =====================================================
        // BASE64 -> ORIGINAL CONTENT
        // =====================================================

        const contentToSign =
            base64ToArrayBuffer(
                contentToSignBase64
            );

        if (
            contentToSign.byteLength === 0
        ) {

            throw new Error(
                "Signing content contains no data."
            );
        }

        // =====================================================
        // RSA SHA-256 SIGNATURE
        // =====================================================

        const signatureBuffer =
            await crypto.subtle.sign(
                {
                    name:
                        "RSASSA-PKCS1-v1_5",
                },
                cryptoKey,
                contentToSign
            );

        if (
            signatureBuffer.byteLength === 0
        ) {

            throw new Error(
                "Browser generated an empty signature."
            );
        }

        // =====================================================
        // SIGNATURE -> BASE64
        // =====================================================

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

        if (!selectedId) {
            setError("Please select an electronic signature.");
            return;
        }

        if (!pdfBlob) {
            setError("Contract PDF is not available.");
            return;
        }

        if (!/^\d{6}$/.test(publicKeyCode)) {
            setError("Public Key Code must contain exactly 6 digits.");
            return;
        }

        if (!/^\d{6}$/.test(pin)) {
            setError("PIN must contain exactly 6 digits.");
            return;
        }

        if (!privateKey) {
            setError("Please unlock your private key first.");
            return;
        }

        try {
            setSigning(true);

            // STEP 1: PREPARE PADES
            console.log("========== PADES PREPARE ==========");

            const prepareResponse =
                await contractApi.preparePadesSigning(
                    id,
                    selectedId,
                    publicKeyCode,
                    pdfBlob,
                    selectedPage,
                    signaturePosition.x,
                    signaturePosition.y,
                    signaturePosition.width,
                    signaturePosition.height
                );

            const prepareData = unwrapApiResponse(prepareResponse);

            if (!prepareData) {
                throw new Error("PAdES prepare response is empty.");
            }

            const { sessionId, documentHash, contentToSign, keyCode } = prepareData;

            if (!sessionId) {
                throw new Error("Backend did not return a PAdES signing session.");
            }
            if (!documentHash) {
                throw new Error("Backend did not return the document hash.");
            }
            if (!contentToSign) {
                throw new Error("Backend did not return signing content.");
            }
            if (keyCode !== publicKeyCode) {
                throw new Error("The signing key does not match the prepared signing session.");
            }

            // STEP 2: SIGN CONTENT IN BROWSER
            console.log("========== PADES BROWSER SIGN ==========");

            const signatureValue = await signContent(privateKey, contentToSign);

            if (!signatureValue) {
                throw new Error("Browser failed to generate the digital signature.");
            }

            console.log("Browser signature generated successfully.");

            // STEP 3: COMPLETE PADES
            console.log("========== PADES COMPLETE ==========");
            console.log("sessionId:", sessionId);
            console.log("signatureValue length:", signatureValue?.length);

            let completeResponse;

            try {
                completeResponse =
                    await contractApi.completePadesSigning(
                        id,
                        sessionId,
                        signatureValue
                    );

                console.log("========== PADES COMPLETE RESPONSE ==========");
                console.log("status:", completeResponse?.status);
                console.log("data:", completeResponse?.data);

            } catch (error) {
                console.error("========== PADES COMPLETE ERROR ==========");
                console.error("status:", error?.response?.status);
                console.error("data:", error?.response?.data);
                console.error("message:", error?.message);
                throw error;
            }

            const signedPdfBase64 = unwrapApiResponse(completeResponse);

            if (!signedPdfBase64 || typeof signedPdfBase64 !== "string") {
                throw new Error("Backend did not return the signed PDF.");
            }

            // STEP 4: BASE64 -> PDF BYTES
            const signedPdfBytes = base64ToArrayBuffer(signedPdfBase64);

            if (signedPdfBytes.byteLength === 0) {
                throw new Error("Signed PDF is empty.");
            }

            // STEP 5: CREATE SIGNED PDF BLOB
            const signedPdfBlob = new Blob([signedPdfBytes], { type: "application/pdf" });

            // STEP 6: UPDATE PDF PREVIEW
            const newPdfUrl = URL.createObjectURL(signedPdfBlob);
            setPdfBlob(signedPdfBlob);
            setPdfUrl(newPdfUrl);

            // STEP 7: CLEAR PRIVATE KEY
            setPrivateKey("");
            setPin("");
            setShowPrivateKey(false);

            // SUCCESS
            setSuccess("Contract signed successfully.");

            // STEP 8: NAVIGATE
            // STEP 8: SAVE BUSINESS SIGNATURE
            await contractApi.signContract(
                id,
                selectedId,
                signatureValue,
                publicKeyCode
            );

        // SUCCESS
            setSuccess("Contract signed successfully.");

            // STEP 9: NAVIGATE
            navigate(`/contract-management/list?viewContractId=${id}`, { replace: true });

        } catch (signError) {
            console.error("PADES SIGNING ERROR:", signError);
            setError(getApiErrorMessage(signError, "The contract could not be signed."));
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
                    disabled={
                        signing
                    }
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
                        {
                            contract?.contractNumber ||
                            "Contract"
                        }

                        {" · "}

                        {
                            contract?.contractTitle ||
                            ""
                        }
                    </p>

                </div>

                <Button
                    onClick={
                        handleSign
                    }
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

                    {
                        signing
                            ? "Signing..."
                            : "Sign contract"
                    }

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

                    <Spinner
                        animation="border"
                    />

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

                            <Alert
                                variant="warning"
                            >

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
                                        className={
                                            `contract-signature-choice ${
                                                selectedId ===
                                                signature.id
                                                    ? "selected"
                                                    : ""
                                            }`
                                        }
                                    >

                                        <input
                                            type="radio"
                                            name="electronicSignature"
                                            checked={
                                                selectedId ===
                                                signature.id
                                            }
                                            onChange={() => {

                                                setSelectedId(
                                                    signature.id
                                                );

                                                setError("");
                                                setSuccess("");

                                                // A different
                                                // electronic
                                                // signature may
                                                // use another key.

                                                setPrivateKey("");
                                                setShowPrivateKey(
                                                    false
                                                );
                                            }}
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

                        <Form.Group
                            className="mb-3"
                        >

                            <Form.Label
                                htmlFor="public-key-code"
                            >
                                Public Key Code
                            </Form.Label>

                            <Form.Control
                                id="public-key-code"
                                type="text"
                                inputMode="numeric"
                                autoComplete="off"
                                maxLength={6}
                                value={
                                    publicKeyCode
                                }
                                onChange={
                                    handlePublicKeyCodeChange
                                }
                                placeholder="Enter 6-digit key code"
                                disabled={
                                    unlockingKey ||
                                    signing
                                }
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

                        <Form.Group
                            className="mb-3"
                        >

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
                                    autoComplete="off"
                                    maxLength={6}
                                    value={
                                        pin
                                    }
                                    onChange={
                                        handlePinChange
                                    }
                                    placeholder="Enter 6-digit PIN"
                                    disabled={
                                        unlockingKey ||
                                        signing
                                    }
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
                                    disabled={
                                        unlockingKey ||
                                        signing
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
                                signing ||
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

                            {
                                unlockingKey
                                    ? "Unlocking..."
                                    : "Unlock Private Key"
                            }

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
                                    display:
                                        "flex",
                                    alignItems:
                                        "center",
                                    gap:
                                        "8px",
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
                                        !privateKey ||
                                        signing
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
                            {pdfNumPages > 0 && (
                                <>
                                    <div className="pdf-page-selector">
                                        <Form.Label>
                                            Signature page
                                        </Form.Label>

                                        <Form.Select
                                            value={selectedPage}
                                            onChange={(event) => {
                                                setSelectedPage(
                                                    Number(event.target.value)
                                                );

                                                setSignaturePosition({
                                                    x: 50,
                                                    y: 50,
                                                    width: 180,
                                                    height: 70,
                                                });
                                            }}
                                            disabled={signing}
                                        >
                                            {Array.from(
                                                { length: pdfNumPages },
                                                (_, index) => (
                                                    <option
                                                        key={index + 1}
                                                        value={index + 1}
                                                    >
                                                        Page {index + 1}
                                                    </option>
                                                )
                                            )}
                                        </Form.Select>
                                    </div>

                                    <div className="signature-position-info">
                                        <strong>
                                            Signature position
                                        </strong>

                                        <div>
                                            Page: {selectedPage}
                                        </div>

                                        <div>
                                            X: {Math.round(signaturePosition.x)}
                                        </div>

                                        <div>
                                            Y: {Math.round(signaturePosition.y)}
                                        </div>

                                        <div>
                                            Width: {Math.round(signaturePosition.width)}
                                        </div>

                                        <div>
                                            Height: {Math.round(signaturePosition.height)}
                                        </div>
                                    </div>
                                </>
                            )}
                        </div>

                        {pdfUrl ? (

                            <div className="pdf-signing-viewer">

                                <Document
                                    file={pdfUrl}
                                    onLoadSuccess={handlePdfLoadSuccess}
                                    onLoadError={(error) => {
                                        console.error(
                                            "PDF LOAD ERROR:",
                                            error
                                        );

                                        setError(
                                            "Unable to render the contract PDF."
                                        );
                                    }}
                                    loading={
                                        <div className="pdf-loading">
                                            Loading PDF...
                                        </div>
                                    }
                                >

                                    {Array.from(
                                        { length: pdfNumPages },
                                        (_, index) => {

                                            const pageNumber =
                                                index + 1;

                                            return (
                                                <div
                                                    key={pageNumber}
                                                    ref={
                                                        pageNumber === selectedPage
                                                            ? pdfContainerRef
                                                            : null
                                                    }
                                                    className="pdf-page-wrapper"
                                                    style={{
                                                        position: "relative",
                                                        width: "fit-content",
                                                        margin: "0 auto 24px",
                                                    }}
                                                >

                                                    <Page
                                                        pageNumber={
                                                            pageNumber
                                                        }
                                                        width={750}
                                                        renderTextLayer={false}
                                                        renderAnnotationLayer={false}
                                                    />

                                                    {pageNumber ===
                                                        selectedPage && (
                                                            <div
                                                                className="signature-overlay"
                                                                style={{
                                                                    position:
                                                                        "absolute",

                                                                    left:
                                                                    signaturePosition.x,

                                                                    top:
                                                                    signaturePosition.y,

                                                                    width:
                                                                    signaturePosition.width,

                                                                    height:
                                                                    signaturePosition.height,

                                                                    cursor:
                                                                        draggingSignature
                                                                            ? "grabbing"
                                                                            : "grab",
                                                                }}
                                                                onMouseDown={
                                                                    handleSignatureMouseDown
                                                                }
                                                            >

                                                                {selectedSignature?.fileUrl ? (

                                                                    <img
                                                                        src={
                                                                            selectedSignature.fileUrl
                                                                        }
                                                                        alt={
                                                                            selectedSignature.signatureName
                                                                        }
                                                                        draggable={
                                                                            false
                                                                        }
                                                                    />

                                                                ) : (

                                                                    <div>
                                                                        ✍
                                                                        <br />
                                                                        Signature
                                                                    </div>

                                                                )}

                                                                <span className="signature-overlay-label">
                                            Drag to position
                                        </span>

                                                            </div>
                                                        )}

                                                </div>
                                            );
                                        }
                                    )}

                                </Document>

                            </div>

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

    // =========================================================
    // AXIOS RESPONSE IS BLOB
    // =========================================================

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
                body?.error ||
                fallbackMessage
            );

        } catch {

            return fallbackMessage;
        }
    }

    // =========================================================
    // NORMAL AXIOS ERROR
    // =========================================================

    return getApiErrorMessage(
        error,
        fallbackMessage
    );
}