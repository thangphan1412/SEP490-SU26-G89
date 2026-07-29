import React from "react";

import "../../assets/styles/css/homepage/HomePage.css";
import WelcomeBanner from "../../components/common/WelcomeBanner.jsx";
import DestinationGrid from "../../components/common/DestinationGrid.jsx";
import ProtectionBanner from "../../components/common/ProtectionBanner.jsx";


function Homepage(){


    return (
        <main className="home-content">
            <div className="home-content__intro">
                <h1>Home</h1>
                <p>Access your workspace and choose where you want to go.</p>
            </div>

            <WelcomeBanner />

            <div className="home-content__subheading">
                <h2>What would you like to do?</h2>
                <p>Choose a destination to continue.</p>
            </div>

            <DestinationGrid />

            <ProtectionBanner />
        </main>
    );
}
export default Homepage