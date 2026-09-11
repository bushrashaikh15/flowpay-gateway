# FlowPay Backend Startup Script
# Uses PostgreSQL + Redis + local Ollama AI

$ErrorActionPreference = "Stop"

Write-Host "Starting FlowPay backend..." -ForegroundColor Cyan

# PostgreSQL
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5433/flowpay_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"

# Get PostgreSQL password from Docker container
$env:SPRING_DATASOURCE_PASSWORD = (
    docker inspect flowpay-postgres --format "{{range .Config.Env}}{{println .}}{{end}}" |
    Select-String "^POSTGRES_PASSWORD=" |
    ForEach-Object { $_.ToString().Split("=",2)[1] }
)

# Redis
$env:SPRING_DATA_REDIS_HOST = "localhost"
$env:SPRING_DATA_REDIS_PORT = "6380"

# Webhook secret
$env:FLOWPAY_WEBHOOK_SECRET = "flowpay-local-webhook-secret"

# Timezone
$env:JAVA_TOOL_OPTIONS = "-Duser.timezone=Asia/Kolkata"

# Local Ollama AI
$env:OLLAMA_BASE_URL = "http://localhost:11434"
$env:OLLAMA_MODEL = "llama3"

Write-Host "PostgreSQL: localhost:5433" -ForegroundColor Green
Write-Host "Redis:      localhost:6380" -ForegroundColor Green
Write-Host "Ollama:     localhost:11434" -ForegroundColor Green
Write-Host "AI Model:   llama3" -ForegroundColor Green

Write-Host ""
Write-Host "Starting Spring Boot..." -ForegroundColor Cyan

.\mvnw.cmd spring-boot:run