param(
    [string[]]$Scopes = @('orders:read', 'orders:write', 'payments:read', 'payments:write'),
    [string]$Secret = 'change-me-in-production-32chars!',
    [string]$Subject = 'local-user',
    [int]$ExpiresInSeconds = 3600
)

function ConvertTo-Base64Url {
    param([byte[]]$Bytes)

    $base64 = [Convert]::ToBase64String($Bytes)
    return $base64.TrimEnd('=') -replace '\+', '-' -replace '/', '_'
}

$now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$headerJson = '{"alg":"HS256","typ":"JWT"}'
$scopeValue = ($Scopes | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join ' '
$payloadJson = "{\"sub\":\"$Subject\",\"scope\":\"$scopeValue\",\"iat\":$now,\"exp\":$($now + $ExpiresInSeconds)}"

$header = ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes($headerJson))
$payload = ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes($payloadJson))
$data = [Text.Encoding]::UTF8.GetBytes("$header.$payload")
$key = [Text.Encoding]::UTF8.GetBytes($Secret)

$hmac = [System.Security.Cryptography.HMACSHA256]::new($key)
try {
    $signatureBytes = $hmac.ComputeHash($data)
}
finally {
    $hmac.Dispose()
}

$signature = ConvertTo-Base64Url $signatureBytes
Write-Output "$header.$payload.$signature"

