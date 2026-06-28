function StatusBadgeComponent({ status }) {
  const badgeClass =
    status === "Active" ? "list-status active" : "list-status inactive";

  return <span className={badgeClass}>{status}</span>;
}

export default StatusBadgeComponent;
