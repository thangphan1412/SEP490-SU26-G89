import React from "react";
import { IconShieldCheck } from "@tabler/icons-react";
 function ProtectionBanner() {
    return (
        <div className="protection-banner">
      <span className="protection-banner__icon" aria-hidden="true">
        <IconShieldCheck size={22} stroke={1.75} />
      </span>
            <span className="protection-banner__body">
        <span className="protection-banner__title">Your workspace is protected</span>
        <span className="protection-banner__description">
          Access is managed securely according to your account permissions.
        </span>
      </span>
        </div>
    );
}
export default ProtectionBanner