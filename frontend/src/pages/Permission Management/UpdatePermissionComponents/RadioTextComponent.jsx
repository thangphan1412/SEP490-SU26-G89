function RadioTextComponent({ text, active = false }) {
  return (
    <button type="button" className="update-radio-text">
      <span className={active ? "update-radio active" : "update-radio"} />
      {text}
    </button>
  );
}

export default RadioTextComponent;
