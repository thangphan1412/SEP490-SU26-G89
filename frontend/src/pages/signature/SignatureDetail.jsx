import { useState, useEffect } from "react";
import { PenLine, Edit3 } from "lucide-react";

// import Field from "../../components/signature/createSignature/Field.jsx";
// import PropTypes from "prop-types";
import { useParams, useNavigate } from "react-router-dom";
import electronicSignatureService from "../../services/signatureService/electronicSignatureService.js";
// import {Card} from "react-bootstrap";
import Field from "../../components/signature/createSignature/Field.jsx";
import PropTypes from "prop-types";
import Card from "../../components/signature/createSignature/Card.jsx";
// import Card from "../../components/signature/createSignature/Card.jsx";

function SignatureCanvas(props) {
    return null;
}

SignatureCanvas.propTypes = {
    mode: PropTypes.string,

    signatureUrl: PropTypes.any
};

function SignatureDetail() {
    const [electronicSignature, setElectronicSignature] = useState(null);
    const [loading, setLoading] = useState(true);
    const { id } = useParams();
    const navigate = useNavigate();

    useEffect(() => {
        const fetchSignature = async () => {
            try {
                const response = await electronicSignatureService.getElectronicSignatureById(id);
                console.log("thay chu ky dien tu", response.data);
                setElectronicSignature(response.data.data);
            } catch (error) {
                console.error("khong lay ra duoc electronic signature " + error);
            } finally {
                setLoading(false);
            }
        };
        fetchSignature();
    }, [id]);

    const handleEdit = () => {
        navigate(`/signature-management/update/${id}`);
    };

    if (loading) {
        return <div>Loading...</div>;
    }
    if (!electronicSignature) {
        return <div>Khong thay chu ky</div>;
    }

    return (
        <div
            style={{
                maxWidth: "1200px",
                margin: "0 auto",
                padding: "20px",
            }}
        >
            <div className="d-flex justify-content-between mb-3">
                <div>
                    <h2 className="mb-1">Signature Details</h2>
                    <p className="text-muted small mb-0">
                        Review the details and usage of your personal electronic signature.
                    </p>
                </div>

                <button className="btn btn-primary btn-sm" onClick={handleEdit}>
                    <Edit3 size={14} /> Edit Signature
                </button>
            </div>

            <Card title="Signature Information" icon={<PenLine size={16} />}>
                <div className="row">
                    <div className="col-md-6">
                        <Field
                            label="Signature Name"
                            value={electronicSignature.electronicSignatureName}
                            mode="view"
                        />
                        <Field
                            label="Signature Type"
                            value={electronicSignature.electronicSignatureType}
                            mode="view"
                        />
                    </div>

                    <div className="col-md-6">
                        <Field
                            label="Status"
                            value={electronicSignature.electronicSignatureStatus}
                            mode="view"
                        />
                        <Field
                            label="Default Signature"
                            value={electronicSignature.default ? "Yes" : "No"}
                            mode="view"
                        />
                        <Field
                            label="Created At"
                            value={electronicSignature.createAt}
                            mode="view"
                        />
                    </div>
                </div>

                <SignatureCanvas mode="view" />
            </Card>
        </div>
    );
}

export default SignatureDetail;