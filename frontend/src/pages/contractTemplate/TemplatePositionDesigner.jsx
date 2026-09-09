import { useEffect, useMemo, useRef, useState } from "react";
import {
    IconCalendar,
    IconArrowDown,
    IconArrowUp,
    IconCheckbox,
    IconEye,
    IconFileText,
    IconPlus,
    IconSignature,
    IconTrash,
    IconUser,
} from "@tabler/icons-react";
import {
    joinContractPages,
    splitContractPages,
} from "../ContractManagement/contractPageUtils.js";
import { createPositionClientId } from "./templatePositionUtils.js";
import { DEFAULT_TEMPLATE_BLOCKS } from "./templateBlockUtils.js";

const MAX_PAGE_COUNT = 50;
const STANDARD_FIELDS = [
    {
        key: "contract_number",
        label: "Contract Number",
        fieldType: "TEXT",
        valueSource: "CONTRACT",
        signerRole: null,
        systemField: true,
        required: true,
        icon: IconFileText,
        sample: "CON-2026-0001",
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
        sample: "Service Supply Contract",
    },
    {
        key: "project_name",
        label: "Project Name",
        fieldType: "TEXT",
        valueSource: "CONTRACT",
        signerRole: null,
        systemField: true,
        required: true,
        icon: IconFileText,
        sample: "Contract Management Project",
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
        sample: "08/08/2026",
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
        sample: "31/12/2026",
    },
    {
        key: "contract_value",
        label: "Contract Value",
        fieldType: "TEXT",
        valueSource: "MANUAL",
        signerRole: null,
        systemField: true,
        required: true,
        icon: IconCheckbox,
        sample: "150,000,000 VND",
    },
    {
        key: "director_name",
        label: "Director Full Name",
        fieldType: "TEXT",
        valueSource: "CURRENT_SIGNER",
        signerRole: "DIRECTOR",
        systemField: true,
        required: true,
        icon: IconUser,
        sample: "Nguyen Van Director",
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
        sample: "[Director signature appears after signing]",
    },
    {
        key: "partner_name",
        label: "Partner Full Name",
        fieldType: "TEXT",
        valueSource: "CURRENT_SIGNER",
        signerRole: "PARTNER",
        systemField: true,
        required: true,
        icon: IconUser,
        sample: "Tran Thi Partner",
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
        sample: "[Partner signature appears after signing]",
    },
];

const FIELD_BY_KEY = new Map(
    STANDARD_FIELDS.map((field) => [field.key, field])
);
const PLACEHOLDER_PATTERN = /\{\{\s*([a-z][a-z0-9_]*)\s*}}/gi;
const BLOCK_LABELS = {
    NATIONAL_HEADER: "Quốc hiệu và tiêu ngữ",
    CONTRACT_HEADING: "Tiêu đề hợp đồng",
    LEGAL_INTRODUCTION: "Căn cứ và lời mở đầu",
    PARTY_A: "Thông tin Bên A",
    PARTY_B: "Thông tin Bên B",
    CLAUSE_HEADING: "Tiêu đề phần điều khoản",
    CONTENT: "Nội dung điều khoản",
    SIGNATURE_SECTION: "Khu vực chữ ký",
};

