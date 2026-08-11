import { useEffect, useState } from "react";
import { Alert, Button, Card, ProgressBar, Spinner, Stack, Table } from "react-bootstrap";
import {
  IconArrowLeft,
  IconChecklist,
  IconFileDescription,
  IconFileText,
  IconSettings,
  IconTimelineEvent,
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

function ViewPhase() {
  const navigate = useNavigate();
  const { projectId, phaseId } = useParams();
  const [phase, setPhase] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(function () {
    const requestController = new AbortController();

    async function loadPhase() {
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
  const canViewTasks = hasProjectAction(
    access,
    PROJECT_ACTIONS.VIEW_TASKS
  );
  const canManageTasks = hasProjectAction(
    access,
    PROJECT_ACTIONS.EDIT_TASKS
  );
  const canAccessTasks = canViewTasks || canManageTasks;
  const canViewDeliverables = hasProjectAction(
    access,
    PROJECT_ACTIONS.VIEW_DELIVERABLES
  );
  const canManageDeliverables = canViewDeliverables
    && hasAnyProjectAction(access, [
      PROJECT_ACTIONS.CREATE_DELIVERABLES,
      PROJECT_ACTIONS.EDIT_DELIVERABLES,
      PROJECT_ACTIONS.DELETE_DELIVERABLES,
    ]);
  const canViewContracts = hasProjectAction(
    access,
    PROJECT_ACTIONS.VIEW_CONTRACTS
  );
  const canManageContracts = canViewContracts
    && hasAnyProjectAction(access, [
      PROJECT_ACTIONS.CREATE_CONTRACTS,
      PROJECT_ACTIONS.EDIT_CONTRACTS,
      PROJECT_ACTIONS.DELETE_CONTRACTS,
    ]);
  const canViewAnyWorkModule = canAccessTasks
    || canViewDeliverables
    || canViewContracts;

  function renderTask(task) {
    return (
      <tr key={task.id}>
        <td><strong>{task.title || `Task #${task.id}`}</strong></td>
        <td>{formatAssignee(task)}</td>
        <td>{formatDateRange(task.startDate, task.endDate)}</td>
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
      <IconArrowLeft size={18} /> Back to project
    </Button>
  );

  return (
    <PhasePage
      title="View Phase"
      description="Review the phase information, tasks, deliverables, and linked contracts."
      action={backAction}
    >
      {loading ? (
        <div className="phase-page-state"><Spinner animation="border" /> Loading phase...</div>
      ) : !phase ? (
        <Alert variant="danger" className="phase-page-message">{error || "Phase was not found."}</Alert>
      ) : (
        <div className="phase-view-content">
          {error && <Alert variant="danger">{error}</Alert>}

          <Card as="section" className="phase-hero">
            <span className="phase-hero-icon"><IconTimelineEvent size={38} stroke={1.7} /></span>
            <div className="phase-hero-main">
              <div className="phase-title-line">
                <h2>{phase.title || "Unnamed phase"}</h2>
                <PhaseStatusBadge status={phase.status} />
              </div>
              <p className="phase-project-name">{formatProjectName(phase)}</p>
              <p className="phase-description">{phase.description || "No description has been added for this phase."}</p>
            </div>
            <span className="phase-id">ID #{phase.id}</span>
          </Card>

          <Card as="section" className="phase-overview-card">
            <div className="phase-overview-grid">
              <PhaseInfoItem label="Start date" value={formatDate(phase.startDate)} />
              <PhaseInfoItem label="End date" value={formatDate(phase.endDate)} />
              <PhaseInfoItem label="Project" value={formatProjectName(phase)} />
              <PhaseInfoItem label="Status">
                <PhaseStatusBadge status={phase.status} />
              </PhaseInfoItem>
            </div>
            <div className="phase-overall-progress">
              <Stack direction="horizontal" className="phase-progress-label">
                <span>Overall progress</span>
                <strong>{progress}%</strong>
              </Stack>
              <ProgressBar now={progress} aria-label={`Phase progress ${progress}%`} />
            </div>
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
            <div className="phase-table-wrap">
              <Table responsive hover className="phase-data-table mb-0">
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
            <div className="phase-table-wrap">
              <Table responsive hover className="phase-data-table mb-0">
                <thead><tr><th>Deliverable</th><th>Description</th><th>Due date</th><th>Status</th></tr></thead>
                <tbody>
                  {deliverables.length === 0 ? (
                    <EmptyTableRow colSpan={4} message="No deliverables have been added to this phase." />
                  ) : deliverables.map((deliverable) => (
                    <tr key={deliverable.id}>
                      <td><strong>{deliverable.title || `Deliverable #${deliverable.id}`}</strong></td>
                      <td className="phase-description-cell">{deliverable.description || "-"}</td>
                      <td>{formatDate(deliverable.dueDate)}</td>
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
            <div className="phase-table-wrap">
              <Table responsive hover className="phase-data-table mb-0">
                <thead><tr><th>Contract</th><th>Number</th><th>Effective period</th><th>Linked at</th><th>Status</th></tr></thead>
                <tbody>
                  {contracts.length === 0 ? (
                    <EmptyTableRow colSpan={5} message="No contracts are linked to this phase." />
                  ) : contracts.map((contract) => (
                    <tr key={contract.id}>
                      <td><strong>{contract.contractTitle || `Contract #${contract.id}`}</strong></td>
                      <td>{contract.contractNumber || "-"}</td>
                      <td>{formatDateRange(contract.effectiveDate, contract.expirationDate)}</td>
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

function normalizeProgress(value) {
  const numberValue = Number(value);
  if (!Number.isFinite(numberValue)) {
    return 0;
  }
  return Math.min(100, Math.max(0, Math.round(numberValue)));
}

function formatProjectName(phase) {
  return [phase?.projectCode, phase?.projectName].filter(Boolean).join(" - ") || "Unassigned project";
}

function formatAssignee(task) {
  if (!task.assignedToName && !task.assignedToEmail) {
    return "Unassigned";
  }
  return task.assignedToName || task.assignedToEmail;
}

function formatDate(value) {
  if (!value) {
    return "-";
  }
  const parts = String(value).split("-");
  return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : value;
}

function formatDateRange(startDate, endDate) {
  return `${formatDate(startDate)} - ${formatDate(endDate)}`;
}

function formatDateTime(value) {
  if (!value) {
    return "-";
  }
  const parsedDate = new Date(value);
  return Number.isNaN(parsedDate.getTime()) ? value : parsedDate.toLocaleString("en-GB");
}

function getErrorMessage(error) {
  return error.response?.data?.message
    || error.response?.data?.detail
    || error.response?.data?.error
    || "Unable to load phase. Please try again later.";
}

export default ViewPhase;
