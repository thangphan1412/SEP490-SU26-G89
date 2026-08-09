import React, { useState } from "react";
import PageHeader from "../../components/signature/createSignature/PageHeader.jsx";
import SignatureInformationCard from "../../components/signature/createSignature/SignatureInformationCard.jsx";
import SignatureCanvasCard from "../../components/signature/createSignature/SignatureCanvasCard.jsx";
import DocumentAutomationPreview from "../../components/signature/createSignature/DocumentAutomationPreview.jsx";
import InfoBanner from "../../components/signature/createSignature/InforBanner.jsx";


function CreateSignaturePage() {
    const [form, setForm] = useState({
        name: "",
        type: "Drawn",
        usedIn: "Contracts",
        accessScope: "Personal Only",
        description: "",
        isDefault: true,
        lastModified: "May 21, 2025",
    });

    const [activeTab, setActiveTab] =
        useState("draw");

    return (
        <div
            className="min-vh-100"
            style={{
                backgroundColor: "#f8fafc",
                padding: "24px",
            }}
        >
            <div
                className="mx-auto"
                style={{
                    maxWidth: "1100px",
                }}
            >
                <PageHeader
                    onCancel={() =>
                        console.log("cancel")
                    }
                    onSave={() =>
                        console.log("save", form)
                    }
                />

                <SignatureInformationCard
                    form={form}
                    setForm={setForm}
                />

                <SignatureCanvasCard
                    activeTab={activeTab}
                    setActiveTab={setActiveTab}
                    onClear={() =>
                        console.log("clear canvas")
                    }
                    onUndo={() =>
                        console.log("undo stroke")
                    }
                />

                <DocumentAutomationPreview />

                <InfoBanner
                    text="Your signature will be available for personal use after saving."
                />
            </div>
        </div>
    );
}

export default CreateSignaturePage;