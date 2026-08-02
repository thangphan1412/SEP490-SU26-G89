import React from "react";
import { IconArrowRight } from "@tabler/icons-react";

function DestinationCard({ icon: Icon, title, description, href }) {
    return (
        <a className="destination-card" href={href}>
      <span className="destination-card__icon">
        <Icon size={26} stroke={1.75} />
      </span>
            <span className="destination-card__body">
        <span className="destination-card__title">{title}</span>
        <span className="destination-card__description">{description}</span>
      </span>
            <span className="destination-card__arrow" aria-hidden="true">
        <IconArrowRight size={18} stroke={1.75} />
      </span>
        </a>
    );
}
export default DestinationCard;