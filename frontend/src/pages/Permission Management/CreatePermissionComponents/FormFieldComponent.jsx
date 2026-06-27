function FormFieldComponent({ label, required = false, children }) {
  return (
    <div className="create-field">
      <span className="create-label">
        {label} {required && <span className="create-required">*</span>}
      </span>
      {children}
    </div>
  );
}

export default FormFieldComponent;
