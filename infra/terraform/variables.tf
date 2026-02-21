variable "hcloud_token" {
  description = "Hetzner Cloud API token"
  type        = string
  sensitive   = true
}

variable "existing_server_name" {
  description = "Name of the already-existing Hetzner server"
  type        = string
  default     = "ubuntu-8gb-hel1-2"
}

variable "ssh_user" {
  description = "SSH user used for deployments"
  type        = string
  default     = "app"
}

variable "location" {
  description = "Location for optional volume (must match existing server location)"
  type        = string
  default     = "hel1"
}

variable "enable_data_volume" {
  description = "Attach dedicated volume mounted at /data"
  type        = bool
  default     = true
}

variable "data_volume_size_gb" {
  description = "Volume size in GB when enabled"
  type        = number
  default     = 40
}

variable "labels" {
  description = "Common labels"
  type        = map(string)
  default = {
    app         = "balmaya"
    environment = "production"
    managed_by  = "terraform"
  }
}
