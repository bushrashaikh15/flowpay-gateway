$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/flowpay_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"

$env:SPRING_DATASOURCE_PASSWORD=(
    docker inspect flowpay-postgres --format "{{range .Config.Env}}{{println .}}{{end}}" |
    Select-String "^POSTGRES_PASSWORD=" |
    ForEach-Object {
        $_.ToString().Split("=",2)[1]
    }
)

$env:SPRING_DATA_REDIS_HOST="localhost"
$env:SPRING_DATA_REDIS_PORT="6380"

$env:FLOWPAY_WEBHOOK_SECRET="flowpay-local-webhook-secret"

$env:JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Kolkata"

# OpenAI configuration
if ([string]::IsNullOrWhiteSpace($env:OPENAI_API_KEY)) {
    Write-Host ""
    Write-Host "ERROR: OPENAI_API_KEY is not set." -ForegroundColor Red
    Write-Host ""
    Write-Host "Set it in PowerShell before starting the backend:" -ForegroundColor Yellow
    Write-Host '$env:OPENAI_API_KEY="YOUR_OPENAI_API_KEY"' -ForegroundColor Yellow
    Write-Host '$env:OPENAI_MODEL="gpt-5.6"' -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

if ([string]::IsNullOrWhiteSpace($env:OPENAI_MODEL)) {
    $env:OPENAI_MODEL="gpt-5.6"
}

Write-Host ""
Write-Host "Starting FlowPay backend..." -ForegroundColor Green
Write-Host "OpenAI model: $env:OPENAI_MODEL" -ForegroundColor Green
Write-Host ""

.\mvnw.cmd spring-boot:run