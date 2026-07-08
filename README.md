# OpenAgent

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0--SNAPSHOT-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-green)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/license-MIT-yellow)

**Enterprise-grade AI Agent Solutions for 23 Vertical Domains**

Hybrid AI Architecture based on Spring Boot + LangChain4J + Spring AI 2.0 + Spring AI Alibaba

</div>

---

## 📖 Project Introduction

OpenAgent is a complete vertical domain AI Agent system covering 23 industry scenarios including healthcare, agriculture, logistics, energy, finance, and manufacturing. It adopts a **hybrid AI architecture** that seamlessly integrates three major AI frameworks:

- **LangChain4J** - Light-weight, high-performance, suitable for complex workflow orchestration
- **Spring AI 2.0** - Native to Spring ecosystem, enterprise application of choice
- **Spring AI Alibaba** - Deep adaptation for domestic large models, supporting Alibaba Cloud DashScope

---

## 🎯 Core Features

### Hybrid AI Architecture
- ✅ Seamless switching and integration of three frameworks
- ✅ Intelligent framework selection and load balancing
- ✅ Automatic fault tolerance and degradation mechanism
- ✅ Parallel invocation and answer aggregation

### Unified Service Layer
- ✅ `HybridAIService` - Unified chat interface
- ✅ `HybridRAGService` - Hybrid retrieval-augmented generation
- ✅ `AIEmbeddingService` - Vector embedding service

### Enterprise Capabilities
- ✅ Vector storage with PostgreSQL + pgvector
- ✅ Redis caching and session management
- ✅ Kafka message queue
- ✅ MyBatis Plus data persistence
- ✅ Actuator monitoring and health checks
- ✅ OpenAPI/Swagger documentation

---

## 📁 Project Structure

```
openagent/
├── 01-docs/                           # Project documentation
│   ├── AI Agent Project Structure Guide.md
│   ├── Spring-AI Integration Guide.md
│   ├── Hybrid AI Architecture Integration Guide.md
│   ├── PostgreSQL Vector Database Configuration Guide.md
│   └── ...
│
├── 02-scripts/                         # Scripts directory
│   ├── db-init-*.sql                  # Database initialization scripts
│   └── ...
│
├── agent-core/                        # Core framework module
│   ├── src/main/java/com/qoobot/agent/core/
│   │   ├── langchain4j/              # LangChain4J integration
│   │   ├── springai/                 # Spring AI integration
│   │   ├── hybrid/                   # Hybrid architecture services
│   │   └── ...
│   └── pom.xml
│
├── drug-research-agent/               # 1. Drug Research Collaboration Agent
├── medical-diagnosis-agent/           # 2. Intelligent Medical Diagnosis Agent
├── medical-device-agent/              # 3. Medical Device Collaboration Agent
├── agricultural-pest-agent/           # 4. Agricultural Pest Prediction Agent
├── cross-border-logistics-agent/      # 5. Cross-border Logistics Optimization Agent
├── nuclear-maintenance-agent/         # 6. Nuclear Equipment Predictive Maintenance Agent
├── power-grid-agent/                  # 7. Power Grid Load Forecasting Agent
├── semiconductor-optimization-agent/  # 8. Semiconductor Process Optimization Agent
├── financial-pricing-agent/           # 9. Financial Dynamic Pricing Agent
├── warehouse-scheduling-agent/        # 10. Intelligent Warehouse Scheduling Agent
├── construction-safety-agent/         # 11. Construction Safety Inspection Agent
├── supply-chain-agent/                # 12. Supply Chain Resilience Agent
├── customer-service-agent/            # 13. Intelligent Customer Service Agent
├── industrial-quality-agent/          # 14. Industrial Quality Inspection Agent
├── intelligent-traffic-agent/         # 15. Intelligent Traffic Management Agent
├── marketing-planning-agent/          # 16. Intelligent Marketing Planning Agent
├── intelligent-ops-agent/             # 17. Intelligent Operations Management Agent
├── investment-advisor-agent/          # 18. Intelligent Investment Advisor Agent
├── power-trading-agent/               # 19. Intelligent Power Trading Agent
├── legal-agent/                       # 20. Legal Consultation Agent ⭐
├── education-agent/                   # 21. Education and Training Agent ⭐
├── retail-agent/                      # 22. E-commerce Retail Agent ⭐
├── hr-agent/                          # 23. Human Resources Agent ⭐
│
├── pom.xml                            # Parent POM configuration
├── docker-compose.yml                 # Docker orchestration configuration
├── README.md                          # English documentation (this document)
└── README_CN.md                       # Chinese documentation
```

