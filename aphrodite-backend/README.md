# Backend

Application Backend

## Terraform

This application provides a [Terraform HTTP backend](https://developer.hashicorp.com/terraform/language/backend/http).

```hcl
terraform {
  backend "http" {
    address        = "http://127.0.0.1:8083/aphrodite-web-api/api/v1/tf-state/<Bucket>/data/<StateName>?AphroditeNamespace=<Optional Namespace>&Aphrodite-API-Key=<ApiKey>"
    lock_address   = "http://127.0.0.1:8083/aphrodite-web-api/api/v1/tf-state/<Bucket>/lock/<StateName>?AphroditeNamespace=<Optional Namespace>&Aphrodite-API-Key=<ApiKey>"
    unlock_address = "http://127.0.0.1:8083/aphrodite-web-api/api/v1/tf-state/<Bucket>/lock/<StateName>?AphroditeNamespace=<Optional Namespace>&Aphrodite-API-Key=<ApiKey>"
    lock_method    = "POST"
    unlock_method  = "DELETE"
  }
}
```
