# MSK Kafka Module for PizzaFlow

variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnet IDs"
  type        = list(string)
}

variable "eks_security_group_id" {
  description = "EKS node security group ID"
  type        = string
}

variable "kafka_version" {
  description = "Apache Kafka version"
  type        = string
}

variable "broker_instance_type" {
  description = "Broker instance type"
  type        = string
}

variable "number_of_brokers" {
  description = "Number of brokers"
  type        = number
}

variable "ebs_volume_size" {
  description = "EBS volume size in GB"
  type        = number
}

variable "common_tags" {
  description = "Common tags for resources"
  type        = map(string)
}

# Security Group for MSK
resource "aws_security_group" "msk" {
  name        = "${var.name_prefix}-msk-sg"
  description = "Security group for MSK Kafka"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Kafka plaintext from EKS"
    from_port       = 9092
    to_port         = 9092
    protocol        = "tcp"
    security_groups = [var.eks_security_group_id]
  }

  ingress {
    description     = "Kafka TLS from EKS"
    from_port       = 9094
    to_port         = 9094
    protocol        = "tcp"
    security_groups = [var.eks_security_group_id]
  }

  ingress {
    description     = "Kafka SASL/SCRAM from EKS"
    from_port       = 9096
    to_port         = 9096
    protocol        = "tcp"
    security_groups = [var.eks_security_group_id]
  }

  ingress {
    description     = "Zookeeper from EKS"
    from_port       = 2181
    to_port         = 2181
    protocol        = "tcp"
    security_groups = [var.eks_security_group_id]
  }

  ingress {
    description = "Internal communication between brokers"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.common_tags, {
    Name = "${var.name_prefix}-msk-sg"
  })
}

# CloudWatch Log Group for MSK
resource "aws_cloudwatch_log_group" "msk" {
  name              = "/aws/msk/${var.name_prefix}"
  retention_in_days = 30

  tags = var.common_tags
}

# MSK Configuration
resource "aws_msk_configuration" "main" {
  name = "${var.name_prefix}-msk-config"
  
  kafka_versions = [var.kafka_version]

  server_properties = <<PROPERTIES
auto.create.topics.enable=true
default.replication.factor=3
min.insync.replicas=2
num.io.threads=8
num.network.threads=5
num.partitions=3
num.replica.fetchers=2
replica.lag.time.max.ms=30000
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
socket.send.buffer.bytes=102400
unclean.leader.election.enable=false
zookeeper.session.timeout.ms=18000
log.retention.hours=168
log.segment.bytes=1073741824
PROPERTIES
}

# MSK Cluster
resource "aws_msk_cluster" "main" {
  cluster_name           = "${var.name_prefix}-msk"
  kafka_version          = var.kafka_version
  number_of_broker_nodes = var.number_of_brokers

  broker_node_group_info {
    instance_type   = var.broker_instance_type
    client_subnets  = var.private_subnet_ids
    security_groups = [aws_security_group.msk.id]

    storage_info {
      ebs_storage_info {
        volume_size = var.ebs_volume_size
      }
    }
  }

  configuration_info {
    arn      = aws_msk_configuration.main.arn
    revision = aws_msk_configuration.main.latest_revision
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS_PLAINTEXT"
      in_cluster    = true
    }
    encryption_at_rest_kms_key_arn = aws_kms_key.msk.arn
  }

  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = true
        log_group = aws_cloudwatch_log_group.msk.name
      }
    }
  }

  open_monitoring {
    prometheus {
      jmx_exporter {
        enabled_in_broker = true
      }
      node_exporter {
        enabled_in_broker = true
      }
    }
  }

  tags = merge(var.common_tags, {
    Name = "${var.name_prefix}-msk"
  })
}

# KMS Key for MSK encryption
resource "aws_kms_key" "msk" {
  description             = "KMS key for MSK encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(var.common_tags, {
    Name = "${var.name_prefix}-msk-key"
  })
}

resource "aws_kms_alias" "msk" {
  name          = "alias/${var.name_prefix}-msk"
  target_key_id = aws_kms_key.msk.key_id
}

# Store MSK connection info in Secrets Manager
resource "aws_secretsmanager_secret" "msk" {
  name        = "${var.name_prefix}/msk/connection"
  description = "MSK connection information for PizzaFlow"

  tags = var.common_tags
}

resource "aws_secretsmanager_secret_version" "msk" {
  secret_id = aws_secretsmanager_secret.msk.id
  secret_string = jsonencode({
    bootstrap_brokers     = aws_msk_cluster.main.bootstrap_brokers
    bootstrap_brokers_tls = aws_msk_cluster.main.bootstrap_brokers_tls
    zookeeper_connect     = aws_msk_cluster.main.zookeeper_connect_string
  })
}

# Outputs
output "bootstrap_brokers" {
  value = aws_msk_cluster.main.bootstrap_brokers
}

output "bootstrap_brokers_tls" {
  value = aws_msk_cluster.main.bootstrap_brokers_tls
}

output "zookeeper_connect" {
  value = aws_msk_cluster.main.zookeeper_connect_string
}

output "cluster_arn" {
  value = aws_msk_cluster.main.arn
}

output "secret_arn" {
  value = aws_secretsmanager_secret.msk.arn
}
