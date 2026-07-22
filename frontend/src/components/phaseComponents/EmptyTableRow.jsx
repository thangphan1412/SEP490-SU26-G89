function EmptyTableRow({ colSpan, message }) {
  return (
    <tr>
      <td colSpan={colSpan} className="phase-empty-row">{message}</td>
    </tr>
  );
}

export default EmptyTableRow;
