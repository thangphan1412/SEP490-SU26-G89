import { useRef, useState } from "react";
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
                                 onSignatureChange,
                             }) {
    const canvasRef = useRef(null);
    const fileInputRef = useRef(null);

    const [activeTab, setActiveTab] = useState("draw");
    const [isDrawing, setIsDrawing] = useState(false);
    const [history, setHistory] = useState([]);
    const [uploadedImage, setUploadedImage] = useState(null);

    // =========================
    // CANVAS CONFIG
    // =========================

    const getCanvasContext = () => {
        const canvas = canvasRef.current;

        if (!canvas) return null;

        return canvas.getContext("2d");
    };

    // =========================
    // DRAW
    // =========================

    const getPosition = (event) => {
        const canvas = canvasRef.current;

        if (!canvas) {
            return { x: 0, y: 0 };
        }

        const rect = canvas.getBoundingClientRect();

        return {
            x:
                (event.clientX - rect.left) *
                (canvas.width / rect.width),

            y:
                (event.clientY - rect.top) *
                (canvas.height / rect.height),
        };
    };

    const startDrawing = (event) => {
        if (activeTab !== "draw") return;

        const canvas = canvasRef.current;
        const ctx = getCanvasContext();

        if (!canvas || !ctx) return;

        const { x, y } =
            getPosition(event);

        // Lưu trạng thái trước khi vẽ
        setHistory((prev) => [
            ...prev,
            canvas.toDataURL(),
        ]);

        ctx.beginPath();
        ctx.moveTo(x, y);

        ctx.lineWidth = 2;
        ctx.lineCap = "round";
        ctx.lineJoin = "round";
        ctx.strokeStyle = "#111827";

        setIsDrawing(true);
    };

    const draw = (event) => {
        if (!isDrawing) return;

        const ctx = getCanvasContext();

        if (!ctx) return;

        const { x, y } =
            getPosition(event);

        ctx.lineTo(x, y);
        ctx.stroke();
    };

    const stopDrawing = () => {
        if (!isDrawing) return;

        setIsDrawing(false);

        const canvas = canvasRef.current;

        if (canvas && onSignatureChange) {
            onSignatureChange(
                canvas.toDataURL("image/png")
            );
        }
    };

    // =========================
    // CLEAR
    // =========================

    const clearCanvas = () => {
        const canvas = canvasRef.current;

        if (!canvas) return;

        const ctx = canvas.getContext("2d");

        ctx.clearRect(
            0,
            0,
            canvas.width,
            canvas.height
        );

        setHistory([]);
        setUploadedImage(null);

        if (onSignatureChange) {
            onSignatureChange(null);
        }
    };

    // =========================
    // UNDO
    // =========================

    const undo = () => {
        const canvas = canvasRef.current;

        if (!canvas || history.length === 0) {
            return;
        }

        const previousImage =
            history[history.length - 1];

        const img = new Image();

        img.onload = () => {
            const ctx =
                canvas.getContext("2d");

            ctx.clearRect(
                0,
                0,
                canvas.width,
                canvas.height
            );

            ctx.drawImage(
                img,
                0,
                0
            );

            setHistory((prev) =>
                prev.slice(0, -1)
            );

            if (onSignatureChange) {
                onSignatureChange(
                    canvas.toDataURL("image/png")
                );
            }
        };

        img.src = previousImage;
    };

    // =========================
    // UPLOAD
    // =========================

    const handleUploadClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = (event) => {
        const file =
            event.target.files?.[0];

        if (!file) return;

        // Chỉ cho phép ảnh
        if (!file.type.startsWith("image/")) {
            alert(
                "Please upload an image file."
            );
            return;
        }

        const imageUrl =
            URL.createObjectURL(file);

        setUploadedImage({
            file,
            url: imageUrl,
        });

        // Preview ảnh lên canvas
        const canvas = canvasRef.current;

        if (!canvas) return;

        const ctx = canvas.getContext("2d");

        const img = new Image();

        img.onload = () => {
            ctx.clearRect(
                0,
                0,
                canvas.width,
                canvas.height
            );

            // Tính tỷ lệ để ảnh không bị méo
            const scale = Math.min(
                canvas.width / img.width,
                canvas.height / img.height
            );

            const width =
                img.width * scale;

            const height =
                img.height * scale;

            const x =
                (canvas.width - width) / 2;

            const y =
                (canvas.height - height) / 2;

            ctx.drawImage(
                img,
                x,
                y,
                width,
                height
            );

            if (onSignatureChange) {
                onSignatureChange(
                    canvas.toDataURL("image/png")
                );
            }
        };

        img.src = imageUrl;
    };

    // =========================
    // TYPE
    // =========================

    const handleTypeSignature = () => {
        const canvas = canvasRef.current;

        if (!canvas) return;

        const ctx = canvas.getContext("2d");

        ctx.clearRect(
            0,
            0,
            canvas.width,
            canvas.height
        );

        ctx.font =
            "42px 'Brush Script MT', cursive";

        ctx.fillStyle = "#111827";

        ctx.textAlign = "center";

        ctx.textBaseline = "middle";

        ctx.fillText(
            "Your Signature",
            canvas.width / 2,
            canvas.height / 2
        );

        if (onSignatureChange) {
            onSignatureChange(
                canvas.toDataURL("image/png")
            );
        }
    };

    return (
        <Card
            title="Signature Canvas"
            icon={<PenLine size={16} />}
        >
            {/* =========================
          TABS
      ========================== */}

            <div className="d-flex gap-2 mb-3">

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
                                onClick={() => {
                                    setActiveTab(id);

                                    if (id === "type") {
                                        setTimeout(
                                            handleTypeSignature,
                                            0
                                        );
                                    }
                                }}
                                className={
                                    active
                                        ? "btn btn-primary btn-sm d-flex align-items-center gap-1"
                                        : "btn btn-light border btn-sm d-flex align-items-center gap-1"
                                }
                            >
                                <Icon size={14} />

                                {label}
                            </button>
                        );
                    }
                )}

            </div>

            {/* =========================
          HIDDEN FILE INPUT
      ========================== */}

            <input
                ref={fileInputRef}
                type="file"
                accept="image/png,image/jpeg,image/jpg"
                onChange={handleFileChange}
                style={{
                    display: "none",
                }}
            />

            {/* =========================
          CANVAS AREA
      ========================== */}

            <div
                className="position-relative"
                style={{
                    border:
                        "1px dashed #cbd5e1",
                    borderRadius: "8px",
                    backgroundColor: "#ffffff",
                    overflow: "hidden",
                }}
            >

                <canvas
                    ref={canvasRef}
                    width={900}
                    height={260}
                    onPointerDown={startDrawing}
                    onPointerMove={draw}
                    onPointerUp={stopDrawing}
                    onPointerLeave={stopDrawing}
                    style={{
                        width: "100%",
                        height: "260px",
                        display: "block",
                        cursor:
                            activeTab === "draw"
                                ? "crosshair"
                                : "default",
                        touchAction: "none",
                    }}
                />

                {/* Draw hint */}

                {activeTab === "draw" && (
                    <div
                        className="position-absolute top-50 start-50 translate-middle text-center"
                        style={{
                            pointerEvents: "none",
                            color: "#94a3b8",
                            fontSize: "12px",
                        }}
                    >
                        Draw your signature here
                    </div>
                )}

                {/* Upload button */}

                {activeTab === "upload" && (
                    <div
                        className="position-absolute top-50 start-50 translate-middle"
                    >
                        <button
                            type="button"
                            onClick={
                                handleUploadClick
                            }
                            className="btn btn-outline-primary btn-sm d-flex align-items-center gap-2"
                        >
                            <Upload size={15} />

                            Choose Signature File
                        </button>
                    </div>
                )}

            </div>

            {/* =========================
          UPLOADED FILE
      ========================== */}

            {uploadedImage && (
                <div
                    className="mt-2 text-secondary"
                    style={{
                        fontSize: "11px",
                    }}
                >
                    Uploaded:{" "}
                    {uploadedImage.file.name}
                </div>
            )}

            {/* =========================
          ACTIONS
      ========================== */}

            <div className="d-flex justify-content-between align-items-center mt-3">

                <div className="d-flex gap-3">

                    <button
                        type="button"
                        onClick={clearCanvas}
                        className="btn btn-link btn-sm text-secondary text-decoration-none p-0 d-flex align-items-center gap-1"
                    >
                        <Eraser size={14} />
                        Clear
                    </button>

                    <button
                        type="button"
                        onClick={undo}
                        disabled={
                            history.length === 0
                        }
                        className="btn btn-link btn-sm text-secondary text-decoration-none p-0 d-flex align-items-center gap-1"
                    >
                        <Undo2 size={14} />
                        Undo
                    </button>

                </div>

                <button
                    type="button"
                    className="btn btn-link btn-sm text-primary text-decoration-none p-0 d-flex align-items-center gap-1"
                    onClick={() => {
                        const canvas =
                            canvasRef.current;

                        if (!canvas) return;

                        const image =
                            canvas.toDataURL(
                                "image/png"
                            );

                        window.open(image, "_blank");
                    }}
                >
                    <Eye size={14} />
                    Preview
                </button>

            </div>
        </Card>
    );
}

export default SignatureCanvasCard;