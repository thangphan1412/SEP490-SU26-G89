import { useState } from "react";
import { ArrowLeft, Save, PenLine } from "lucide-react";

import Card from "./Card";
import Field from "./Field";
import SignatureCanvas from "./SignatureCanvas";

function UpdateSignature({ onCancel, onSave }) {
    // ============================================
    // FIX DATA
    // ============================================

    const [formData, setFormData] = useState({
        id: "signature-001",

        signatureName: "Default Work Signature",

        status: "ACTIVE",

        signatureType: "DRAWN",

        usedIn: "CONTRACTS",

        description:
            "Primary signature used for standard contract execution.",

        isDefault: true,

        accessScope: "PERSONAL_ONLY",

        lastUpdated: "May 22, 2025",

        signatureUrl:
            "https://dummyimage.com/600x180/ffffff/1648ff.png&text=Alex+Morgan",
    });

    const [saving, setSaving] = useState(false);

    // ============================================
    // HANDLE INPUT
    // ============================================

    const handleChange = (field, value) => {
        setFormData((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    // ============================================
    // SAVE
    // ============================================

    const handleSubmit = () => {
        setSaving(true);

        // Fake API
        setTimeout(() => {
            console.log("Updated signature:", formData);

            setSaving(false);

            if (onSave) {
                onSave(formData);
            }
        }, 800);
    };

    return (
        <div
            style={{
                maxWidth: "1200px",
                margin: "0 auto",
                padding: "20px",
                backgroundColor: "#fff",
                minHeight: "100vh",
            }}
        >
            {/* ============================================
          HEADER
      ============================================ */}

            <div
                className="d-flex justify-content-between align-items-start"
                style={{
                    marginBottom: "18px",
                }}
            >
                <div>
                    <div
                        className="d-flex align-items-center gap-2"
                        style={{
                            marginBottom: "4px",
                        }}
                    >
                        <button
                            type="button"
                            onClick={onCancel}
                            className="btn btn-link p-0 text-secondary"
                        >
                            <ArrowLeft size={18} />
                        </button>

                        <h2
                            style={{
                                margin: 0,
                                fontSize: "24px",
                                fontWeight: 700,
                                color: "#111827",
                            }}
                        >
                            Edit Signature
                        </h2>
                    </div>

                    <p
                        style={{
                            margin: "4px 0 0 26px",
                            fontSize: "12px",
                            color: "#6b7280",
                        }}
                    >
                        Update your personal electronic signature
                        information.
                    </p>
                </div>

                <div className="d-flex gap-2">
                    <button
                        type="button"
                        onClick={onCancel}
                        className="btn btn-light btn-sm"
                        disabled={saving}
                    >
                        Cancel
                    </button>

                    <button
                        type="button"
                        onClick={handleSubmit}
                        className="btn btn-primary btn-sm d-flex align-items-center gap-2"
                        disabled={saving}
                    >
                        <Save size={14} />

                        {saving
                            ? "Saving..."
                            : "Save Changes"}
                    </button>
                </div>
            </div>

            {/* ============================================
          SIGNATURE INFORMATION
      ============================================ */}

            <Card
                title="Signature Information"
                icon={<PenLine size={16} />}
            >
                <div className="row">
                    {/* ========================================
              LEFT
          ======================================== */}

                    <div className="col-md-6">
                        <Field
                            label="Signature Name"
                            required
                            mode="edit"
                        >
                            <input
                                type="text"
                                className="form-control form-control-sm"
                                value={formData.signatureName}
                                onChange={(e) =>
                                    handleChange(
                                        "signatureName",
                                        e.target.value
                                    )
                                }
                            />
                        </Field>

                        <Field
                            label="Status"
                            mode="edit"
                        >
                            <select
                                className="form-select form-select-sm"
                                value={formData.status}
                                onChange={(e) =>
                                    handleChange(
                                        "status",
                                        e.target.value
                                    )
                                }
                            >
                                <option value="ACTIVE">
                                    Active
                                </option>

                                <option value="INACTIVE">
                                    Inactive
                                </option>
                            </select>

                            <div
                                style={{
                                    marginTop: "4px",
                                    fontSize: "10px",
                                    color: "#94a3b8",
                                }}
                            >
                                Active signatures can be used in
                                documents.
                            </div>
                        </Field>

                        <Field
                            label="Signature Type"
                            required
                            mode="edit"
                        >
                            <select
                                className="form-select form-select-sm"
                                value={formData.signatureType}
                                onChange={(e) =>
                                    handleChange(
                                        "signatureType",
                                        e.target.value
                                    )
                                }
                            >
                                <option value="DRAWN">
                                    Drawn
                                </option>

                                <option value="UPLOADED">
                                    Uploaded
                                </option>

                                <option value="TYPED">
                                    Typed
                                </option>
                            </select>
                        </Field>

                        <Field
                            label="Used In"
                            required
                            mode="edit"
                        >
                            <select
                                className="form-select form-select-sm"
                                value={formData.usedIn}
                                onChange={(e) =>
                                    handleChange(
                                        "usedIn",
                                        e.target.value
                                    )
                                }
                            >
                                <option value="CONTRACTS">
                                    Contracts
                                </option>

                                <option value="APPROVALS">
                                    Approvals
                                </option>

                                <option value="INTERNAL_DOCUMENTS">
                                    Internal Documents
                                </option>
                            </select>
                        </Field>
                    </div>

                    {/* ========================================
              RIGHT
          ======================================== */}

                    <div className="col-md-6">
                        <Field
                            label="Set as Default"
                            mode="edit"
                        >
                            <div
                                className="form-check form-switch"
                                style={{
                                    paddingTop: "2px",
                                }}
                            >
                                <input
                                    className="form-check-input"
                                    type="checkbox"
                                    checked={formData.isDefault}
                                    onChange={(e) =>
                                        handleChange(
                                            "isDefault",
                                            e.target.checked
                                        )
                                    }
                                />

                                <label
                                    className="form-check-label"
                                    style={{
                                        fontSize: "12px",
                                    }}
                                >
                                    Make this your default signature
                                    for new documents.
                                </label>
                            </div>
                        </Field>

                        <Field
                            label="Access Scope"
                            mode="edit"
                        >
                            <select
                                className="form-select form-select-sm"
                                value={formData.accessScope}
                                onChange={(e) =>
                                    handleChange(
                                        "accessScope",
                                        e.target.value
                                    )
                                }
                            >
                                <option value="PERSONAL_ONLY">
                                    Personal Only
                                </option>

                                <option value="COMPANY">
                                    Company
                                </option>
                            </select>

                            <div
                                style={{
                                    marginTop: "4px",
                                    fontSize: "10px",
                                    color: "#94a3b8",
                                }}
                            >
                                Define who can access and use this
                                signature.
                            </div>
                        </Field>

                        <Field
                            label="Description"
                            mode="edit"
                        >
              <textarea
                  className="form-control form-control-sm"
                  rows={4}
                  value={formData.description}
                  onChange={(e) =>
                      handleChange(
                          "description",
                          e.target.value
                      )
                  }
              />
                        </Field>

                        <Field
                            label="Last Updated"
                            mode="view"
                            value={formData.lastUpdated}
                        />
                    </div>
                </div>

                {/* ============================================
            SIGNATURE
        ============================================ */}

                <div
                    style={{
                        marginTop: "10px",
                    }}
                >
                    <div
                        style={{
                            fontSize: "12px",
                            fontWeight: 600,
                            color: "#374151",
                            marginBottom: "8px",
                        }}
                    >
                        Signature
                    </div>

                    <SignatureCanvas
                        mode="edit"
                        signatureUrl={
                            formData.signatureUrl
                        }
                        onSignatureChange={(value) => {
                            handleChange(
                                "signatureUrl",
                                value
                            );
                        }}
                    />
                </div>
            </Card>

            {/* ============================================
          BOTTOM ACTION
      ============================================ */}

            <div
                className="d-flex justify-content-end gap-2"
                style={{
                    marginTop: "14px",
                }}
            >
                <button
                    type="button"
                    onClick={onCancel}
                    className="btn btn-light btn-sm"
                    disabled={saving}
                >
                    Cancel
                </button>

                <button
                    type="button"
                    onClick={handleSubmit}
                    className="btn btn-primary btn-sm d-flex align-items-center gap-2"
                    disabled={saving}
                >
                    <Save size={14} />

                    {saving
                        ? "Saving..."
                        : "Save Changes"}
                </button>
            </div>
        </div>
    );
}

export default UpdateSignature;