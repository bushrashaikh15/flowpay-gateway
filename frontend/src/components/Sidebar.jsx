import { useNavigate, useLocation } from "react-router-dom";

function Sidebar() {

    const navigate = useNavigate();
    const location = useLocation();

    function handleLogout() {

        localStorage.removeItem("flowpay_api_key");

        navigate("/login");
    }

    function getClass(path) {

        return location.pathname === path
            ? "text-white fw-bold mb-3"
            : "text-secondary mb-3";
    }

    return (

        <div className="col-md-2 bg-dark text-white min-vh-100 p-4">

            <h3
                className="mb-5"
                style={{ cursor: "pointer" }}
                onClick={() => navigate("/dashboard")}
            >
                FlowPay
            </h3>


            {/* DASHBOARD */}

            <div
                className={getClass("/dashboard")}
                style={{ cursor: "pointer" }}
                onClick={() => navigate("/dashboard")}
            >
                Dashboard
            </div>


            {/* PAYMENTS */}

            <div
                className={getClass("/payment-intents")}
                style={{ cursor: "pointer" }}
                onClick={() => navigate("/payment-intents")}
            >
                Payments
            </div>


            {/* TRANSACTIONS */}

            <div
                className={getClass("/transactions")}
                style={{ cursor: "pointer" }}
                onClick={() => navigate("/transactions")}
            >
                Transactions
            </div>


            {/* AUDIT LOGS */}

            <div
                className={getClass("/audit-logs")}
                style={{ cursor: "pointer" }}
                onClick={() => navigate("/audit-logs")}
            >
                Audit Logs
            </div>


            {/* AI ANALYTICS */}

            <div
                className={getClass("/analytics")}
                style={{ cursor: "pointer" }}
                onClick={() => navigate("/analytics")}
            >
                🤖 AI Analytics
            </div>


            <hr className="border-secondary mt-5" />


            {/* LOGOUT */}

            <button
                className="btn btn-outline-light w-100 mt-3"
                onClick={handleLogout}
            >
                Logout
            </button>

        </div>
    );
}

export default Sidebar;