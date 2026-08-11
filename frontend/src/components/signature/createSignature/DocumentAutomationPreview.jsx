import {
    FileText,
    ShieldCheck,
    ClipboardList,
    BadgeCheck,
} from "lucide-react";

import Card from "./Card";

const AUTOMATION_ITEMS = [
    {
        icon: FileText,
        title: "Contract Templates",
        description:
            "Automatically apply your signature to contract templates.",
        bg: "#eef2ff",
        color: "#6366f1",
    },
    {
        icon: BadgeCheck,
        title: "Approvals",
        description:
            "Use your signature for approvals and requests.",
        bg: "#ecfdf5",
        color: "#10b981",
    },
    {
        icon: ClipboardList,
        title: "Internal Forms",
        description:
            "Apply signatures to internal forms and requests.",
        bg: "#f5f3ff",
        color: "#8b5cf6",
    },
    {
        icon: ShieldCheck,
        title: "Compliance",
        description:
            "Meets compliance and audit requirements.",
        bg: "#fffbeb",
        color: "#f59e0b",
    },
];

function DocumentAutomationPreview() {
    return (
        <Card
            title="Document Automation Preview"
            icon={<ClipboardList size={16} />}
        >
            <div className="row g-3">
                {AUTOMATION_ITEMS.map(
                    ({
                         icon: Icon,
                         title,
                         description,
                         bg,
                         color,
                     }) => (
                        <div
                            key={title}
                            className="col-lg-3 col-md-6"
                        >
                            <div
                                className="h-100 border rounded p-3"
                                style={{
                                    borderColor: "#f1f5f9",
                                }}
                            >
                                <div
                                    className="d-flex align-items-center justify-content-center mb-2"
                                    style={{
                                        width: "32px",
                                        height: "32px",
                                        borderRadius: "6px",
                                        backgroundColor: bg,
                                        color: color,
                                    }}
                                >
                                    <Icon size={17} />
                                </div>

                                <h3
                                    className="fw-semibold mb-1"
                                    style={{
                                        fontSize: "12px",
                                        color: "#334155",
                                    }}
                                >
                                    {title}
                                </h3>

                                <p
                                    className="mb-0"
                                    style={{
                                        fontSize: "10px",
                                        lineHeight: "15px",
                                        color: "#94a3b8",
                                    }}
                                >
                                    {description}
                                </p>
                            </div>
                        </div>
                    )
                )}
            </div>
        </Card>
    );
}

export default DocumentAutomationPreview;