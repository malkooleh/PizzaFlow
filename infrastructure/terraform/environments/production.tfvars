# Production Environment Configuration
aws_region  = "us-east-1"
environment = "production"

# VPC
vpc_cidr = "10.2.0.0/16"

# EKS
kubernetes_version       = "1.29"
eks_node_instance_types = ["m5.xlarge", "m5.2xlarge"]
eks_node_desired_size    = 5
eks_node_min_size        = 3
eks_node_max_size        = 20

# RDS
rds_instance_class    = "db.r5.large"
rds_allocated_storage = 100

# Redis
redis_node_type       = "cache.r5.large"
redis_num_cache_nodes = 3

# MSK
kafka_version            = "3.5.1"
msk_broker_instance_type = "kafka.m5.2xlarge"
msk_number_of_brokers    = 6
msk_ebs_volume_size      = 500

# Features
enable_external_dns = true
