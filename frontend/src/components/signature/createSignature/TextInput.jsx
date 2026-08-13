function TextInput({
                       placeholder,
                       value,
                       onChange,
                   }) {
    return (
        <input
            type="text"
            className="form-control form-control-sm"
            placeholder={placeholder}
            value={value}
            onChange={onChange}
        />
    );
}

export default TextInput;