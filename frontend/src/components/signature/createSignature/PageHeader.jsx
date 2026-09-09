import {
    X,
    Save,
} from "lucide-react";

function PageHeader({
                        onCancel,
                        onSave,
                        loading = false,
                    }) {
    return (
        <div className="d-flex justify-content-between align-items-start mb-3">
            <div>
                <h1
                    className="fw-semibold text-dark mb-1"
                    style={{
                        fontSize: "22px",
                    }}
                >
                    Create Signature
                </h1>

                <p
                    className="text-secondary mb-0"
                    style={{
                        fontSize: "12px",
                    }}
                >
                    Create a personal electronic signature for use
                    in contracts, approvals, and internal documents.
                </p>
            </div>

            <div className="d-flex gap-2">
                <button
                    type="button"
                    onClick={onCancel}
                    disabled={loading}
                    className="btn btn-sm btn-light border d-flex align-items-center gap-1"
                >
                    <X size={15} />
                    Cancel
                </button>

                <button
                    type="button"
                    onClick={onSave}
                    disabled={loading}
                    className="btn btn-sm btn-primary d-flex align-items-center gap-1"
                >
                    <Save size={15} />
                    {loading ? "Saving..." : "Save Signature"}
                </button>
            </div>
        </div>
    );
}

export default PageHeader;