---

## 🏗️ Technology Stack

### Core Frameworks
| Technology | Version | Description |
|------------|---------|-------------|
| Java | 21 | Programming Language |
| Spring Boot | 3.5.10 | Application Framework |
| Maven | 3.9.0+ | Build Tool |

### AI/LLM Frameworks
| Framework | Version | Description |
|-----------|---------|-------------|
| LangChain4J | 0.34.0 | Lightweight AI Orchestration Framework |
| Spring AI | 1.0.0-M5 | Native Spring AI Framework |
| Spring AI Alibaba | 1.0.0-M5.2 | Alibaba Cloud DashScope Integration |
| OpenAI API | - | GPT-4 / GPT-3.5 |
| Alibaba Cloud DashScope | - | Qwen / Tongyi Qianwen |

### Data Storage
| Technology | Version | Purpose |
|------------|---------|---------|
| PostgreSQL | 16+ | Primary Database + Vector Storage (pgvector) |
| MySQL | 8.0+ | Alternative Database |
| Redis | 7.0+ | Cache / Session Management |
| MongoDB | 6.0+ | Document Storage |
| Neo4j | 5.0+ | Knowledge Graph |
| InfluxDB | 2.0+ | Time Series Database |
| Milvus | 2.3+ | Vector Database (Optional) |

### Messaging & Queues
| Technology | Version | Purpose |
|------------|---------|---------|
| Apache Kafka | 3.5+ | Message Queue |
| RabbitMQ | 3.12+ | Task Queue |

### Data Access
| Technology | Version | Description |
|------------|---------|-------------|
| MyBatis Plus | 3.5.7 | ORM Framework |
| Spring Data | - | Redis / Mongo / Neo4j |

### Monitoring & Observability
| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot Actuator | 3.x | Monitoring Endpoints |
| Micrometer | 1.12+ | Metrics Collection |
| Prometheus | 2.47+ | Metrics Storage |
| Zipkin | 2.25+ | Tracing |

### API Documentation
| Technology | Version | Purpose |
|------------|---------|---------|
| SpringDoc OpenAPI | 2.3.0 | API Documentation Generation |

---

## 🚀 Quick Start

### 1. Environment Requirements

- JDK 21+
- Maven 3.9.0+
- PostgreSQL 16+ (with pgvector extension)
- Redis 7.0+
- Docker & Docker Compose (optional)

### 2. Clone Project

```bash
git clone https://github.com/qoobots/openagent.git
cd openagent
```

### 3. Start Basic Services

Quickly start PostgreSQL, Redis, and Kafka using Docker Compose:

```bash
docker-compose up -d
```

### 4. Initialize Database

```bash
# Method 1: Batch initialize all databases
cd 02-scripts
for file in db-init-*.sql; do
  echo "Initializing $file..."
  psql -U postgres -h localhost -f "$file"
done

# Method 2: Initialize single database
psql -U postgres -h localhost -f db-init-drug-research-agent.sql
```

### 5. Configure Environment Variables

```bash
# LangChain4J OpenAI
export LANGCHAIN4J_OPENAI_API_KEY=sk-...

# Spring AI OpenAI
export OPENAI_API_KEY=sk-...

# Spring AI Alibaba (Domestic deployment)
export DASHSCOPE_API_KEY=sk-...

# PostgreSQL
export DB_USERNAME=postgres
export DB_PASSWORD=123456
```

### 6. Build Project

```bash
# Build all modules
mvn clean install

# Build specific module
cd drug-research-agent
mvn clean install
```

### 7. Start Agent

```bash
# Start Drug Research Agent
cd drug-research-agent
mvn spring-boot:run

# Or run Application.java using IDE
```

