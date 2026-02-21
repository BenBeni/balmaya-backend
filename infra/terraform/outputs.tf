output "server_ipv4" {
  value       = data.hcloud_server.existing.ipv4_address
  description = "Public IPv4 address"
}

output "server_ipv6" {
  value       = try(data.hcloud_server.existing.ipv6_address, null)
  description = "Public IPv6"
}

output "ssh_user" {
  value       = var.ssh_user
  description = "SSH user for deployments"
}
