function Dashboard() {

    const apiKey =
        localStorage.getItem("flowpay_api_key");

    return (
        <div className="container-fluid">

            <div className="row">

                <div className="col-md-2 bg-dark text-white min-vh-100 p-4">

                    <h3 className="mb-4">
                        FlowPay
                    </h3>

                    <div className="mb-3">
                        Dashboard
                    </div>

                    <div className="mb-3">
                        Payments
                    </div>

                    <div className="mb-3">
                        Transactions
                    </div>

                    <div className="mb-3">
                        Audit Logs
                    </div>

                </div>

                <div className="col-md-10 p-4">

                    <h2 className="mb-4">
                        Dashboard
                    </h2>

                    <div className="row">

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm">

                                <div className="card-body">

                                    <h5 className="card-title">
                                        Payment Intents
                                    </h5>

                                    <p className="card-text text-muted">
                                        Manage your payment intents.
                                    </p>

                                </div>

                            </div>

                        </div>

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm">

                                <div className="card-body">

                                    <h5 className="card-title">
                                        Transactions
                                    </h5>

                                    <p className="card-text text-muted">
                                        View your transaction history.
                                    </p>

                                </div>

                            </div>

                        </div>

                        <div className="col-md-4 mb-4">

                            <div className="card shadow-sm">

                                <div className="card-body">

                                    <h5 className="card-title">
                                        Audit Logs
                                    </h5>

                                    <p className="card-text text-muted">
                                        Monitor payment activity.
                                    </p>

                                </div>

                            </div>

                        </div>

                    </div>

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