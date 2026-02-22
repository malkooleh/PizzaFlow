# Staging Environment Configuration
aws_region  = "us-east-1"
environment = "staging"

# VPC
vpc_cidr = "10.1.0.0/16"

# EKS
kubernetes_version       = "1.29"
eks_node_instance_types = ["t3.large"]
eks_node_desired_size    = 3
eks_node_min_size        = 2
eks_node_max_size        = 8

# RDS
rds_instance_class    = "db.t3.medium"
rds_allocated_storage = 50

# Redis
redis_node_type       = "cache.t3.small"
redis_num_cache_nodes = 2

# MSK
kafka_version            = "3.5.1"
msk_broker_instance_type = "kafka.m5.large"
msk_number_of_brokers    = 3
msk_ebs_volume_size      = 100

# Features
enable_external_dns = true
