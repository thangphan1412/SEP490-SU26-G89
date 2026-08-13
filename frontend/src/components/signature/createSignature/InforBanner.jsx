import { Info } from "lucide-react";

function InfoBanner({ text }) {
    return (
        <div
            className="d-flex align-items-center gap-2 rounded p-2 mb-3"
            style={{
                backgroundColor: "#eef2ff",
                border: "1px solid #e0e7ff",
                color: "#4f46e5",
                fontSize: "11px",
            }}
        >
            <Info size={15} />
            {text}
        </div>
    );
}

export default InfoBanner;