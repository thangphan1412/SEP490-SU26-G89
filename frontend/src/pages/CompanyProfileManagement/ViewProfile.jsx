import {
    Alert,
    Badge,
    Button,
    Card,
    Col,
    Container,
    Row,
    Stack,
} from "react-bootstrap";
import { useNavigate } from "react-router-dom"; // <-- 1. Thêm import useNavigate ở đây

const defaultCompanyProfile = {
    companyName: "ABC Holdings Co., Ltd.",
    email: "legal@abcholdings.vn",
    taxCode: "0312345678",
    phone: "+84 28 3822 5678",
    registeredAddress:
        "125 Nguyen Hue Boulevard, Ben Nghe Ward, District 1, Ho Chi Minh City, Vietnam",
    businessRegistrationNumber: "BRN-2025-00981",
    legalRepresentative: "Nguyen Minh An",
    registrationDate: "May 12, 2020",
    lastVerifiedDate: "May 10, 2025",
    verifiedBy: "Alex Morgan",
};

const profileInformationFields = [
    {
        label: "Company Name",
        fieldName: "companyName",
    },
    {
        label: "Email",
        fieldName: "email",
    },
    {
        label: "Tax Code (MST)",
        fieldName: "taxCode",
    },
    {
        label: "Phone",
        fieldName: "phone",
    },
    {
        label: "Registered Address",
        fieldName: "registeredAddress",
    },
    {
        label: "Business Registration No.",
        fieldName: "businessRegistrationNumber",
    },
    {
        label: "Legal Representative",
        fieldName: "legalRepresentative",
    },
    {
        label: "Registration Date",
        fieldName: "registrationDate",
    },
];

const autoFillUsageItems = [
    {
        title: "Contract Templates",
        description:
            "Company information is auto-filled in all contract templates.",
    },
    {
        title: "Generated Documents",
        description:
            "Used in quotes, agreements, reports, and legal documents.",
    },
    {
        title: "Compliance",
        description:
            "Ensures consistent and accurate company information.",
    },
    {
        title: "Last Verified",
        description: "Verified on {lastVerifiedDate} by {verifiedBy}",
    },
];

function ViewProfile({ companyProfile = defaultCompanyProfile, onEditProfile }) {
    const navigate = useNavigate(); // <-- 2. Khai báo hook điều hướng ở đây

    const getUsageDescription = (description) => {
        return description
            .replace("{lastVerifiedDate}", companyProfile.lastVerifiedDate)
            .replace("{verifiedBy}", companyProfile.verifiedBy);
    };

    const handleEditProfile = () => {
        // Khởi chạy callback cũ nếu có truyền từ file cha bên ngoài
        onEditProfile?.(companyProfile);

        // 3. Tự động chuyển trang sang URL update-profile (Bạn có thể sửa lại path nếu router dự án đặt tên khác)
        navigate("/company-profile/update");
    };

    return (
        <Container fluid className="py-4">
            <Card className="border-0 shadow-sm">
                <Card.Body className="p-4">
                    <Stack
                        direction="horizontal"
                        className="align-items-start justify-content-between mb-4"
                        gap={3}
                    >
                        <div>
                            <h1 className="h3 mb-2">Company Profile</h1>
                            <p className="text-muted mb-0">
                                Manage your company&apos;s legal identity information
                                for automatic use in contracts and documents.
                            </p>
                        </div>
                        <Button type="button" variant="primary" onClick={handleEditProfile}>
                            Edit Profile
                        </Button>
                    </Stack>

                    <Card className="mb-4">
                        <Card.Header className="bg-white py-3">
                            <h2 className="h5 mb-0">Company Legal Information</h2>
                        </Card.Header>
                        <Card.Body>
                            <Row className="g-0">
                                {profileInformationFields.map((item) => (
                                    <Col md={6} key={item.fieldName}>
                                        <Row className="border-bottom py-3 mx-0">
                                            <Col sm={5} className="text-muted">
                                                {item.label}
                                            </Col>
                                            <Col sm={7}>
                                                {companyProfile[item.fieldName]}
                                            </Col>
                                        </Row>
                                    </Col>
                                ))}
                            </Row>
                        </Card.Body>
                    </Card>

                    <Card className="mb-4">
                        <Card.Body>
                            <Stack
                                direction="horizontal"
                                className="align-items-start justify-content-between mb-3"
                                gap={3}
                            >
                                <div>
                                    <h2 className="h5 mb-2">Auto-fill Usage</h2>
                                    <p className="text-muted mb-0">
                                        These details are automatically inserted into
                                        contract templates and generated legal documents.
                                    </p>
                                </div>
                                <Badge bg="success" className="px-3 py-2">
                                    Verified
                                </Badge>
                            </Stack>

                            <Row className="g-3">
                                {autoFillUsageItems.map((item) => (
                                    <Col md={6} xl={3} key={item.title}>
                                        <div className="h-100 rounded border p-3">
                                            <h3 className="h6 mb-2">{item.title}</h3>
                                            <p className="text-muted mb-0">
                                                {getUsageDescription(item.description)}
                                            </p>
                                        </div>
                                    </Col>
                                ))}
                            </Row>
                        </Card.Body>
                    </Card>

                    <Alert variant="info" className="mb-0">
                        To update your company information, click Edit Profile.
                    </Alert>
                </Card.Body>
            </Card>
        </Container>
    );
}

export default ViewProfile;