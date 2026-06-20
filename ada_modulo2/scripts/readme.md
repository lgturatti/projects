# Local API helper scripts

## Generate a JWT compatible with the local Docker Compose setup

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Get-LocalJwt.ps1
```

## List orders with authentication

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Invoke-LocalOrderApi.ps1 -Action list-orders -CustomerId customer-active-001
```

## Try to create an order with a mocked active customer

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Invoke-LocalOrderApi.ps1 -Action create-order -CustomerId customer-active-001
```

## Try to create an order with a mocked non-existent customer

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Invoke-LocalOrderApi.ps1 -Action create-order -CustomerId customer-not-found-001
```

