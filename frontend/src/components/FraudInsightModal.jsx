import React, { useEffect, useState } from "react";
import api from "../services/api";

export default function FraudInsightModal({
                                              paymentIntentId,
                                              onClose,
                                          }) {
    const [insight, setInsight] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function loadFraudInsight() {
            try {
                setLoading(true);
                setError("");

                const response = await api.get(
                    `/fraud-insights/payment/${paymentIntentId}`
                );

                setInsight(response.data);
            } catch (err) {
                console.error("Fraud insight error:", err);

                setError(
                    err.response?.data?.message ||
                    "Unable to load fraud insight."
                );
            } finally {
                setLoading(false);
            }
        }

        loadFraudInsight();
    }, [paymentIntentId]);

    function getRiskClass(riskLevel) {
        if (riskLevel === "HIGH") {
            return "fraud-risk-high";
        }

        if (riskLevel === "MEDIUM") {
            return "fraud-risk-medium";
        }

        return "fraud-risk-low";
    }

    return (
        <div className="fraud-modal-overlay">
            <div className="fraud-modal">

                <div className="fraud-modal-header">
                    <div>
                        <h2>Fraud Risk Insight</h2>
                        <p>
                            Payment #{paymentIntentId}
                        </p>
                    </div>

                    <button
                        className="fraud-close-button"
                        onClick={onClose}
                    >
                        ×
                    </button>
                </div>

                {loading && (
                    <div className="fraud-loading">
                        Analyzing payment risk...
                    </div>
                )}

                {error && (
                    <div className="fraud-error">
                        {error}
                    </div>
                )}

                {insight && !loading && (
                    <div className="fraud-content">

                        <div className="fraud-summary">

                            <div className="fraud-score-card">
                                <span>Risk Score</span>

                                <strong>
                                    {insight.riskScore}
                                    <small>/100</small>
                                </strong>
                            </div>

                            <div
                                className={`fraud-level-card ${getRiskClass(
                                    insight.riskLevel
                                )}`}
                            >
                                <span>Risk Level</span>

                                <strong>
                                    {insight.riskLevel}
                                </strong>
                            </div>

                        </div>

                        <div className="fraud-section">
                            <h3>Risk Signals</h3>

                            {insight.riskSignals &&
                            insight.riskSignals.length > 0 ? (
                                <ul className="fraud-signals">
                                    {insight.riskSignals.map(
                                        (signal, index) => (
                                            <li key={index}>
                                                ⚠️ {signal}
                                            </li>
                                        )
                                    )}
                                </ul>
                            ) : (
                                <p className="fraud-no-signals">
                                    No significant risk signals detected.
                                </p>
                            )}
                        </div>

                        <div className="fraud-section">
                            <h3>AI Explanation</h3>

                            <div className="fraud-ai-box">
                                <div className="fraud-ai-title">
                                    🤖 Local AI Analysis
                                </div>

                                <p>
                                    {insight.aiExplanation}
                                </p>
                            </div>
                        </div>

                        <div className="fraud-disclaimer">
                            <strong>Note:</strong>{" "}
                            {insight.disclaimer}
                        </div>

                    </div>
                )}

                <div className="fraud-modal-footer">
                    <button
                        className="fraud-close-footer"
                        onClick={onClose}
                    >
                        Close
                    </button>
                </div>

            </div>
        </div>
    );
}