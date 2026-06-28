function InfoComponent({ label, value, pill = false }) {
  return (
    <div>
      <p className="view-info-label">{label}</p>
      {pill ? (
        <span className="view-pill">{value}</span>
      ) : (
        <p className="view-info-value">{value}</p>
      )}
    </div>
  );
}

export default InfoComponent;
