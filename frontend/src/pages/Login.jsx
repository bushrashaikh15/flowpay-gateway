import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Login() {

    const [apiKey, setApiKey] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    async function handleLogin(event) {

        event.preventDefault();

        setError("");

        if (!apiKey.trim()) {
            setError("API key is required");
            return;
        }

        try {

            setLoading(true);

            // Temporarily store the key so Axios interceptor
            // can send it to the backend.
            localStorage.setItem(
                "flowpay_api_key",
                apiKey.trim()
            );

            // This request validates the API key.
            // Invalid key -> backend returns 401.
            await api.get("/api/payment-intents");

            // Only reach dashboard if request succeeds.
            navigate("/dashboard");

        } catch (error) {

            // Remove invalid key.
            localStorage.removeItem(
                "flowpay_api_key"
            );

            if (error.response) {

                if (error.response.status === 401) {

                    setError(
                        "Invalid or inactive API key"
                    );

                } else {

                    setError(
                        "Unable to connect to FlowPay"
                    );
                }

            } else {

                setError(
                    "Unable to connect to FlowPay backend"
                );
            }

        } finally {

            setLoading(false);
        }
    }

    return (
        <div className="login-page">

            <div className="login-card">

                <div className="text-center mb-4">

                    <h1 className="fw-bold">
                        FlowPay
                    </h1>

                    <p className="text-muted">
                        Payment Gateway Dashboard
                    </p>

                </div>

                <form onSubmit={handleLogin}>

                    <div className="mb-3">

                        <label
                            htmlFor="apiKey"
                            className="form-label"
                        >
                            API Key
                        </label>

                        <input
                            id="apiKey"
                            type="password"
                            className="form-control"
                            placeholder="Enter your FlowPay API key"
                            value={apiKey}
                            onChange={(event) =>
                                setApiKey(event.target.value)
                            }
                            disabled={loading}
                        />

                    </div>

                    {error && (
                        <div className="alert alert-danger">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        className="btn btn-dark w-100"
                        disabled={loading}
                    >
                        {loading
                            ? "Logging in..."
                            : "Login"}
                    </button>

                </form>

            </div>

        </div>
    );
}

export default Login;