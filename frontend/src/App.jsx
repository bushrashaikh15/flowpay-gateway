import {
    BrowserRouter,
    Routes,
    Route,
    Navigate
} from "react-router-dom";

import "./App.css";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import PaymentIntents from "./pages/PaymentIntents";
import Transactions from "./pages/Transactions";
import AuditLogs from "./pages/AuditLogs";
import Analytics from "./pages/Analytics";


function App() {

    return (

        <BrowserRouter>

            <Routes>

                <Route
                    path="/"
                    element={
                        <Navigate
                            to="/login"
                            replace
                        />
                    }
                />

                <Route
                    path="/login"
                    element={<Login />}
                />

                <Route
                    path="/dashboard"
                    element={<Dashboard />}
                />

                <Route
                    path="/payment-intents"
                    element={<PaymentIntents />}
                />

                <Route
                    path="/transactions"
                    element={<Transactions />}
                />

                <Route
                    path="/audit-logs"
                    element={<AuditLogs />}
                />

                <Route
                    path="/analytics"
                    element={<Analytics />}
                />

            </Routes>

        </BrowserRouter>
    );
}

export default App;