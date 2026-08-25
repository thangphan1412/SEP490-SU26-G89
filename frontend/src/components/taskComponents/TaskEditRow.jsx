import { useState } from "react";
import { Button, Form } from "react-bootstrap";
import { IconCalendar, IconCheck, IconDeviceFloppy, IconEye } from "@tabler/icons-react";

function TaskEditRow({
  task,
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
    <tr className="task-edit-row">
      <td>
        <Form.Control
          name="title"
          value={form.title}
          onChange={handleChange}
          disabled={taskIsDone}
          maxLength={255}
          aria-label="Task title"
          required
        />
      </td>
      <td>
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
      </td>
      <td>
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
      </td>
      <td>
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
      </td>
      <td>
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
      </td>
      <td className="task-contract-cell">
        {contracts.length === 0 ? (
          <span>-</span>
        ) : (
          <div className="task-contract-buttons">
            {contracts.map(function (contract) {
              const contractLabel = contract.contractNumber
                || contract.contractTitle
                || "contract";

              return (
                <Button
                  key={contract.id}
                  type="button"
                  size="sm"
                  variant="outline-primary"
                  className="task-contract-view-button"
                  title={`View ${contractLabel}`}
                  aria-label={`View ${contractLabel}`}
                  onClick={function () {
                    onViewContract(contract.id);
                  }}
                >
                  <IconEye size={17} />
                </Button>
              );
            })}
          </div>
        )}
      </td>
      <td className="task-row-actions">
        <Button
          type="button"
          size="sm"
          variant="primary"
          disabled={saving || taskIsDone}
          onClick={handleSave}
        >
          <IconDeviceFloppy size={16} /> Save
        </Button>
        {canApproveTasks && canChangeTaskStatus && !taskIsDone && (
          <Button
            type="button"
            size="sm"
            variant="success"
            disabled={saving}
            onClick={handleMarkDone}
          >
            <IconCheck size={16} /> Mark As Done
          </Button>
        )}
        {message && <span className="task-row-success">{message}</span>}
        {error && <span className="task-row-error">{error}</span>}
      </td>
    </tr>
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
  readOnly = false,
  ...inputProperties
}) {
  function openDatePicker(event) {
    if (!readOnly && typeof event.currentTarget.showPicker === "function") {
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
        readOnly={readOnly}
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
