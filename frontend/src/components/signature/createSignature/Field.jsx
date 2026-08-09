function Field({ label, required, hint, children }) {
    return (
        <div className="mb-3">
            <label className="form-label small fw-medium text-dark mb-2">
                {label}

                {required && (
                    <span className="text-danger"> *</span>
                )}
            </label>

            {children}

            {hint && (
                <div className="form-text text-secondary">
                    {hint}
                </div>
            )}
        </div>
    );
}

export default Field;