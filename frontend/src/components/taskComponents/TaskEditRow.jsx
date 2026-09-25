import { useState } from "react";
import { Button, Form } from "react-bootstrap";
import { IconCalendar, IconCheck, IconDeviceFloppy, IconEye } from "@tabler/icons-react";

function TaskEditRow({
  task,
  sequence,
  memberOptions,
  statusOptions,
  phaseStartDate,
  phaseEndDate,
  allowReassignment,
  canApproveTasks,
  canChangeTaskStatus,
  onSave,
  onMarkDone,
  onViewContract,
}) {
  const [form, setForm] = useState(createForm(task));
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const contracts = Array.isArray(task.contracts) ? task.contracts : [];
  const taskIsDone = form.status === "DONE";
  const statusClassName = String(form.status || "TODO")
    .toLowerCase()
    .replaceAll("_", "-");

  function handleChange(event) {
    const fieldName = event.target.name;
    const fieldValue = event.target.value;

    setForm(function (currentForm) {
      return {
        ...currentForm,
        [fieldName]: fieldValue,
      };
    });
  }

  async function handleSave() {
    const validationMessage = validateForm(
      form,
      phaseStartDate,
      phaseEndDate
    );

    if (validationMessage) {
      setError(validationMessage);
      setMessage("");
      return;
    }

    try {
      setSaving(true);
      setError("");
      setMessage("");

      const request = {
        title: form.title.trim(),
        startDate: form.startDate,
        endDate: form.endDate,
        status: form.status,
        assignedToId: form.assignedToId || null,
      };
      const updatedTask = await onSave(task.id, request);

      setForm(createForm(updatedTask));
      setMessage("Saved");
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  async function handleMarkDone() {
    try {
      setSaving(true);
      setError("");
      setMessage("");

      const updatedTask = await onMarkDone(task.id);

      setForm(createForm(updatedTask));
      setMessage("Task marked as done");
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  return (
    <article className={`task-card task-card--${statusClassName}`}>
      <header className="task-card-header">
        <div className="task-card-identity">
          <span className="task-card-number">
            {String(sequence).padStart(2, "0")}
          </span>
          <div>
            <span>Task</span>
            <strong>{form.title || "Untitled task"}</strong>
          </div>
        </div>
        <div className="task-card-meta">
          <span className={`task-status-pill task-status-pill--${statusClassName}`}>
            {formatStatus(form.status)}
          </span>
          <div className="task-card-contracts">
            {contracts.length === 0 ? (
              <span className="task-contract-empty">No contract</span>
            ) : contracts.map(function (contract, index) {
              const contractLabel = contract.contractNumber
                || contract.contractTitle
                || `Contract ${index + 1}`;

              return (
                <Button
                  key={contract.id}
                  type="button"
                  size="sm"
                  variant="outline-primary"
                  className="task-contract-view-button"
                  title={`View ${contractLabel}`}
                  onClick={function () {
                    onViewContract(contract.id);
                  }}
                >
                  <IconEye size={16} /> {contractLabel}
                </Button>
              );
            })}
          </div>
        </div>
      </header>

      <div className="task-card-form-grid">
        <Form.Group className="task-field task-field--title">
          <Form.Label>Task name</Form.Label>
        <Form.Control
          name="title"
          value={form.title}
          onChange={handleChange}
          disabled={taskIsDone}
          maxLength={255}
          aria-label="Task title"
          required
        />
        </Form.Group>
        <Form.Group className="task-field task-field--assignee">
          <Form.Label>Assignee</Form.Label>
        <Form.Select
          name="assignedToId"
          value={form.assignedToId}
          onChange={handleChange}
          disabled={!allowReassignment || taskIsDone}
          aria-label="Task assignee"
        >
          <option value="">Unassigned</option>
          {memberOptions.map(renderMemberOption)}
        </Form.Select>
        </Form.Group>
        <Form.Group className="task-field task-field--date">
          <Form.Label>Start date</Form.Label>
        <TaskDateInput
          name="startDate"
          value={form.startDate}
          min={phaseStartDate}
          max={form.endDate || phaseEndDate}
          onChange={handleChange}
          disabled={taskIsDone}
          aria-label="Task start date"
          required
        />
        </Form.Group>
        <Form.Group className="task-field task-field--date">
          <Form.Label>End date</Form.Label>
        <TaskDateInput
          name="endDate"
          value={form.endDate}
          min={form.startDate || phaseStartDate}
          max={phaseEndDate}
          onChange={handleChange}
          disabled={taskIsDone}
          aria-label="Task end date"
          required
        />
        </Form.Group>
        <Form.Group className="task-field task-field--status">
          <Form.Label>Status</Form.Label>
        {taskIsDone || !canChangeTaskStatus ? (
          <Form.Control value={form.status} disabled aria-label="Task status" />
        ) : (
          <Form.Select
            name="status"
            value={form.status}
            onChange={handleChange}
            aria-label="Task status"
            required
          >
            {statusOptions.map(renderStatusOption)}
          </Form.Select>
        )}
        </Form.Group>
      </div>

      <footer className="task-card-footer">
        <div className="task-row-feedback" aria-live="polite">
          {message && <span className="task-row-success">{message}</span>}
          {error && <span className="task-row-error">{error}</span>}
        </div>
        <div className="task-row-actions">
          <Button
            type="button"
            size="sm"
            variant="primary"
            disabled={saving || taskIsDone}
            onClick={handleSave}
          >
            <IconDeviceFloppy size={16} /> Save changes
          </Button>
          {canApproveTasks && canChangeTaskStatus && !taskIsDone && (
            <Button
              type="button"
              size="sm"
              variant="success"
              disabled={saving}
              onClick={handleMarkDone}
            >
              <IconCheck size={16} /> Mark as done
            </Button>
          )}
        </div>
      </footer>
    </article>
  );
}

function createForm(task) {
  return {
    title: task.title || "",
    assignedToId: task.assignedToId || "",
    startDate: task.startDate || "",
    endDate: task.endDate || "",
    status: task.status || "TODO",
  };
}

function validateForm(form, phaseStartDate, phaseEndDate) {
  if (!form.title.trim()) {
    return "Task title is required.";
  }

  if (!form.startDate || !form.endDate) {
    return "Task start date and end date are required.";
  }

  if (form.startDate > form.endDate) {
    return "Task start date must not be after its end date.";
  }

  if (form.startDate < phaseStartDate || form.endDate > phaseEndDate) {
    return "Task dates must be within the phase timeline.";
  }

  return "";
}

function renderMemberOption(member) {
  let label = member.name || member.email || "Unnamed member";

  if (member.name && member.email && member.name !== member.email) {
    label += ` (${member.email})`;
  }

  return (
    <option key={member.id} value={member.id}>
      {label}
    </option>
  );
}

function renderStatusOption(status) {
  return (
    <option key={status} value={status}>
      {String(status).replaceAll("_", " ")}
    </option>
  );
}

function formatStatus(status) {
  return String(status || "TODO")
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/(^|\s)\S/g, function (letter) {
      return letter.toUpperCase();
    });
}

function getErrorMessage(error) {
  if (error && error.response && error.response.data) {
    const responseData = error.response.data;

    if (responseData.message) {
      return responseData.message;
    }

    if (responseData.detail) {
      return responseData.detail;
    }
  }

  return "Unable to update this task. Please try again.";
}

// Hiển thị ngày theo dd/mm/yyyy nhưng vẫn dùng input date để mở lịch và lưu YYYY-MM-DD.
function TaskDateInput({
  value = "",
  disabled = false,
  ...inputProperties
}) {
  function openDatePicker(event) {
    if (typeof event.currentTarget.showPicker === "function") {
      event.currentTarget.showPicker();
    }
  }

  return (
    <div className="task-date-input">
      <input
        type="text"
        value={formatDateForInput(value)}
        placeholder="dd/mm/yyyy"
        className="form-control task-date-input__display"
        readOnly
        disabled={disabled}
        tabIndex={-1}
        aria-hidden="true"
      />
      <input
        {...inputProperties}
        type="date"
        value={value}
        disabled={disabled}
        className="task-date-input__native"
        onClick={openDatePicker}
      />
      <IconCalendar
        size={18}
        className="task-date-input__icon"
        aria-hidden="true"
      />
    </div>
  );
}

function formatDateForInput(value) {
  const matchedDate = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);

  if (!matchedDate) {
    return "";
  }

  return matchedDate[3] + "/" + matchedDate[2] + "/" + matchedDate[1];
}

export default TaskEditRow;
