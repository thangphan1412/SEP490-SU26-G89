import React from "react";

function SigningKeyCard({
                            keyStatus,
                            keyId,
                            keyCode,
                            onGenerateKey,
                            loading = false,
                        }) {
    const isActive = keyStatus === "ACTIVE";

    return (
        <div className="card border-0 shadow-sm mt-4">
            <div className="card-header bg-white py-3">
                <div className="d-flex align-items-center gap-2">
                    <div
                        className="d-flex align-items-center justify-content-center rounded"
                        style={{
                            width: "36px",
                            height: "36px",
                            backgroundColor: "#eff6ff",
                            color: "#0d6efd",
                        }}
                    >
                        <i className="bi bi-shield-lock-fill"></i>
                    </div>

                    <div>
                        <h5 className="mb-0">
                            Signing Key
                        </h5>

                        <small className="text-muted">
                            Cryptographic key used to sign electronic documents.
                        </small>
                    </div>
                </div>
            </div>

            <div className="card-body">

                {/* Description */}
                <div
                    className="alert alert-info d-flex gap-2 align-items-start"
                    style={{
                        backgroundColor: "#eff6ff",
                        borderColor: "#bfdbfe",
                    }}
                >
                    <i className="bi bi-info-circle-fill"></i>

                    <div>
                        <strong>About your signing key</strong>

                        <div className="small mt-1">
                            A cryptographic key pair is required to create
                            an electronic signature. Your private key should
                            remain securely under your control.
                        </div>
                    </div>
                </div>

                {/* Key information */}
                <div className="row g-3 mt-1">

                    <div className="col-md-4">
                        <div className="border rounded p-3 h-100">
                            <div className="text-muted small">
                                Algorithm
                            </div>

                            <div className="fw-semibold mt-1">
                                RSA
                            </div>
                        </div>
                    </div>

                    <div className="col-md-4">
                        <div className="border rounded p-3 h-100">
                            <div className="text-muted small">
                                Key Size
                            </div>

                            <div className="fw-semibold mt-1">
                                2048 bit
                            </div>
                        </div>
                    </div>

                    <div className="col-md-4">
                        <div className="border rounded p-3 h-100">
                            <div className="text-muted small">
                                Status
                            </div>

                            <div className="mt-1">
                                {isActive ? (
                                    <span className="badge bg-success-subtle text-success">
                                        <i className="bi bi-check-circle me-1"></i>
                                        Active
                                    </span>
                                ) : (
                                    <span className="badge bg-secondary-subtle text-secondary">
                                        <i className="bi bi-exclamation-circle me-1"></i>
                                        Not configured
                                    </span>
                                )}
                            </div>
                        </div>
                    </div>

                </div>

                {/* Key ID */}
                {isActive && keyId && (
                    <div className="border rounded p-3 mt-3">

                        <div className="d-flex justify-content-between align-items-center">

                            <div>
                                <div className="text-muted small">
                                    Signing Key ID
                                </div>

                                <div className="fw-semibold font-monospace mt-1">
                                    {keyId}
                                </div>
                            </div>
                            <div>
                                <div className="text-muted small">
                                    Public Key Code
                                </div>

                                <div className="fw-semibold font-monospace mt-1">
                                    {keyCode}
                                </div>
                            </div>
                            <span className="text-success">
                                <i className="bi bi-shield-check fs-4"></i>
                            </span>

                        </div>

                    </div>
                )}

                {/* Private key information */}
                <div
                    className="mt-3 p-3 rounded"
                    style={{
                        backgroundColor: "#f8fafc",
                    }}
                >
                    <div className="d-flex gap-2">

                        <i
                            className="bi bi-lock-fill text-warning"
                            style={{ fontSize: "20px" }}
                        ></i>

                        <div>
                            <div className="fw-semibold">
                                Private Key Code Protection
                            </div>

                            <div className="small text-muted mt-1">
                                Your private key must never be shared with
                                other users or exposed publicly. Only the
                                public key is registered with the system.
                            </div>
                        </div>

                    </div>
                </div>

                {/* Generate button */}
                {!isActive && (
                    <div className="d-flex justify-content-end mt-4">

                        <button
                            type="button"
                            className="btn btn-primary"
                            onClick={onGenerateKey}
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <span
                                        className="spinner-border spinner-border-sm me-2"
                                    ></span>

                                    Generating...
                                </>
                            ) : (
                                <>
                                    <i className="bi bi-key-fill me-2"></i>
                                    Generate Key Pair
                                </>
                            )}
                        </button>

                    </div>
                )}

                {/* Active state */}
                {isActive && (
                    <div className="d-flex justify-content-end mt-4">

                        <button
                            type="button"
                            className="btn btn-outline-primary"
                        >
                            <i className="bi bi-eye me-2"></i>
                            View Key Information
                        </button>

                    </div>
                )}

            </div>
        </div>
    );
}

export default SigningKeyCard;