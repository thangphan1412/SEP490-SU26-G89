import { Download, KeyRound, ShieldCheck } from "lucide-react";
import Card from "./Card.jsx";

function RsaKeyPairCard({
    publicKey,
    fingerprint,
    privateKeyAvailable,
    registered,
    generating,
    onGenerate,
    onDownload,
}) {
    return (
        <Card title="RSA-2048 digital identity" icon={<KeyRound size={16} />}>
            <div className="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                <div>
                    <div className="fw-semibold text-dark mb-1">
                        {registered
                            ? "Public key registered"
                            : "Generate a key pair for this signature"}
                    </div>
                    <p className="text-secondary small mb-0">
                        The public key is stored by the server. The PKCS#8 private
                        key is downloaded to your device and is never uploaded.
                    </p>
                </div>
                {!registered && (
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-primary"
                        onClick={onGenerate}
                        disabled={generating}
                    >
                        <KeyRound size={14} className="me-1" />
                        {generating ? "Generating..." : "Generate RSA keys"}
                    </button>
                )}
            </div>

            {publicKey && (
                <div className="mt-3">
                    <label className="form-label small fw-semibold">
                        Public key (X.509/SPKI PEM)
                    </label>
                    <textarea
                        className="form-control font-monospace small"
                        rows={5}
                        value={publicKey}
                        readOnly
                        spellCheck="false"
                    />
                    {fingerprint && (
                        <div className="text-secondary small mt-2 text-break">
                            SHA-256 fingerprint: {fingerprint}
                        </div>
                    )}
                </div>
            )}

            {privateKeyAvailable && (
                <div className="alert alert-warning mt-3 mb-0">
                    <div className="d-flex justify-content-between align-items-center gap-3 flex-wrap">
                        <span>
                            Download and keep this private key safely. It cannot
                            be recovered from the server.
                        </span>
                        <button
                            type="button"
                            className="btn btn-sm btn-warning"
                            onClick={onDownload}
                        >
                            <Download size={14} className="me-1" />
                            Download private key
                        </button>
                    </div>
                </div>
            )}

            {registered && !privateKeyAvailable && (
                <div className="d-flex align-items-center gap-2 text-success small mt-3">
                    <ShieldCheck size={16} />
                    RSA verification is enabled for this electronic signature.
                </div>
            )}
        </Card>
    );
}

export default RsaKeyPairCard;
