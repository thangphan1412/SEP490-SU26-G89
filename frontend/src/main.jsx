import { StrictMode } from 'react'

import React from "react";
import ReactDom from "react-dom/client"
import App from './App.jsx'
import 'bootstrap/dist/css/bootstrap.min.css';



ReactDom.createRoot(document.getElementById('root')).render(
    <StrictMode>
        <App/>
    </StrictMode>
)
