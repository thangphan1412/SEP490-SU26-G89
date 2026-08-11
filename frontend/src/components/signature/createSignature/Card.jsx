import React, { useState } from "react";


function Card({ title, icon, children }) {
    return (
        <section className="card border shadow-sm mb-3">
            {title && (
                <div className="card-header bg-white d-flex align-items-center gap-2 px-4 py-3">
          <span className="text-primary d-flex align-items-center">
            {icon}
          </span>

                    <h2 className="mb-0 fs-6 fw-semibold text-dark">
                        {title}
                    </h2>
                </div>
            )}

            <div className="card-body p-4">
                {children}
            </div>
        </section>
    );
}

export default Card;