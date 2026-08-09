import { PenLine, Edit3 } from "lucide-react";
import {Card} from "react-bootstrap";
import Field from "../../components/signature/createSignature/Field.jsx";



function SignatureCanvas(props) {
    return null;
}

SignatureCanvas.propTypes = {
    mode: PropTypes.string,
    signatureUrl: PropTypes.any
};

function SignatureDetail({
                             signature,
                             onEdit,
                         }) {
    return (
        <div
            style={{
                maxWidth: "1200px",
                margin: "0 auto",
                padding: "20px",
            }}
        >
            {/* Header */}

            <div className="d-flex justify-content-between mb-3">
                <div>
                    <h2 className="mb-1">
                        Signature Details
                    </h2>

                    <p className="text-muted small mb-0">
                        Review the details and usage of your
                        personal electronic signature.
                    </p>
                </div>

                <button
                    className="btn btn-primary btn-sm"
                    onClick={onEdit}
                >
                    <Edit3 size={14} />
                    {" "}Edit Signature
                </button>
            </div>

            {/* Information */}

            <Card
                title="Signature Information"
                icon={<PenLine size={16} />}
            >
                <div
                    className="row"
                >
                    <div className="col-md-6">
                        <Field
                            label="Signature Name"
                            value={signature.signatureName}
                        />

                        <Field
                            label="Signature Type"
                            value={signature.signatureType}
                        />

                        <Field
                            label="Used In"
                            value={signature.usedIn}
                        />

                        <Field
                            label="Description"
                            value={signature.description}
                        />
                    </div>

                    <div className="col-md-6">
                        <Field
                            label="Status"
                            value={signature.status}
                        />

                        <Field
                            label="Default Signature"
                            value={
                                signature.isDefault
                                    ? "Yes"
                                    : "No"
                            }
                        />

                        <Field
                            label="Access Scope"
                            value={signature.accessScope}
                        />

                        <Field
                            label="Last Updated"
                            value={signature.updatedAt}
                        />
                    </div>
                </div>

                {/* Dùng lại component canvas */}

                <SignatureCanvas
                    mode="view"
                    signatureUrl={
                        signature.fileStorage?.url
                    }
                />
            </Card>
        </div>
    );
}

export default SignatureDetail;