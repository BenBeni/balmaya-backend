variable "hcloud_token" {
  description = "Hetzner Cloud API token"
  type        = string
  sensitive   = true
}

variable "ssh_public_key" {
  description = "Public SSH key content for server access"
  type        = string
}

variable "ssh_key_name" {
  description = "Name of the SSH key in Hetzner"
  type        = string
  default     = "balmaya-deploy-key"
}

variable "server_name" {
  description = "VM name"
  type        = string
  default     = "balmaya-prod-1"
}

variable "server_type" {
  description = "Hetzner server type. cx31 gives >= 4GB RAM."
  type        = string
  default     = "cx31"
}

variable "location" {
  description = "Hetzner location"
  type        = string
  default     = "nbg1"
}

variable "image" {
  description = "Base server image"
  type        = string
  default     = "ubuntu-22.04"
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
