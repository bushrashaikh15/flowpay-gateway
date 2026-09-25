import {
    LayoutDashboard,
    CreditCard,
    ArrowLeftRight,
    ShieldCheck,
    BarChart3,
    FileText,
    Webhook,
    Code2,
    Settings,
    LogOut,
    ChevronRight
} from "lucide-react";

import { useLocation, useNavigate } from "react-router-dom";

function Sidebar() {

    const navigate = useNavigate();
    const location = useLocation();

    function handleLogout() {

        localStorage.removeItem("flowpay_api_key");

        navigate("/login");
    }

    function isActive(path) {

        return location.pathname === path;
    }

    function menuItemClass(path) {

        return isActive(path)
            ? "flowpay-menu-item active"
            : "flowpay-menu-item";
    }

    return (

        <div className="flowpay-sidebar col-md-2">

            {/* Brand */}

            <div
                className="flowpay-brand"
                onClick={() => navigate("/dashboard")}
            >

                <div className="flowpay-brand-icon">
                    F
                </div>

                <div>

                    <div className="flowpay-brand-name">
                        FlowPay
                    </div>

                    <div className="flowpay-brand-subtitle">
                        Payment Platform
                    </div>

                </div>

            </div>


            {/* Navigation */}

            <div className="flowpay-navigation">


                {/* Overview */}

                <div className="flowpay-section-title">
                    OVERVIEW
                </div>

                <div
                    className={menuItemClass("/dashboard")}
                    onClick={() => navigate("/dashboard")}
                >

                    <LayoutDashboard size={18} />

                    <span>
                        Dashboard
                    </span>

                </div>


                {/* Payments */}

                <div className="flowpay-section-title">
                    PAYMENTS
                </div>

                <div
                    className={menuItemClass("/payment-intents")}
                    onClick={() => navigate("/payment-intents")}
                >

                    <CreditCard size={18} />

                    <span>
                        Payments
                    </span>

                </div>


                <div
                    className={menuItemClass("/transactions")}
                    onClick={() => navigate("/transactions")}
                >

                    <ArrowLeftRight size={18} />

                    <span>
                        Transactions
                    </span>

                </div>


                {/* Risk */}

                <div className="flowpay-section-title">
                    RISK & ANALYTICS
                </div>


                <div
                    className={menuItemClass("/analytics")}
                    onClick={() => navigate("/analytics")}
                >

                    <BarChart3 size={18} />

                    <span>
                        Analytics
                    </span>

                </div>


                {/* Operations */}

                <div className="flowpay-section-title">
                    OPERATIONS
                </div>


                <div
                    className={menuItemClass("/audit-logs")}
                    onClick={() => navigate("/audit-logs")}
                >

                    <FileText size={18} />

                    <span>
                        Audit Logs
                    </span>

                </div>


                {/* Coming soon visual section */}

                <div className="flowpay-section-title">
                    DEVELOPER
                </div>


                <div className="flowpay-menu-item disabled">

                    <Webhook size={18} />

                    <span>
                        Webhooks
                    </span>

                    <span className="flowpay-coming-soon">
                        API
                    </span>

                </div>


                <div className="flowpay-menu-item disabled">

                    <Code2 size={18} />

                    <span>
                        API Docs
                    </span>

                    <span className="flowpay-coming-soon">
                        SOON
                    </span>

                </div>


                {/* Settings */}

                <div className="flowpay-section-title">
                    SYSTEM
                </div>


                <div className="flowpay-menu-item disabled">

                    <Settings size={18} />

                    <span>
                        Settings
                    </span>

                </div>

            </div>


            {/* Bottom merchant area */}

            <div className="flowpay-sidebar-bottom">


                <div className="flowpay-merchant-card">

                    <div className="flowpay-merchant-avatar">
                        M
                    </div>

                    <div className="flowpay-merchant-info">

                        <div className="flowpay-merchant-name">
                            Current Merchant
                        </div>

                        <div className="flowpay-merchant-status">

                            <span className="flowpay-status-dot"></span>

                            API Connected

                        </div>

                    </div>

                    <ChevronRight size={16} />

                </div>


                <button
                    className="flowpay-logout"
                    onClick={handleLogout}
                >

                    <LogOut size={17} />

                    <span>
                        Logout
                    </span>

                </button>

            </div>

        </div>
    );
}

export default Sidebar;