function ScopeCardComponent({ title, description, active = false }) {
  return (
    <button type="button" className="create-scope-card">
      <span className={active ? "create-radio active" : "create-radio"} />
      <span>
        <strong>{title}</strong>
        <small>{description}</small>
      </span>
    </button>
  );
}

export default ScopeCardComponent;