### 8. Access Services

| Service | Address |
|---------|---------|
| Swagger UI | http://localhost:8080/drug-research-agent/swagger-ui.html |
| Health Check | http://localhost:8080/drug-research-agent/actuator/health |
| Actuator | http://localhost:8080/drug-research-agent/actuator |

---

## 📦 Agent List

| No. | Agent | Port | Path | Database | Description |
|-----|-------|------|------|----------|-------------|
| 1 | drug-research-agent | 8080 | /drug-research-agent | openagent_drug_research_agent | Drug Research Collaboration |
| 2 | medical-diagnosis-agent | 8080 | /medical-diagnosis-agent | openagent_medical_diagnosis_agent | Intelligent Medical Diagnosis |
| 3 | medical-device-agent | 8080 | /medical-device-agent | openagent_medical_device_agent | Medical Device Collaboration |
| 4 | agricultural-pest-agent | 8080 | /agricultural-pest-agent | openagent_agricultural_pest_agent | Agricultural Pest Prediction |
| 5 | cross-border-logistics-agent | 8080 | /cross-border-logistics-agent | openagent_cross_border_logistics_agent | Cross-border Logistics Optimization |
| 6 | nuclear-maintenance-agent | 8080 | /nuclear-maintenance-agent | openagent_nuclear_maintenance_agent | Nuclear Equipment Predictive Maintenance |
| 7 | power-grid-agent | 8080 | /power-grid-agent | openagent_power_grid_agent | Power Grid Load Forecasting |
| 8 | semiconductor-optimization-agent | 8080 | /semiconductor-optimization-agent | openagent_semiconductor_optimization_agent | Semiconductor Process Optimization |
| 9 | financial-pricing-agent | 8080 | /financial-pricing-agent | openagent_financial_pricing_agent | Financial Dynamic Pricing |
| 10 | warehouse-scheduling-agent | 8080 | /warehouse-scheduling-agent | openagent_warehouse_scheduling_agent | Intelligent Warehouse Scheduling |
| 11 | construction-safety-agent | 8080 | /construction-safety-agent | openagent_construction_safety_agent | Construction Safety Inspection |
| 12 | supply-chain-agent | 8080 | /supply-chain-agent | openagent_supply_chain_agent | Supply Chain Resilience |
| 13 | customer-service-agent | 8080 | /customer-service-agent | openagent_customer_service_agent | Intelligent Customer Service |
| 14 | industrial-quality-agent | 8080 | /industrial-quality-agent | openagent_industrial_quality_agent | Industrial Quality Inspection |
| 15 | intelligent-traffic-agent | 8080 | /intelligent-traffic-agent | openagent_intelligent_traffic_agent | Intelligent Traffic Management |
| 16 | marketing-planning-agent | 8080 | /marketing-planning-agent | openagent_marketing_planning_agent | Intelligent Marketing Planning |
| 17 | intelligent-ops-agent | 8080 | /intelligent-ops-agent | openagent_intelligent_ops_agent | Intelligent Operations Management |
| 18 | investment-advisor-agent | 8080 | /investment-advisor-agent | openagent_investment_advisor_agent | Intelligent Investment Advisor |
| 19 | power-trading-agent | 8080 | /power-trading-agent | openagent_power_trading_agent | Intelligent Power Trading |
| 20 | legal-agent | 8201 | /legal-agent | openagent_legal_agent | Legal Consultation ⭐ |
| 21 | education-agent | 8202 | /education-agent | openagent_education_agent | Education and Training ⭐ |
| 22 | retail-agent | 8203 | /retail-agent | openagent_retail_agent | E-commerce Retail ⭐ |
| 23 | hr-agent | 8204 | /hr-agent | openagent_hr_agent | Human Resources ⭐ |

---

## 💻 Usage Examples

### Example 1: Using Hybrid AI Service

