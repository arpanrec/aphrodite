terraform {
  backend "http" {
    address        = "http://127.0.0.1:8083/aphrodite-web-api/api/v1/tf-state/string/data/rgrst4534523rea?AphroditeNamespace=aphrodite&Aphrodite-API-Key=eyJ0eXAiOiJKV1QiLCJraWQiOiJhZmMxZmE5OC04YjU2LTQzZGYtYTNiMy1jNGFiMWIzNTJhY2IiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NjUzMDYwNzR9.0g6xYQw0m6AUdIMQoSLV11GrmnSl3rlMfiGo9k3DL68" # gitleaks:allow
    lock_address   = "http://127.0.0.1:8083/aphrodite-web-api/api/v1/tf-state/string/lock/rgrst4534523rea?AphroditeNamespace=aphrodite&Aphrodite-API-Key=eyJ0eXAiOiJKV1QiLCJraWQiOiJhZmMxZmE5OC04YjU2LTQzZGYtYTNiMy1jNGFiMWIzNTJhY2IiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NjUzMDYwNzR9.0g6xYQw0m6AUdIMQoSLV11GrmnSl3rlMfiGo9k3DL68" # gitleaks:allow
    unlock_address = "http://127.0.0.1:8083/aphrodite-web-api/api/v1/tf-state/string/lock/rgrst4534523rea?AphroditeNamespace=aphrodite&Aphrodite-API-Key=eyJ0eXAiOiJKV1QiLCJraWQiOiJhZmMxZmE5OC04YjU2LTQzZGYtYTNiMy1jNGFiMWIzNTJhY2IiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NjUzMDYwNzR9.0g6xYQw0m6AUdIMQoSLV11GrmnSl3rlMfiGo9k3DL68" # gitleaks:allow
    lock_method    = "POST"
    unlock_method  = "DELETE"
    # password = "eyJ0eXAiOiJKV1QiLCJraWQiOiJhZmMxZmE5OC04YjU2LTQzZGYtYTNiMy1jNGFiMWIzNTJhY2IiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NjUzMDYwNzR9.0g6xYQw0m6AUdIMQoSLV11GrmnSl3rlMfiGo9k3DL68" gitleaks:allow
  }
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "3.6.2"
    }
  }
}

provider "docker" {
  host = "unix:///var/run/docker.sock"
}

resource "docker_image" "ubuntu11" {
  name = "alpine:latest"
}
