import { useState } from "react";
import Sidebar from "../components/Sidebar";
import api from "../services/api";

function Analytics() {

    // =========================================================
    // ANALYTICS CHAT STATE
    // =========================================================

    const [question, setQuestion] = useState("");

    const [answer, setAnswer] = useState("");

    const [summary, setSummary] = useState(null);

    const [loading, setLoading] = useState(false);

    const [error, setError] = useState("");


    // =========================================================
    // MONTHLY REPORT STATE
    // =========================================================

    const [monthlyReport, setMonthlyReport] =
        useState(null);

    const [reportLoading, setReportLoading] =
        useState(false);

    const [reportError, setReportError] =
        useState("");


    // =========================================================
    // SUGGESTED QUESTIONS
    // =========================================================

    const suggestedQuestions = [

        "How many payments do I have?",

        "How many payments were captured?",

        "How much money was refunded?",

        "Which currency do I use most?",

        "Give me a summary of my payments."

    ];


    // =========================================================
    // ASK AI ANALYTICS
    // =========================================================

    async function handleAsk(event) {

        event.preventDefault();

        setError("");

        setAnswer("");

        setSummary(null);


        if (!question.trim()) {

            setError(
                "Please enter a question."
            );

            return;
        }


        try {

            setLoading(true);


            const response =
                await api.post(
                    "/api/analytics/ask",
                    {
                        question:
                            question.trim()
                    }
                );


            setAnswer(
                response.data.answer || ""
            );


            setSummary(
                response.data.summary || null
            );


        } catch (error) {

            console.error(
                "Analytics error:",
                error
            );


            if (
                error.response?.status === 401
            ) {

                setError(
                    "Session expired or API key is invalid."
                );

            } else if (
                error.response?.status === 400
            ) {

                setError(
                    error.response?.data?.message ||
                    "Invalid question."
                );

            } else {

                setError(
                    "Unable to generate analytics answer."
                );
            }

        } finally {

            setLoading(false);

        }
    }


    // =========================================================
    // SUGGESTED QUESTION
    // =========================================================

    function handleSuggestedQuestion(
        suggestedQuestion
    ) {

        setQuestion(
            suggestedQuestion
        );

        setError("");

        setAnswer("");

        setSummary(null);
    }


    // =========================================================
    // CLEAR ANALYTICS
    // =========================================================

    function clearAnalytics() {

        setQuestion("");

        setAnswer("");

        setSummary(null);

        setError("");
    }


    // =========================================================
    // LOAD MONTHLY AI REPORT
    // =========================================================

    async function handleMonthlyReport() {

        setReportError("");

        setMonthlyReport(null);


        try {

            setReportLoading(true);


            // Get the current year and month
            const currentDate = new Date();

            const year =
                currentDate.getFullYear();

            const month =
                currentDate.getMonth() + 1;


            /*
             * Backend endpoint:
             *
             * GET /api/analytics/monthly-report
             *
             * Required parameters:
             *
             * year
             * month
             */

            const response =
                await api.get(
                    "/api/analytics/monthly-report",
                    {
                        params: {
                            year: year,
                            month: month
                        }
                    }
                );


            setMonthlyReport(
                response.data
            );


        } catch (error) {

            console.error(
                "Monthly report error:",
                error
            );


            if (
                error.response?.status === 401
            ) {

                setReportError(
                    "Session expired or API key is invalid."
                );

            } else if (
                error.response?.status === 400
            ) {

                setReportError(
                    error.response?.data?.message ||
                    "Unable to generate monthly report."
                );

            } else {

                setReportError(
                    "Unable to generate monthly AI report."
                );
            }

        } finally {

            setReportLoading(false);

        }
    }


    // =========================================================
    // FORMAT MONTH
    // =========================================================

    function formatMonth(
        year,
        month
    ) {

        if (!year || !month) {

            return "-";
        }


        const date =
            new Date(
                year,
                month - 1,
                1
            );


        return date.toLocaleString(
            "en-US",
            {
                month: "long",
                year: "numeric"
            }
        );
    }


    // =========================================================
    // FORMAT NUMBER
    // =========================================================

    function formatAmount(amount) {

        if (
            amount === null ||
            amount === undefined
        ) {

            return "0.00";
        }


        return Number(amount)
            .toFixed(2);
    }


    // =========================================================
    // UI
    // =========================================================

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
                        PAGE HEADER
                    ================================================= */}

                    <div className="mb-4">

                        <h2 className="fw-bold mb-1">
                            🤖 AI Analytics
                        </h2>

                        <p className="text-muted mb-0">
                            Ask questions and generate
                            AI-powered insights from your
                            FlowPay payment activity.
                        </p>

                    </div>


                    {/* =================================================
                        ASK FLOWPAY AI
                    ================================================= */}

                    <div className="card shadow-sm mb-4">

                        <div className="card-body">

                            <h5 className="fw-semibold mb-3">
                                Ask FlowPay AI
                            </h5>


                            <form
                                onSubmit={
                                    handleAsk
                                }
                            >

                                <div className="input-group">

                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder="e.g. How many payments were captured?"
                                        value={
                                            question
                                        }
                                        onChange={
                                            (event) =>
                                                setQuestion(
                                                    event
                                                        .target
                                                        .value
                                                )
                                        }
                                        disabled={
                                            loading
                                        }
                                    />


                                    <button
                                        type="submit"
                                        className="btn btn-dark"
                                        disabled={
                                            loading
                                        }
                                    >

                                        {loading
                                            ? "Analyzing..."
                                            : "Ask AI"}

                                    </button>

                                </div>

                            </form>


                            {/* =================================================
                                SUGGESTED QUESTIONS
                            ================================================= */}

                            <div className="mt-4">

                                <p className="text-muted mb-2">
                                    Suggested questions:
                                </p>


                                <div className="d-flex flex-wrap gap-2">

                                    {suggestedQuestions.map(
                                        (
                                            suggestedQuestion,
                                            index
                                        ) => (

                                            <button
                                                key={
                                                    index
                                                }
                                                type="button"
                                                className="btn btn-outline-secondary btn-sm"
                                                onClick={() =>
                                                    handleSuggestedQuestion(
                                                        suggestedQuestion
                                                    )
                                                }
                                                disabled={
                                                    loading
                                                }
                                            >
                                                {
                                                    suggestedQuestion
                                                }
                                            </button>

                                        )
                                    )}

                                </div>

                            </div>


                            {/* =================================================
                                ANALYTICS ERROR
                            ================================================= */}

                            {error && (

                                <div className="alert alert-danger mt-4 mb-0">

                                    {error}

                                </div>

                            )}

                        </div>

                    </div>


                    {/* =================================================
                        ANALYTICS LOADING
                    ================================================= */}

                    {loading && (

                        <div className="card shadow-sm mb-4">

                            <div className="card-body text-center py-5">

                                <div
                                    className="spinner-border"
                                    role="status"
                                />

                                <p className="text-muted mt-3 mb-0">
                                    FlowPay AI is analyzing
                                    your payment data...
                                </p>

                            </div>

                        </div>

                    )}


                    {/* =================================================
                        AI ANSWER
                    ================================================= */}

                    {answer && !loading && (

                        <div className="card shadow-sm mb-4">

                            <div className="card-body">

                                <div className="d-flex justify-content-between align-items-center mb-3">

                                    <h5 className="fw-semibold mb-0">
                                        🤖 AI Response
                                    </h5>


                                    <button
                                        type="button"
                                        className="btn btn-sm btn-outline-secondary"
                                        onClick={
                                            clearAnalytics
                                        }
                                    >
                                        Clear
                                    </button>

                                </div>


                                <div className="bg-light rounded p-4">

                                    <p
                                        className="mb-0"
                                        style={{
                                            whiteSpace:
                                                "pre-line"
                                        }}
                                    >
                                        {answer}
                                    </p>

                                </div>

                            </div>

                        </div>

                    )}


                    {/* =================================================
                        BACKEND PAYMENT SUMMARY
                    ================================================= */}

                    {summary && !loading && (

                        <div className="card shadow-sm mb-4">

                            <div className="card-body">

                                <h5 className="fw-semibold mb-4">
                                    Payment Statistics
                                </h5>


                                <div className="row g-3">


                                    {/* TOTAL */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Total Payments
                                            </div>

                                            <div className="fs-3 fw-bold">
                                                {
                                                    summary.totalPayments
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* CREATED */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Created
                                            </div>

                                            <div className="fs-3 fw-bold">
                                                {
                                                    summary.createdPayments
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* AUTHORIZED */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Authorized
                                            </div>

                                            <div className="fs-3 fw-bold">
                                                {
                                                    summary.authorizedPayments
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* CAPTURED */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Captured
                                            </div>

                                            <div className="fs-3 fw-bold">
                                                {
                                                    summary.capturedPayments
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* REFUNDED */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Refunded
                                            </div>

                                            <div className="fs-3 fw-bold">
                                                {
                                                    summary.refundedPayments
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* FAILED */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Failed
                                            </div>

                                            <div className="fs-3 fw-bold">
                                                {
                                                    summary.failedPayments
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* TOTAL PAYMENT AMOUNT */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Total Payment Amount
                                            </div>

                                            <div className="fs-4 fw-bold">
                                                {
                                                    formatAmount(
                                                        summary.totalPaymentAmount
                                                    )
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* CAPTURED AMOUNT */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Total Captured Amount
                                            </div>

                                            <div className="fs-4 fw-bold">
                                                {
                                                    formatAmount(
                                                        summary.totalCapturedAmount
                                                    )
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* REFUNDED AMOUNT */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Total Refunded Amount
                                            </div>

                                            <div className="fs-4 fw-bold">
                                                {
                                                    formatAmount(
                                                        summary.totalRefundedAmount
                                                    )
                                                }
                                            </div>

                                        </div>

                                    </div>


                                    {/* CURRENCY */}

                                    <div className="col-md-4">

                                        <div className="border rounded p-3 h-100">

                                            <div className="text-muted small">
                                                Most Used Currency
                                            </div>

                                            <div className="fs-4 fw-bold">
                                                {
                                                    summary.mostUsedCurrency
                                                }
                                            </div>

                                        </div>

                                    </div>

                                </div>

                            </div>

                        </div>

                    )}


                    {/* =================================================
                        MONTHLY AI REPORT
                    ================================================= */}

                    <div className="card shadow-sm mb-4">

                        <div className="card-body">

                            <div className="d-flex justify-content-between align-items-center mb-3">

                                <div>

                                    <h5 className="fw-semibold mb-1">
                                        📊 Monthly AI Report
                                    </h5>

                                    <p className="text-muted mb-0">
                                        Generate an AI-powered summary
                                        of your current month's payment activity.
                                    </p>

                                </div>


                                <button
                                    type="button"
                                    className="btn btn-dark"
                                    onClick={
                                        handleMonthlyReport
                                    }
                                    disabled={
                                        reportLoading
                                    }
                                >

                                    {reportLoading
                                        ? "Generating..."
                                        : "Generate Report"}

                                </button>

                            </div>


                            {/* =================================================
                                REPORT ERROR
                            ================================================= */}

                            {reportError && (

                                <div className="alert alert-danger mt-3 mb-0">

                                    {reportError}

                                </div>

                            )}


                            {/* =================================================
                                REPORT LOADING
                            ================================================= */}

                            {reportLoading && (

                                <div className="text-center py-5">

                                    <div
                                        className="spinner-border"
                                        role="status"
                                    />

                                    <p className="text-muted mt-3 mb-0">
                                        FlowPay AI is generating
                                        your monthly report...
                                    </p>

                                </div>

                            )}


                            {/* =================================================
                                REPORT
                            ================================================= */}

                            {monthlyReport &&
                                !reportLoading && (

                                    <div className="mt-4">


                                        {/* REPORT TITLE */}

                                        <div className="bg-light rounded p-4 mb-4">

                                            <h4 className="fw-bold mb-1">

                                                {formatMonth(
                                                    monthlyReport.year,
                                                    monthlyReport.month
                                                )}

                                            </h4>

                                            <p className="text-muted mb-0">
                                                Monthly Payment Activity
                                            </p>

                                        </div>


                                        {/* =================================================
                                            MONTHLY STATISTICS
                                        ================================================= */}

                                        <div className="row g-3 mb-4">


                                            {/* TOTAL */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Total Payments
                                                    </div>

                                                    <div className="fs-3 fw-bold">
                                                        {
                                                            monthlyReport.totalPayments
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* CREATED */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Created
                                                    </div>

                                                    <div className="fs-3 fw-bold">
                                                        {
                                                            monthlyReport.createdPayments
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* AUTHORIZED */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Authorized
                                                    </div>

                                                    <div className="fs-3 fw-bold">
                                                        {
                                                            monthlyReport.authorizedPayments
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* CAPTURED */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Captured
                                                    </div>

                                                    <div className="fs-3 fw-bold">
                                                        {
                                                            monthlyReport.capturedPayments
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* REFUNDED */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Refunded
                                                    </div>

                                                    <div className="fs-3 fw-bold">
                                                        {
                                                            monthlyReport.refundedPayments
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* FAILED */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Failed
                                                    </div>

                                                    <div className="fs-3 fw-bold">
                                                        {
                                                            monthlyReport.failedPayments
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* TOTAL PAYMENT AMOUNT */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Total Payment Amount
                                                    </div>

                                                    <div className="fs-4 fw-bold">
                                                        {
                                                            formatAmount(
                                                                monthlyReport.totalPaymentAmount
                                                            )
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* CAPTURED AMOUNT */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Total Captured Amount
                                                    </div>

                                                    <div className="fs-4 fw-bold">
                                                        {
                                                            formatAmount(
                                                                monthlyReport.totalCapturedAmount
                                                            )
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* REFUNDED AMOUNT */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Total Refunded Amount
                                                    </div>

                                                    <div className="fs-4 fw-bold">
                                                        {
                                                            formatAmount(
                                                                monthlyReport.totalRefundedAmount
                                                            )
                                                        }
                                                    </div>

                                                </div>

                                            </div>


                                            {/* CURRENCY */}

                                            <div className="col-md-4">

                                                <div className="border rounded p-3 h-100">

                                                    <div className="text-muted small">
                                                        Most Used Currency
                                                    </div>

                                                    <div className="fs-4 fw-bold">
                                                        {
                                                            monthlyReport.mostUsedCurrency
                                                        }
                                                    </div>

                                                </div>

                                            </div>

                                        </div>


                                        {/* =================================================
                                            AI MONTHLY SUMMARY
                                        ================================================= */}

                                        <div className="border rounded p-4">

                                            <h5 className="fw-semibold mb-3">
                                                🤖 AI Monthly Summary
                                            </h5>

                                            <p
                                                className="mb-0"
                                                style={{
                                                    whiteSpace:
                                                        "pre-line"
                                                }}
                                            >
                                                {
                                                    monthlyReport.aiSummary
                                                }
                                            </p>

                                        </div>

                                    </div>

                                )}


                            {/* =================================================
                                EMPTY STATE
                            ================================================= */}

                            {!monthlyReport &&
                                !reportLoading &&
                                !reportError && (

                                    <div className="text-center text-muted py-4">

                                        Click

                                        <strong className="mx-1">
                                            Generate Report
                                        </strong>

                                        to create your monthly AI report.

                                    </div>

                                )}

                        </div>

                    </div>

                </div>

            </div>

        </div>
    );
}

export default Analytics;