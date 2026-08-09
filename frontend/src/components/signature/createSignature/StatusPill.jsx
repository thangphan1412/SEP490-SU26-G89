import { CheckCircle2 } from "lucide-react";

function StatusPill({
                        label = "Ready to Save",
                    }) {
    return (
        <span
            className="badge rounded-pill fw-medium"
            style={{
                backgroundColor: "#ecfdf5",
                color: "#059669",
                fontSize: "11px",
                padding: "6px 10px",
            }}
        >
      <CheckCircle2 size={13} className="me-1" />
            {label}
    </span>
    );
}

export default StatusPill;