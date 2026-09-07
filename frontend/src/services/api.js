import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080",
    headers: {
        "Content-Type": "application/json"
    }
});

api.interceptors.request.use(
    (config) => {

        const apiKey =
            localStorage.getItem("flowpay_api_key");

        if (apiKey) {
            config.headers["X-API-KEY"] = apiKey;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;