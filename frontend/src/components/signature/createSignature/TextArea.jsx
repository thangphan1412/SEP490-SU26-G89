function TextArea({
                      placeholder,
                      value,
                      onChange,
                      rows = 3,
                  }) {
    return (
        <textarea
            rows={rows}
            className="form-control form-control-sm"
            placeholder={placeholder}
            value={value}
            onChange={onChange}
        />
    );
}

export default TextArea;