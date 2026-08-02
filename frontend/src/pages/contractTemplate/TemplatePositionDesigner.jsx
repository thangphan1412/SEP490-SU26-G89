import { useMemo, useRef, useState } from "react";
import {
    IconCalendar,
    IconCheckbox,
    IconFileText,
    IconSignature,
    IconTrash,
    IconUser,
} from "@tabler/icons-react";
import { createPositionClientId } from "./templatePositionUtils.js";

const FIELD_PALETTE = [
    {
        key: "contract_number",
        label: "Contract Number",
        fieldType: "TEXT",
        valueSource: "CONTRACT",
        signerRole: null,
        systemField: true,
        required: true,
        icon: IconFileText,
    },
    {
        key: "contract_title",
        label: "Contract Title",
        fieldType: "TEXT",
        valueSource: "CONTRACT",
        signerRole: null,
        systemField: true,
        required: true,
        icon: IconFileText,
    },
    {
        key: "effective_date",
        label: "Effective Date",
        fieldType: "DATE",
        valueSource: "CONTRACT",
        signerRole: null,
        systemField: true,
        required: true,
        icon: IconCalendar,
    },
    {
        key: "expiration_date",
        label: "Expiration Date",
        fieldType: "DATE",
        valueSource: "CONTRACT",
        signerRole: null,
        systemField: true,
        required: true,
        icon: IconCalendar,
    },
    {
        key: "director_full_name",
        label: "Director Full Name",
        fieldType: "TEXT",
        valueSource: "CURRENT_SIGNER",
        signerRole: "DIRECTOR",
        systemField: true,
        required: true,
        icon: IconUser,
    },
    {
        key: "director_signature",
        label: "Director Signature",
        fieldType: "SIGNATURE",
        valueSource: "CURRENT_SIGNER",
        signerRole: "DIRECTOR",
        systemField: true,
        required: true,
        icon: IconSignature,
    },
    {
        key: "partner_full_name",
        label: "Partner Full Name",
        fieldType: "TEXT",
        valueSource: "CURRENT_SIGNER",
        signerRole: "PARTNER",
        systemField: true,
        required: true,
        icon: IconUser,
    },
    {
        key: "partner_signature",
        label: "Partner Signature",
        fieldType: "SIGNATURE",
        valueSource: "CURRENT_SIGNER",
        signerRole: "PARTNER",
        systemField: true,
        required: true,
        icon: IconSignature,
    },
    {
        key: "manual_field",
        label: "Manual Field",
        fieldType: "TEXT",
        valueSource: "MANUAL",
        signerRole: null,
        systemField: false,
        required: false,
        icon: IconCheckbox,
    },
];

