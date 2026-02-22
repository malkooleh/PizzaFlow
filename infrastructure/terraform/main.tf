# PizzaFlow Infrastructure - Terraform Configuration
# AWS-based Kubernetes deployment

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.30"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.24"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.12"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  backend "s3" {
    bucket         = "pizzaflow-terraform-state"
    key            = "infrastructure/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "pizzaflow-terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "PizzaFlow"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# Data sources for existing resources
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

# Local values
locals {
  name_prefix = "pizzaflow-${var.environment}"
  
  common_tags = {
    Project     = "PizzaFlow"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
  
  azs = slice(data.aws_availability_zones.available.names, 0, 3)
}

# VPC Module
module "vpc" {
  source = "./modules/vpc"

  name_prefix  = local.name_prefix
  environment  = var.environment
  vpc_cidr     = var.vpc_cidr
  azs          = local.azs
  common_tags  = local.common_tags
}

# EKS Cluster
module "eks" {
  source = "./modules/eks"

  name_prefix          = local.name_prefix
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  kubernetes_version   = var.kubernetes_version
  node_instance_types  = var.eks_node_instance_types
  node_desired_size    = var.eks_node_desired_size
  node_min_size        = var.eks_node_min_size
  node_max_size        = var.eks_node_max_size
  common_tags          = local.common_tags
}

# RDS PostgreSQL
module "rds" {
  source = "./modules/rds"

  name_prefix          = local.name_prefix
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  eks_security_group_id = module.eks.node_security_group_id
  instance_class       = var.rds_instance_class
  allocated_storage    = var.rds_allocated_storage
  multi_az            = var.environment == "production"
  common_tags          = local.common_tags
}

# ElastiCache Redis
module "redis" {
  source = "./modules/redis"

  name_prefix          = local.name_prefix
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  eks_security_group_id = module.eks.node_security_group_id
  node_type            = var.redis_node_type
  num_cache_nodes      = var.redis_num_cache_nodes
  common_tags          = local.common_tags
}

# MSK Kafka
module "msk" {
  source = "./modules/msk"

  name_prefix          = local.name_prefix
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  eks_security_group_id = module.eks.node_security_group_id
  kafka_version        = var.kafka_version
  broker_instance_type = var.msk_broker_instance_type
  number_of_brokers    = var.msk_number_of_brokers
  ebs_volume_size      = var.msk_ebs_volume_size
  common_tags          = local.common_tags
}

# Kubernetes Provider Configuration
provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_ca_certificate)

  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name]
  }
}

# Helm Provider Configuration
provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_ca_certificate)

    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name]
    }
  }
}

# Kubernetes Add-ons
module "k8s_addons" {
  source = "./modules/k8s-addons"

  depends_on = [module.eks]

  cluster_name              = module.eks.cluster_name
  cluster_oidc_provider_arn = module.eks.oidc_provider_arn
  environment               = var.environment
  vpc_id                    = module.vpc.vpc_id
  
  # Enable add-ons
  enable_aws_load_balancer_controller = true
  enable_external_dns                 = var.enable_external_dns
  enable_cert_manager                 = true
  enable_metrics_server               = true
  enable_cluster_autoscaler           = true
}
