output "server_ipv4" {
  value       = hcloud_server.prod.ipv4_address
  description = "Public IPv4 address"
}

output "server_ipv6" {
  value       = hcloud_server.prod.ipv6_address
  description = "Public IPv6 network"
}

output "ssh_user" {
  value       = "app"
  description = "Initial SSH user for first bootstrap"
}
