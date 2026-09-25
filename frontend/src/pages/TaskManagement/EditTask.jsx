import { useEffect, useState } from "react";
import { Alert, Button, Card, Container, Spinner } from "react-bootstrap";
import {
  IconArrowLeft,
  IconBriefcase,
  IconCalendarEvent,
  IconChecklist,
  IconLayoutKanban,
  IconPlus,
} from "@tabler/icons-react";
import { useNavigate, useParams } from "react-router-dom";
import TaskCreateRow from "../../components/taskComponents/TaskCreateRow.jsx";
import TaskEditRow from "../../components/taskComponents/TaskEditRow.jsx";
import {
  createTask,
  getTasksByPhaseId,
  markTaskAsDone,
  updateTask,
} from "../../services/taskService/taskApi.js";
import "../../assets/styles/css/taskStyles/EditTask.css";

// Hiển thị trang tạo, chỉnh sửa và hoàn thành task của một phase.
function EditTask() {
  const navigate = useNavigate();
  const routeParameters = useParams();
  const projectId = routeParameters.projectId;
  const phaseId = routeParameters.phaseId;
  const [taskData, setTaskData] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showCreateRow, setShowCreateRow] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  // Tải lại dữ liệu task khi mã phase trên route thay đổi.
  useEffect(function () {
    const requestController = new AbortController();

    // Gọi API và đồng bộ dữ liệu quản lý task vào state trang.
    async function loadTasks() {
      try {
        setLoading(true);
        setError("");

        const response = await getTasksByPhaseId(
          phaseId,
          requestController.signal
        );

        // Bỏ qua response khi component đã hủy request.
        if (requestController.signal.aborted) {
          return;
        }

        setTaskData(response);

        // Chỉ nhận danh sách task khi response có đúng cấu trúc.
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

  // Cập nhật task rồi thay thế bản ghi tương ứng trong state.
  async function handleSave(taskId, request) {
    const updatedTask = await updateTask(taskId, request);
    replaceTask(updatedTask);
    return updatedTask;
  }

  // Tạo task mới rồi thêm task vào đầu danh sách hiện tại.
  async function handleCreate(request) {
    const createdTask = await createTask(phaseId, request);

    setTasks(function (currentTasks) {
      return [createdTask, ...currentTasks];
    });
    setShowCreateRow(false);
    setSuccessMessage("Task created successfully.");
  }

  // Đánh dấu task hoàn thành rồi cập nhật bản ghi trong state.
  async function handleMarkDone(taskId) {
    const updatedTask = await markTaskAsDone(taskId);
    replaceTask(updatedTask);
    return updatedTask;
  }

  // Thay thế task có cùng id bằng dữ liệu mới từ backend.
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

  // Điều hướng trở lại trang chi tiết phase hiện tại.
  function goBackToPhase() {
    const responseProjectId = taskData ? taskData.projectId : null;
    const backProjectId = responseProjectId || projectId;

    navigate(`/phase-management/view/${backProjectId}/${phaseId}`);
  }

  // Hiển thị một hàng chỉnh sửa cho task được truyền vào.
  function renderTask(task, index) {
    return (
      <TaskEditRow
        key={task.id}
        task={task}
        sequence={index + 1}
        memberOptions={taskData.memberOptions}
        statusOptions={taskData.statusOptions}
        phaseStartDate={taskData.phaseStartDate}
        phaseEndDate={taskData.phaseEndDate}
        allowReassignment={taskData.fullWorkScope}
        canApproveTasks={taskData.canApproveTasks}
        canChangeTaskStatus={taskData.canChangeTaskStatus}
        onSave={handleSave}
        onMarkDone={handleMarkDone}
        onViewContract={viewContract}
      />
    );
  }

  // Điều hướng tới danh sách hợp đồng và mở hợp đồng được chọn.
  function viewContract(contractId) {
    navigate(
      `/contract-management/list?viewContractId=${contractId}`
    );
  }

  const completedTaskCount = tasks.filter(function (task) {
    return task.status === "DONE";
  }).length;

  return (
    <Container fluid as="main" className="task-page">
      <Card className="task-panel">
        <Card.Header className="task-page-header">
          <div className="task-page-heading">
            <span className="task-heading-icon">
              <IconChecklist size={28} />
            </span>
            <div>
              <span className="task-page-eyebrow">Task workspace</span>
              <h1>Manage phase tasks</h1>
              <p>Plan assignments, timelines and delivery status in one focused workspace.</p>
            </div>
          </div>
          <Button
            type="button"
            variant="outline-light"
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
              <TaskSummaryItem
                icon={<IconBriefcase size={20} />}
                label="Project"
                value={taskData.projectName || "Unnamed project"}
              />
              <TaskSummaryItem
                icon={<IconLayoutKanban size={20} />}
                label="Phase"
                value={taskData.phaseTitle || "Unnamed phase"}
              />
              <TaskSummaryItem
                icon={<IconCalendarEvent size={20} />}
                label="Phase timeline"
                value={`${formatDate(taskData.phaseStartDate)} — ${formatDate(taskData.phaseEndDate)}`}
              />
              <TaskSummaryItem
                icon={<IconChecklist size={20} />}
                label="Progress"
                value={`${completedTaskCount} of ${tasks.length} completed`}
              />
            </div>

            {!taskData.fullWorkScope && (
              <Alert variant="info" className="task-scope-message">
                You can only edit tasks assigned to you.
              </Alert>
            )}

            {successMessage && (
              <Alert variant="success" className="task-create-message">
                {successMessage}
              </Alert>
            )}

            <div className="task-list-toolbar">
              <div>
                <span className="task-section-kicker">Phase delivery</span>
                <h2>Tasks</h2>
                <p>{tasks.length} tasks · {completedTaskCount} completed</p>
              </div>
              {taskData.canCreateTasks && (
                <Button
                  type="button"
                  variant="primary"
                  className="task-create-button"
                  disabled={showCreateRow}
                  onClick={function () {
                    setSuccessMessage("");
                    setShowCreateRow(true);
                  }}
                >
                  <IconPlus size={18} /> Create Task
                </Button>
              )}
            </div>

            <div className="task-card-list">
              {taskData.canCreateTasks && showCreateRow && (
                <TaskCreateRow
                  memberOptions={taskData.memberOptions}
                  phaseStartDate={taskData.phaseStartDate}
                  phaseEndDate={taskData.phaseEndDate}
                  allowReassignment={taskData.fullWorkScope}
                  onCreate={handleCreate}
                  onCancel={function () {
                    setShowCreateRow(false);
                  }}
                />
              )}
              {tasks.length === 0 && !showCreateRow ? (
                <div className="task-empty-state">
                  <span><IconChecklist size={28} /></span>
                  <strong>No tasks in this phase yet</strong>
                  <p>Create the first task to start planning the phase delivery.</p>
                </div>
              ) : tasks.map(renderTask)}
            </div>
          </Card.Body>
        )}
      </Card>
    </Container>
  );
}

function TaskSummaryItem({ icon, label, value }) {
  return (
    <div className="task-summary-item">
      <span className="task-summary-icon">{icon}</span>
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
    </div>
  );
}

// Định dạng chuỗi ngày ISO thành ngày/tháng/năm.
function formatDate(value) {
  // Hiển thị ký hiệu trống khi chưa có giá trị ngày.
  if (!value) {
    return "-";
  }

  const parts = String(value).split("-");

  // Giữ nguyên giá trị khi chuỗi không đúng định dạng ISO date.
  if (parts.length !== 3) {
    return value;
  }

  return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

// Chuyển lỗi tải trang thành thông báo phù hợp cho người dùng.
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
