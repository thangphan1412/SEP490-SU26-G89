import React from "react";
import {
    IconFolder,
    IconFileText,
    IconSignature,
    IconUser,
    IconBuilding,
    IconHelpCircle,
} from "@tabler/icons-react";
import DestinationCard from "./DestinationCard.jsx";


const destinations = [
    {
        icon: IconFolder,
        title: "View Your Projects",
        description: "Track project progress, phases, and deadlines.",
        href: "/projects",
    },
    {
        icon: IconFileText,
        title: "View Your Contracts",
        description: "Review agreements and monitor contract status.",
        href: "/contracts",
    },
    {
        icon: IconSignature,
        title: "View Your Signatures",
        description: "Open documents that require electronic signatures.",
        href: "/signatures",
    },
    {
        icon: IconUser,
        title: "View Your Profile",
        description: "Review your account and contact information.",
        href: "/profile",
    },
    {
        icon: IconBuilding,
        title: "View Company Information",
        description: "See organization and department details.",
        href: "/company",
    },
    {
        icon: IconHelpCircle,
        title: "Help & Support",
        description: "Find guides and answers when you need help.",
        href: "/support",
    },
];

 function DestinationGird() {
    return (
        <div className="destination-grid">
            {destinations.map((item) => (
                <DestinationCard key={item.title} {...item} />
            ))}
        </div>
    );
}
export default DestinationGird;