import { IconChevronLeft, IconChevronRight } from "@tabler/icons-react"

function SignaturePagination({ currentPage, totalPages, totalResults, pageSize, onPageChange, onPageSizeChange }) {
    return (
        <div className="signature-pagination">
            <span className="results-text">Showing {totalResults === 0 ? 0 : 1}–{Math.min(pageSize, totalResults)} of {totalResults} results</span>

            <div className="pagination-controls">
                <button disabled={currentPage === 1} onClick={() => onPageChange(currentPage - 1)}>
                    <IconChevronLeft size={16} />
                </button>
                <span className="page-number">{currentPage}</span>
                <button disabled={currentPage === totalPages} onClick={() => onPageChange(currentPage + 1)}>
                    <IconChevronRight size={16} />
                </button>

                <select value={pageSize} onChange={(e) => onPageSizeChange(Number(e.target.value))}>
                    <option value={10}>10 / page</option>
                    <option value={20}>20 / page</option>
                    <option value={50}>50 / page</option>
                </select>
            </div>
        </div>
    )
}

export default SignaturePagination