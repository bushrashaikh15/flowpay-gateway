import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import Sidebar from "../components/Sidebar";

function Dashboard() {

    const navigate = useNavigate();

    const [stats, setStats] = useState({
        totalPayments: 0,
        createdPayments: 0,
        authorizedPayments: 0,
        capturedPayments: 0,
        refundedPayments: 0,
        failedPayments: 0,
        capturedAmount: 0,
        refundedAmount: 0
    });

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchDashboardStats();
    }, []);

    async function fetchDashboardStats() {

        try {

            setLoading(true);
            setError("");

            const response =
                await api.get("/api/payment-intents", {
                    params: {
                        page: 0,
                        size: 100
                    }
                });

            const paymentIntents =
                response.data.content || [];

            const calculatedStats = {
                totalPayments: paymentIntents.length,

                createdPayments:
                paymentIntents.filter(
                    payment =>
                        payment.status === "CREATED"
                ).length,

                authorizedPayments:
                paymentIntents.filter(
                    payment =>
                        payment.status === "AUTHORIZED"
                ).length,

                capturedPayments:
                paymentIntents.filter(
                    payment =>
                        payment.status === "CAPTURED"
                ).length,

                refundedPayments:
                paymentIntents.filter(
                    payment =>
                        payment.status === "REFUNDED"
                ).length,

                failedPayments:
                paymentIntents.filter(
                    payment =>
                        payment.status === "FAILED"
                ).length,

                capturedAmount:
                    paymentIntents
                        .filter(
                            payment =>
                                payment.status === "CAPTURED"
                        )
                        .reduce(
                            (total, payment) =>
                                total +
                                Number(payment.amount || 0),
                            0
                        ),

                refundedAmount:
                    paymentIntents
                        .filter(
                            payment =>
                                payment.status === "REFUNDED"
                        )
                        .reduce(
                            (total, payment) =>
                                total +
                                Number(payment.amount || 0),
                            0
                        )
            };

            setStats(calculatedStats);

        } catch (error) {

            console.error(error);

            if (error.response?.status === 401) {

                setError(
                    "Session expired or API key is invalid."
                );

            } else {

                setError(
                    "Unable to load dashboard statistics."
                );
            }

        } finally {

            setLoading(false);

        }
    }

    function formatAmount(amount) {

        return new Intl.NumberFormat(
            "en-IN",
            {
                style: "currency",
                currency: "INR",
                maximumFractionDigits: 2
            }
        ).format(amount);
    }

    const apiKey =
        localStorage.getItem("flowpay_api_key");

    return (

        <div className="container-fluid">

            <div className="row">

                {/* REUSABLE SIDEBAR */}

                <Sidebar />

                {/* MAIN CONTENT */}

                <div className="col-md-10 p-4">

                    {/* HEADER */}

                    <div className="d-flex justify-content-between align-items-center mb-4">

                        <div>

                            <h2 className="fw-bold mb-1">
                                Dashboard
                            </h2>

                            <p className="text-muted mb-0">
                                Monitor your FlowPay payment activity
                            </p>

                        </div>

                        <button
                            className="btn btn-outline-dark"
                            onClick={fetchDashboardStats}
                            disabled={loading}
                        >
                            ↻ Refresh
                        </button>

                    </div>


                    {/* ERROR */}

                    {error && (

                        <div className="alert alert-danger">
                            {error}
                        </div>

                    )}


                    {/* STATISTICS */}

                    <div className="row">

                        {/* TOTAL PAYMENTS */}

                        <div className="col-md-3 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <p className="text-muted mb-2">
                                        Total Payments
                                    </p>

                                    <h2 className="fw-bold mb-0">

                                        {loading
                                            ? "..."
                                            : stats.totalPayments}

                                    </h2>

                                </div>

                            </div>

                        </div>


                        {/* CAPTURED PAYMENTS */}

                        <div className="col-md-3 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <p className="text-muted mb-2">
                                        Captured Payments
                                    </p>

                                    <h2 className="fw-bold text-success mb-0">

                                        {loading
                                            ? "..."
                                            : stats.capturedPayments}

                                    </h2>

                                </div>

                            </div>

                        </div>


                        {/* CAPTURED AMOUNT */}

                        <div className="col-md-3 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <p className="text-muted mb-2">
                                        Captured Amount
                                    </p>

                                    <h2 className="fw-bold mb-0">

                                        {loading
                                            ? "..."
                                            : formatAmount(
                                                stats.capturedAmount
                                            )}

                                    </h2>

                                </div>

                            </div>

                        </div>


                        {/* REFUNDED AMOUNT */}

                        <div className="col-md-3 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <p className="text-muted mb-2">
                                        Refunded Amount
                                    </p>

                                    <h2 className="fw-bold text-info mb-0">

                                        {loading
                                            ? "..."
                                            : formatAmount(
                                                stats.refundedAmount
                                            )}

                                    </h2>

                                </div>

                            </div>

                        </div>

                    </div>


                    {/* PAYMENT STATUS */}

                    <div className="card shadow-sm mb-4">

                        <div className="card-body">

                            <h5 className="mb-4">
                                Payment Status
                            </h5>

                            <div className="row">

                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Created
                                    </div>

                                    <h4>
                                        {stats.createdPayments}
                                    </h4>

                                </div>


                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Authorized
                                    </div>

                                    <h4>
                                        {stats.authorizedPayments}
                                    </h4>

                                </div>


                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Captured
                                    </div>

                                    <h4 className="text-success">
                                        {stats.capturedPayments}
                                    </h4>

                                </div>


                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Refunded
                                    </div>

                                    <h4 className="text-info">
                                        {stats.refundedPayments}
                                    </h4>

                                </div>


                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Failed
                                    </div>

                                    <h4 className="text-danger">
                                        {stats.failedPayments}
                                    </h4>

                                </div>

                            </div>

                        </div>

                    </div>


                    {/* NAVIGATION CARDS */}

                    <div className="row">

                        {/* PAYMENT INTENTS */}

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <h5 className="card-title">
                                        Payment Intents
                                    </h5>

                                    <p className="card-text text-muted">
                                        Create, authorize, capture and
                                        refund payments.
                                    </p>

                                    <button
                                        className="btn btn-dark"
                                        onClick={() =>
                                            navigate(
                                                "/payment-intents"
                                            )
                                        }
                                    >
                                        View Payments
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
                                        View captured and refunded
                                        transaction history.
                                    </p>

                                    <button
                                        className="btn btn-outline-dark"
                                        onClick={() =>
                                            navigate(
                                                "/transactions"
                                            )
                                        }
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
                                        Monitor payment activity and
                                        system events.
                                    </p>

                                    <button
                                        className="btn btn-outline-dark"
                                        onClick={() =>
                                            navigate(
                                                "/audit-logs"
                                            )
                                        }
                                    >
                                        View Audit Logs
                                    </button>

                                </div>

                            </div>

                        </div>

                    </div>


                    {/* AUTHENTICATION STATUS */}

                    <div className="card shadow-sm mt-2">

                        <div className="card-body">

                            <div className="d-flex justify-content-between align-items-center">

                                <div>

                                    <h5 className="mb-1">
                                        Authentication Status
                                    </h5>

                                    <p className="text-success mb-0">

                                        {apiKey
                                            ? "API key authenticated successfully"
                                            : "No API key found"}

                                    </p>

                                </div>

                                <span className="badge bg-success">
                                    SECURE
                                </span>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </div>
    );
}

export default Dashboard;