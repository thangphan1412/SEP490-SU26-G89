import { Eye, Pencil } from "lucide-react";
import { useNavigate } from "react-router-dom";

function SignatureRow({ signature }) {

    const navigate = useNavigate();

    const handleDetail = () => {
        navigate(
            `/signatures/${signature.id}`
        );
    };

    const handleEdit = () => {
        navigate(
            `/signature-management/update/${signature.id}`
        );
    };

    return (
        <tr>

            <td>
                <strong>
                    {signature.signatureName}
                </strong>
            </td>

            <td>
                {signature.type}
            </td>

            <td>
                <span
                    className={
                        signature.status === "ACTIVE"
                            ? "badge bg-success"
                            : "badge bg-secondary"
                    }
                >
                    {signature.status}
                </span>
            </td>

            <td>
                {signature.default ? "Yes" : "No"}
            </td>

            <td>
                {signature.uploadAt || "-"}
            </td>

            <td className="text-end">

                <button
                    type="button"
                    className="btn btn-sm btn-light me-1"
                    onClick={handleDetail}
                >
                    <Eye size={14} />
                </button>

                <button
                    type="button"
                    className="btn btn-sm btn-light"
                    onClick={handleEdit}
                >
                    <Pencil size={14} />
                </button>

            </td>

        </tr>
    );
}

export default SignatureRow;
