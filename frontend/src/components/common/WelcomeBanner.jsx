import React from "react";
import {
    IconFileText,
    IconSignature,
    IconFolder,
    IconShieldCheck,
} from "@tabler/icons-react";

 function WelcomeBanner() {
    return (
        <section className="home-banner" aria-label="Welcome">
            <div className="home-banner__text">
                <h2>Welcome to E-CONTRACT</h2>
                <p>Manage your work from one secure and connected workspace.</p>
            </div>
            <div className="home-banner__graphic" aria-hidden="true">
                <div className="home-banner__step">
                    <IconFileText size={36} stroke={1.5} />
                </div>
                <span className="home-banner__connector" />
                <div className="home-banner__step">
                    <IconSignature size={36} stroke={1.5} />
                </div>
                <span className="home-banner__connector" />
                <div className="home-banner__step home-banner__step--filled">
                    <IconFolder size={36} stroke={1.5} />
                </div>
                <span className="home-banner__connector" />
                <div className="home-banner__step home-banner__step--filled">
                    <IconShieldCheck size={36} stroke={1.5} />
                </div>
            </div>
        </section>
    );
}
export  default WelcomeBanner;