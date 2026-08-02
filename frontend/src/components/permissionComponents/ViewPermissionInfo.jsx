import "../../assets/styles/css/permissionStyles/PermissionComponents.css";

function ViewPermissionInfo({ label, value }) {
  const displayValue = value === null || value === undefined || value === "" ? "-" : value;

  return (
    <div className="permission-info-item">
      <p className="permission-info-label">{label}</p>
      <p className="permission-info-value">{displayValue}</p>
    </div>
  );
}

export default ViewPermissionInfo;
