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
        refundedAmount: 0,
        mostUsedCurrency: null
    });

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [lastUpdated, setLastUpdated] = useState(null);

    useEffect(() => {
        fetchDashboardStats();
    }, []);

    // ============================================================
    // FETCH DASHBOARD SUMMARY
    // ============================================================

    async function fetchDashboardStats() {

        try {

            setLoading(true);
            setError("");

            const response =
                await api.get(
                    "/api/dashboard/summary"
                );

            const data = response.data;

            setStats({
                totalPayments:
                    data.totalPayments ?? 0,

                createdPayments:
                    data.createdPayments ?? 0,

                authorizedPayments:
                    data.authorizedPayments ?? 0,

                capturedPayments:
                    data.capturedPayments ?? 0,

                refundedPayments:
                    data.refundedPayments ?? 0,

                failedPayments:
                    data.failedPayments ?? 0,

                capturedAmount:
                    Number(data.capturedAmount ?? 0),

                refundedAmount:
                    Number(data.refundedAmount ?? 0),

                mostUsedCurrency:
                    data.mostUsedCurrency ?? null
            });

            setLastUpdated(
                new Date()
            );

        } catch (error) {

            console.error(
                "Dashboard error:",
                error
            );

            if (
                error.response?.status === 401
            ) {

                setError(
                    "Session expired or API key is invalid."
                );

            } else if (
                error.response?.status === 429
            ) {

                setError(
                    "Too many requests. Please wait a moment and refresh."
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

    // ============================================================
    // FORMAT CURRENCY
    // ============================================================

    function formatAmount(amount) {

        const currency =
            stats.mostUsedCurrency || "INR";

        try {

            return new Intl.NumberFormat(
                "en-IN",
                {
                    style: "currency",
                    currency: currency,
                    maximumFractionDigits: 2
                }
            ).format(amount);

        } catch {

            return `${currency} ${Number(amount).toFixed(2)}`;
        }
    }

    // ============================================================
    // LAST UPDATED
    // ============================================================

    function formatLastUpdated() {

        if (!lastUpdated) {
            return "";
        }

        return lastUpdated.toLocaleTimeString(
            "en-IN",
            {
                hour: "2-digit",
                minute: "2-digit"
            }
        );
    }

    // ============================================================
    // CURRENCY LABEL
    // ============================================================

    function getCurrencyLabel() {

        return stats.mostUsedCurrency
            || "No currency data";
    }

    const apiKey =
        localStorage.getItem(
            "flowpay_api_key"
        );

    // ============================================================
    // UI
    // ============================================================

    return (

        <div className="container-fluid">

            <div className="row">

                {/* ==================================================
                    SIDEBAR
                ================================================== */}

                <Sidebar />

                {/* ==================================================
                    MAIN CONTENT
                ================================================== */}

                <div className="col-md-10 p-4">

                    {/* ==================================================
                        HEADER
                    ================================================== */}

                    <div className="d-flex justify-content-between align-items-center mb-4">

                        <div>

                            <h2 className="fw-bold mb-1">
                                Dashboard
                            </h2>

                            <p className="text-muted mb-0">
                                Monitor your FlowPay payment activity
                            </p>

                        </div>

                        <div className="d-flex align-items-center gap-3">

                            {lastUpdated && (

                                <small className="text-muted">
                                    Last updated:{" "}
                                    {formatLastUpdated()}
                                </small>

                            )}

                            <button
                                className="btn btn-outline-dark"
                                onClick={
                                    fetchDashboardStats
                                }
                                disabled={loading}
                            >

                                {loading
                                    ? "Refreshing..."
                                    : "↻ Refresh"}

                            </button>

                        </div>

                    </div>

                    {/* ==================================================
                        ERROR
                    ================================================== */}

                    {error && (

                        <div className="alert alert-danger d-flex justify-content-between align-items-center">

                            <span>
                                {error}
                            </span>

                            <button
                                className="btn btn-sm btn-outline-danger"
                                onClick={
                                    fetchDashboardStats
                                }
                            >
                                Retry
                            </button>

                        </div>

                    )}

                    {/* ==================================================
                        TOP STATISTICS
                    ================================================== */}

                    <div className="row">

                        {/* TOTAL PAYMENTS */}

                        <div className="col-md-3 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <div className="d-flex justify-content-between align-items-start">

                                        <div>

                                            <p className="text-muted mb-2">
                                                Total Payments
                                            </p>

                                            <h2 className="fw-bold mb-1">

                                                {loading
                                                    ? "..."
                                                    : stats.totalPayments}

                                            </h2>

                                            <small className="text-muted">
                                                All payment intents
                                            </small>

                                        </div>

                                        <div
                                            className="bg-light rounded p-2"
                                            style={{
                                                fontSize: "22px"
                                            }}
                                        >
                                            ◉
                                        </div>

                                    </div>

                                </div>

                            </div>

                        </div>

                        {/* CAPTURED PAYMENTS */}

                        <div className="col-md-3 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <div className="d-flex justify-content-between align-items-start">

                                        <div>

                                            <p className="text-muted mb-2">
                                                Captured Payments
                                            </p>

                                            <h2 className="fw-bold text-success mb-1">

                                                {loading
                                                    ? "..."
                                                    : stats.capturedPayments}

                                            </h2>

                                            <small className="text-muted">
                                                Successfully captured
                                            </small>

                                        </div>

                                        <div
                                            className="bg-success-subtle rounded p-2"
                                            style={{
                                                fontSize: "22px"
                                            }}
                                        >
                                            ✓
                                        </div>

                                    </div>

                                </div>

                            </div>

                        </div>

                        {/* CAPTURED AMOUNT */}

                        <div className="col-md-3 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <div className="d-flex justify-content-between align-items-start">

                                        <div>

                                            <p className="text-muted mb-2">
                                                Captured Amount
                                            </p>

                                            <h2 className="fw-bold mb-1">

                                                {loading
                                                    ? "..."
                                                    : formatAmount(
                                                        stats.capturedAmount
                                                    )}

                                            </h2>

                                            <small className="text-muted">
                                                {getCurrencyLabel()}
                                            </small>

                                        </div>

                                        <div
                                            className="bg-light rounded p-2"
                                            style={{
                                                fontSize: "22px"
                                            }}
                                        >
                                            ₹
                                        </div>

                                    </div>

                                </div>

                            </div>

                        </div>

                        {/* REFUNDED AMOUNT */}

                        <div className="col-md-3 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <div className="d-flex justify-content-between align-items-start">

                                        <div>

                                            <p className="text-muted mb-2">
                                                Refunded Amount
                                            </p>

                                            <h2 className="fw-bold text-info mb-1">

                                                {loading
                                                    ? "..."
                                                    : formatAmount(
                                                        stats.refundedAmount
                                                    )}

                                            </h2>

                                            <small className="text-muted">
                                                {getCurrencyLabel()}
                                            </small>

                                        </div>

                                        <div
                                            className="bg-info-subtle rounded p-2"
                                            style={{
                                                fontSize: "22px"
                                            }}
                                        >
                                            ↩
                                        </div>

                                    </div>

                                </div>

                            </div>

                        </div>

                    </div>

                    {/* ==================================================
                        PAYMENT STATUS
                    ================================================== */}

                    <div className="card shadow-sm mb-4">

                        <div className="card-body">

                            <div className="d-flex justify-content-between align-items-center mb-4">

                                <div>

                                    <h5 className="mb-1">
                                        Payment Status
                                    </h5>

                                    <small className="text-muted">
                                        Current distribution of payment intents
                                    </small>

                                </div>

                            </div>

                            <div className="row">

                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Created
                                    </div>

                                    <h4 className="mt-1">
                                        {loading
                                            ? "..."
                                            : stats.createdPayments}
                                    </h4>

                                </div>

                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Authorized
                                    </div>

                                    <h4 className="mt-1">
                                        {loading
                                            ? "..."
                                            : stats.authorizedPayments}
                                    </h4>

                                </div>

                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Captured
                                    </div>

                                    <h4 className="text-success mt-1">
                                        {loading
                                            ? "..."
                                            : stats.capturedPayments}
                                    </h4>

                                </div>

                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Refunded
                                    </div>

                                    <h4 className="text-info mt-1">
                                        {loading
                                            ? "..."
                                            : stats.refundedPayments}
                                    </h4>

                                </div>

                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Failed
                                    </div>

                                    <h4 className="text-danger mt-1">
                                        {loading
                                            ? "..."
                                            : stats.failedPayments}
                                    </h4>

                                </div>

                                <div className="col-md-2">

                                    <div className="text-muted">
                                        Currency
                                    </div>

                                    <h4 className="mt-1">
                                        {loading
                                            ? "..."
                                            : getCurrencyLabel()}
                                    </h4>

                                </div>

                            </div>

                        </div>

                    </div>

                    {/* ==================================================
                        QUICK ACTIONS
                    ================================================== */}

                    <div className="row">

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <div className="d-flex justify-content-between">

                                        <div>

                                            <h5 className="card-title">
                                                Payment Intents
                                            </h5>

                                            <p className="card-text text-muted">
                                                Create, authorize, capture
                                                and refund payments.
                                            </p>

                                        </div>

                                        <span
                                            className="text-muted"
                                            style={{
                                                fontSize: "24px"
                                            }}
                                        >
                                            $
                                        </span>

                                    </div>

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

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <div className="d-flex justify-content-between">

                                        <div>

                                            <h5 className="card-title">
                                                Transactions
                                            </h5>

                                            <p className="card-text text-muted">
                                                View captured and refunded
                                                transaction history.
                                            </p>

                                        </div>

                                        <span
                                            className="text-muted"
                                            style={{
                                                fontSize: "24px"
                                            }}
                                        >
                                            ⇄
                                        </span>

                                    </div>

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

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm h-100">

                                <div className="card-body">

                                    <div className="d-flex justify-content-between">

                                        <div>

                                            <h5 className="card-title">
                                                Audit Logs
                                            </h5>

                                            <p className="card-text text-muted">
                                                Monitor payment activity
                                                and system events.
                                            </p>

                                        </div>

                                        <span
                                            className="text-muted"
                                            style={{
                                                fontSize: "24px"
                                            }}
                                        >
                                            ◫
                                        </span>

                                    </div>

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

                    {/* ==================================================
                        AI ANALYTICS
                    ================================================== */}

                    <div className="card shadow-sm mb-4">

                        <div className="card-body">

                            <div className="row align-items-center">

                                <div className="col-md-8">

                                    <span className="badge bg-dark mb-2">
                                        AI ANALYTICS
                                    </span>

                                    <h4 className="fw-bold">
                                        Understand your payment activity
                                    </h4>

                                    <p className="text-muted mb-3">
                                        Ask questions about payment
                                        activity, risk insights and
                                        monthly performance using
                                        FlowPay's local AI analytics.
                                    </p>

                                    <button
                                        className="btn btn-dark"
                                        onClick={() =>
                                            navigate(
                                                "/analytics"
                                            )
                                        }
                                    >
                                        Open AI Analytics
                                    </button>

                                </div>

                                <div className="col-md-4 text-md-end mt-3 mt-md-0">

                                    <div
                                        className="border rounded p-3"
                                        style={{
                                            backgroundColor:
                                                "#f8f9fa"
                                        }}
                                    >

                                        <div className="text-muted small">
                                            Powered by
                                        </div>

                                        <div className="fw-bold">
                                            FlowPay AI
                                        </div>

                                        <div className="small text-muted">
                                            Local AI • Ollama
                                        </div>

                                    </div>

                                </div>

                            </div>

                        </div>

                    </div>

                    {/* ==================================================
                        SECURITY / AUTHENTICATION
                    ================================================== */}

                    <div className="card shadow-sm mb-4">

                        <div className="card-body">

                            <div className="d-flex justify-content-between align-items-center">

                                <div>

                                    <h5 className="mb-1">
                                        Authentication Status
                                    </h5>

                                    <p
                                        className={
                                            apiKey
                                                ? "text-success mb-0"
                                                : "text-danger mb-0"
                                        }
                                    >

                                        {apiKey
                                            ? "API key authenticated successfully"
                                            : "No API key found"}

                                    </p>

                                </div>

                                <span
                                    className={
                                        apiKey
                                            ? "badge bg-success"
                                            : "badge bg-danger"
                                    }
                                >
                                    {apiKey
                                        ? "SECURE"
                                        : "UNAUTHENTICATED"}
                                </span>

                            </div>

                        </div>

                    </div>

                    {/* ==================================================
                        FOOTER
                    ================================================== */}

                    <div className="text-center text-muted small mt-4 mb-3">

                        FlowPay Payment Gateway Simulator

                        <span className="mx-2">
                            •
                        </span>

                        Secure API-key authentication

                        <span className="mx-2">
                            •
                        </span>

                        AI-assisted analytics

                    </div>

                </div>

            </div>

        </div>
    );
}

export default Dashboard;