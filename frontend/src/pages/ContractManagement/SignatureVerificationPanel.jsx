import { useEffect, useRef, useState } from "react";
import { Alert, Button, Spinner } from "react-bootstrap";
import contractApi from "../../services/contractService/contractApi.js";
import { getApiErrorMessage, unwrapApiResponse } from "./contractUtils.js";

export default function SignatureVerificationPanel({ contractId }) {
    const [report, setReport] = useState(null);
    const [verifying, setVerifying] = useState(false);
    const [error, setError] = useState("");
    const requestVersion = useRef(0);

    useEffect(() => () => { requestVersion.current += 1; }, []);

    const verify = async () => {
        const request = ++requestVersion.current;
        setVerifying(true);
        setReport(null);
        setError("");
        try {
            const response = await contractApi.verifyStoredSignatures(contractId);
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
                Verify the stored PDF using each signer's registered public key.
                No private key is needed. This checks the PDF, not the text preview below.
            </p>
            <Button onClick={verify} disabled={verifying}>
                {verifying && <Spinner animation="border" size="sm" className="me-2" />}
                {verifying ? "Verifying..." : "Verify signatures"}
            </Button>
            <div aria-live="polite" aria-busy={verifying} className="mt-3">
                {error && <Alert variant="danger">{error}</Alert>}
                {report && (
                    <>
                        <Alert variant={report.verified ? "success" : "warning"}>
                            <strong>{report.verified ? "Verified. " : "Not verified. "}</strong>
                            {report.message}
                        </Alert>
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