function TemplatePositionDesigner({
    content,
    pageCount = 1,
    positions = [],
    onChange,
}) {
    const [currentPage, setCurrentPage] = useState(1);
    const [selectedId, setSelectedId] = useState(null);
    const canvasRef = useRef(null);
    const pointerActionRef = useRef(null);
    const contentPages = useMemo(
        () => String(content || "").split(/<!--\s*pagebreak\s*-->/i),
        [content]
    );
    const activePage = clamp(currentPage, 1, pageCount);
    const selectedPosition = positions.find(
        (position) => position.clientId === selectedId
    );
    const currentPositions = positions.filter(
        (position) => Number(position.pageNumber) === activePage
    );

    const emitPositions = (nextPositions, nextPageCount = pageCount) => {
        onChange({
            pageCount: nextPageCount,
            positions: nextPositions,
        });
    };

    const addField = (definition, dropX = 0.08, dropY = 0.08) => {
        const width = definition.fieldType === "SIGNATURE" ? 0.3 : 0.25;
        const height = definition.fieldType === "SIGNATURE" ? 0.09 : 0.055;
        const field = {
            clientId: createPositionClientId(),
            attributeKey: definition.key,
            fieldLabel: definition.label,
            pageNumber: activePage,
            xPosition: clamp(dropX, 0, 1 - width),
            yPosition: clamp(dropY, 0, 1 - height),
            width,
            height,
            fieldType: definition.fieldType,
            valueSource: definition.valueSource,
            signerRole: definition.signerRole,
            systemField: definition.systemField,
            required: definition.required,
        };

        emitPositions([...positions, field]);
        setSelectedId(field.clientId);
    };

    const updateField = (clientId, patch) => {
        emitPositions(
            positions.map((position) =>
                position.clientId === clientId
                    ? { ...position, ...patch }
                    : position
            )
        );
    };

    const removeSelectedField = () => {
        if (!selectedId) {
            return;
        }
        emitPositions(
            positions.filter((position) => position.clientId !== selectedId)
        );
        setSelectedId(null);
    };

    const handlePageCountChange = (event) => {
        const nextPageCount = clamp(
            Number.parseInt(event.target.value, 10) || 1,
            1,
            50
        );
        const nextPositions = positions.map((position) => ({
            ...position,
            pageNumber: Math.min(position.pageNumber, nextPageCount),
        }));
        emitPositions(nextPositions, nextPageCount);
        setCurrentPage((page) => Math.min(page, nextPageCount));
    };

    const handlePaletteDragStart = (event, fieldKey) => {
        event.dataTransfer.effectAllowed = "copy";
        event.dataTransfer.setData(
            "application/x-contract-template-field",
            fieldKey
        );
    };

    const handleCanvasDrop = (event) => {
        event.preventDefault();
        const fieldKey = event.dataTransfer.getData(
            "application/x-contract-template-field"
        );
        const definition = FIELD_PALETTE.find((field) => field.key === fieldKey);
        const canvas = canvasRef.current;

        if (!definition || !canvas) {
            return;
        }

        const bounds = canvas.getBoundingClientRect();
        addField(
            definition,
            (event.clientX - bounds.left) / bounds.width - 0.12,
            (event.clientY - bounds.top) / bounds.height - 0.03
        );
    };

    const beginPointerAction = (event, position, mode) => {
        event.preventDefault();
        event.stopPropagation();
        event.currentTarget.setPointerCapture(event.pointerId);
        pointerActionRef.current = {
            clientId: position.clientId,
            mode,
            pointerId: event.pointerId,
            startClientX: event.clientX,
            startClientY: event.clientY,
            original: { ...position },
        };
        setSelectedId(position.clientId);
    };

    const continuePointerAction = (event) => {
        const action = pointerActionRef.current;
        const canvas = canvasRef.current;
        if (!action || !canvas || action.pointerId !== event.pointerId) {
            return;
        }

        const bounds = canvas.getBoundingClientRect();
        const deltaX = (event.clientX - action.startClientX) / bounds.width;
        const deltaY = (event.clientY - action.startClientY) / bounds.height;
        const original = action.original;

        if (action.mode === "resize") {
            updateField(action.clientId, {
                width: roundCoordinate(clamp(
                    original.width + deltaX,
                    0.06,
                    1 - original.xPosition
                )),
                height: roundCoordinate(clamp(
                    original.height + deltaY,
                    0.035,
                    1 - original.yPosition
                )),
            });
            return;
        }

        updateField(action.clientId, {
            xPosition: roundCoordinate(clamp(
                original.xPosition + deltaX,
                0,
                1 - original.width
            )),
            yPosition: roundCoordinate(clamp(
                original.yPosition + deltaY,
                0,
                1 - original.height
            )),
        });
    };

    const endPointerAction = (event) => {
        if (pointerActionRef.current?.pointerId === event.pointerId) {
            pointerActionRef.current = null;
        }
    };

    const updateSelectedGeometry = (name, percentValue) => {
        if (!selectedPosition) {
            return;
        }

        const normalizedValue = (Number(percentValue) || 0) / 100;
        const maximum = name === "xPosition"
            ? 1 - selectedPosition.width
            : name === "yPosition"
              ? 1 - selectedPosition.height
              : name === "width"
                ? 1 - selectedPosition.xPosition
                : 1 - selectedPosition.yPosition;
        const minimum = ["width", "height"].includes(name) ? 0.01 : 0;
        updateField(selectedId, {
            [name]: roundCoordinate(clamp(normalizedValue, minimum, maximum)),
        });
    };

    return (
        <section className="template-position-designer">
            <header className="template-designer-header">
                <div>
                    <h3>Template Position Designer</h3>
                    <p>
                        Drag fields onto a page. Coordinates are saved to this
                        version only and use normalized page values.
                    </p>
                </div>
                <label className="template-page-count-control">
                    Pages
                    <input
                        type="number"
                        min="1"
                        max="50"
                        value={pageCount}
                        onChange={handlePageCountChange}
                    />
                </label>
            </header>

            <div className="template-designer-grid">
                <aside className="template-field-palette">
                    <div className="template-designer-panel-heading">
                        <strong>Available fields</strong>
                        <small>Drag or click to add</small>
                    </div>
                    <div className="template-field-palette-list">
                        {FIELD_PALETTE.map((field) => {
                            const FieldIcon = field.icon;
                            return (
                                <button
                                    key={field.key}
                                    type="button"
                                    draggable
                                    className="template-palette-field"
                                    onDragStart={(event) =>
                                        handlePaletteDragStart(event, field.key)
                                    }
                                    onClick={() => addField(field)}
                                >
                                    <FieldIcon size={18} />
                                    <span>
                                        <strong>{field.label}</strong>
                                        <small>
                                            {field.signerRole || field.valueSource}
                                        </small>
                                    </span>
                                </button>
                            );
                        })}
                    </div>
                    <div className="template-privacy-note">
                        Only field keys and positions are saved. Signer names and
                        signatures are inserted from the signing account later.
                    </div>
                </aside>

                <div className="template-page-workspace">
                    <div className="template-page-toolbar">
                        <button
                            type="button"
                            disabled={activePage <= 1}
                            onClick={() => setCurrentPage((page) => page - 1)}
                        >
                            Previous
                        </button>
                        <span>
                            Page <strong>{activePage}</strong> / {pageCount}
                        </span>
                        <button
                            type="button"
                            disabled={activePage >= pageCount}
                            onClick={() => setCurrentPage((page) => page + 1)}
                        >
                            Next
                        </button>
                    </div>
                    <div className="template-page-scroll">
                        <div
                            ref={canvasRef}
                            className="template-page-canvas"
                            onClick={() => setSelectedId(null)}
                            onDragOver={(event) => {
                                event.preventDefault();
                                event.dataTransfer.dropEffect = "copy";
                            }}
                            onDrop={handleCanvasDrop}
                        >
                            <pre className="template-page-content">
                                {contentPages[activePage - 1]
                                    || (activePage === 1
                                        ? "Enter template content above."
                                        : `Page ${activePage}`)}
                            </pre>
                            {currentPositions.map((position) => (
                                <div
                                    key={position.clientId}
                                    className={[
                                        "template-position-field",
                                        position.fieldType === "SIGNATURE"
                                            ? "signature"
                                            : "",
                                        position.clientId === selectedId
                                            ? "selected"
                                            : "",
                                    ].filter(Boolean).join(" ")}
                                    style={{
                                        left: `${position.xPosition * 100}%`,
                                        top: `${position.yPosition * 100}%`,
                                        width: `${position.width * 100}%`,
                                        height: `${position.height * 100}%`,
                                    }}
                                    onClick={(event) => event.stopPropagation()}
                                    onPointerDown={(event) =>
                                        beginPointerAction(event, position, "move")
                                    }
                                    onPointerMove={continuePointerAction}
                                    onPointerUp={endPointerAction}
                                    onPointerCancel={endPointerAction}
                                >
                                    <span>{position.fieldLabel}</span>
                                    <small>
                                        {position.signerRole || position.valueSource}
                                    </small>
                                    <button
                                        type="button"
                                        className="template-position-resize"
                                        aria-label={`Resize ${position.fieldLabel}`}
                                        onPointerDown={(event) =>
                                            beginPointerAction(
                                                event,
                                                position,
                                                "resize"
                                            )
                                        }
                                        onPointerMove={(event) => {
                                            event.stopPropagation();
                                            continuePointerAction(event);
                                        }}
                                        onPointerUp={(event) => {
                                            event.stopPropagation();
                                            endPointerAction(event);
                                        }}
                                        onPointerCancel={(event) => {
                                            event.stopPropagation();
                                            endPointerAction(event);
                                        }}
                                    />
                                </div>
                            ))}
                        </div>
                    </div>
                    <small className="template-page-break-hint">
                        Use &lt;!-- pagebreak --&gt; in the content to preview
                        different text on each page.
                    </small>
                </div>

                <aside className="template-field-inspector">
                    <div className="template-designer-panel-heading">
                        <strong>Field settings</strong>
                        <small>{positions.length} field(s) in version</small>
                    </div>
                    {!selectedPosition ? (
                        <div className="template-inspector-empty">
                            Select a field on the page to edit its key, source,
                            role and dimensions.
                        </div>
                    ) : (
                        <div className="template-inspector-form">
                            <InspectorInput
                                label="Label"
                                value={selectedPosition.fieldLabel}
                                onChange={(value) =>
                                    updateField(selectedId, { fieldLabel: value })
                                }
                            />
                            <InspectorInput
                                label="Attribute Key"
                                value={selectedPosition.attributeKey}
                                onChange={(value) =>
                                    updateField(selectedId, {
                                        attributeKey: value
                                            .toLowerCase()
                                            .replace(/[^a-z0-9_]/g, "_"),
                                    })
                                }
                            />
                            <InspectorSelect
                                label="Field Type"
                                value={selectedPosition.fieldType}
                                options={["TEXT", "DATE", "SIGNATURE", "CHECKBOX"]}
                                onChange={(fieldType) =>
                                    updateField(selectedId, {
                                        fieldType,
                                        valueSource: fieldType === "SIGNATURE"
                                            ? "CURRENT_SIGNER"
                                            : selectedPosition.valueSource,
                                        signerRole: fieldType === "SIGNATURE"
                                            && !selectedPosition.signerRole
                                            ? "DIRECTOR"
                                            : selectedPosition.signerRole,
                                    })
                                }
                            />
                            <InspectorSelect
                                label="Value Source"
                                value={selectedPosition.valueSource}
                                options={["CONTRACT", "CURRENT_SIGNER", "MANUAL"]}
                                disabled={selectedPosition.fieldType === "SIGNATURE"}
                                onChange={(valueSource) =>
                                    updateField(selectedId, {
                                        valueSource,
                                        signerRole: valueSource === "CURRENT_SIGNER"
                                            ? selectedPosition.signerRole || "DIRECTOR"
                                            : null,
                                    })
                                }
                            />
                            <InspectorSelect
                                label="Signer Role"
                                value={selectedPosition.signerRole || "NONE"}
                                options={
                                    selectedPosition.fieldType === "SIGNATURE"
                                    || selectedPosition.valueSource === "CURRENT_SIGNER"
                                        ? ["DIRECTOR", "PARTNER"]
                                        : ["NONE", "DIRECTOR", "PARTNER"]
                                }
                                onChange={(signerRole) =>
                                    updateField(selectedId, {
                                        signerRole: signerRole === "NONE"
                                            ? null
                                            : signerRole,
                                    })
                                }
                            />
                            <InspectorInput
                                label="Page"
                                type="number"
                                min="1"
                                max={pageCount}
                                value={selectedPosition.pageNumber}
                                onChange={(value) => {
                                    const pageNumber = clamp(
                                        Number.parseInt(value, 10) || 1,
                                        1,
                                        pageCount
                                    );
                                    updateField(selectedId, { pageNumber });
                                    setCurrentPage(pageNumber);
                                }}
                            />
                            <div className="template-geometry-grid">
                                {[
                                    ["X %", "xPosition"],
                                    ["Y %", "yPosition"],
                                    ["Width %", "width"],
                                    ["Height %", "height"],
                                ].map(([label, name]) => (
                                    <InspectorInput
                                        key={name}
                                        label={label}
                                        type="number"
                                        min="0"
                                        max="100"
                                        step="0.1"
                                        value={roundPercent(
                                            selectedPosition[name] * 100
                                        )}
                                        onChange={(value) =>
                                            updateSelectedGeometry(name, value)
                                        }
                                    />
                                ))}
                            </div>
                            <label className="template-inspector-checkbox">
                                <input
                                    type="checkbox"
                                    checked={selectedPosition.required}
                                    onChange={(event) =>
                                        updateField(selectedId, {
                                            required: event.target.checked,
                                        })
                                    }
                                />
                                Required field
                            </label>
                            <button
                                type="button"
                                className="template-remove-position"
                                onClick={removeSelectedField}
                            >
                                <IconTrash size={17} />
                                Remove field
                            </button>
                        </div>
                    )}
                </aside>
            </div>
        </section>
    );
}

function InspectorInput({ label, onChange, ...inputProps }) {
    return (
        <label className="template-inspector-control">
            <span>{label}</span>
            <input
                {...inputProps}
                onChange={(event) => onChange(event.target.value)}
            />
        </label>
    );
}

function InspectorSelect({ label, options, onChange, ...selectProps }) {
    return (
        <label className="template-inspector-control">
            <span>{label}</span>
            <select
                {...selectProps}
                onChange={(event) => onChange(event.target.value)}
            >
                {options.map((option) => (
                    <option key={option} value={option}>
                        {option.replaceAll("_", " ")}
                    </option>
                ))}
            </select>
        </label>
    );
}

function clamp(value, minimum, maximum) {
    return Math.min(Math.max(value, minimum), maximum);
}

function roundCoordinate(value) {
    return Math.round(Number(value) * 10000) / 10000;
}

function roundPercent(value) {
    return Math.round(Number(value) * 10) / 10;
}

export default TemplatePositionDesigner;