```java
@Service
public class YourAgentService {

    @Autowired
    private HybridAIService hybridAI;

    @Autowired
    private HybridRAGService hybridRAG;

    // Simple chat (automatic framework selection)
    public String chat(String message) {
        return hybridAI.chat("your-agent-id", message);
    }

    // RAG Query
    public String query(String query) {
        return hybridRAG.retrieveAndGenerate(query, "your-agent-id");
    }

    // Parallel call (fastest response)
    public String fastQuery(String question) {
        return hybridAI.chatParallel(question);
    }
}
```

### Example 2: Configuring Framework Switching Strategy

```yaml
agent:
  ai:
    switch-strategy: fallback  # Recommended
    langchain4j:
      weight: 0.3
      enabled: true
    spring-ai:
      weight: 0.4
      enabled: true
      provider: openai
    spring-ai-alibaba:
      weight: 0.3
      enabled: true
```

**Supported Switching Strategies:**
- `ALWAYS` - Always use default framework
- `FALLBACK` - Fallback to backup framework on failure
- `LOAD_BALANCE` - Load balance by weight
- `AGENT_SPECIFIC` - Select based on Agent type
- `TASK_AWARE` - Intelligent selection based on task type

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [AI Agent Project Structure Guide](01-docs/AI%20Agent项目结构说明.md) | Project structure and architecture design |
| [Spring-AI Integration Guide](01-docs/Spring-AI集成指南.md) | Spring AI 2.0 + Spring AI Alibaba integration |
| [Hybrid AI Architecture Integration Guide](01-docs/混合AI架构集成指南.md) | Detailed explanation of three-framework hybrid architecture |
| [PostgreSQL Vector Database Configuration Guide](01-docs/PostgreSQL向量数据库配置指南.md) | pgvector configuration and usage |
| [Vertical Domain AI Agent Business Design](01-docs/垂直领域AI%20Agent商业设计.md) | Business design and scenario analysis |

---

## 🔧 Configuration Guide

### Database Configuration

Each Agent uses an independent PostgreSQL database:

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/openagent_{agent_id}
    username: postgres
    password: 123456
```

### Redis Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password: ${REDIS_PASSWORD:}
```

### Kafka Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ${spring.application.name}-group
```

---

## 🛠️ Development Guide

### Create New Agent

Quickly create new Agent using Python script:

```bash
python create_agent_modules.py your-new-agent
```

Or create manually:
1. Create Agent directory structure
2. Configure `pom.xml`
3. Create `Application.java`
4. Configure `application.yml`
5. Create database initialization script
6. Declare module in parent `pom.xml`

### Add Dependencies

```xml
<dependency>
    <groupId>com.qoobot</groupId>
    <artifactId>agent-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 📊 Dependencies

```
openagent (Parent POM)
  ├── agent-core (Core Framework)
  │   ├── LangChain4J Integration
  │   ├── Spring AI Integration
  │   └── Hybrid Architecture Services
  └── 23 Business Agents (depend on agent-core)
```

---

## 🔐 Security & Compliance

- ✅ Sensitive information managed through environment variables
- ✅ API Keys not committed to code repository
- ✅ Supports HTTPS encrypted communication
- ✅ SQL injection protection
- ✅ XSS attack protection

---

## 📈 Monitoring & Operations

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health Check |
| `/actuator/metrics` | Performance Metrics |
| `/actuator/info` | Application Info |
| `/actuator/prometheus` | Prometheus Metrics |

### Logging Configuration

```yaml
logging:
  level:
    com.qoobot.agent: DEBUG
    org.springframework.ai: DEBUG
    dev.langchain4j: DEBUG
```

---

## 🤝 Contribution Guide

Welcome to participate in this project development, contribute code or make suggestions!

1. Fork this repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

---

## 📧 Contact

- Project Homepage: https://github.com/qoobots/openagent
- Issue Tracker: https://github.com/qoobots/openagent/issues

---

## 🙏 Acknowledgements

Thanks to the following open-source projects:

- [Spring Boot](https://spring.io/projects/spring-boot)
- [LangChain4J](https://github.com/langchain4j/langchain4j)
- [Spring AI](https://github.com/spring-projects/spring-ai)
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
- [pgvector](https://github.com/pgvector/pgvector)

---

<div align="center">

**Made with ❤️ by Qoobot Team**

</div>