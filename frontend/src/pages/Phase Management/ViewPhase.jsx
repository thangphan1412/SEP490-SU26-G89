import { useEffect, useState } from "react";
import { Alert, Button, Card, ProgressBar, Spinner, Table } from "react-bootstrap";
import {
  IconArrowLeft,
  IconBuilding,
  IconCalendar,
  IconChecklist,
  IconFileDescription,
  IconFileText,
  IconFlag,
  IconInfoCircle,
  IconSettings,
  IconTimelineEvent,
  IconUser,
} from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import EmptyTableRow from "../../components/phaseComponents/EmptyTableRow.jsx";
import PhaseInfoItem from "../../components/phaseComponents/PhaseInfoItem.jsx";
import PhasePage from "../../components/phaseComponents/PhasePage.jsx";
import PhaseStatusBadge from "../../components/phaseComponents/PhaseStatusBadge.jsx";
import PhaseTableSection from "../../components/phaseComponents/PhaseTableSection.jsx";
import {
  hasAnyProjectAction,
  hasProjectAction,
  PROJECT_ACTIONS,
} from "../../components/permissionComponents/permissionAccess.js";
import { viewPhase } from "../../services/phaseService/phaseApi.js";
import "../../assets/styles/css/phaseStyles/ViewPhase.css";

