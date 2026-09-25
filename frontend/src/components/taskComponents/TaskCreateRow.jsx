import { useState } from "react";
import { Button, Form } from "react-bootstrap";
import { IconCalendar, IconPlus, IconX } from "@tabler/icons-react";

function TaskCreateRow({
  memberOptions,
  phaseStartDate,
  phaseEndDate,
  allowReassignment,
  onCreate,
  onCancel,
}) {
  const [form, setForm] = useState(createInitialForm(
    memberOptions,
    phaseStartDate,
    phaseEndDate,
    allowReassignment
  ));
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState("");

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

  async function handleCreate() {
    const validationMessage = validateForm(
      form,
      phaseStartDate,
      phaseEndDate
    );

    if (validationMessage) {
      setError(validationMessage);
      return;
    }

    try {
      setCreating(true);
      setError("");
      await onCreate({
        title: form.title.trim(),
        startDate: form.startDate,
        endDate: form.endDate,
        assignedToId: form.assignedToId || null,
      });
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setCreating(false);
    }
  }

  return (
    <article className="task-card task-card--create">
      <header className="task-card-header">
        <div className="task-card-identity">
          <span className="task-card-number task-card-number--new">
            <IconPlus size={18} />
          </span>
          <div>
            <span>New task</span>
            <strong>Plan a new phase task</strong>
          </div>
        </div>
        <span className="task-status-pill task-status-pill--todo">To do</span>
      </header>

      <div className="task-card-form-grid">
        <Form.Group className="task-field task-field--title">
          <Form.Label>Task name</Form.Label>
        <Form.Control
          name="title"
          value={form.title}
          onChange={handleChange}
          maxLength={255}
          placeholder="Enter task title"
          aria-label="New task title"
          required
        />
        </Form.Group>
        <Form.Group className="task-field task-field--assignee">
          <Form.Label>Assignee</Form.Label>
        <Form.Select
          name="assignedToId"
          value={form.assignedToId}
          onChange={handleChange}
          disabled={!allowReassignment}
          aria-label="New task assignee"
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
          aria-label="New task start date"
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
          aria-label="New task end date"
          required
        />
        </Form.Group>
        <Form.Group className="task-field task-field--status">
          <Form.Label>Status</Form.Label>
          <Form.Control value="TODO" disabled aria-label="New task status" />
        </Form.Group>
      </div>

      <footer className="task-card-footer">
        <div className="task-row-feedback" aria-live="polite">
          {error && <span className="task-row-error">{error}</span>}
        </div>
        <div className="task-row-actions">
          <Button
            type="button"
            size="sm"
            variant="outline-secondary"
            disabled={creating}
            onClick={onCancel}
          >
            <IconX size={16} /> Cancel
          </Button>
          <Button
            type="button"
            size="sm"
            variant="primary"
            disabled={creating}
            onClick={handleCreate}
          >
            <IconPlus size={16} />
            {creating ? "Creating..." : "Create task"}
          </Button>
        </div>
      </footer>
    </article>
  );
}

function createInitialForm(
  memberOptions,
  phaseStartDate,
  phaseEndDate,
  allowReassignment
) {
  let assignedToId = "";

  if (!allowReassignment && memberOptions.length > 0) {
    assignedToId = memberOptions[0].id;
  }

  return {
    title: "",
    assignedToId,
    startDate: phaseStartDate || "",
    endDate: phaseEndDate || "",
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

  return "Unable to create this task. Please try again.";
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

export default TaskCreateRow;
