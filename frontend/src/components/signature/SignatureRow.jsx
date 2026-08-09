import { IconDots } from "@tabler/icons-react"
import StatusBadge from "./StatusBadge.jsx";


function SignatureRow({ signature }) {
    const { signatureName, type, status,default: isDefault, uploadAt, avatarText, avatarColor } = signature

    return (
        <tr className="signature-row">
            <td>
                <div className="signature-name-cell">

                    <span className="signature-name-text">{signatureName}</span>
                </div>
            </td>
            <td className="text-muted">{type}</td>

            <td><StatusBadge status={status} /></td>
            <td className="text-muted">{String(isDefault)}</td>
            <td className="text-muted">{uploadAt}</td>
            <td className="text-end">
                <button className="row-action-btn">
                    <IconDots size={18} />
                </button>
            </td>
        </tr>
    )
}

export default SignatureRow