resource "hcloud_firewall" "prod" {
  name   = "${var.existing_server_name}-fw"
  labels = var.labels

  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "22"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "80"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  rule {
    direction  = "in"
    protocol   = "tcp"
    port       = "443"
    source_ips = ["0.0.0.0/0", "::/0"]
  }

  # Outbound unrestricted: required for package updates, GHCR pulls, ACME, OIDC providers.
  rule {
    direction       = "out"
    protocol        = "tcp"
    port            = "1-65535"
    destination_ips = ["0.0.0.0/0", "::/0"]
  }

  rule {
    direction       = "out"
    protocol        = "udp"
    port            = "1-65535"
    destination_ips = ["0.0.0.0/0", "::/0"]
  }

  apply_to {
    server = data.hcloud_server.existing.id
  }
}

data "hcloud_server" "existing" {
  name = var.existing_server_name
}

resource "hcloud_volume" "data" {
  count    = var.enable_data_volume ? 1 : 0
  name     = "${var.existing_server_name}-data"
  size     = var.data_volume_size_gb
  location = var.location
  format   = "ext4"
  labels   = var.labels
}

resource "hcloud_volume_attachment" "data" {
  count     = var.enable_data_volume ? 1 : 0
  volume_id = hcloud_volume.data[0].id
  server_id = data.hcloud_server.existing.id
  automount = true
}
