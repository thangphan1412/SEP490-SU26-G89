$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$systemTestRoot = Join-Path $PSScriptRoot 'system-tests'

if (-not (Test-Path -LiteralPath (Join-Path $systemTestRoot '.env'))) {
    throw 'Create TESTING/system-tests/.env from .env.example and fill the test-only values first.'
}

docker compose -f (Join-Path $repositoryRoot 'deploy/docker-compose.yml') up -d --build

Push-Location $systemTestRoot
try {
    npm ci
    npx playwright install chromium
    npm run test:smoke
    npm run test:report53
}
finally {
    Pop-Location
}
