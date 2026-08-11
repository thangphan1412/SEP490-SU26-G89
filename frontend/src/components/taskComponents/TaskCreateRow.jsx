import { useState } from "react";
import { Button, Form } from "react-bootstrap";
import { IconPlus, IconX } from "@tabler/icons-react";

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
    <tr className="task-create-row">
      <td>
        <Form.Control
          name="title"
          value={form.title}
          onChange={handleChange}
          maxLength={255}
          placeholder="Enter task title"
          aria-label="New task title"
          required
        />
      </td>
      <td>
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
      </td>
      <td>
        <Form.Control
          type="date"
          name="startDate"
          value={form.startDate}
          min={phaseStartDate}
          max={form.endDate || phaseEndDate}
          onChange={handleChange}
          aria-label="New task start date"
          required
        />
      </td>
      <td>
        <Form.Control
          type="date"
          name="endDate"
          value={form.endDate}
          min={form.startDate || phaseStartDate}
          max={phaseEndDate}
          onChange={handleChange}
          aria-label="New task end date"
          required
        />
      </td>
      <td>
        <Form.Control value="TODO" disabled aria-label="New task status" />
      </td>
      <td className="task-contract-cell">-</td>
      <td className="task-row-actions">
        <Button
          type="button"
          size="sm"
          variant="primary"
          disabled={creating}
          onClick={handleCreate}
        >
          <IconPlus size={16} />
          {creating ? "Creating..." : "Create"}
        </Button>
        <Button
          type="button"
          size="sm"
          variant="outline-secondary"
          disabled={creating}
          onClick={onCancel}
        >
          <IconX size={16} /> Cancel
        </Button>
        {error && <span className="task-row-error">{error}</span>}
      </td>
    </tr>
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

export default TaskCreateRow;
