function FormFieldComponent({ label, required = false, children }) {
  return (
    <div className="update-field">
      <span className="update-label">
        {label} {required && <span className="update-required">*</span>}
      </span>
      {children}
    </div>
  );
}

export default FormFieldComponent;
