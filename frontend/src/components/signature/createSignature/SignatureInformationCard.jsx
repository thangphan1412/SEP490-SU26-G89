import {
    Info,
} from "lucide-react";

import Card from "./Card";
import Field from "./Field";
import TextInput from "./TextInput";
import Select from "./Select";
import Toggle from "./Toggle";
import StatusPill from "./StatusPill";

function SignatureInformationCard({
                                      form,
                                      setForm,
                                      onTypeChange,
                                  }) {

    const updateName = (e) => {

        setForm((prev) => ({
            ...prev,
            electronicSignatureName: e.target.value,
        }));

    };

    return (
        <Card
            title="Signature Information"
            icon={<Info size={16} />}
        >

            <div className="row gx-4">


                <div className="col-md-6">

                    <Field
                        label="Signature Name"
                        required
                    >

                        <TextInput
                            placeholder="Enter signature name"
                            value={
                                form.electronicSignatureName || ""
                            }
                            onChange={updateName}
                        />

                    </Field>

                </div>


                <div className="col-md-6">

                    <Field label="Status">

                        <div className="d-flex align-items-center">

                            <StatusPill />

                        </div>

                        <div className="form-text">
                            Active signatures can be used in documents.
                        </div>

                    </Field>

                </div>


                <div className="col-md-6">

                    <Field
                        label="Signature Type"
                        required
                    >

                        <Select
                            value={
                                form.electronicSignatureType
                            }
                            onChange={(e) => {

                                const value = e.target.value;

                                onTypeChange(value);

                            }}
                            options={[
                                "DRAW",
                                "UPLOAD",
                            ]}
                        />

                        <div className="form-text">
                            Choose how your signature will be created.
                        </div>

                    </Field>

                </div>


                <div className="col-md-6">

                    <Field label="Set as Default">

                        <div className="d-flex justify-content-between align-items-center">

                            <span
                                className="text-secondary"
                                style={{
                                    fontSize: "11px",
                                }}
                            >
                                Make this your default signature
                                for new documents.
                            </span>

                            <Toggle
                                checked={Boolean(form.isDefault)}
                                onChange={(value) => {

                                    setForm((prev) => ({
                                        ...prev,
                                        isDefault: Boolean(value),
                                    }));

                                }}
                            />

                        </div>

                    </Field>

                </div>

            </div>

        </Card>
    );
}

export default SignatureInformationCard;