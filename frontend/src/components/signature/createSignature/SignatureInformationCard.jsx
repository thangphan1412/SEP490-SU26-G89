import {
    Info,
} from "lucide-react";

import Card from "./Card";
import Field from "./Field";
import TextInput from "./TextInput";
import TextArea from "./TextArea";
import Select from "./Select";
import Toggle from "./Toggle";
import StatusPill from "./StatusPill";

function SignatureInformationCard({
                                      form,
                                      setForm,
                                  }) {
    const update = (key) => (e) =>
        setForm((f) => ({
            ...f,
            [key]: e.target.value,
        }));

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
                            value={form.name}
                            onChange={update("name")}
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
                            value={form.type}
                            onChange={update("type")}
                            options={[
                                "Drawn",
                                "Uploaded",
                                "Typed",
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
                Make this your default signature for new
                documents.
              </span>

                            <Toggle
                                checked={form.isDefault}
                                onChange={(value) =>
                                    setForm((f) => ({
                                        ...f,
                                        isDefault: value,
                                    }))
                                }
                            />
                        </div>
                    </Field>
                </div>

                <div className="col-md-6">
                    <Field
                        label="Used In"
                        required
                    >
                        <Select
                            value={form.usedIn}
                            onChange={update("usedIn")}
                            options={[
                                "Contracts",
                                "Approvals",
                                "Internal Forms",
                                "All",
                            ]}
                        />

                        <div className="form-text">
                            Select where this signature will be used.
                        </div>
                    </Field>
                </div>

                <div className="col-md-6">
                    <Field label="Access Scope">
                        <Select
                            value={form.accessScope}
                            onChange={update("accessScope")}
                            options={[
                                "Personal Only",
                                "Team",
                                "Organization",
                            ]}
                        />

                        <div className="form-text">
                            Define who can access and use this signature.
                        </div>
                    </Field>
                </div>

                <div className="col-md-6">
                    <Field label="Description">
                        <TextArea
                            placeholder="Add notes or intended usage"
                            value={form.description}
                            onChange={update("description")}
                            rows={3}
                        />

                        <div className="form-text">
                            Optional description to help identify this
                            signature.
                        </div>
                    </Field>
                </div>

                <div className="col-md-6">
                    <Field label="Last Modified">
                        <div
                            className="form-control form-control-sm bg-light text-secondary"
                        >
                            {form.lastModified}
                        </div>

                        <div className="form-text">
                            Date when this signature was last updated.
                        </div>
                    </Field>
                </div>

            </div>
        </Card>
    );
}

export default SignatureInformationCard;