// Hiển thị chi tiết phase và các module công việc theo quyền truy cập.
function ViewPhase() {
  const navigate = useNavigate();
  const { projectId, phaseId } = useParams();
  const [phase, setPhase] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Tải lại chi tiết phase mỗi khi phase id thay đổi.
  useEffect(function () {
    const requestController = new AbortController();

    // Gọi API và đồng bộ chi tiết phase vào state trang.
    async function loadPhase() {
      // Dừng tải và báo lỗi khi route không có phase id.
      if (!phaseId) {
        setError("Phase id is missing.");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");
        const payload = await viewPhase(phaseId, requestController.signal);

        if (requestController.signal.aborted) {
          return;
        }

        setPhase(payload || null);
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load phase:", requestError);
        setPhase(null);
        setError(getErrorMessage(requestError));
      } finally {
        if (!requestController.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadPhase();

    return function () {
      requestController.abort();
    };
  }, [phaseId]);

  const tasks = Array.isArray(phase?.tasks) ? phase.tasks : [];
  const deliverables = Array.isArray(phase?.deliverables) ? phase.deliverables : [];
  const contracts = Array.isArray(phase?.contracts) ? phase.contracts : [];
  const progress = normalizeProgress(phase?.progress);
  const access = phase?.currentUserAccess;
  const phaseIsInProgress = phase?.status === "IN_PROGRESS";
  const phaseSupportsTaskManagement = phase?.status === "PLANNING"
    || phaseIsInProgress;
  const canViewTasks = hasProjectAction(
    access,
    PROJECT_ACTIONS.VIEW_TASKS
  );
  const canManageTasks = phaseSupportsTaskManagement
    && hasProjectAction(access, PROJECT_ACTIONS.EDIT_TASKS);
  const canAccessTasks = canViewTasks || canManageTasks;
  const canViewDeliverables = hasProjectAction(
    access,
    PROJECT_ACTIONS.VIEW_DELIVERABLES
  );
  const canManageDeliverables = phaseIsInProgress
    && canViewDeliverables
    && hasAnyProjectAction(access, [
      PROJECT_ACTIONS.CREATE_DELIVERABLES,
      PROJECT_ACTIONS.EDIT_DELIVERABLES,
      PROJECT_ACTIONS.DELETE_DELIVERABLES,
    ]);
  const canViewContracts = hasProjectAction(
    access,
    PROJECT_ACTIONS.VIEW_CONTRACTS
  );
  const canManageContracts = phaseIsInProgress
    && canViewContracts
    && hasAnyProjectAction(access, [
      PROJECT_ACTIONS.CREATE_CONTRACTS,
      PROJECT_ACTIONS.EDIT_CONTRACTS,
      PROJECT_ACTIONS.DELETE_CONTRACTS,
    ]);
  const canViewAnyWorkModule = canAccessTasks
    || canViewDeliverables
    || canViewContracts;

  // Hiển thị một hàng task trong bảng của phase.
  function renderTask(task) {
    return (
      <tr key={task.id}>
        <td className="phase-title-cell">
          <div className="phase-task-title">
            <span className="phase-task-icon" aria-hidden="true"><IconChecklist size={17} stroke={1.7} /></span>
            <strong>{task.title || `Task #${task.id}`}</strong>
          </div>
        </td>
        <td>
          <div className="phase-assignee">
            <span className="phase-assignee-avatar" aria-hidden="true"><IconUser size={16} stroke={1.7} /></span>
            <span>{formatAssignee(task)}</span>
          </div>
        </td>
        <td><DateRange startDate={task.startDate} endDate={task.endDate} /></td>
        <td><PhaseStatusBadge status={task.status} /></td>
      </tr>
    );
  }

  const backAction = (
    <Button
      type="button"
      className="phase-back-button"
      onClick={() => navigate(
        projectId
          ? `/project-management/view?id=${projectId}`
          : "/project-management/list"
      )}
    >
      <IconArrowLeft size={17} aria-hidden="true" /> Back to project
    </Button>
  );

  return (
    <PhasePage
      title="Phase Details"
      description="Track the timeline, progress, and work connected to this phase."
      action={backAction}
    >
      {loading ? (
        <Card as="section" className="phase-page-state" role="status">
          <Spinner animation="border" size="sm" aria-hidden="true" />
          <h2>Loading phase</h2>
          <p>Getting the latest phase information...</p>
        </Card>
      ) : !phase ? (
        <Card as="section" className="phase-page-state" role="alert">
          <span className="phase-state-icon" aria-hidden="true"><IconInfoCircle size={24} /></span>
          <h2>Unable to open phase</h2>
          <p>{error || "Phase was not found."}</p>
        </Card>
      ) : (
        <div className="phase-view-content">
          <Card as="section" className="phase-hero">
            <div className="phase-hero-heading">
              <div className="phase-identity">
                <span className="phase-hero-icon" aria-hidden="true"><IconTimelineEvent size={28} stroke={1.7} /></span>
                <div className="phase-hero-main">
                  <span className="phase-eyebrow">Phase overview</span>
                  <h2>{phase.title || "Unnamed phase"}</h2>
                  <p className="phase-project-name"><IconBuilding size={15} aria-hidden="true" /> {formatProjectName(phase)}</p>
                </div>
              </div>
              <PhaseStatusBadge status={phase.status} />
            </div>

            <div className="phase-summary-grid">
              <div className="phase-summary-details">
                <div className="phase-overview-grid">
                  <PhaseInfoItem label="Start date" value={formatDate(phase.startDate)} icon={<IconCalendar size={20} stroke={1.7} />} />
                  <PhaseInfoItem label="End date" value={formatDate(phase.endDate)} icon={<IconFlag size={20} stroke={1.7} />} />
                </div>
                <div className="phase-description-block">
                  <span className="phase-info-label">About this phase</span>
                  <p className="phase-description">{phase.description || "No description has been added for this phase."}</p>
                </div>
              </div>
              <div className="phase-overall-progress">
                <span className="phase-progress-label">Overall progress</span>
                <strong className="phase-progress-value">{progress}<small>%</small></strong>
                <ProgressBar>
                  <ProgressBar now={progress} aria-label="Overall phase progress" />
                </ProgressBar>
                <p>Based on completed tasks</p>
              </div>
            </div>
            <div className="phase-reference"><span>Phase ID</span><span>{phase.id}</span></div>
          </Card>

          {!canViewAnyWorkModule && (
            <Alert variant="info" className="phase-page-message">
              You do not have permission to view tasks, deliverables, or contracts in this project.
            </Alert>
          )}

          {canAccessTasks && (
          <PhaseTableSection
            icon={<IconChecklist size={22} />}
            title="Tasks"
            description="Work items planned for this phase."
            count={tasks.length}
            action={canManageTasks ? (
              <Button
                type="button"
                variant="outline-primary"
                className="phase-manage-button"
                onClick={() => navigate(
                  `/task-management/edit/${projectId}/${phaseId}`
                )}
              >
                <IconSettings size={16} /> Manage Tasks
              </Button>
            ) : null}
          >
            <div className={`phase-table-wrap${tasks.length === 0 ? " phase-table-wrap--empty" : ""}`}>
              <Table hover className="phase-data-table mb-0">
                <caption className="visually-hidden">Tasks in this phase</caption>
                <thead><tr><th>Task</th><th>Assignee</th><th>Date range</th><th>Status</th></tr></thead>
                <tbody>
                  {tasks.length === 0 ? (
                    <EmptyTableRow colSpan={4} message="No tasks have been added to this phase." />
                  ) : tasks.map(renderTask)}
                </tbody>
              </Table>
            </div>
          </PhaseTableSection>
          )}

          {canViewDeliverables && (
          <PhaseTableSection
            icon={<IconFileDescription size={22} />}
            title="Deliverables"
            description="Outputs that must be completed in this phase."
            count={deliverables.length}
            action={canManageDeliverables ? (
              <Button
                type="button"
                variant="outline-primary"
                className="phase-manage-button"
              >
                <IconSettings size={16} /> Manage Deliverables
              </Button>
            ) : null}
          >
            <div className={`phase-table-wrap${deliverables.length === 0 ? " phase-table-wrap--empty" : ""}`}>
              <Table hover className="phase-data-table mb-0">
                <caption className="visually-hidden">Deliverables in this phase</caption>
                <thead><tr><th>Deliverable</th><th>Description</th><th>Due date</th><th>Status</th></tr></thead>
                <tbody>
                  {deliverables.length === 0 ? (
                    <EmptyTableRow colSpan={4} message="No deliverables have been added to this phase." />
                  ) : deliverables.map((deliverable) => (
                    <tr key={deliverable.id}>
                      <td className="phase-title-cell"><strong>{deliverable.title || `Deliverable #${deliverable.id}`}</strong></td>
                      <td className="phase-description-cell">{deliverable.description || "-"}</td>
                      <td className="phase-date-cell">{formatDate(deliverable.dueDate)}</td>
                      <td><PhaseStatusBadge status={deliverable.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          </PhaseTableSection>
          )}

          {canViewContracts && (
          <PhaseTableSection
            icon={<IconFileText size={22} />}
            title="Contracts"
            description="Contracts linked directly to this phase."
            count={contracts.length}
            action={canManageContracts ? (
              <Button
                type="button"
                variant="outline-primary"
                className="phase-manage-button"
                onClick={() => navigate("/contract-management/list")}
              >
                <IconSettings size={16} /> Manage Contracts
              </Button>
            ) : null}
          >
            <div className={`phase-table-wrap${contracts.length === 0 ? " phase-table-wrap--empty" : ""}`}>
              <Table hover className="phase-data-table phase-contract-table mb-0">
                <caption className="visually-hidden">Contracts linked to this phase</caption>
                <thead><tr><th>Contract</th><th>Number</th><th>Effective period</th><th>Linked at</th><th>Status</th></tr></thead>
                <tbody>
                  {contracts.length === 0 ? (
                    <EmptyTableRow colSpan={5} message="No contracts are linked to this phase." />
                  ) : contracts.map((contract) => (
                    <tr key={contract.id}>
                      <td className="phase-title-cell"><strong>{contract.contractTitle || `Contract #${contract.id}`}</strong></td>
                      <td><span className="phase-contract-number">{contract.contractNumber || "-"}</span></td>
                      <td><DateRange startDate={contract.effectiveDate} endDate={contract.expirationDate} /></td>
                      <td>{formatDateTime(contract.linkedAt)}</td>
                      <td><PhaseStatusBadge status={contract.contractStatus} /></td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          </PhaseTableSection>
          )}
        </div>
      )}
    </PhasePage>
  );
}

// Chuẩn hóa tiến độ thành số nguyên trong khoảng từ 0 đến 100.
function normalizeProgress(value) {
  const numberValue = Number(value);
  // Trả về 0 khi giá trị tiến độ không phải số hữu hạn.
  if (!Number.isFinite(numberValue)) {
    return 0;
  }
  return Math.min(100, Math.max(0, Math.round(numberValue)));
}

// Ghép mã và tên dự án thành nhãn hiển thị của phase.
function formatProjectName(phase) {
  return [phase?.projectCode, phase?.projectName].filter(Boolean).join(" - ") || "Unassigned project";
}

// Lấy tên hoặc email assignee và hiển thị Unassigned khi chưa giao.
function formatAssignee(task) {
  // Trả nhãn chưa giao khi thiếu cả tên và email assignee.
  if (!task.assignedToName && !task.assignedToEmail) {
    return "Unassigned";
  }
  return task.assignedToName || task.assignedToEmail;
}

// Định dạng chuỗi ngày ISO thành ngày/tháng/năm.
function formatDate(value) {
  // Hiển thị ký hiệu trống khi chưa có giá trị ngày.
  if (!value) {
    return "-";
  }
  const parts = String(value).split("-");
  return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : value;
}

// Hiển thị khoảng ngày trên hai dòng để bảng dễ đọc ở màn hình nhỏ.
function DateRange({ startDate, endDate }) {
  return (
    <div className="phase-date-range">
      <span>{formatDate(startDate)}</span>
      <small>to {formatDate(endDate)}</small>
    </div>
  );
}

// Định dạng thời điểm liên kết hợp đồng theo locale en-GB.
function formatDateTime(value) {
  // Hiển thị ký hiệu trống khi chưa có giá trị thời gian.
  if (!value) {
    return "-";
  }
  const parsedDate = new Date(value);
  return Number.isNaN(parsedDate.getTime()) ? value : parsedDate.toLocaleString("en-GB");
}

// Lấy thông báo lỗi tải phase từ response hoặc dùng nội dung dự phòng.
function getErrorMessage(error) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || "Unable to load phase. Please try again later.";
}

export default ViewPhase;