function TemplatePositionDesigner({
    content = "",
    pageCount = 1,
    positions = [],
    blocks = DEFAULT_TEMPLATE_BLOCKS,
    onChange,
    onContentChange,
}) {
    const [currentPage, setCurrentPage] = useState(1);
    const layoutBlocks = Array.isArray(blocks) && blocks.length > 0
        ? blocks
        : DEFAULT_TEMPLATE_BLOCKS;
    const editorRef = useRef(null);
    const selectionRef = useRef(null);
    const pages = useMemo(
        () => splitContractPages(content, pageCount),
        [content, pageCount]
    );
    const totalPages = pages.length;
    const activePage = Math.min(currentPage, totalPages);
    const pageContent = pages[activePage - 1] || "";
    const pageUsedKeys = useMemo(
        () => extractPlaceholderKeys(pageContent),
        [pageContent]
    );
    const allUsedKeys = useMemo(
        () => extractPlaceholderKeys(content),
        [content]
    );
    const manualFields = useMemo(
        () => uniqueManualFields(positions),
        [positions]
    );
    const previewContent = useMemo(
        () => buildPreview(pageContent, positions),
        [pageContent, positions]
    );

    useEffect(() => {
        const canonicalContent = joinContractPages(pages);
        const synchronizedPositions = synchronizePositionsWithPages(
            pages,
            positions
        );

        if (canonicalContent !== content) {
            onContentChange(canonicalContent);
        }
        if (
            synchronizedPositions !== positions
            || Number(pageCount) !== totalPages
        ) {
            onChange({
                pageCount: totalPages,
                positions: synchronizedPositions,
            });
        }
    }, [content, onChange, onContentChange, pageCount, pages, positions, totalPages]);

    const commitPages = (nextPages, nextPositions = positions) => {
        const normalizedPages = nextPages.length > 0 ? nextPages : [""];
        const nextPageCount = normalizedPages.length;
        const synchronizedPositions = synchronizePositionsWithPages(
            normalizedPages,
            nextPositions
        );

        onContentChange(joinContractPages(normalizedPages));
        if (
            synchronizedPositions !== positions
            || Number(pageCount) !== nextPageCount
        ) {
            onChange({
                pageCount: nextPageCount,
                positions: synchronizedPositions,
            });
        }
    };

    const updateCurrentPageContent = (
        nextPageContent,
        nextPositions = positions
    ) => {
        const nextPages = [...pages];
        nextPages[activePage - 1] = nextPageContent;
        commitPages(nextPages, nextPositions);
    };

    const selectPage = (pageNumber) => {
        selectionRef.current = null;
        setCurrentPage(pageNumber);
    };

    const addPage = () => {
        if (totalPages >= MAX_PAGE_COUNT) {
            return;
        }

        const nextPages = [...pages, ""];
        selectionRef.current = null;
        commitPages(nextPages);
        setCurrentPage(nextPages.length);
    };

    const removeCurrentPage = () => {
        if (totalPages === 1) {
            return;
        }

        const hasPageData = pageContent.trim()
            || positions.some(
                (position) => Number(position.pageNumber) === activePage
            );
        if (
            hasPageData
            && !window.confirm(
                `Remove Page ${activePage} and all fields placed on it?`
            )
        ) {
            return;
        }

        const nextPages = pages.filter(
            (_page, index) => index !== activePage - 1
        );
        const nextPositions = positions
            .filter(
                (position) => Number(position.pageNumber) !== activePage
            )
            .map((position) => ({
                ...position,
                pageNumber: Number(position.pageNumber) > activePage
                    ? Number(position.pageNumber) - 1
                    : Number(position.pageNumber),
            }));

        selectionRef.current = null;
        commitPages(nextPages, nextPositions);
        setCurrentPage(Math.min(activePage, nextPages.length));
    };

    const insertPlaceholder = (field, sourcePositions = positions) => {
        const token = `{{${field.key}}}`;
        const savedSelection = selectionRef.current;
        const start = Math.min(
            savedSelection?.start ?? pageContent.length,
            pageContent.length
        );
        const end = Math.min(
            savedSelection?.end ?? pageContent.length,
            pageContent.length
        );
        const prefix = pageContent.slice(0, start);
        const suffix = pageContent.slice(end);
        const needsLeadingBreak = start === pageContent.length
            && pageContent.length > 0
            && !pageContent.endsWith("\n");
        const insertion = `${needsLeadingBreak ? "\n" : ""}${token}`;
        const nextPageContent = `${prefix}${insertion}${suffix}`;
        const nextPositions = ensurePagePosition(
            sourcePositions,
            field,
            activePage
        );
        const caretPosition = start + insertion.length;

        updateCurrentPageContent(nextPageContent, nextPositions);
        selectionRef.current = {
            start: caretPosition,
            end: caretPosition,
        };
        window.requestAnimationFrame(() => {
            editorRef.current?.focus();
            editorRef.current?.setSelectionRange(caretPosition, caretPosition);
        });
    };

    const addManualField = () => {
        const attributeKey = createUniqueManualKey(positions);
        const field = {
            key: attributeKey,
            label: "Custom Field",
            fieldType: "TEXT",
            valueSource: "MANUAL",
            signerRole: null,
            systemField: false,
            required: false,
        };
        const nextPositions = [
            ...positions,
            createAutomaticPosition(
                field,
                activePage,
                positions.length
            ),
        ];

        insertPlaceholder(field, nextPositions);
    };

    const updateManualField = (attributeKey, patch) => {
        const currentField = positions.find(
            (position) => position.attributeKey === attributeKey
        );
        if (!currentField) {
            return;
        }

        const normalizedPatch = { ...patch };
        if (Object.hasOwn(patch, "attributeKey")) {
            normalizedPatch.attributeKey = normalizeAttributeKey(
                patch.attributeKey
            );
            if (!normalizedPatch.attributeKey) {
                return;
            }
        }

        const nextPositions = positions.map((position) =>
            position.attributeKey === attributeKey
                ? { ...position, ...normalizedPatch }
                : position
        );
        let nextContent = content;

        if (
            normalizedPatch.attributeKey
            && normalizedPatch.attributeKey !== attributeKey
        ) {
            nextContent = replacePlaceholderKey(
                content,
                attributeKey,
                normalizedPatch.attributeKey
            );
        }

        onContentChange(nextContent);
        onChange({ pageCount: totalPages, positions: nextPositions });
    };

    const removeManualField = (field) => {
        const nextPositions = positions.filter(
            (position) => position.attributeKey !== field.attributeKey
        );
        const nextContent = removePlaceholder(content, field.attributeKey);

        onContentChange(nextContent);
        onChange({ pageCount: totalPages, positions: nextPositions });
    };

    const updateBlock = (key, patch) => {
        onChange({
            blocks: layoutBlocks.map((block) =>
                block.key === key ? { ...block, ...patch } : block
            ),
        });
    };

    const moveBlock = (index, offset) => {
        const targetIndex = index + offset;
        if (targetIndex < 0 || targetIndex >= layoutBlocks.length) {
            return;
        }
        const nextBlocks = [...layoutBlocks];
        [nextBlocks[index], nextBlocks[targetIndex]] = [
            nextBlocks[targetIndex],
            nextBlocks[index],
        ];
        onChange({ blocks: nextBlocks });
    };

    return (
        <section className="template-simple-editor">
            <header className="template-simple-editor-header">
                <div>
                    <h3>Build Template Content</h3>
                    <p>
                        Each tab is a contract page. Add the same name or signature
                        field to every page where it must appear after signing.
                    </p>
                </div>
                <span className="template-simple-field-count">
                    {totalPages} page(s) · {allUsedKeys.size} field type(s)
                </span>
            </header>

            <div className="template-simple-editor-body">
                <section className="template-block-editor">
                    <div className="template-section-heading">
                        <strong>Cấu trúc tài liệu PDF</strong>
                        <small>
                            Bật/tắt, sửa nội dung và đổi thứ tự tất cả phần của
                            hợp đồng. Có thể dùng placeholder dạng {"{{contract_title}}"}.
                        </small>
                    </div>
                    <div className="template-block-list">
                        {layoutBlocks.map((block, index) => (
                            <TemplateBlockRow
                                key={block.key}
                                block={block}
                                index={index}
                                total={layoutBlocks.length}
                                onChange={(patch) => updateBlock(block.key, patch)}
                                onMove={(offset) => moveBlock(index, offset)}
                            />
                        ))}
                    </div>
                </section>

                <div className="template-page-navigation">
                    <div className="template-page-tabs" role="tablist">
                        {pages.map((_page, index) => {
                            const pageNumber = index + 1;
                            return (
                                <button
                                    type="button"
                                    role="tab"
                                    aria-selected={activePage === pageNumber}
                                    className={
                                        activePage === pageNumber ? "active" : ""
                                    }
                                    key={pageNumber}
                                    onClick={() => selectPage(pageNumber)}
                                >
                                    Page {pageNumber}
                                </button>
                            );
                        })}
                    </div>
                    <div className="template-page-actions">
                        <button
                            type="button"
                            onClick={addPage}
                            disabled={totalPages >= MAX_PAGE_COUNT}
                        >
                            <IconPlus size={16} />
                            Add page
                        </button>
                        <button
                            type="button"
                            className="danger"
                            onClick={removeCurrentPage}
                            disabled={totalPages === 1}
                        >
                            <IconTrash size={16} />
                            Remove page
                        </button>
                    </div>
                </div>

                <div className="template-page-guidance">
                    Page {activePage} of {totalPages}. PDF export starts each tab
                    on a new page; normal overflow can still continue to another
                    physical PDF page.
                </div>

                <div className="template-insert-panel">
                    <div className="template-section-heading">
                        <strong>Insert a dynamic field on Page {activePage}</strong>
                        <small>
                            No dragging or coordinates required. Reuse signer fields
                            on as many pages as needed.
                        </small>
                    </div>
                    <div className="template-insert-grid">
                        {STANDARD_FIELDS.map((field) => {
                            const FieldIcon = field.icon;
                            const isUsed = pageUsedKeys.has(field.key);
                            return (
                                <button
                                    type="button"
                                    className={`template-insert-button${
                                        isUsed ? " used" : ""
                                    }`}
                                    key={field.key}
                                    onClick={() => insertPlaceholder(field)}
                                    title={`Insert {{${field.key}}} on Page ${activePage}`}
                                >
                                    <FieldIcon size={17} />
                                    <span>
                                        <strong>{field.label}</strong>
                                        <small>
                                            {isUsed
                                                ? `Used on Page ${activePage}`
                                                : `{{${field.key}}}`}
                                        </small>
                                    </span>
                                </button>
                            );
                        })}
                    </div>
                </div>

                <div className="template-content-preview-grid">
                    <div className="template-content-pane">
                        <div className="template-section-heading">
                            <strong>Page {activePage} content</strong>
                            <small>
                                Place the cursor where a value should appear, then
                                choose a field above.
                            </small>
                        </div>
                        <textarea
                            ref={editorRef}
                            id="templateContent"
                            className="form-control template-content-textarea"
                            value={pageContent}
                            onChange={(event) => {
                                selectionRef.current = {
                                    start: event.target.selectionStart,
                                    end: event.target.selectionEnd,
                                };
                                updateCurrentPageContent(event.target.value);
                            }}
                            onSelect={(event) => {
                                selectionRef.current = {
                                    start: event.target.selectionStart,
                                    end: event.target.selectionEnd,
                                };
                            }}
                            placeholder={`Write the reusable content for Page ${activePage}...`}
                            required={activePage === 1}
                        />
                    </div>

                    <div className="template-preview-pane">
                        <div className="template-section-heading template-preview-heading">
                            <span>
                                <IconEye size={18} />
                                <strong>Page {activePage} preview</strong>
                            </span>
                            <small>Sample values only</small>
                        </div>
                        <div className="template-content-preview">
                            {previewContent || (
                                <span className="template-preview-empty">
                                    Start writing to preview Page {activePage}.
                                </span>
                            )}
                        </div>
                    </div>
                </div>

                <section className="template-manual-fields">
                    <div className="template-manual-fields-header">
                        <div className="template-section-heading">
                            <strong>Fields entered when creating a contract</strong>
                            <small>
                                One input value can be reused on multiple pages.
                            </small>
                        </div>
                        <button
                            type="button"
                            className="template-add-manual-button"
                            onClick={addManualField}
                        >
                            <IconPlus size={17} />
                            Add custom field
                        </button>
                    </div>

                    {manualFields.length === 0 ? (
                        <div className="template-manual-empty">
                            Insert Contract Value above or add a custom field if
                            the employee must enter extra information.
                        </div>
                    ) : (
                        <div className="template-manual-field-list">
                            {manualFields.map((field) => (
                                <ManualFieldRow
                                    key={field.attributeKey}
                                    field={field}
                                    pageNumbers={findFieldPages(
                                        pages,
                                        field.attributeKey
                                    )}
                                    currentPage={activePage}
                                    onChange={(patch) =>
                                        updateManualField(
                                            field.attributeKey,
                                            patch
                                        )
                                    }
                                    onInsert={() =>
                                        insertPlaceholder(
                                            positionToField(field)
                                        )
                                    }
                                    onRemove={() => removeManualField(field)}
                                />
                            ))}
                        </div>
                    )}
                </section>
            </div>
        </section>
    );
}

