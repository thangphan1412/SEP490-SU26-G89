function Select({
                    value,
                    onChange,
                    options,
                }) {
    return (
        <select
            value={value}
            onChange={onChange}
            className="form-select form-select-sm"
        >
            {options.map((opt) => (
                <option key={opt} value={opt}>
                    {opt}
                </option>
            ))}
        </select>
    );
}

export default Select;