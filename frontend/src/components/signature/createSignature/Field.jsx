import React from "react";

function Field({
                   label,
                   value,
                   children,
                   mode = "edit",
                   required = false,
                   description,
                   className = "",
               }) {
    const isView = mode === "view";

    return (
        <div
            className={`mb-3 ${className}`}
            style={{
                width: "100%",
            }}
        >
            {/* Label */}
            <label
                style={{
                    display: "block",
                    marginBottom: "6px",
                    fontSize: "12px",
                    fontWeight: 500,
                    color: "#374151",
                }}
            >
                {label}

                {required && (
                    <span
                        style={{
                            color: "#dc2626",
                            marginLeft: "3px",
                        }}
                    >
            *
          </span>
                )}
            </label>



            {isView ? (
                <div
                    style={{
                        minHeight: "32px",
                        display: "flex",
                        alignItems: "center",
                        padding: "6px 10px",
                        border: "1px solid #e5e7eb",
                        borderRadius: "5px",
                        backgroundColor: "#f9fafb",
                        color: "#374151",
                        fontSize: "12px",
                        lineHeight: "18px",
                    }}
                >
                    {value !== undefined &&
                    value !== null &&
                    value !== ""
                        ? value
                        : "-"}
                </div>
            ) : (


                <div>{children}</div>
            )}



            {description && (
                <div
                    style={{
                        marginTop: "4px",
                        fontSize: "10px",
                        lineHeight: "14px",
                        color: "#94a3b8",
                    }}
                >
                    {description}
                </div>
            )}
        </div>
    );
}

export default Field;