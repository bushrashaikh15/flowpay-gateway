import { useNavigate } from "react-router-dom";

function Dashboard() {

    const apiKey =
        localStorage.getItem("flowpay_api_key");

    const navigate = useNavigate();

    return (
        <div className="container-fluid">

            <div className="row">

                {/* SIDEBAR */}

                <div className="col-md-2 bg-dark text-white min-vh-100 p-4">

                    <h3 className="mb-4">
                        FlowPay
                    </h3>

                    <div
                        className="mb-3"
                        style={{ cursor: "pointer" }}
                        onClick={() => navigate("/dashboard")}
                    >
                        Dashboard
                    </div>

                    <div
                        className="mb-3"
                        style={{ cursor: "pointer" }}
                        onClick={() => navigate("/payment-intents")}
                    >
                        Payments
                    </div>

                    <div
                        className="mb-3"
                        style={{ cursor: "pointer" }}
                        onClick={() => navigate("/transactions")}
                    >
                        Transactions
                    </div>

                    <div
                        className="mb-3"
                        style={{ cursor: "pointer" }}
                        onClick={() => navigate("/audit-logs")}
                    >
                        Audit Logs
                    </div>

                </div>


                {/* MAIN CONTENT */}

                <div className="col-md-10 p-4">

                    <h2 className="mb-4">
                        Dashboard
                    </h2>


                    {/* DASHBOARD CARDS */}

                    <div className="row">


                        {/* PAYMENT INTENTS */}

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <h5 className="card-title">
                                        Payment Intents
                                    </h5>

                                    <p className="card-text text-muted">
                                        Manage your payment intents.
                                    </p>

                                    <button
                                        className="btn btn-dark"
                                        onClick={() =>
                                            navigate(
                                                "/payment-intents"
                                            )
                                        }
                                    >
                                        View Payment Intents
                                    </button>

                                </div>

                            </div>

                        </div>


                        {/* TRANSACTIONS */}

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <h5 className="card-title">
                                        Transactions
                                    </h5>

                                    <p className="card-text text-muted">
                                        View your transaction history.
                                    </p>

                                    <button
                                        className="btn btn-outline-dark"
                                        onClick={() => navigate("/transactions")}
                                    >
                                        View Transactions
                                    </button>

                                </div>

                            </div>

                        </div>


                        {/* AUDIT LOGS */}

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <h5 className="card-title">
                                        Audit Logs
                                    </h5>

                                    <p className="card-text text-muted">
                                        Monitor payment activity.
                                    </p>

                                    <button
                                        className="btn btn-outline-dark"
                                        onClick={() => navigate("/audit-logs")}
                                    >
                                        View Audit Logs
                                    </button>

                                </div>

                            </div>

                        </div>

                    </div>


                    {/* AUTHENTICATION STATUS */}

                    <div className="card shadow-sm mt-3">

                        <div className="card-body">

                            <h5>
                                Authentication Status
                            </h5>

                            <p className="text-success mb-0">

                                {apiKey
                                    ? "API key stored successfully"
                                    : "No API key found"}

                            </p>

                        </div>

                    </div>

                </div>

            </div>

        </div>
    );
}

export default Dashboard;