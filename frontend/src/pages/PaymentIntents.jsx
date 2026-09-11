import { useEffect, useState } from "react";
import api from "../services/api";
import Sidebar from "../components/Sidebar";

function PaymentIntents() {

    // =============================================================
    // PAYMENT INTENTS
    // =============================================================

    const [paymentIntents, setPaymentIntents] = useState([]);

    const [status, setStatus] = useState("");
    const [currency, setCurrency] = useState("");
    const [minAmount, setMinAmount] = useState("");
    const [maxAmount, setMaxAmount] = useState("");

    const [page, setPage] = useState(0);
    const [pageData, setPageData] = useState(null);

    const [loading, setLoading] = useState(false);
    const [actionLoading, setActionLoading] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");


    // =============================================================
    // CREATE PAYMENT
    // =============================================================

    const [showCreateForm, setShowCreateForm] = useState(false);

    const [amount, setAmount] = useState("");
    const [createCurrency, setCreateCurrency] = useState("INR");
    const [merchantId, setMerchantId] = useState("2");
    const [creating, setCreating] = useState(false);


    // =============================================================
    // FRAUD INSIGHT
    // =============================================================

    const [showFraudModal, setShowFraudModal] = useState(false);

    const [fraudInsight, setFraudInsight] = useState(null);

    const [fraudLoading, setFraudLoading] = useState(false);

    const [fraudError, setFraudError] = useState("");


    // =============================================================
    // CONSTANTS
    // =============================================================

    const pageSize = 10;


    // =============================================================
    // FETCH PAYMENT INTENTS
    // =============================================================

    useEffect(() => {
        fetchPaymentIntents();
    }, [page]);


    async function fetchPaymentIntents() {

        try {

            setLoading(true);
            setError("");

            const params = {
                page: page,
                size: pageSize
            };

            if (status) {
                params.status = status;
            }

            if (currency.trim()) {

                params.currency =
                    currency
                        .trim()
                        .toUpperCase();

            }

            if (minAmount !== "") {
                params.minAmount = minAmount;
            }

            if (maxAmount !== "") {
                params.maxAmount = maxAmount;
            }


            const response =
                await api.get(
                    "/api/payment-intents",
                    { params }
                );


            setPaymentIntents(
                response.data.content || []
            );

            setPageData(response.data);


        } catch (error) {

            console.error(error);

            if (error.response?.status === 401) {

                setError(
                    "Session expired or API key is invalid."
                );

            } else {

                setError(
                    "Unable to load payment intents."
                );

            }

        } finally {

            setLoading(false);

        }
    }


    // =============================================================
    // SEARCH
    // =============================================================

    function handleSearch(event) {

        event.preventDefault();

        setSuccess("");
        setError("");

        if (page === 0) {

            fetchPaymentIntents();

        } else {

            setPage(0);

        }
    }


    // =============================================================
    // CLEAR FILTERS
    // =============================================================

    function handleClearFilters() {

        setStatus("");
        setCurrency("");
        setMinAmount("");
        setMaxAmount("");

        setSuccess("");
        setError("");

        if (page === 0) {

            fetchPaymentIntents();

        } else {

            setPage(0);

        }
    }


    // =============================================================
    // OPEN CREATE FORM
    // =============================================================

    function openCreateForm() {

        setAmount("");
        setCreateCurrency("INR");
        setMerchantId("2");

        setError("");
        setSuccess("");

        setShowCreateForm(true);
    }


    // =============================================================
    // CLOSE CREATE FORM
    // =============================================================

    function closeCreateForm() {

        if (!creating) {

            setShowCreateForm(false);

        }
    }


    // =============================================================
    // CREATE PAYMENT
    // =============================================================

    async function handleCreatePayment(event) {

        event.preventDefault();

        setError("");
        setSuccess("");


        if (!amount || Number(amount) <= 0) {

            setError(
                "Amount must be greater than 0."
            );

            return;
        }


        if (!createCurrency.trim()) {

            setError(
                "Currency is required."
            );

            return;
        }


        if (!merchantId || Number(merchantId) <= 0) {

            setError(
                "Merchant ID is required."
            );

            return;
        }


        try {

            setCreating(true);


            const idempotencyKey =
                crypto.randomUUID();


            const requestData = {

                amount: Number(amount),

                currency:
                    createCurrency
                        .trim()
                        .toUpperCase(),

                merchantId:
                    Number(merchantId)

            };


            const response =
                await api.post(
                    "/api/payment-intents",
                    requestData,
                    {
                        headers: {
                            "Idempotency-Key":
                            idempotencyKey
                        }
                    }
                );


            const createdPayment =
                response.data;


            setShowCreateForm(false);

            setAmount("");


            setSuccess(
                `Payment Intent #${createdPayment.id} successfully created.`
            );


            setPage(0);

            await fetchPaymentIntents();


        } catch (error) {

            console.error(error);


            if (error.response?.status === 401) {

                setError(
                    "Invalid or inactive API key."
                );

            } else if (error.response?.status === 400) {

                setError(
                    error.response?.data?.message ||
                    "Invalid payment details."
                );

            } else {

                setError(
                    "Unable to create payment intent."
                );

            }

        } finally {

            setCreating(false);

        }
    }


    // =============================================================
    // PAYMENT ACTION
    // =============================================================

    async function handlePaymentAction(
        paymentId,
        action
    ) {

        try {

            setActionLoading(true);

            setError("");
            setSuccess("");


            await api.put(
                `/api/payment-intents/${paymentId}/${action}`
            );


            let message = "";


            if (action === "authorize") {

                message =
                    `Payment Intent #${paymentId} successfully authorized.`;

            } else if (action === "capture") {

                message =
                    `Payment Intent #${paymentId} successfully captured.`;

            } else if (action === "refund") {

                message =
                    `Payment Intent #${paymentId} successfully refunded.`;

            }


            setSuccess(message);


            await fetchPaymentIntents();


        } catch (error) {

            console.error(error);


            if (error.response?.status === 401) {

                setError(
                    "Session expired or API key is invalid."
                );

            } else if (error.response?.status === 400) {

                setError(
                    error.response?.data?.message ||
                    "Invalid payment state."
                );

            } else {

                setError(
                    "Unable to update payment intent."
                );

            }

        } finally {

            setActionLoading(false);

        }
    }


    // =============================================================
    // FRAUD INSIGHT
    // =============================================================

    async function handleFraudInsight(paymentId) {

        try {

            setFraudLoading(true);

            setFraudError("");

            setFraudInsight(null);

            setShowFraudModal(true);


            /*
             * IMPORTANT:
             *
             * Backend controller:
             *
             * @RequestMapping("/api/fraud-insights")
             *
             * @GetMapping("/payment/{paymentIntentId}")
             *
             * Therefore the complete endpoint is:
             *
             * /api/fraud-insights/payment/{paymentId}
             */

            const response =
                await api.get(
                    `/api/fraud-insights/payment/${paymentId}`
                );


            setFraudInsight(
                response.data
            );


        } catch (error) {

            console.error(
                "Fraud insight error:",
                error
            );


            if (error.response?.status === 401) {

                setFraudError(
                    "Session expired or API key is invalid."
                );

            } else if (error.response?.status === 403) {

                setFraudError(
                    "You are not authorized to analyze this payment."
                );

            } else if (error.response?.status === 404) {

                setFraudError(
                    "Payment intent not found."
                );

            } else if (error.response?.status === 500) {

                setFraudError(
                    "The backend could not generate the fraud insight. Check the backend terminal for the exact error."
                );

            } else {

                setFraudError(
                    "Unable to generate fraud insight."
                );

            }

        } finally {

            setFraudLoading(false);

        }
    }


    // =============================================================
    // CLOSE FRAUD MODAL
    // =============================================================

    function closeFraudModal() {

        if (!fraudLoading) {

            setShowFraudModal(false);

            setFraudInsight(null);

            setFraudError("");

        }
    }


    // =============================================================
    // FORMAT DATE
    // =============================================================

    function formatDate(date) {

        if (!date) {

            return "-";

        }

        return new Date(date)
            .toLocaleString();

    }


    // =============================================================
    // STATUS CLASS
    // =============================================================

    function getStatusClass(status) {

        switch (status) {

            case "CREATED":
                return "badge bg-secondary";

            case "AUTHORIZED":
                return "badge bg-warning text-dark";

            case "CAPTURED":
                return "badge bg-success";

            case "REFUNDED":
                return "badge bg-info text-dark";

            case "FAILED":
                return "badge bg-danger";

            default:
                return "badge bg-secondary";

        }

    }


    // =============================================================
    // FRAUD RISK CLASS
    // =============================================================

    function getRiskClass(riskLevel) {

        switch (riskLevel) {

            case "HIGH":
                return "badge bg-danger";

            case "MEDIUM":
                return "badge bg-warning text-dark";

            case "LOW":
                return "badge bg-success";

            default:
                return "badge bg-secondary";

        }

    }


    // =============================================================
    // PAYMENT ACTION BUTTON
    // =============================================================

    function renderActionButton(payment) {

        if (payment.status === "CREATED") {

            return (

                <button
                    className="btn btn-sm btn-warning"
                    disabled={actionLoading}
                    onClick={() =>
                        handlePaymentAction(
                            payment.id,
                            "authorize"
                        )
                    }
                >
                    Authorize
                </button>

            );

        }


        if (payment.status === "AUTHORIZED") {

            return (

                <button
                    className="btn btn-sm btn-success"
                    disabled={actionLoading}
                    onClick={() =>
                        handlePaymentAction(
                            payment.id,
                            "capture"
                        )
                    }
                >
                    Capture
                </button>

            );

        }


        if (payment.status === "CAPTURED") {

            return (

                <button
                    className="btn btn-sm btn-info"
                    disabled={actionLoading}
                    onClick={() =>
                        handlePaymentAction(
                            payment.id,
                            "refund"
                        )
                    }
                >
                    Refund
                </button>

            );

        }


        return (

            <span className="text-muted">
                Completed
            </span>

        );

    }


    // =============================================================
    // RENDER
    // =============================================================

    return (

        <div className="container-fluid">

            <div className="row">


                {/* =================================================
                    SIDEBAR
                ================================================= */}

                <Sidebar />


                {/* =================================================
                    MAIN CONTENT
                ================================================= */}

                <div className="col-md-10 p-4">


                    {/* =================================================
                        HEADER
                    ================================================= */}

                    <div className="d-flex justify-content-between align-items-center mb-4">

                        <div>

                            <h2 className="fw-bold mb-1">
                                Payment Intents
                            </h2>

                            <p className="text-muted mb-0">
                                Create, authorize, capture and refund payments
                            </p>

                        </div>


                        <button
                            className="btn btn-dark"
                            onClick={openCreateForm}
                        >
                            + Create Payment
                        </button>

                    </div>


                    {/* =================================================
                        SUCCESS MESSAGE
                    ================================================= */}

                    {success && (

                        <div className="alert alert-success">
                            {success}
                        </div>

                    )}


                    {/* =================================================
                        ERROR MESSAGE
                    ================================================= */}

                    {error && (

                        <div className="alert alert-danger">
                            {error}
                        </div>

                    )}


                    {/* =================================================
                        CREATE PAYMENT FORM
                    ================================================= */}

                    {showCreateForm && (

                        <div className="card shadow-sm mb-4">

                            <div className="card-body">


                                <div className="d-flex justify-content-between align-items-center mb-3">

                                    <h5 className="mb-0">
                                        Create Payment Intent
                                    </h5>


                                    <button
                                        type="button"
                                        className="btn-close"
                                        onClick={closeCreateForm}
                                        disabled={creating}
                                    />

                                </div>


                                <form
                                    onSubmit={
                                        handleCreatePayment
                                    }
                                >

                                    <div className="row g-3">


                                        {/* AMOUNT */}

                                        <div className="col-md-4">

                                            <label className="form-label">
                                                Amount
                                            </label>

                                            <input
                                                type="number"
                                                className="form-control"
                                                placeholder="1000"
                                                min="0.01"
                                                step="0.01"
                                                value={amount}
                                                onChange={(event) =>
                                                    setAmount(
                                                        event.target.value
                                                    )
                                                }
                                                disabled={creating}
                                                required
                                            />

                                        </div>


                                        {/* CURRENCY */}

                                        <div className="col-md-3">

                                            <label className="form-label">
                                                Currency
                                            </label>

                                            <select
                                                className="form-select"
                                                value={
                                                    createCurrency
                                                }
                                                onChange={(event) =>
                                                    setCreateCurrency(
                                                        event.target.value
                                                    )
                                                }
                                                disabled={creating}
                                            >

                                                <option value="INR">
                                                    INR
                                                </option>

                                                <option value="USD">
                                                    USD
                                                </option>

                                                <option value="EUR">
                                                    EUR
                                                </option>

                                            </select>

                                        </div>


                                        {/* MERCHANT ID */}

                                        <div className="col-md-3">

                                            <label className="form-label">
                                                Merchant ID
                                            </label>

                                            <input
                                                type="number"
                                                className="form-control"
                                                value={
                                                    merchantId
                                                }
                                                onChange={(event) =>
                                                    setMerchantId(
                                                        event.target.value
                                                    )
                                                }
                                                disabled={creating}
                                                required
                                            />

                                            <small className="text-muted">
                                                Must belong to your API key
                                            </small>

                                        </div>


                                        {/* BUTTONS */}

                                        <div className="col-md-2 d-flex align-items-end gap-2">

                                            <button
                                                type="submit"
                                                className="btn btn-dark"
                                                disabled={creating}
                                            >
                                                {creating
                                                    ? "Creating..."
                                                    : "Create"}
                                            </button>


                                            <button
                                                type="button"
                                                className="btn btn-outline-secondary"
                                                onClick={
                                                    closeCreateForm
                                                }
                                                disabled={creating}
                                            >
                                                Cancel
                                            </button>

                                        </div>

                                    </div>

                                </form>

                            </div>

                        </div>

                    )}


                    {/* =================================================
                        FILTER CARD
                    ================================================= */}

                    <div className="card shadow-sm mb-4">

                        <div className="card-body">


                            <h5 className="card-title mb-3">
                                Filters
                            </h5>


                            <form onSubmit={handleSearch}>

                                <div className="row g-3">


                                    {/* STATUS */}

                                    <div className="col-md-3">

                                        <label className="form-label">
                                            Status
                                        </label>

                                        <select
                                            className="form-select"
                                            value={status}
                                            onChange={(event) =>
                                                setStatus(
                                                    event.target.value
                                                )
                                            }
                                        >

                                            <option value="">
                                                All Statuses
                                            </option>

                                            <option value="CREATED">
                                                Created
                                            </option>

                                            <option value="AUTHORIZED">
                                                Authorized
                                            </option>

                                            <option value="CAPTURED">
                                                Captured
                                            </option>

                                            <option value="REFUNDED">
                                                Refunded
                                            </option>

                                            <option value="FAILED">
                                                Failed
                                            </option>

                                        </select>

                                    </div>


                                    {/* CURRENCY */}

                                    <div className="col-md-2">

                                        <label className="form-label">
                                            Currency
                                        </label>

                                        <input
                                            type="text"
                                            className="form-control"
                                            placeholder="INR"
                                            value={currency}
                                            onChange={(event) =>
                                                setCurrency(
                                                    event.target.value
                                                )
                                            }
                                        />

                                    </div>


                                    {/* MIN AMOUNT */}

                                    <div className="col-md-2">

                                        <label className="form-label">
                                            Min Amount
                                        </label>

                                        <input
                                            type="number"
                                            className="form-control"
                                            placeholder="0"
                                            min="0"
                                            value={minAmount}
                                            onChange={(event) =>
                                                setMinAmount(
                                                    event.target.value
                                                )
                                            }
                                        />

                                    </div>


                                    {/* MAX AMOUNT */}

                                    <div className="col-md-2">

                                        <label className="form-label">
                                            Max Amount
                                        </label>

                                        <input
                                            type="number"
                                            className="form-control"
                                            placeholder="10000"
                                            min="0"
                                            value={maxAmount}
                                            onChange={(event) =>
                                                setMaxAmount(
                                                    event.target.value
                                                )
                                            }
                                        />

                                    </div>


                                    {/* BUTTONS */}

                                    <div className="col-md-3 d-flex align-items-end gap-2">

                                        <button
                                            type="submit"
                                            className="btn btn-dark"
                                            disabled={loading}
                                        >
                                            Search
                                        </button>


                                        <button
                                            type="button"
                                            className="btn btn-outline-secondary"
                                            onClick={
                                                handleClearFilters
                                            }
                                        >
                                            Clear
                                        </button>

                                    </div>

                                </div>

                            </form>

                        </div>

                    </div>


                    {/* =================================================
                        PAYMENT INTENT LIST
                    ================================================= */}

                    <div className="card shadow-sm">

                        <div className="card-body">


                            <div className="d-flex justify-content-between align-items-center mb-3">

                                <h5 className="mb-0">
                                    Payment Intent List
                                </h5>


                                {pageData && (

                                    <span className="text-muted">
                                        Total:{" "}
                                        {pageData.totalElements}
                                    </span>

                                )}

                            </div>


                            {/* LOADING */}

                            {loading ? (

                                <div className="text-center py-5">

                                    <div
                                        className="spinner-border"
                                        role="status"
                                    />

                                    <p className="mt-2 text-muted">
                                        Loading payment intents...
                                    </p>

                                </div>


                            ) : paymentIntents.length === 0 ? (

                                <div className="text-center py-5">

                                    <h5>
                                        No Payment Intents Found
                                    </h5>

                                    <p className="text-muted">
                                        Create your first payment
                                        intent above.
                                    </p>


                                    <button
                                        className="btn btn-dark"
                                        onClick={
                                            openCreateForm
                                        }
                                    >
                                        + Create Payment
                                    </button>

                                </div>


                            ) : (

                                <div className="table-responsive">

                                    <table className="table table-hover align-middle">


                                        <thead className="table-light">

                                        <tr>

                                            <th>ID</th>

                                            <th>Amount</th>

                                            <th>Currency</th>

                                            <th>Status</th>

                                            <th>Merchant</th>

                                            <th>Created At</th>

                                            <th>Action</th>

                                            <th>Fraud</th>

                                        </tr>

                                        </thead>


                                        <tbody>

                                        {paymentIntents.map(
                                            (payment) => (

                                                <tr
                                                    key={
                                                        payment.id
                                                    }
                                                >


                                                    {/* ID */}

                                                    <td>
                                                        #
                                                        {
                                                            payment.id
                                                        }
                                                    </td>


                                                    {/* AMOUNT */}

                                                    <td className="fw-semibold">

                                                        {
                                                            payment.amount
                                                        }

                                                    </td>


                                                    {/* CURRENCY */}

                                                    <td>

                                                        {
                                                            payment.currency
                                                        }

                                                    </td>


                                                    {/* STATUS */}

                                                    <td>

                                                        <span
                                                            className={
                                                                getStatusClass(
                                                                    payment.status
                                                                )
                                                            }
                                                        >
                                                            {
                                                                payment.status
                                                            }
                                                        </span>

                                                    </td>


                                                    {/* MERCHANT */}

                                                    <td>

                                                        {
                                                            payment.merchantName
                                                        }

                                                    </td>


                                                    {/* CREATED */}

                                                    <td>

                                                        {
                                                            formatDate(
                                                                payment.createdAt
                                                            )
                                                        }

                                                    </td>


                                                    {/* ACTION */}

                                                    <td>

                                                        {
                                                            renderActionButton(
                                                                payment
                                                            )
                                                        }

                                                    </td>


                                                    {/* FRAUD */}

                                                    <td>

                                                        <button
                                                            className="btn btn-sm btn-outline-danger"
                                                            onClick={() =>
                                                                handleFraudInsight(
                                                                    payment.id
                                                                )
                                                            }
                                                            disabled={
                                                                fraudLoading
                                                            }
                                                        >
                                                            🔍 Fraud Insight
                                                        </button>

                                                    </td>

                                                </tr>

                                            )
                                        )}

                                        </tbody>

                                    </table>

                                </div>

                            )}


                            {/* =================================================
                                PAGINATION
                            ================================================= */}

                            {pageData &&
                                pageData.totalPages > 1 && (

                                    <div className="d-flex justify-content-between align-items-center mt-3">


                                        <button
                                            className="btn btn-outline-dark"
                                            disabled={
                                                page === 0 ||
                                                loading
                                            }
                                            onClick={() =>
                                                setPage(
                                                    page - 1
                                                )
                                            }
                                        >
                                            ← Previous
                                        </button>


                                        <span className="text-muted">

                                            Page{" "}
                                            {page + 1}{" "}

                                            of{" "}

                                            {
                                                pageData.totalPages
                                            }

                                        </span>


                                        <button
                                            className="btn btn-outline-dark"
                                            disabled={
                                                page >=
                                                pageData.totalPages - 1 ||
                                                loading
                                            }
                                            onClick={() =>
                                                setPage(
                                                    page + 1
                                                )
                                            }
                                        >
                                            Next →
                                        </button>

                                    </div>

                                )}

                        </div>

                    </div>

                </div>

            </div>


            {/* =============================================================
                FRAUD INSIGHT MODAL
            ============================================================= */}

            {showFraudModal && (

                <div
                    className="modal d-block"
                    tabIndex="-1"
                    style={{
                        backgroundColor:
                            "rgba(0, 0, 0, 0.55)"
                    }}
                >

                    <div className="modal-dialog modal-lg modal-dialog-centered">

                        <div className="modal-content">


                            {/* =================================================
                                MODAL HEADER
                            ================================================= */}

                            <div className="modal-header">

                                <h5 className="modal-title fw-bold">

                                    🤖 AI Fraud Insight

                                </h5>


                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={
                                        closeFraudModal
                                    }
                                    disabled={
                                        fraudLoading
                                    }
                                />

                            </div>


                            {/* =================================================
                                MODAL BODY
                            ================================================= */}

                            <div className="modal-body">


                                {/* LOADING */}

                                {fraudLoading && (

                                    <div className="text-center py-5">

                                        <div
                                            className="spinner-border text-danger"
                                            role="status"
                                        />

                                        <p className="mt-3 mb-0">

                                            Analyzing payment risk...

                                        </p>

                                        <small className="text-muted">

                                            FlowPay rules + AI explanation

                                        </small>

                                    </div>

                                )}


                                {/* ERROR */}

                                {!fraudLoading &&
                                    fraudError && (

                                        <div className="alert alert-danger">

                                            {fraudError}

                                        </div>

                                    )}


                                {/* RESULT */}

                                {!fraudLoading &&
                                    !fraudError &&
                                    fraudInsight && (

                                        <div>


                                            {/* =================================================
                                                SUMMARY CARDS
                                            ================================================= */}

                                            <div className="row g-3 mb-4">


                                                {/* RISK SCORE */}

                                                <div className="col-md-4">

                                                    <div className="card h-100">

                                                        <div className="card-body text-center">

                                                            <h6 className="text-muted">
                                                                Risk Score
                                                            </h6>


                                                            <div className="display-5 fw-bold">

                                                                {
                                                                    fraudInsight.riskScore
                                                                }

                                                                <small className="fs-5">
                                                                    /100
                                                                </small>

                                                            </div>

                                                        </div>

                                                    </div>

                                                </div>


                                                {/* RISK LEVEL */}

                                                <div className="col-md-4">

                                                    <div className="card h-100">

                                                        <div className="card-body text-center">

                                                            <h6 className="text-muted">
                                                                Risk Level
                                                            </h6>


                                                            <span
                                                                className={
                                                                    getRiskClass(
                                                                        fraudInsight.riskLevel
                                                                    ) +
                                                                    " fs-6"
                                                                }
                                                            >
                                                                {
                                                                    fraudInsight.riskLevel
                                                                }
                                                            </span>

                                                        </div>

                                                    </div>

                                                </div>


                                                {/* PAYMENT ID */}

                                                <div className="col-md-4">

                                                    <div className="card h-100">

                                                        <div className="card-body text-center">

                                                            <h6 className="text-muted">
                                                                Payment Intent
                                                            </h6>


                                                            <div className="fw-bold fs-4">

                                                                #
                                                                {
                                                                    fraudInsight.paymentIntentId
                                                                }

                                                            </div>

                                                        </div>

                                                    </div>

                                                </div>

                                            </div>


                                            {/* =================================================
                                                RISK SIGNALS
                                            ================================================= */}

                                            <div className="card mb-3">

                                                <div className="card-body">

                                                    <h6 className="fw-bold">

                                                        Risk Signals

                                                    </h6>


                                                    {fraudInsight.riskSignals &&
                                                    fraudInsight.riskSignals.length > 0 ? (

                                                        <ul className="mb-0">

                                                            {
                                                                fraudInsight.riskSignals.map(
                                                                    (
                                                                        signal,
                                                                        index
                                                                    ) => (

                                                                        <li
                                                                            key={
                                                                                index
                                                                            }
                                                                        >
                                                                            {
                                                                                signal
                                                                            }
                                                                        </li>

                                                                    )
                                                                )
                                                            }

                                                        </ul>

                                                    ) : (

                                                        <p className="text-success mb-0">

                                                            ✓ No significant
                                                            risk signals
                                                            detected.

                                                        </p>

                                                    )}

                                                </div>

                                            </div>


                                            {/* =================================================
                                                AI EXPLANATION
                                            ================================================= */}

                                            <div className="card mb-3">

                                                <div className="card-body">

                                                    <h6 className="fw-bold">

                                                        AI Explanation

                                                    </h6>


                                                    <p className="mb-0">

                                                        {
                                                            fraudInsight.aiExplanation
                                                        }

                                                    </p>

                                                </div>

                                            </div>


                                            {/* =================================================
                                                DISCLAIMER
                                            ================================================= */}

                                            <div className="alert alert-warning mb-0">

                                                <strong>
                                                    Disclaimer:
                                                </strong>{" "}

                                                {
                                                    fraudInsight.disclaimer
                                                }

                                            </div>

                                        </div>

                                    )}

                            </div>


                            {/* =================================================
                                MODAL FOOTER
                            ================================================= */}

                            <div className="modal-footer">

                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={
                                        closeFraudModal
                                    }
                                    disabled={
                                        fraudLoading
                                    }
                                >
                                    Close
                                </button>

                            </div>

                        </div>

                    </div>

                </div>

            )}

        </div>

    );
}

export default PaymentIntents;