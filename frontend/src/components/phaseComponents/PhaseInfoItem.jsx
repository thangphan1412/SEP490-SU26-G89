import "../../assets/styles/css/phaseStyles/PhaseComponents.css";

function PhaseInfoItem({ label, value, children }) {
  const displayValue = value === null || value === undefined || value === "" ? "-" : value;

  return (
    <div className="phase-info-item">
      <span className="phase-info-label">{label}</span>
      {children || <strong className="phase-info-value">{displayValue}</strong>}
    </div>
  );
}

export default PhaseInfoItem;
