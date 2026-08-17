import CreateSignaturePage from "../../pages/signature/CreateSignaturePage.jsx";

export const navConfig = [
    {
        key: "signature",
        headerLabel: "Signature Management",
        matchPaths: ["/signature-management"],
        children: [
            { label: "Signature List", path: "/signature-management/list" },
            { label: "Create Signature", path: "/signature-management/create-signature" },
            { label: "View Signature", path: "/signature-management/detail/:id" },
            { label: "Update Signature", path: "/signature-management/update/:id" },
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
            // { label: "View Department", path: "/department-management/view/:id" },
            // { label: "Update Department", path: "/department-management/update/:id" },
        ],
    },
    {
        key: "role",
        headerLabel: "Role Management",
        matchPaths: ["/role-management"],
        children: [
            { label: "List Role", path: "/role-management/list" },
            { label: "Create Role", path: "/role-management/create" },
        ],
    },
    {
        key: "employee", // Mặc dù key cũ là employee nhưng màn hình là User
        headerLabel: "User Management",
        matchPaths: ["/user-management"],
        children: [
            {
                label: "List User",
                path: "/user-management/list",
                allowedRoles: ["CEO", "Administrator", "Accountant", "HeadOfDepartment"]
            },
            {
                label: "Create User",
                path: "/user-management/create",
                allowedRoles: ["Accountant", "HeadOfDepartment"] // Ẩn Create đối với CEO/Admin
            },
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

    // --- THÊM KHỐI DASHBOARD VÀO ĐÂY ---
    {
        key: "dashboard",
        headerLabel: "Dashboard & Analytics",
        matchPaths: ["/dashboard"],
        children: [
            {
                label: "Agreement Statistics",
                path: "/dashboard/agreement-statistics",
                allowedRoles: ["CEO", "Administrator", "Accountant", "HeadOfDepartment"]
            },
            {
                label: "Total Agreements",
                path: "/dashboard/total-agreements",
                allowedRoles: ["CEO", "Administrator", "Accountant", "HeadOfDepartment"]
            },
            {
                label: "Pending Signatures",
                path: "/dashboard/pending-signature-agreements",
                allowedRoles: ["CEO", "Administrator", "Accountant", "HeadOfDepartment"]
            },
            {
                label: "Statistical Reports",
                path: "/dashboard/contract-statistical-reports",
                allowedRoles: ["CEO", "Administrator", "Accountant", "HeadOfDepartment"]
            },
        ],
    },

]
