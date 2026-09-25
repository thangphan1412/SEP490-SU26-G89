import { useEffect, useRef, useState } from "react";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import contractApi from "../../services/contractService/contractApi.js";
import { getApiErrorMessage, unwrapApiResponse } from "./contractUtils.js";

export default function SignatureVerificationPanel({ contractId }) {
    const [report, setReport] = useState(null);
    const [verifying, setVerifying] = useState(false);
    const [error, setError] = useState("");
    const [publicKeyCode, setPublicKeyCode] = useState("");
    const requestVersion = useRef(0);

    useEffect(() => {
        requestVersion.current += 1;
        setPublicKeyCode("");
        setReport(null);
        setError("");
        setVerifying(false);
        return () => { requestVersion.current += 1; };
    }, [contractId]);

    const verify = async () => {
        if (!/^[0-9]{6}$/.test(publicKeyCode.trim())) {
            setReport(null);
            setError("Enter the other signer's 6-digit public key code.");
            return;
        }
        const request = ++requestVersion.current;
        setVerifying(true);
        setReport(null);
        setError("");
        try {
            const response = await contractApi.verifyStoredSignatures(contractId, publicKeyCode.trim());
            if (request === requestVersion.current) setReport(unwrapApiResponse(response));
        } catch (exception) {
            if (request === requestVersion.current) {
                setError(getApiErrorMessage(exception, "Unable to verify the stored PDF. Please try again."));
            }
        } finally {
            if (request === requestVersion.current) setVerifying(false);
        }
    };

    return (
        <section className="contract-content-preview">
            <h3>Signature verification</h3>
            <p>
                Partners must enter the issuing CEO's public key code.
                The issuing CEO can enter any partner's signing key code.
                No private key is needed.
            </p>
            <Form.Group controlId="verification-public-key-code" className="mb-3">
                <Form.Label>Other signer's public key code</Form.Label>
                <Form.Control
                    type="text"
                    inputMode="numeric"
                    placeholder="Enter the 6-digit public key code"
                    value={publicKeyCode}
                    disabled={verifying}
                    onChange={(event) => {
                        setPublicKeyCode(event.target.value);
                        setReport(null);
                        setError("");
                    }}
                />
                <Form.Text>Use the code of the key that signed this contract. Partners cannot use another partner's code.</Form.Text>
            </Form.Group>
            <Button onClick={verify} disabled={verifying || !publicKeyCode.trim()}>
                {verifying && <Spinner animation="border" size="sm" className="me-2" />}
                {verifying ? "Verifying..." : "Verify signatures"}
            </Button>
            <div aria-live="polite" aria-busy={verifying} className="mt-3">
                {error && <Alert variant="danger">{error}</Alert>}
                {report && (
                    <>
                        {report.selectedSignature && (
                            <Alert variant={report.selectedSignature.valid ? "success" : "danger"}>
                                Selected signer: {report.selectedSignature.signerName}.
                                {" "}{report.selectedSignature.valid ? "Signature valid." : "Signature invalid."}
                            </Alert>
                        )}
                        <Alert variant={report.verified ? "success" : "warning"}>
                            <strong>PDF hash and signatures: {report.verified ? "Verified. " : "Not verified. "}</strong>
                            {report.message}
                        </Alert>
                        {report.revisionContentReviewRequired && (
                            <Alert variant="warning">
                                Content changes between signing revisions have not been verified.
                                Valid signatures do not by themselves confirm that the current content
                                is unchanged from the CEO's signed revision.
                            </Alert>
                        )}
                        {report.signatures.map((signature) => (
                            <article key={signature.signatureId} className="border-top py-3">
                                <strong>
                                    {signature.signerName}
                                    {signature.ownSignature ? " (You)" : " (Other signer)"}
                                </strong>
                                <p className={signature.valid ? "text-success" : "text-danger"}>
                                    {signature.message}
                                </p>
                                {signature.publicKeyFingerprint && (
                                    <small style={{ overflowWrap: "anywhere" }}>
                                        Public key fingerprint (SHA-256): {signature.publicKeyFingerprint}
                                    </small>
                                )}
                            </article>
                        ))}
                    </>
                )}
            </div>
        </section>
    );
}
