export const navConfig = [
    {
        key: "signature",
        headerLabel: "Signature Management",
        matchPaths: ["/signature-management"],
        children: [
            { label: "Digital Signatures", path: "/signature-management/list" },
            { label: "Signature Templates", path: "/signature-management/templates" },
        ],
    },
    {
        key: "contract",
        headerLabel: "Contract Management",
        matchPaths: [
            "/contract-management",
            "/contract-types",
            "/contract-templates",
        ],
        children: [
            { label: "List Contract", path: "/contract-management/list" },
            { label: "List Contract Type", path: "/contract-types" },
            { label: "List Contract Template", path: "/contract-templates" },
        ],
    },
    {
        key: "project",
        headerLabel: "Project Management",
        matchPaths: ["/project-management"],
        children: [
            { label: "List Project", path: "/project-management/list" },
            { label: "Create Project", path: "/project-management/create" },
            { label: "View Project", path: "/project-management/view" },
            { label: "Update Project", path: "/project-management/update" },
        ],
    },
    {
        key: "permission",
        headerLabel: "Permission Management",
        matchPaths: ["/permission"],
        children: [
            { label: "List Permission", path: "/permission/list" },
            { label: "Create Permission", path: "/permission/create" },
            { label: "View Permission", path: "/permission/view" },
            { label: "Update Permission", path: "/permission/update" },
        ],
    },
    {
        key: "department",
        headerLabel: "Department Management",
        matchPaths: ["/department-management"],
        children: [
            { label: "List Department", path: "/department-management/list" },
            { label: "Create Department", path: "/department-management/create" },
            { label: "View Department", path: "/department-management/view/:id" },
            { label: "Update Department", path: "/department-management/update/:id" },
        ],
    },
    {
        key: "employee",
        headerLabel: "Employee Management",
        matchPaths: ["/user-management"],
        children: [
            { label: "List User", path: "/user-management/list" },
            { label: "Create User", path: "/user-management/create" },
        ],
    },
    {
        key: "profile",
        headerLabel: "Profile",
        matchPaths: ["/user-profile", "/company-profile"],
        children: [
            { label: "My Profile", path: "/user-profile/view" },
            { label: "Update Profile", path: "/user-profile/update" },
            { label: "Company Profile", path: "/company-profile/view" },
            { label: "Update Company Profile", path: "/company-profile/update" },
        ],
    },
]
