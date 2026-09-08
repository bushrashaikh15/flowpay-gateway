import { useEffect, useState } from "react";
import api from "../services/api";

function AuditLogs() {

    const [auditLogs, setAuditLogs] = useState([]);
    const [paymentIntentId, setPaymentIntentId] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchAuditLogs();
    }, []);

    async function fetchAuditLogs() {

        try {

            setLoading(true);
            setError("");

            let response;

            if (paymentIntentId.trim()) {

                response = await api.get(
                    `/api/audit-logs/payment/${paymentIntentId}`
                );

            } else {

                response = await api.get(
                    "/api/audit-logs"
                );

            }

            setAuditLogs(response.data);

        } catch (error) {

            console.error(error);

            if (error.response?.status === 401) {

                setError(
                    "Session expired or API key is invalid."
                );

            } else if (error.response?.status === 404) {

                setError(
                    "Payment intent not found."
                );

            } else {

                setError(
                    "Unable to load audit logs."
                );
            }

        } finally {

            setLoading(false);

        }
    }

    function handleSearch(event) {

        event.preventDefault();

        fetchAuditLogs();
    }

    function handleClear() {

        setPaymentIntentId("");

        setTimeout(() => {
            fetchAuditLogs();
        }, 0);
    }

    function formatDate(date) {

        if (!date) {
            return "-";
        }

        return new Date(date).toLocaleString();
    }

    function getActionClass(action) {

        const value =
            action?.toUpperCase();

        if (value?.includes("CREATE")) {
            return "badge bg-secondary";
        }

        if (value?.includes("AUTHOR")) {
            return "badge bg-warning text-dark";
        }

        if (value?.includes("CAPTURE")) {
            return "badge bg-success";
        }

        if (value?.includes("REFUND")) {
            return "badge bg-info text-dark";
        }

        return "badge bg-primary";
    }

    return (

        <div className="container-fluid p-4">

            {/* HEADER */}

            <div className="d-flex justify-content-between align-items-center mb-4">

                <div>

                    <h2 className="fw-bold mb-1">
                        Audit Logs
                    </h2>

                    <p className="text-muted mb-0">
                        Monitor payment activity and system events
                    </p>

                </div>

                <button
                    className="btn btn-outline-dark"
                    onClick={fetchAuditLogs}
                    disabled={loading}
                >
                    ↻ Refresh
                </button>

            </div>


            {/* FILTER */}

            <div className="card shadow-sm mb-4">

                <div className="card-body">

                    <h5 className="mb-3">
                        Filter Audit Logs
                    </h5>

                    <form onSubmit={handleSearch}>

                        <div className="row g-3">

                            <div className="col-md-6">

                                <label className="form-label">
                                    Payment Intent ID
                                </label>

                                <input
                                    type="number"
                                    className="form-control"
                                    placeholder="Example: 2"
                                    min="1"
                                    value={paymentIntentId}
                                    onChange={(event) =>
                                        setPaymentIntentId(
                                            event.target.value
                                        )
                                    }
                                />

                            </div>

                            <div className="col-md-6 d-flex align-items-end gap-2">

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
                                    onClick={handleClear}
                                >
                                    Clear
                                </button>

                            </div>

                        </div>

                    </form>

                </div>

            </div>


            {/* ERROR */}

            {error && (

                <div className="alert alert-danger">
                    {error}
                </div>

            )}


            {/* LOGS */}

            <div className="card shadow-sm">

                <div className="card-body">

                    <div className="d-flex justify-content-between align-items-center mb-3">

                        <h5 className="mb-0">
                            Activity History
                        </h5>

                        <span className="text-muted">
                            Total: {auditLogs.length}
                        </span>

                    </div>


                    {loading ? (

                        <div className="text-center py-5">

                            <div
                                className="spinner-border"
                                role="status"
                            />

                            <p className="mt-2 text-muted">
                                Loading audit logs...
                            </p>

                        </div>

                    ) : auditLogs.length === 0 ? (

                        <div className="text-center py-5">

                            <h5>
                                No Audit Logs Found
                            </h5>

                            <p className="text-muted">
                                Payment activity will appear here.
                            </p>

                        </div>

                    ) : (

                        <div className="table-responsive">

                            <table className="table table-hover align-middle">

                                <thead className="table-light">

                                <tr>

                                    <th>ID</th>

                                    <th>
                                        Payment Intent
                                    </th>

                                    <th>
                                        Action
                                    </th>

                                    <th>
                                        Description
                                    </th>

                                    <th>
                                        Created At
                                    </th>

                                </tr>

                                </thead>

                                <tbody>

                                {auditLogs.map(
                                    (log) => (

                                        <tr key={log.id}>

                                            <td className="fw-semibold">
                                                #{log.id}
                                            </td>

                                            <td>
                                                #
                                                {
                                                    log.paymentIntentId
                                                }
                                            </td>

                                            <td>

                                                    <span
                                                        className={
                                                            getActionClass(
                                                                log.action
                                                            )
                                                        }
                                                    >
                                                        {
                                                            log.action
                                                        }
                                                    </span>

                                            </td>

                                            <td>
                                                {
                                                    log.description
                                                }
                                            </td>

                                            <td>
                                                {
                                                    formatDate(
                                                        log.createdAt
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
    );
}

export default AuditLogs;