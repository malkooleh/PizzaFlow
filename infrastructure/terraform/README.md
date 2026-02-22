# Terraform Infrastructure for PizzaFlow

This directory contains Terraform configurations for deploying the PizzaFlow microservices platform on AWS.

## Architecture

The infrastructure consists of:

| Component | AWS Service | Purpose |
|-----------|-------------|---------|
| Networking | VPC | Isolated network with public/private subnets |
| Container Orchestration | EKS | Managed Kubernetes cluster |
| Database | RDS PostgreSQL | Relational database for services |
| Caching | ElastiCache Redis | Session and data caching |
| Message Broker | MSK | Apache Kafka for event streaming |

## Prerequisites

1. **AWS CLI** configured with appropriate credentials
2. **Terraform** >= 1.6.0
3. **kubectl** for Kubernetes management
4. **Helm** >= 3.x for add-on deployments

## Directory Structure

```
terraform/
├── main.tf                    # Main configuration with module calls
├── variables.tf               # Input variables
├── outputs.tf                 # Output values
├── environments/              # Environment-specific configurations
│   ├── dev.tfvars
│   ├── staging.tfvars
│   └── production.tfvars
└── modules/
    ├── vpc/                   # VPC, subnets, NAT gateways
    ├── eks/                   # EKS cluster and node groups
    ├── rds/                   # PostgreSQL database
    ├── redis/                 # ElastiCache Redis cluster
    ├── msk/                   # Kafka cluster
    └── k8s-addons/            # Kubernetes add-ons (ALB, autoscaler, etc.)
```

## Quick Start

### 1. Initialize Terraform

```bash
cd infrastructure/terraform
terraform init
```

### 2. Select Workspace (Environment)

```bash
# Create and select workspace
terraform workspace new dev
# or select existing
terraform workspace select dev
```

### 3. Plan Infrastructure

```bash
terraform plan -var-file=environments/dev.tfvars
```

### 4. Apply Infrastructure

```bash
terraform apply -var-file=environments/dev.tfvars
```

### 5. Configure kubectl

```bash
aws eks update-kubeconfig --name pizzaflow-dev-eks --region us-east-1
```

## Environment Configurations

### Development (`dev.tfvars`)
- **EKS**: 2 x t3.medium nodes
- **RDS**: db.t3.small (20GB)
- **Redis**: 1 x cache.t3.micro
- **MSK**: 3 x kafka.t3.small (50GB)

### Staging (`staging.tfvars`)
- **EKS**: 3 x t3.large nodes
- **RDS**: db.t3.medium (50GB)
- **Redis**: 2 x cache.t3.small
- **MSK**: 3 x kafka.m5.large (100GB)

### Production (`production.tfvars`)
- **EKS**: 5 x m5.xlarge nodes (auto-scale to 20)
- **RDS**: db.r5.large (100GB) with Multi-AZ
- **Redis**: 3 x cache.r5.large with Multi-AZ
- **MSK**: 6 x kafka.m5.2xlarge (500GB)

## Module Details

### VPC Module
Creates:
- VPC with DNS support
- Public subnets (one per AZ) with Internet Gateway
- Private subnets (one per AZ) with NAT Gateways
- Route tables for public/private traffic
- VPC Flow Logs for network monitoring

### EKS Module
Creates:
- EKS cluster with specified Kubernetes version
- Managed node group with auto-scaling
- IAM roles for cluster and nodes
- OIDC provider for IRSA (IAM Roles for Service Accounts)
- Security groups for cluster/node communication
- CloudWatch logging for control plane

### RDS Module
Creates:
- PostgreSQL 15.x with encryption
- DB subnet group in private subnets
- Security group allowing EKS access
- Parameter group with logging enabled
- Secrets Manager secret for credentials
- Performance Insights enabled
- Optional Multi-AZ for production

### Redis Module
Creates:
- Redis 7.x replication group
- Subnet group in private subnets
- Security group allowing EKS access
- TLS encryption in transit
- Auth token stored in Secrets Manager
- Optional Multi-AZ failover

### MSK Module
Creates:
- Apache Kafka cluster with specified version
- Custom configuration for production workloads
- KMS encryption at rest
- CloudWatch logging
- JMX and Node Exporter for Prometheus
- Connection info in Secrets Manager

### K8s Add-ons Module
Installs:
- **AWS Load Balancer Controller**: Manages ALB/NLB for Ingress
- **Cluster Autoscaler**: Scales nodes based on demand
- **Metrics Server**: Resource metrics for HPA
- **cert-manager**: TLS certificate management
- **External DNS** (optional): Route53 DNS management
- **Prometheus Stack**: Monitoring and alerting

## Accessing Resources

### EKS Cluster
```bash
# Update kubeconfig
aws eks update-kubeconfig --name <cluster-name> --region <region>

# Verify connection
kubectl get nodes
```

### RDS Connection
```bash
# Get credentials from Secrets Manager
aws secretsmanager get-secret-value --secret-id pizzaflow-<env>/rds/credentials

# Connection URL format
jdbc:postgresql://<endpoint>:5432/pizzaflow
```

### Redis Connection
```bash
# Get credentials from Secrets Manager
aws secretsmanager get-secret-value --secret-id pizzaflow-<env>/redis/credentials

# Connection URL format (TLS)
rediss://:<auth-token>@<endpoint>:6379
```

### Kafka Connection
```bash
# Get bootstrap servers from Secrets Manager
aws secretsmanager get-secret-value --secret-id pizzaflow-<env>/msk/connection

# Bootstrap servers for TLS
<broker1>:9094,<broker2>:9094,<broker3>:9094
```

## Cost Optimization

### Development Environment
Approximate monthly cost: **$300-400**
- Use smaller instance types
- Single-AZ deployments
- Minimal storage

### Production Environment
Approximate monthly cost: **$2,000-3,000**
- Multi-AZ for high availability
- Reserved instances for predictable workloads
- Consider Savings Plans for EKS nodes

## Destroying Infrastructure

```bash
# First, remove Kubernetes resources
kubectl delete all --all -A

# Then destroy Terraform resources
terraform destroy -var-file=environments/<env>.tfvars
```

**Warning**: Production environments have deletion protection. Remove protection manually before destroying.

## Troubleshooting

### EKS Nodes Not Joining
```bash
# Check node group status
aws eks describe-nodegroup --cluster-name <name> --nodegroup-name <name>

# Check IAM role permissions
aws iam list-attached-role-policies --role-name <node-role>
```

### RDS Connection Issues
```bash
# Verify security group rules
aws ec2 describe-security-groups --group-ids <rds-sg-id>

# Check network ACLs
aws ec2 describe-network-acls --filters Name=vpc-id,Values=<vpc-id>
```

### MSK Connectivity
```bash
# Test Kafka connectivity from a pod
kubectl run kafka-test --rm -it --image=bitnami/kafka -- \
  kafka-console-producer.sh --broker-list <bootstrap-servers> --topic test
```

## Security Considerations

1. **Encryption**: All data encrypted at rest and in transit
2. **Secrets**: Managed via AWS Secrets Manager
3. **Network**: Private subnets for databases and brokers
4. **IAM**: Fine-grained IRSA for Kubernetes workloads
5. **Logging**: VPC Flow Logs, CloudWatch, audit logs enabled
