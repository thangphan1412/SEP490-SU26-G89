import { Alert, Badge, Button, Card, Container, Stack } from "react-bootstrap";
import "../../assets/styles/css/projectStyles/ProjectComponents.css";

export function Icon({ name, size = 22, color = "#1f4fff" }) {
    const props = {
        width: size,
        height: size,
        viewBox: "0 0 24 24",
        fill: "none",
        stroke: color,
        strokeWidth: 2,
        strokeLinecap: "round",
        strokeLinejoin: "round",
        "aria-hidden": true,
    };
    const paths = {
        plus: <><circle cx="12" cy="12" r="9" /><path d="M12 8v8" /><path d="M8 12h8" /></>,
        search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></>,
        filter: <path d="M4 5h16l-6 7v5l-4 2v-7z" />,
        refresh: <><path d="M20 6v5h-5" /><path d="M4 18v-5h5" /><path d="M18 9a7 7 0 0 0-11.6-2.6L4 9" /><path d="M6 15a7 7 0 0 0 11.6 2.6L20 15" /></>,
        chevron: <path d="m8 10 4 4 4-4" />,
        dots: <><circle cx="6" cy="12" r="1" fill="currentColor" stroke="none" /><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" /><circle cx="18" cy="12" r="1" fill="currentColor" stroke="none" /></>,
        sort: <><path d="m8 9 3-3 3 3" /><path d="m14 15-3 3-3-3" /></>,
        arrowLeft: <path d="m15 18-6-6 6-6" />,
        arrowRight: <path d="m9 18 6-6-6-6" />,
        document: <><path d="M7 3h7l4 4v14H7z" /><path d="M14 3v5h5" /><path d="M10 13h5" /><path d="M10 17h4" /></>,
        users: <><circle cx="9" cy="8" r="3.5" /><path d="M2 20c1.4-3.6 3.7-5.4 7-5.4 3.2 0 5.6 1.8 7 5.4" /><path d="M17 11a3 3 0 1 0-1.2-5.8" /><path d="M18 14.5c1.8.6 3.1 2.2 4 5.5" /></>,
        building: <><path d="M4 21h16" /><path d="M6 21V5h7v16" /><path d="M13 9h5v12" /><path d="M9 9h1" /><path d="M16 13h1" /></>,
        shield: <><path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" /><path d="m9 12 2 2 4-4" /></>,
        chart: <><path d="M5 20V9" /><path d="M12 20V4" /><path d="M19 20v-7" /><path d="M3 20h18" /></>,
        edit: <><path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z" /><path d="m13.5 6.5 4 4" /></>,
        save: <><path d="M5 3h12l2 2v16H5z" /><path d="M8 3v6h8V3" /><path d="M8 21v-7h8v7" /></>,
        calendar: <><path d="M5 4h14v16H5z" /><path d="M8 2v4" /><path d="M16 2v4" /><path d="M5 9h14" /></>,
        dollar: <><circle cx="12" cy="12" r="9" /><path d="M12 6v12" /><path d="M15 8.5c-.8-.7-1.8-1-3-1-1.6 0-2.7.8-2.7 2s1 1.8 2.7 2.2c1.8.4 3 1 3 2.5S13.7 17 12 17c-1.3 0-2.5-.4-3.5-1.2" /></>,
        location: <><path d="M12 21s7-5.2 7-11a7 7 0 0 0-14 0c0 5.8 7 11 7 11Z" /><circle cx="12" cy="10" r="2.5" /></>,
        flag: <><path d="M5 21V4" /><path d="M5 5h11l-1.5 4L16 13H5" /></>,
        link: <><path d="M10 13a5 5 0 0 0 7 0l2-2a5 5 0 0 0-7-7l-1 1" /><path d="M14 11a5 5 0 0 0-7 0l-2 2a5 5 0 0 0 7 7l1-1" /></>,
        task: <><path d="M5 5h14v14H5z" /><path d="m8 12 2.5 2.5L16 9" /></>,
        info: <><circle cx="12" cy="12" r="9" /><path d="M12 11v5" /><path d="M12 8h.01" /></>,
    };

    return <svg {...props}>{paths[name]}</svg>;
}

export function StatusBadge({ status }) {
    const normalizedStatus = String(status || "Unknown").trim().toLowerCase().replaceAll("_", " ");
    const classByStatus = {
        active: "project-status-badge--active",
        approved: "project-status-badge--active",
        signed: "project-status-badge--active",
        planning: "project-status-badge--planning",
        "in progress": "project-status-badge--planning",
        "in review": "project-status-badge--hold",
        "on hold": "project-status-badge--hold",
        completed: "project-status-badge--completed",
        done: "project-status-badge--completed",
        draft: "project-status-badge--draft",
        inactive: "project-status-badge--draft",
        cancelled: "project-status-badge--danger",
        canceled: "project-status-badge--danger",
    };

    return (
        <Badge bg="transparent" as="span" className={`project-status-badge ${classByStatus[normalizedStatus] || "project-status-badge--draft"}`}>
            {status || "Unknown"}
        </Badge>
    );
}

export function PagePanel({ title, description, action, children }) {
    return (
        <Container fluid as="main" className="project-page">
            <Card as="section" className="project-panel">
                <Card.Header className="project-page-header">
                    <div>
                        <h1 className="project-page-title">{title}</h1>
                        <p className="project-page-description">{description}</p>
                    </div>
                    {action}
                </Card.Header>
                {children}
            </Card>
        </Container>
    );
}

export function PrimaryButton({ children, onClick, type = "button", ...buttonProps }) {
    return <Button type={type} className="project-primary-button" onClick={onClick} {...buttonProps}>{children}</Button>;
}

export function CancelButton({ children = "Cancel", onClick, ...buttonProps }) {
    return <Button type="button" variant="light" className="project-cancel-button" onClick={onClick} {...buttonProps}>{children}</Button>;
}

export function InfoAlert({ children }) {
    return (
        <Alert variant="primary" className="project-info-alert">
            <Stack direction="horizontal" gap={2}>
                <Icon name="info" size={20} />
                <span>{children}</span>
            </Stack>
        </Alert>
    );
}
