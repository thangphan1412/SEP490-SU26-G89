function Toggle({ checked, onChange }) {
    return (
        <div className="form-check form-switch">
            <input
                className="form-check-input"
                type="checkbox"
                role="switch"
                checked={checked}
                onChange={(e) => onChange(e.target.checked)}
                style={{
                    width: "38px",
                    height: "21px",
                    cursor: "pointer",
                }}
            />
        </div>
    );
}

export default Toggle;