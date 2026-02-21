locals {
  cloud_init = templatefile("${path.module}/cloud-init.yaml.tftpl", {
    enable_data_volume = var.enable_data_volume
  })
}

resource "hcloud_ssh_key" "deploy" {
  name       = var.ssh_key_name
  public_key = var.ssh_public_key
  labels     = var.labels
}

resource "hcloud_firewall" "prod" {
  name   = "${var.server_name}-fw"
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
    server = hcloud_server.prod.id
  }
}

resource "hcloud_server" "prod" {
  name        = var.server_name
  server_type = var.server_type
  image       = var.image
  location    = var.location
  ssh_keys    = [hcloud_ssh_key.deploy.id]
  user_data   = local.cloud_init
  labels      = var.labels

  public_net {
    ipv4_enabled = true
    ipv6_enabled = true
  }
}

resource "hcloud_volume" "data" {
  count     = var.enable_data_volume ? 1 : 0
  name      = "${var.server_name}-data"
  size      = var.data_volume_size_gb
  location  = var.location
  format    = "ext4"
  automount = true
  labels    = var.labels
}

resource "hcloud_volume_attachment" "data" {
  count     = var.enable_data_volume ? 1 : 0
  volume_id = hcloud_volume.data[0].id
  server_id = hcloud_server.prod.id
  automount = true
}
