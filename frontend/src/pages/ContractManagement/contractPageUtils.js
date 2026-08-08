export const CONTRACT_PAGE_BREAK_MARKER = "<!-- pagebreak -->";

const PAGE_BREAK_PATTERN = /(?:\r?\n)?<!--\s*pagebreak\s*-->(?:\r?\n)?/gi;

export function splitContractPages(content, minimumPageCount = 1) {
    const pages = String(content || "").split(PAGE_BREAK_PATTERN);
    const requiredPageCount = Math.max(
        1,
        Number.parseInt(minimumPageCount, 10) || 1
    );

    while (pages.length < requiredPageCount) {
        pages.push("");
    }

    return pages;
}

export function joinContractPages(pages) {
    const normalizedPages = Array.isArray(pages) && pages.length > 0
        ? pages
        : [""];

    return normalizedPages.join(`\n${CONTRACT_PAGE_BREAK_MARKER}\n`);
}
