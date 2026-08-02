import { IconDots } from "@tabler/icons-react"
import StatusBadge from "./StatusBadge.jsx";


function SignatureRow({ signature }) {
    const { name, type, usedIn, status, updatedAt, avatarText, avatarColor } = signature

    return (
        <tr className="signature-row">
            <td>
                <div className="signature-name-cell">
                    <div className="signature-avatar" style={{ color: avatarColor }}>
                        {avatarText}
                    </div>
                    <span className="signature-name-text">{name}</span>
                </div>
            </td>
            <td className="text-muted">{type}</td>
            <td className="text-muted">{usedIn}</td>
            <td><StatusBadge status={status} /></td>
            <td className="text-muted">{updatedAt}</td>
            <td className="text-end">
                <button className="row-action-btn">
                    <IconDots size={18} />
                </button>
            </td>
        </tr>
    )
}

export default SignatureRow