import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import Sidebar from "../components/Sidebar";

function AuditLogs() {

    const navigate = useNavigate();

    const [auditLogs, setAuditLogs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchAuditLogs();
    }, []);

    async function fetchAuditLogs() {

        try {

            setLoading(true);
            setError("");

            const response =
                await api.get("/api/audit-logs");

            setAuditLogs(response.data || []);

        } catch (error) {

            console.error(error);

            if (error.response?.status === 401) {

                localStorage.removeItem("flowpay_api_key");

                setError(
                    "Session expired or API key is invalid."
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

    function formatDate(date) {

        if (!date) {
            return "-";
        }

        return new Date(date).toLocaleString();
    }

    function getActionClass(action) {

        switch (action) {

            case "PAYMENT_CREATED":
                return "badge bg-primary";

            case "PAYMENT_AUTHORIZED":
                return "badge bg-warning text-dark";

            case "PAYMENT_CAPTURED":
                return "badge bg-success";

            case "PAYMENT_REFUNDED":
                return "badge bg-info text-dark";

            default:
                return "badge bg-secondary";
        }
    }

    return (

        <div className="container-fluid p-0">

            <div className="row g-0">

                <Sidebar />

                <div className="col-md-10">

                    <div className="p-4">

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

                        {error && (

                            <div className="alert alert-danger">
                                {error}
                            </div>

                        )}

                        <div className="card shadow-sm">

                            <div className="card-body">

                                <div className="d-flex justify-content-between align-items-center mb-3">

                                    <h5 className="mb-0">
                                        Activity Log
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
                                            Audit activity will appear here when
                                            payment actions are performed.
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

                                                    <tr
                                                        key={
                                                            log.id
                                                        }
                                                    >

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

                </div>

            </div>

        </div>
    );
}

export default AuditLogs;