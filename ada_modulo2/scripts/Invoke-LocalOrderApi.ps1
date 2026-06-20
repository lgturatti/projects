param(
    [ValidateSet('list-orders', 'create-order')]
    [string]$Action = 'list-orders',
    [string]$CustomerId = 'customer-active-001',
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$Secret = 'change-me-in-production-32chars!'
)

$token = & "$PSScriptRoot\Get-LocalJwt.ps1" -Secret $Secret
$headers = @{ Authorization = "Bearer $token" }

try {
    if ($Action -eq 'list-orders') {
        $uri = "$BaseUrl/api/v1/orders?customerId=$CustomerId"
        $response = Invoke-WebRequest -UseBasicParsing -Uri $uri -Headers $headers -ErrorAction Stop
    }
    else {
        $uri = "$BaseUrl/api/v1/orders"
        $body = "{\"customerId\":\"$CustomerId\"}"
        $response = Invoke-WebRequest -UseBasicParsing -Uri $uri -Method Post -ContentType 'application/json' -Headers $headers -Body $body -ErrorAction Stop
    }

    Write-Output ("STATUS: {0}" -f [int]$response.StatusCode)
    Write-Output $response.Content
}
catch {
    if ($_.Exception.Response) {
        $resp = $_.Exception.Response
        Write-Output ("STATUS: {0}" -f [int]$resp.StatusCode)
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
        try {
            Write-Output $reader.ReadToEnd()
        }
        finally {
            $reader.Dispose()
        }
    }
    else {
        throw
    }
}

