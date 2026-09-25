import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Login() {

    const [apiKey, setApiKey] = useState("");
    const [showApiKey, setShowApiKey] = useState(false);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    async function handleLogin(event) {

        event.preventDefault();

        setError("");

        const trimmedApiKey = apiKey.trim();

        if (!trimmedApiKey) {

            setError("API key is required");

            return;
        }

        try {

            setLoading(true);

            /*
             * Store the API key temporarily.
             *
             * The Axios interceptor reads this value and
             * sends it as:
             *
             * X-API-KEY: <api-key>
             */
            localStorage.setItem(
                "flowpay_api_key",
                trimmedApiKey
            );

            /*
             * Validate the API key by making an authenticated
             * request to the backend.
             */
            await api.get(
                "/api/payment-intents",
                {
                    params: {
                        page: 0,
                        size: 1
                    }
                }
            );

            /*
             * API key is valid.
             * Keep it in localStorage and open dashboard.
             */
            navigate("/dashboard");

        } catch (error) {

            /*
             * Remove the key only when authentication fails.
             */
            if (
                error.response &&
                error.response.status === 401
            ) {

                localStorage.removeItem(
                    "flowpay_api_key"
                );

                setError(
                    "Invalid or inactive API key."
                );

            } else if (error.response) {

                /*
                 * Backend responded, but with another error.
                 */
                setError(
                    `FlowPay backend returned HTTP ${error.response.status}.`
                );

            } else {

                /*
                 * No response usually means the backend
                 * cannot be reached.
                 */
                setError(
                    "Unable to connect to FlowPay backend. Make sure the backend is running on port 8080."
                );

                /*
                 * Remove the key so an old/stale key does
                 * not remain in localStorage.
                 */
                localStorage.removeItem(
                    "flowpay_api_key"
                );
            }

        } finally {

            setLoading(false);
        }
    }

    return (

        <div className="login-page">

            <div className="login-card">

                {/* Logo / Brand */}

                <div className="text-center mb-4">

                    <div
                        style={{
                            width: "52px",
                            height: "52px",
                            margin: "0 auto 14px",
                            borderRadius: "12px",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            background:
                                "linear-gradient(135deg, #6366f1, #4f46e5)",
                            color: "#ffffff",
                            fontSize: "24px",
                            fontWeight: "700",
                            boxShadow:
                                "0 8px 20px rgba(79, 70, 229, 0.25)"
                        }}
                    >
                        F
                    </div>

                    <h1 className="fw-bold mb-1">
                        FlowPay
                    </h1>

                    <p className="text-muted mb-0">
                        Payment Gateway Dashboard
                    </p>

                </div>


                {/* Login Form */}

                <form onSubmit={handleLogin}>

                    <div className="mb-3">

                        <label
                            htmlFor="apiKey"
                            className="form-label"
                        >
                            API Key
                        </label>


                        <div
                            style={{
                                position: "relative"
                            }}
                        >

                            <input
                                id="apiKey"
                                type={
                                    showApiKey
                                        ? "text"
                                        : "password"
                                }
                                className="form-control"
                                placeholder="Enter your FlowPay API key"
                                value={apiKey}
                                onChange={(event) => {
                                    setApiKey(
                                        event.target.value
                                    );

                                    if (error) {
                                        setError("");
                                    }
                                }}
                                disabled={loading}
                                autoComplete="off"
                                style={{
                                    paddingRight: "75px"
                                }}
                            />


                            <button
                                type="button"
                                onClick={() =>
                                    setShowApiKey(
                                        !showApiKey
                                    )
                                }
                                disabled={loading}
                                style={{
                                    position: "absolute",
                                    right: "8px",
                                    top: "50%",
                                    transform:
                                        "translateY(-50%)",
                                    border: "none",
                                    background:
                                        "transparent",
                                    color: "#6b7280",
                                    fontSize: "12px",
                                    fontWeight: "600",
                                    cursor: "pointer"
                                }}
                            >
                                {showApiKey
                                    ? "Hide"
                                    : "Show"}
                            </button>

                        </div>

                    </div>


                    {/* Error */}

                    {error && (

                        <div
                            className="alert alert-danger"
                            role="alert"
                        >
                            {error}
                        </div>

                    )}


                    {/* Login Button */}

                    <button
                        type="submit"
                        className="btn btn-dark w-100"
                        disabled={
                            loading ||
                            !apiKey.trim()
                        }
                    >
                        {loading
                            ? "Validating API Key..."
                            : "Login"}
                    </button>

                </form>


                {/* Information */}

                <div
                    className="text-center text-muted mt-4"
                    style={{
                        fontSize: "12px"
                    }}
                >
                    Your API key is used to securely
                    authenticate with FlowPay.
                </div>

            </div>

        </div>
    );
}

export default Login;