export const DEFAULT_TEMPLATE_BLOCKS = [
    {
        key: "national_header",
        type: "NATIONAL_HEADER",
        enabled: true,
        heading: "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM",
        content: "Độc lập - Tự do - Hạnh phúc",
        leftLabel: null,
        rightLabel: null,
    },
    {
        key: "contract_heading",
        type: "CONTRACT_HEADING",
        enabled: true,
        heading: "{{contract_title}}",
        content: "Số: {{contract_number}}",
        leftLabel: null,
        rightLabel: null,
    },
    {
        key: "legal_introduction",
        type: "LEGAL_INTRODUCTION",
        enabled: true,
        heading: null,
        content: "- Căn cứ các quy định pháp luật hiện hành;\n"
            + "- Căn cứ nhu cầu và sự thỏa thuận của các bên.\n"
            + "Hôm nay, {{contract_date}}, các bên gồm:",
        leftLabel: null,
        rightLabel: null,
    },
    {
        key: "party_a",
        type: "PARTY_A",
        enabled: true,
        heading: "BÊN A",
        content: null,
        leftLabel: null,
        rightLabel: null,
    },
    {
        key: "party_b",
        type: "PARTY_B",
        enabled: true,
        heading: "BÊN B",
        content: null,
        leftLabel: null,
        rightLabel: null,
    },
    {
        key: "clause_heading",
        type: "CLAUSE_HEADING",
        enabled: true,
        heading: "CÁC ĐIỀU KHOẢN HỢP ĐỒNG",
        content: null,
        leftLabel: null,
        rightLabel: null,
    },
    {
        key: "main_content",
        type: "CONTENT",
        enabled: true,
        heading: null,
        content: null,
        leftLabel: null,
        rightLabel: null,
    },
    {
        key: "signature_section",
        type: "SIGNATURE_SECTION",
        enabled: true,
        heading: "ĐẠI DIỆN CÁC BÊN",
        content: null,
        leftLabel: "BÊN A",
        rightLabel: "BÊN B",
    },
];

export function cloneTemplateBlocks(blocks) {
    const source = Array.isArray(blocks) && blocks.length > 0
        ? blocks
        : DEFAULT_TEMPLATE_BLOCKS;
    return source.map((block) => ({ ...block }));
}
