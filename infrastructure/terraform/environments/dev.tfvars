# Development Environment Configuration
aws_region  = "us-east-1"
environment = "dev"

# VPC
vpc_cidr = "10.0.0.0/16"

# EKS
kubernetes_version       = "1.29"
eks_node_instance_types = ["t3.medium"]
eks_node_desired_size    = 2
eks_node_min_size        = 2
eks_node_max_size        = 5

# RDS
rds_instance_class    = "db.t3.small"
rds_allocated_storage = 20

# Redis
redis_node_type       = "cache.t3.micro"
redis_num_cache_nodes = 1

# MSK
kafka_version            = "3.5.1"
msk_broker_instance_type = "kafka.t3.small"
msk_number_of_brokers    = 3
msk_ebs_volume_size      = 50

# Features
enable_external_dns = false
