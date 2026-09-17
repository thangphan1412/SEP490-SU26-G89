import "../../assets/styles/css/phaseStyles/PhaseComponents.css";

function PhaseInfoItem({ label, value, icon }) {
  const displayValue = value === null || value === undefined || value === "" ? "-" : value;

  return (
    <div className="phase-info-item">
      {icon && <span className="phase-info-icon" aria-hidden="true">{icon}</span>}
      <div className="phase-info-copy">
        <span className="phase-info-label">{label}</span>
        <strong className="phase-info-value">{displayValue}</strong>
      </div>
    </div>
  );
}

export default PhaseInfoItem;