function TemplateBlockRow({
    block,
    index,
    total,
    onChange,
    onMove,
}) {
    const showHeading = !["LEGAL_INTRODUCTION", "CONTENT"].includes(block.type);
    const showContent = [
        "NATIONAL_HEADER",
        "CONTRACT_HEADING",
        "LEGAL_INTRODUCTION",
    ].includes(block.type);
    const isSignature = block.type === "SIGNATURE_SECTION";

    return (
        <article className={`template-block-row${block.enabled ? "" : " disabled"}`}>
            <div className="template-block-toolbar">
                <label className="template-block-enabled">
                    <input
                        type="checkbox"
                        checked={Boolean(block.enabled)}
                        onChange={(event) => onChange({ enabled: event.target.checked })}
                    />
                    <strong>{BLOCK_LABELS[block.type] || block.type}</strong>
                </label>
                <div className="template-block-order-actions">
                    <button
                        type="button"
                        disabled={index === 0}
                        onClick={() => onMove(-1)}
                        aria-label="Move block up"
                    >
                        <IconArrowUp size={16} />
                    </button>
                    <button
                        type="button"
                        disabled={index === total - 1}
                        onClick={() => onMove(1)}
                        aria-label="Move block down"
                    >
                        <IconArrowDown size={16} />
                    </button>
                </div>
            </div>

            {showHeading && (
                <label>
                    <span>Tiêu đề</span>
                    <input
                        value={block.heading || ""}
                        disabled={!block.enabled}
                        onChange={(event) => onChange({ heading: event.target.value })}
                    />
                </label>
            )}
            {showContent && (
                <label>
                    <span>{block.type === "LEGAL_INTRODUCTION" ? "Nội dung" : "Dòng phụ"}</span>
                    <textarea
                        rows={block.type === "LEGAL_INTRODUCTION" ? 4 : 2}
                        value={block.content || ""}
                        disabled={!block.enabled}
                        onChange={(event) => onChange({ content: event.target.value })}
                    />
                </label>
            )}
            {isSignature && (
                <div className="template-block-signature-labels">
                    <label>
                        <span>Nhãn cột trái</span>
                        <input
                            value={block.leftLabel || ""}
                            disabled={!block.enabled}
                            onChange={(event) => onChange({ leftLabel: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>Nhãn cột phải</span>
                        <input
                            value={block.rightLabel || ""}
                            disabled={!block.enabled}
                            onChange={(event) => onChange({ rightLabel: event.target.value })}
                        />
                    </label>
                </div>
            )}
            {block.type === "CONTENT" && (
                <small className="template-block-note">
                    Nội dung chi tiết được chỉnh theo từng trang ở phần bên dưới.
                </small>
            )}
        </article>
    );
}

function ManualFieldRow({
    field,
    pageNumbers,
    currentPage,
    onChange,
    onInsert,
    onRemove,
}) {
    const isBuiltIn = field.attributeKey === "contract_value";

    return (
        <div className="template-manual-field-row">
            <label>
                <span>Label</span>
                <input
                    value={field.fieldLabel}
                    onChange={(event) =>
                        onChange({ fieldLabel: event.target.value })
                    }
                />
                <small className="template-manual-page-usage">
                    {pageNumbers.length > 0
                        ? `Used on page(s): ${pageNumbers.join(", ")}`
                        : "Not inserted in content"}
                </small>
            </label>
            <label>
                <span>Attribute key</span>
                <input
                    value={field.attributeKey}
                    readOnly={isBuiltIn}
                    onChange={(event) =>
                        onChange({ attributeKey: event.target.value })
                    }
                />
            </label>
            <label>
                <span>Input type</span>
                <select
                    value={field.fieldType}
                    onChange={(event) =>
                        onChange({ fieldType: event.target.value })
                    }
                >
                    <option value="TEXT">Text</option>
                    <option value="DATE">Date</option>
                    <option value="CHECKBOX">Checkbox</option>
                </select>
            </label>
            <label className="template-manual-required">
                <input
                    type="checkbox"
                    checked={Boolean(field.required)}
                    onChange={(event) =>
                        onChange({ required: event.target.checked })
                    }
                />
                Required
            </label>
            <div className="template-manual-actions">
                <button type="button" onClick={onInsert}>
                    Insert on Page {currentPage}
                </button>
                <button
                    type="button"
                    className="danger"
                    onClick={onRemove}
                    aria-label={`Remove ${field.fieldLabel}`}
                >
                    <IconTrash size={16} />
                </button>
            </div>
        </div>
    );
}

function synchronizePositionsWithPages(pages, positions) {
    const definitions = new Map();
    positions.forEach((position) => {
        if (!definitions.has(position.attributeKey)) {
            definitions.set(position.attributeKey, positionToField(position));
        }
    });
    STANDARD_FIELDS.forEach((field) => definitions.set(field.key, field));

    let nextPositions = positions;
    pages.forEach((pageContent, pageIndex) => {
        const pageNumber = pageIndex + 1;
        extractPlaceholderKeys(pageContent).forEach((key) => {
            const definition = definitions.get(key);
            if (
                definition
                && !nextPositions.some(
                    (position) =>
                        position.attributeKey === key
                        && Number(position.pageNumber) === pageNumber
                )
            ) {
                nextPositions = [
                    ...nextPositions,
                    createAutomaticPosition(
                        definition,
                        pageNumber,
                        nextPositions.length
                    ),
                ];
            }
        });
    });

    return nextPositions;
}

function ensurePagePosition(positions, field, pageNumber) {
    if (
        positions.some(
            (position) =>
                position.attributeKey === field.key
                && Number(position.pageNumber) === pageNumber
        )
    ) {
        return positions;
    }

    return [
        ...positions,
        createAutomaticPosition(field, pageNumber, positions.length),
    ];
}

function createAutomaticPosition(field, pageNumber, index) {
    const isSignature = field.fieldType === "SIGNATURE";
    const height = isSignature ? 0.08 : 0.05;

    return {
        clientId: createPositionClientId(),
        attributeKey: field.key,
        fieldLabel: field.label,
        pageNumber,
        xPosition: 0.08,
        yPosition: Math.min(0.08 + (index % 10) * 0.075, 1 - height),
        width: isSignature ? 0.36 : 0.84,
        height,
        fieldType: field.fieldType,
        valueSource: field.valueSource,
        signerRole: field.signerRole || null,
        systemField: Boolean(field.systemField),
        required: Boolean(field.required),
    };
}

function positionToField(position) {
    return {
        key: position.attributeKey,
        label: position.fieldLabel,
        fieldType: position.fieldType,
        valueSource: position.valueSource,
        signerRole: position.signerRole || null,
        systemField: Boolean(position.systemField),
        required: Boolean(position.required),
    };
}

function uniqueManualFields(positions) {
    const fields = new Map();
    positions
        .filter((position) => position.valueSource === "MANUAL")
        .forEach((position) => {
            if (!fields.has(position.attributeKey)) {
                fields.set(position.attributeKey, position);
            }
        });
    return [...fields.values()];
}

function findFieldPages(pages, attributeKey) {
    return pages
        .map((pageContent, index) =>
            extractPlaceholderKeys(pageContent).has(attributeKey)
                ? index + 1
                : null
        )
        .filter(Boolean);
}

function extractPlaceholderKeys(content) {
    const keys = new Set();
    String(content || "").replace(
        PLACEHOLDER_PATTERN,
        (_match, key) => {
            keys.add(key.toLowerCase());
            return _match;
        }
    );
    return keys;
}

function buildPreview(content, positions) {
    const positionByKey = new Map(
        positions.map((position) => [position.attributeKey, position])
    );

    return String(content || "").replace(
        PLACEHOLDER_PATTERN,
        (_match, rawKey) => {
            const key = rawKey.toLowerCase();
            const standardField = FIELD_BY_KEY.get(key);
            if (standardField) {
                return standardField.sample;
            }

            const position = positionByKey.get(key);
            return `[${position?.fieldLabel || humanizeKey(key)}]`;
        }
    );
}

function createUniqueManualKey(positions) {
    const existingKeys = new Set(
        positions.map((position) => position.attributeKey)
    );
    let sequence = 1;
    let candidate = "custom_field";

    while (existingKeys.has(candidate)) {
        sequence += 1;
        candidate = `custom_field_${sequence}`;
    }

    return candidate;
}

function normalizeAttributeKey(value) {
    return String(value || "")
        .toLowerCase()
        .replace(/[^a-z0-9_]/g, "_")
        .replace(/_+/g, "_")
        .replace(/^_+/, "");
}

function replacePlaceholderKey(content, previousKey, nextKey) {
    const pattern = new RegExp(
        `\\{\\{\\s*${escapeRegExp(previousKey)}\\s*}}`,
        "gi"
    );
    return String(content || "").replace(pattern, `{{${nextKey}}}`);
}

function removePlaceholder(content, key) {
    const pattern = new RegExp(
        `\\{\\{\\s*${escapeRegExp(key)}\\s*}}`,
        "gi"
    );
    return String(content || "").replace(pattern, "");
}

function escapeRegExp(value) {
    return String(value).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function humanizeKey(key) {
    return String(key || "")
        .split("_")
        .filter(Boolean)
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");
}

export default TemplatePositionDesigner;
