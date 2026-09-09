import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import Sidebar from "../components/Sidebar";

function Transactions() {

    const navigate = useNavigate();

    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchTransactions();
    }, []);

    async function fetchTransactions() {

        try {

            setLoading(true);
            setError("");

            const response =
                await api.get("/api/transactions");

            setTransactions(response.data);

        } catch (error) {

            console.error(error);

            if (error.response?.status === 401) {

                localStorage.removeItem("flowpay_api_key");

                setError(
                    "Session expired or API key is invalid."
                );

            } else {

                setError(
                    "Unable to load transactions."
                );
            }

        } finally {

            setLoading(false);
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

            case "CAPTURED":
                return "badge bg-success";

            case "REFUNDED":
                return "badge bg-info text-dark";

            case "FAILED":
                return "badge bg-danger";

            case "PENDING":
                return "badge bg-warning text-dark";

            default:
                return "badge bg-secondary";
        }
    }

    function getTypeClass(type) {

        switch (type) {

            case "PAYMENT":
                return "badge bg-primary";

            case "REFUND":
                return "badge bg-info text-dark";

            default:
                return "badge bg-secondary";
        }
    }

    return (

        <div className="container-fluid p-0">

            <div className="row g-0">

                {/* SIDEBAR */}

                <Sidebar />

                {/* MAIN CONTENT */}

                <div className="col-md-10">

                    <div className="p-4">

                        {/* PAGE HEADER */}

                        <div className="d-flex justify-content-between align-items-center mb-4">

                            <div>

                                <h2 className="fw-bold mb-1">
                                    Transactions
                                </h2>

                                <p className="text-muted mb-0">
                                    View your payment transaction history
                                </p>

                            </div>

                            <button
                                className="btn btn-outline-dark"
                                onClick={fetchTransactions}
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


                        {/* TRANSACTION CARD */}

                        <div className="card shadow-sm">

                            <div className="card-body">

                                <div className="d-flex justify-content-between align-items-center mb-3">

                                    <h5 className="mb-0">
                                        Transaction History
                                    </h5>

                                    <span className="text-muted">
                                        Total: {transactions.length}
                                    </span>

                                </div>


                                {/* LOADING */}

                                {loading ? (

                                    <div className="text-center py-5">

                                        <div
                                            className="spinner-border"
                                            role="status"
                                        />

                                        <p className="mt-2 text-muted">
                                            Loading transactions...
                                        </p>

                                    </div>

                                ) : transactions.length === 0 ? (

                                    /* EMPTY STATE */

                                    <div className="text-center py-5">

                                        <h5>
                                            No Transactions Found
                                        </h5>

                                        <p className="text-muted">
                                            Transactions will appear here after
                                            payments are captured or refunded.
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

                                ) : (

                                    /* TABLE */

                                    <div className="table-responsive">

                                        <table className="table table-hover align-middle">

                                            <thead className="table-light">

                                            <tr>

                                                <th>
                                                    ID
                                                </th>

                                                <th>
                                                    Payment Intent
                                                </th>

                                                <th>
                                                    Amount
                                                </th>

                                                <th>
                                                    Currency
                                                </th>

                                                <th>
                                                    Type
                                                </th>

                                                <th>
                                                    Status
                                                </th>

                                                <th>
                                                    Created At
                                                </th>

                                            </tr>

                                            </thead>

                                            <tbody>

                                            {transactions.map(
                                                (transaction) => (

                                                    <tr
                                                        key={
                                                            transaction.id
                                                        }
                                                    >

                                                        <td className="fw-semibold">

                                                            #
                                                            {
                                                                transaction.id
                                                            }

                                                        </td>

                                                        <td>

                                                            #
                                                            {
                                                                transaction.paymentIntentId
                                                            }

                                                        </td>

                                                        <td className="fw-semibold">

                                                            {
                                                                transaction.amount
                                                            }

                                                        </td>

                                                        <td>

                                                            {
                                                                transaction.currency
                                                            }

                                                        </td>

                                                        <td>

                                                                <span
                                                                    className={
                                                                        getTypeClass(
                                                                            transaction.type
                                                                        )
                                                                    }
                                                                >

                                                                    {
                                                                        transaction.type
                                                                    }

                                                                </span>

                                                        </td>

                                                        <td>

                                                                <span
                                                                    className={
                                                                        getStatusClass(
                                                                            transaction.status
                                                                        )
                                                                    }
                                                                >

                                                                    {
                                                                        transaction.status
                                                                    }

                                                                </span>

                                                        </td>

                                                        <td>

                                                            {
                                                                formatDate(
                                                                    transaction.createdAt
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

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </div>
    );
}

export default Transactions;