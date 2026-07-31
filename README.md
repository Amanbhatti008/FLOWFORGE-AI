<div align="center">
  <img src="docs/assets/logo.png" alt="FlowForge AI Logo" width="120" />

  # FlowForge AI
  
  **A scalable, distributed workflow orchestration and automation platform.**

  [![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
  [![React](https://img.shields.io/badge/React-19.2.7-blue.svg)](https://reactjs.org/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![Kafka](https://img.shields.io/badge/Kafka-Enabled-black.svg)](https://kafka.apache.org/)
  [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)

</div>

## 📖 Overview

FlowForge AI is a distributed workflow orchestration platform designed to help teams design, automate, and monitor complex processes. Powered by a robust Spring Boot backend, an event-driven Kafka architecture, and a modern React frontend, it provides an intuitive visual builder to orchestrate data pipelines, CI/CD tasks, and AI triaging systems.

## ✨ Key Features

- **Visual Workflow Builder**: Intuitive drag-and-drop interface powered by ReactFlow.
- **Distributed Execution**: Fault-tolerant execution engine backed by Apache Kafka and PostgreSQL.
- **Real-Time Monitoring**: Live dashboards with active run tracking and real-time execution status.
- **Scalable Architecture**: Microservices-ready design with Redis for distributed locking and caching.
- **Template Library**: Pre-built templates for CI/CD, Data ETL, and AI triage pipelines.
- **Modern UI/UX**: Premium glassmorphism design with responsive and interactive components.

## 🛠 Tech Stack

### Frontend
- **Framework**: React 19, TypeScript, Vite
- **State & Routing**: React Router
- **UI Components**: ReactFlow (Node based UI), Lucide React (Icons)
- **Styling**: Tailwind CSS / Custom Glassmorphism CSS

### Backend
- **Core**: Java 21, Spring Boot 3.3.4
- **Database**: PostgreSQL (managed via Flyway)
- **Messaging**: Apache Kafka
- **Caching & Locks**: Redis (via Redisson)
- **Security**: JWT Authentication, Spring Security

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Orchestration**: Kubernetes manifests included (`/k8s`)
- **Observability**: Prometheus & Grafana integrations

## 📂 Project Structure

```text
flowforge-ai/
├── backend/          # Spring Boot application (REST API, Workflow Engine)
├── frontend/         # React/Vite web application (UI, Dashboard, Builder)
├── docker/           # Docker Compose setups for Postgres, Redis, Kafka, etc.
├── k8s/              # Kubernetes deployment manifests
└── docs/             # Architecture Decision Records (ADRs) and documentation
```

## 🚀 Getting Started

### Prerequisites
- [Docker & Docker Compose](https://docs.docker.com/get-docker/)
- [Java 21](https://adoptium.net/) (for local backend development)
- [Node.js 20+](https://nodejs.org/) (for local frontend development)

### Running Locally with Docker (Full Stack)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/flowforge-ai.git
   cd flowforge-ai
   ```

2. **Start Infrastructure Services:**
   ```bash
   cd docker
   docker-compose up -d
   ```
   *This starts PostgreSQL, Redis, Kafka, Prometheus, and Grafana.*

3. **Start Backend Server:**
   ```bash
   cd ../backend
   ./mvnw spring-boot:run
   ```

4. **Start Frontend Client:**
   ```bash
   cd ../frontend
   npm install
   npm run dev
   ```

5. **Access the Application:**
   Open `http://localhost:5173` in your browser.

## 📸 Screenshots

| Login Page | Dashboard |
|:---:|:---:|
| ![Login Page](docs/assets/login.png) | ![Dashboard](docs/assets/dashboard.png) |

| Workflow Builder | Execution Viewer |
|:---:|:---:|
| ![Workflow Builder](docs/assets/builder.png) | *(Execution Viewer tracks live telemetry)* |

## 🔮 Future Improvements

- [ ] Add support for custom Python execution nodes.
- [ ] Implement advanced RBAC (Role-Based Access Control) for teams.
- [ ] Introduce a plugin marketplace for community-driven nodes.
- [ ] Comprehensive E2E testing suite using Playwright or Cypress.

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on how to get started. By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## 🛡️ Security

If you discover a security vulnerability within this project, please refer to our [Security Policy](SECURITY.md) for reporting guidelines.

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---
*Built with ❤️ by Aman Bhatti*
