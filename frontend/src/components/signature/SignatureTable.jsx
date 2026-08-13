import SignatureRow from "./SignatureRow"

function SignatureTable({ signatures }) {
    return (
        <table className="signature-table">
            <thead>
            <tr>
                <th>Signature Name</th>
                <th>Type</th>
                <th>Status</th>
                <th>Default</th>
                <th>Upload At</th>
                <th className="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            {signatures.map((sig) => (
                <SignatureRow key={sig.id} signature={sig} />
            ))}
            </tbody>
        </table>
    )
}

export default SignatureTable