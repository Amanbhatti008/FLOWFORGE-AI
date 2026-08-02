# Deployment Guide

FlowForge AI uses Kubernetes for orchestration in production environments.

## Local Deployment (Minikube)
Follow instructions in the `README.md` to deploy locally using Minikube. The manifest files in `/k8s` include all necessary services.

## Cloud Deployment (Google Kubernetes Engine - GKE)
You can deploy FlowForge AI on the GKE Free Tier:

1. Setup Google Cloud CLI (`gcloud`).
2. Create a small cluster:
   `gcloud container clusters create flowforge-cluster --num-nodes=1 --zone=us-central1-a`
3. Connect kubectl:
   `gcloud container clusters get-credentials flowforge-cluster`
4. Apply the K8s manifests:
   `kubectl apply -f k8s/`
5. Monitor deployment:
   `kubectl get pods`

## CI/CD
Continuous Integration is managed via GitHub Actions.
- **ci.yml**: Builds Java and Node projects on every commit to main. Runs JUnit tests and builds Docker images.
- **deploy.yml**: Automatically triggers a K8s deployment when CI passes.
