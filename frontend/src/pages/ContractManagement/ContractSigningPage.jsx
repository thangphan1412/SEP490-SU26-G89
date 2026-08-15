import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Spinner } from "react-bootstrap";
import { IconArrowLeft, IconCheck, IconFileTypePdf } from "@tabler/icons-react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import contractApi from "../../services/contractService/contractApi.js";
import electronicSignatureService from "../../services/signatureService/electronicSignatureService.js";
import { getApiErrorMessage, unwrapApiResponse } from "./contractUtils.js";
import "../../assets/styles/css/layoutStyles/ContractSigning.css";

export default function ContractSigningPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const action = searchParams.get("action") || "COMPLETE_STEP";
    const [contract, setContract] = useState(null);
    const [signatures, setSignatures] = useState([]);
    const [selectedId, setSelectedId] = useState("");
    const [pdfUrl, setPdfUrl] = useState("");
    const [loading, setLoading] = useState(true);
    const [signing, setSigning] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        let objectUrl = "";
        Promise.all([
            contractApi.getContractById(id),
            electronicSignatureService.getAllElectronicSignature(),
            contractApi.exportContractPdf(id),
        ]).then(([contractResponse, signatureResponse, pdfResponse]) => {
            if (!active) return;
            const rows = signatureResponse?.data?.data || [];
            const activeRows = rows.filter((item) => item.status === "ACTIVE");
            objectUrl = URL.createObjectURL(pdfResponse.data instanceof Blob
                ? pdfResponse.data
                : new Blob([pdfResponse.data], { type: "application/pdf" }));
            setContract(unwrapApiResponse(contractResponse));
            setSignatures(activeRows);
            setSelectedId(activeRows.find((item) => item.default || item.isDefault)?.id || "");
            setPdfUrl(objectUrl);
        }).catch((requestError) => {
            if (active) setError(getApiErrorMessage(requestError, "Unable to prepare the signing workspace."));
        }).finally(() => {
            if (active) setLoading(false);
        });
        return () => {
            active = false;
            if (objectUrl) URL.revokeObjectURL(objectUrl);
        };
    }, [id]);

    const selectedSignature = useMemo(
        () => signatures.find((item) => item.id === selectedId),
        [signatures, selectedId]
    );

    async function handleSign() {
        if (!selectedId || signing) return;
        setSigning(true);
        setError("");
        try {
            await contractApi.signContract(id, action, selectedId);
            navigate(`/contract-management/list?viewContractId=${id}`, { replace: true });
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "The contract could not be signed."));
            setSigning(false);
        }
    }

    return (
        <main className="contract-signing-page">
            <header className="contract-signing-header">
                <Button variant="outline-secondary" onClick={() => navigate(-1)} disabled={signing}>
                    <IconArrowLeft size={18} /> Back
                </Button>
                <div>
                    <h1>Review and sign contract</h1>
                    <p>{contract?.contractNumber || "Contract"} · {contract?.contractTitle || ""}</p>
                </div>
                <Button onClick={handleSign} disabled={!selectedId || signing || loading}>
                    {signing ? <Spinner animation="border" size="sm" /> : <IconCheck size={18} />}
                    {signing ? "Signing..." : "Sign contract"}
                </Button>
            </header>
            {error && <Alert variant="danger">{error}</Alert>}
            {loading ? (
                <div className="contract-signing-loading"><Spinner animation="border" /> Preparing PDF and signatures...</div>
            ) : (
                <div className="contract-signing-workspace">
                    <aside className="contract-signature-panel">
                        <div className="contract-signing-section-title">
                            <h2>Your electronic signatures</h2>
                            <p>Select the signature that will be linked to this PDF.</p>
                        </div>
                        {signatures.length === 0 ? (
                            <Alert variant="warning">You have no active signature. Create one in Signature Management first.</Alert>
                        ) : signatures.map((signature) => (
                            <label key={signature.id} className={`contract-signature-choice ${selectedId === signature.id ? "selected" : ""}`}>
                                <input type="radio" name="electronicSignature" checked={selectedId === signature.id} onChange={() => setSelectedId(signature.id)} />
                                <div className="contract-signature-image-wrap">
                                    {signature.fileUrl ? <img src={signature.fileUrl} alt={signature.signatureName} /> : <span>Signature image</span>}
                                </div>
                                <strong>{signature.signatureName}</strong>
                                <small>{signature.type}{(signature.default || signature.isDefault) ? " · Default" : ""}</small>
                            </label>
                        ))}
                        {selectedSignature && <div className="contract-signing-confirmation"><IconCheck size={18} /> Selected: {selectedSignature.signatureName}</div>}
                    </aside>
                    <section className="contract-pdf-panel">
                        <div className="contract-pdf-heading"><IconFileTypePdf size={20} /><strong>Generated contract PDF</strong></div>
                        {pdfUrl ? <iframe title="Contract PDF preview" src={pdfUrl} /> : <Alert variant="danger">PDF preview is unavailable.</Alert>}
                    </section>
                </div>
            )}
        </main>
    );
}
