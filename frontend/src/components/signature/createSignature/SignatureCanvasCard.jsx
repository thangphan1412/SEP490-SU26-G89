import React, {
    useEffect,
    useRef,
    useState,
} from "react";

import Card from "./Card";

function SignatureCanvasCard({
                                 activeTab,
                                 setActiveTab,
                                 signatureType,
                                 onFileChange,
                                 onClear,
                             }) {

    const canvasRef = useRef(null);

    const [drawing, setDrawing] = useState(false);

    const [hasSignature, setHasSignature] =
        useState(false);


    useEffect(() => {

        if (activeTab !== "draw") {
            return;
        }

        const canvas = canvasRef.current;

        if (!canvas) {
            return;
        }

        const ctx = canvas.getContext("2d");

        ctx.clearRect(
            0,
            0,
            canvas.width,
            canvas.height
        );

        ctx.lineWidth = 2;
        ctx.lineCap = "round";
        ctx.lineJoin = "round";

    }, [activeTab]);


    const getPosition = (event) => {

        const canvas = canvasRef.current;

        const rect =
            canvas.getBoundingClientRect();

        const clientX =
            event.clientX ??
            event.touches?.[0]?.clientX;

        const clientY =
            event.clientY ??
            event.touches?.[0]?.clientY;

        return {
            x:
                (clientX - rect.left)
                * (canvas.width / rect.width),

            y:
                (clientY - rect.top)
                * (canvas.height / rect.height),
        };
    };


    const startDrawing = (event) => {

        event.preventDefault();

        const canvas = canvasRef.current;

        const ctx = canvas.getContext("2d");

        const {
            x,
            y,
        } = getPosition(event);

        ctx.beginPath();

        ctx.moveTo(x, y);

        setDrawing(true);

        setHasSignature(true);
    };


    const draw = (event) => {

        if (!drawing) {
            return;
        }

        event.preventDefault();

        const canvas = canvasRef.current;

        const ctx = canvas.getContext("2d");

        const {
            x,
            y,
        } = getPosition(event);

        ctx.lineTo(x, y);

        ctx.stroke();

    };


    const stopDrawing = () => {

        if (!drawing) {
            return;
        }

        setDrawing(false);


        const canvas = canvasRef.current;

        canvas.toBlob(
            (blob) => {

                if (!blob) {
                    return;
                }

                const file = new File(
                    [blob],
                    "signature.png",
                    {
                        type: "image/png",
                    }
                );

                console.log(
                    "DRAW FILE:",
                    file
                );

                onFileChange(file);

            },
            "image/png"
        );
    };


    const handleClear = () => {

        const canvas = canvasRef.current;

        if (!canvas) {
            return;
        }

        const ctx =
            canvas.getContext("2d");

        ctx.clearRect(
            0,
            0,
            canvas.width,
            canvas.height
        );

        setHasSignature(false);

        onClear();
    };


    const handleUpload = (event) => {

        const file =
            event.target.files?.[0];

        if (!file) {
            return;
        }


        if (!file.type.startsWith("image/")) {

            alert(
                "Please select an image file."
            );

            return;
        }

        console.log(
            "UPLOAD FILE:",
            file
        );

        onFileChange(file);
    };

    return (
        <Card title="Signature">



            <div className="d-flex gap-2 mb-3">

                <button
                    type="button"
                    className={
                        activeTab === "draw"
                            ? "btn btn-primary"
                            : "btn btn-outline-secondary"
                    }
                    onClick={() => {

                        setActiveTab("draw");

                    }}
                >
                    Draw
                </button>

                <button
                    type="button"
                    className={
                        activeTab === "upload"
                            ? "btn btn-primary"
                            : "btn btn-outline-secondary"
                    }
                    onClick={() => {

                        setActiveTab("upload");

                    }}
                >
                    Upload
                </button>

            </div>



            {activeTab === "draw" && (

                <div>

                    <div
                        style={{
                            border:
                                "1px solid #dee2e6",

                            borderRadius:
                                "8px",

                            background:
                                "#fff",

                            width:
                                "100%",

                            overflow:
                                "hidden",
                        }}
                    >

                        <canvas
                            ref={canvasRef}
                            width={1000}
                            height={300}
                            style={{
                                width: "100%",
                                height: "300px",
                                display: "block",
                                cursor: "crosshair",
                                touchAction: "none",
                            }}

                            onMouseDown={
                                startDrawing
                            }

                            onMouseMove={
                                draw
                            }

                            onMouseUp={
                                stopDrawing
                            }

                            onMouseLeave={
                                stopDrawing
                            }

                            onTouchStart={
                                startDrawing
                            }

                            onTouchMove={
                                draw
                            }

                            onTouchEnd={
                                stopDrawing
                            }
                        />

                    </div>

                    <div className="d-flex justify-content-between mt-2">

                        <small className="text-muted">
                            Draw your signature above.
                        </small>

                        <button
                            type="button"
                            className="btn btn-sm btn-outline-secondary"
                            onClick={handleClear}
                            disabled={!hasSignature}
                        >
                            Clear
                        </button>

                    </div>

                </div>
            )}



            {activeTab === "upload" && (

                <div>

                    <div className="mb-3">

                        <label
                            className="form-label"
                        >
                            Upload Signature
                        </label>

                        <input
                            type="file"
                            className="form-control"
                            accept="image/png,image/jpeg,image/jpg,image/webp"
                            onChange={handleUpload}
                        />

                    </div>

                    <small className="text-muted">
                        Upload a PNG, JPG or WEBP image
                        containing your signature.
                    </small>

                </div>
            )}

        </Card>
    );
}

export default SignatureCanvasCard;