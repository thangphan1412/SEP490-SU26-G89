import {
    PenLine,
    Upload,
    Type,
    Eraser,
    Undo2,
    Eye,
} from "lucide-react";

import Card from "./Card";

const CANVAS_TABS = [
    {
        id: "draw",
        label: "Draw",
        icon: PenLine,
    },
    {
        id: "upload",
        label: "Upload",
        icon: Upload,
    },
    {
        id: "type",
        label: "Type",
        icon: Type,
    },
];

function SignatureCanvasCard({
                                 activeTab,
                                 setActiveTab,
                                 onClear,
                                 onUndo,
                             }) {
    return (
        <Card
            title="Signature Canvas"
            icon={<PenLine size={16} />}
        >
            {/* Tabs */}
            <div className="d-flex gap-1 mb-3">
                {CANVAS_TABS.map(
                    ({
                         id,
                         label,
                         icon: Icon,
                     }) => {
                        const active =
                            activeTab === id;

                        return (
                            <button
                                key={id}
                                type="button"
                                onClick={() =>
                                    setActiveTab(id)
                                }
                                className={`btn btn-sm ${
                                    active
                                        ? "btn-primary"
                                        : "btn-light"
                                } d-flex align-items-center gap-1`}
                                style={{
                                    fontSize: "11px",
                                }}
                            >
                                <Icon size={14} />
                                {label}
                            </button>
                        );
                    }
                )}
            </div>

            {/* Canvas */}
            <div
                className="d-flex align-items-center justify-content-center"
                style={{
                    height: "130px",
                    border: "1px dashed #cbd5e1",
                    borderRadius: "8px",
                    backgroundColor: "#fff",
                }}
            >
        <span
            style={{
                fontFamily:
                    "'Brush Script MT', 'Segoe Script', cursive",
                fontSize: "34px",
                color: "#4f46e5",
            }}
        >
          Alex Morgan
        </span>
            </div>

            {/* Actions */}
            <div className="d-flex justify-content-between align-items-center mt-3">

                <div className="d-flex gap-3">
                    <button
                        type="button"
                        onClick={onClear}
                        className="btn btn-link btn-sm text-secondary text-decoration-none p-0 d-flex align-items-center gap-1"
                        style={{ fontSize: "11px" }}
                    >
                        <Eraser size={14} />
                        Clear
                    </button>

                    <button
                        type="button"
                        onClick={onUndo}
                        className="btn btn-link btn-sm text-secondary text-decoration-none p-0 d-flex align-items-center gap-1"
                        style={{ fontSize: "11px" }}
                    >
                        <Undo2 size={14} />
                        Undo
                    </button>
                </div>

                <button
                    type="button"
                    className="btn btn-link btn-sm text-primary text-decoration-none p-0 d-flex align-items-center gap-1"
                    style={{ fontSize: "11px" }}
                >
                    <Eye size={14} />
                    Preview
                </button>

            </div>
        </Card>
    );
}

export default SignatureCanvasCard;