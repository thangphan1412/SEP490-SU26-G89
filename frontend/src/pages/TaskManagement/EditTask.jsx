import { useEffect, useState } from "react";
import { Alert, Button, Card, Container, Spinner, Table } from "react-bootstrap";
import { IconArrowLeft, IconChecklist } from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import TaskEditRow from "../../components/taskComponents/TaskEditRow.jsx";
import {
  getTasksByPhaseId,
  markTaskAsDone,
  updateTask,
} from "../../services/taskService/taskApi.js";
import "../../assets/styles/css/taskStyles/EditTask.css";

function EditTask() {
  const navigate = useNavigate();
  const routeParameters = useParams();
  const projectId = routeParameters.projectId;
  const phaseId = routeParameters.phaseId;
  const [taskData, setTaskData] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(function () {
    const requestController = new AbortController();

    async function loadTasks() {
      try {
        setLoading(true);
        setError("");

        const response = await getTasksByPhaseId(
          phaseId,
          requestController.signal
        );

        if (requestController.signal.aborted) {
          return;
        }

        setTaskData(response);

        if (response && Array.isArray(response.tasks)) {
          setTasks(response.tasks);
        } else {
          setTasks([]);
        }
      } catch (requestError) {
        if (requestController.signal.aborted) {
          return;
        }

        console.error("Unable to load tasks:", requestError);
        setTaskData(null);
        setTasks([]);
        setError(getPageErrorMessage(requestError));
      } finally {
        if (!requestController.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadTasks();

    return function () {
      requestController.abort();
    };
  }, [phaseId]);

  async function handleSave(taskId, request) {
    const updatedTask = await updateTask(taskId, request);
    replaceTask(updatedTask);
    return updatedTask;
  }

  async function handleMarkDone(taskId) {
    const updatedTask = await markTaskAsDone(taskId);
    replaceTask(updatedTask);
    return updatedTask;
  }

  function replaceTask(updatedTask) {
    setTasks(function (currentTasks) {
      const updatedTasks = [];

      for (const task of currentTasks) {
        if (task.id === updatedTask.id) {
          updatedTasks.push(updatedTask);
        } else {
          updatedTasks.push(task);
        }
      }

      return updatedTasks;
    });
  }

  function goBackToPhase() {
    const responseProjectId = taskData ? taskData.projectId : null;
    const backProjectId = responseProjectId || projectId;

    navigate(`/phase-management/view/${backProjectId}/${phaseId}`);
  }

  function renderTask(task) {
    return (
      <TaskEditRow
        key={task.id}
        task={task}
        memberOptions={taskData.memberOptions}
        statusOptions={taskData.statusOptions}
        phaseStartDate={taskData.phaseStartDate}
        phaseEndDate={taskData.phaseEndDate}
        allowReassignment={taskData.fullWorkScope}
        onSave={handleSave}
        onMarkDone={handleMarkDone}
      />
    );
  }

  return (
    <Container fluid as="main" className="task-page">
      <Card className="task-panel">
        <Card.Header className="task-page-header">
          <div className="task-page-heading">
            <span className="task-heading-icon">
              <IconChecklist size={28} />
            </span>
            <div>
              <h1>Edit Tasks</h1>
              <p>Edit the tasks currently assigned to this phase.</p>
            </div>
          </div>
          <Button
            type="button"
            variant="light"
            className="task-back-button"
            onClick={goBackToPhase}
          >
            <IconArrowLeft size={18} /> Back to phase
          </Button>
        </Card.Header>

        {loading ? (
          <div className="task-page-state">
            <Spinner animation="border" /> Loading tasks...
          </div>
        ) : !taskData ? (
          <Alert variant="danger" className="task-page-message">
            {error || "Task information could not be loaded."}
          </Alert>
        ) : (
          <Card.Body className="task-page-body">
            <div className="task-phase-summary">
              <div>
                <span>Project</span>
                <strong>{taskData.projectName || "Unnamed project"}</strong>
              </div>
              <div>
                <span>Phase</span>
                <strong>{taskData.phaseTitle || "Unnamed phase"}</strong>
              </div>
              <div>
                <span>Phase timeline</span>
                <strong>
                  {formatDate(taskData.phaseStartDate)} - {formatDate(taskData.phaseEndDate)}
                </strong>
              </div>
            </div>

            {!taskData.fullWorkScope && (
              <Alert variant="info" className="task-scope-message">
                You can only edit tasks assigned to you.
              </Alert>
            )}

            <div className="task-table-wrap">
              <Table responsive hover className="task-edit-table mb-0">
                <thead>
                  <tr>
                    <th>Task</th>
                    <th>Assignee</th>
                    <th>Start date</th>
                    <th>End date</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="task-empty-row">
                        No editable tasks were found in this phase.
                      </td>
                    </tr>
                  ) : tasks.map(renderTask)}
                </tbody>
              </Table>
            </div>
          </Card.Body>
        )}
      </Card>
    </Container>
  );
}

function formatDate(value) {
  if (!value) {
    return "-";
  }

  const parts = String(value).split("-");

  if (parts.length !== 3) {
    return value;
  }

  return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

function getPageErrorMessage(error) {
  if (error && error.response) {
    if (error.response.status === 403) {
      return "You do not have permission to edit tasks in this project.";
    }

    if (error.response.data && error.response.data.message) {
      return error.response.data.message;
    }
  }

  return "Unable to load tasks. Please try again later.";
}

export default EditTask;
