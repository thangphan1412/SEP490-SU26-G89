import { IconInbox } from "@tabler/icons-react";

function EmptyTableRow({ colSpan, message }) {
  return (
    <tr>
      <td colSpan={colSpan} className="phase-empty-row">
        <div className="phase-empty-content">
          <IconInbox size={25} stroke={1.6} aria-hidden="true" />
          <span>{message}</span>
        </div>
      </td>
    </tr>
  );
}

export default EmptyTableRow;
