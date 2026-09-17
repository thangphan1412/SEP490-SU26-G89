function Icon({ name, size = 22, color = "#1f4fff" }) {
  const props = {
    width: size,
    height: size,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: color,
    strokeWidth: 2,
    strokeLinecap: "round",
    strokeLinejoin: "round",
    "aria-hidden": true,
  };
  const paths = {
    plus: <><circle cx="12" cy="12" r="9" /><path d="M12 8v8" /><path d="M8 12h8" /></>,
    search: <><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></>,
    filter: <path d="M4 5h16l-6 7v5l-4 2v-7z" />,
    sort: <><path d="m8 9 3-3 3 3" /><path d="m14 15-3 3-3-3" /></>,
    arrowLeft: <path d="m15 18-6-6 6-6" />,
    arrowRight: <path d="m9 18 6-6-6-6" />,
    document: <><path d="M7 3h7l4 4v14H7z" /><path d="M14 3v5h5" /><path d="M10 13h5" /><path d="M10 17h4" /></>,
    users: <><circle cx="9" cy="8" r="3.5" /><path d="M2 20c1.4-3.6 3.7-5.4 7-5.4 3.2 0 5.6 1.8 7 5.4" /><path d="M17 11a3 3 0 1 0-1.2-5.8" /><path d="M18 14.5c1.8.6 3.1 2.2 4 5.5" /></>,
    building: <><path d="M4 21h16" /><path d="M6 21V5h7v16" /><path d="M13 9h5v12" /><path d="M9 9h1" /><path d="M16 13h1" /></>,
    shield: <><path d="M12 3 19 6v5c0 5-3.5 8.5-7 10-3.5-1.5-7-5-7-10V6z" /><path d="m9 12 2 2 4-4" /></>,
    chart: <><path d="M5 20V9" /><path d="M12 20V4" /><path d="M19 20v-7" /><path d="M3 20h18" /></>,
    edit: <><path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z" /><path d="m13.5 6.5 4 4" /></>,
    trash: <><path d="M4 7h16" /><path d="M9 7V4h6v3" /><path d="M7 7l1 14h8l1-14" /><path d="M10 11v6" /><path d="M14 11v6" /></>,
    save: <><path d="M5 3h12l2 2v16H5z" /><path d="M8 3v6h8V3" /><path d="M8 21v-7h8v7" /></>,
    calendar: <><path d="M5 4h14v16H5z" /><path d="M8 2v4" /><path d="M16 2v4" /><path d="M5 9h14" /></>,
    flag: <><path d="M5 21V4" /><path d="M5 5h11l-1.5 4L16 13H5" /></>,
    info: <><circle cx="12" cy="12" r="9" /><path d="M12 11v5" /><path d="M12 8h.01" /></>,
  };

  return <svg {...props}>{paths[name]}</svg>;
}

export default Icon;
