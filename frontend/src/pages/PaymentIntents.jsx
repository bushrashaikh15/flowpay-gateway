import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import Sidebar from "../components/Sidebar";

function PaymentIntents() {

    const navigate = useNavigate();

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

    // Create payment form
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [amount, setAmount] = useState("");
    const [createCurrency, setCreateCurrency] = useState("INR");
    const [merchantId, setMerchantId] = useState("2");
    const [creating, setCreating] = useState(false);

    const pageSize = 10;

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
                    currency.trim().toUpperCase();
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


    function handleSearch(event) {

        event.preventDefault();

        setSuccess("");
        setError("");

        // If already on page 0, fetch manually.
        if (page === 0) {
            fetchPaymentIntents();
        } else {
            setPage(0);
        }
    }


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


    function openCreateForm() {

        setAmount("");
        setCreateCurrency("INR");
        setMerchantId("2");

        setError("");
        setSuccess("");

        setShowCreateForm(true);
    }


    function closeCreateForm() {

        if (!creating) {
            setShowCreateForm(false);
        }
    }


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


    function formatDate(date) {

        if (!date) {
            return "-";
        }

        return new Date(date).toLocaleString();
    }


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


                    {/* SUCCESS MESSAGE */}

                    {success && (

                        <div className="alert alert-success">
                            {success}
                        </div>

                    )}


                    {/* ERROR MESSAGE */}

                    {error && (

                        <div className="alert alert-danger">
                            {error}
                        </div>

                    )}


                    {/* CREATE PAYMENT FORM */}

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
                                                value={merchantId}
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


                    {/* FILTER CARD */}

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


                    {/* PAYMENT INTENT LIST */}

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

                                                    <td>
                                                        #
                                                        {
                                                            payment.id
                                                        }
                                                    </td>

                                                    <td className="fw-semibold">
                                                        {
                                                            payment.amount
                                                        }
                                                    </td>

                                                    <td>
                                                        {
                                                            payment.currency
                                                        }
                                                    </td>

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

                                                    <td>
                                                        {
                                                            payment.merchantName
                                                        }
                                                    </td>

                                                    <td>
                                                        {
                                                            formatDate(
                                                                payment.createdAt
                                                            )
                                                        }
                                                    </td>

                                                    <td>
                                                        {
                                                            renderActionButton(
                                                                payment
                                                            )
                                                        }
                                                    </td>

                                                </tr>

                                            )
                                        )}

                                        </tbody>

                                    </table>

                                </div>

                            )}


                            {/* PAGINATION */}

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
                                                pageData.totalPages -
                                                1 ||
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

        </div>
    );
}

export default PaymentIntents;