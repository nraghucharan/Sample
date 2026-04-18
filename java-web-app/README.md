# Java Web App — Spring Boot on AWS EC2

A full-stack web application built with **Spring Boot 3**, **Thymeleaf**, **Spring Security**, and **Spring Data JPA**, designed to be hosted on **AWS EC2** with **RDS (MySQL or PostgreSQL)**.

---

## Tech Stack

| Layer      | Technology                        |
|------------|-----------------------------------|
| Backend    | Spring Boot 3.2, Java 17          |
| UI         | Thymeleaf, HTML5, CSS3            |
| Security   | Spring Security 6 (BCrypt)        |
| Database   | MySQL / PostgreSQL via AWS RDS     |
| Local DB   | H2 (in-memory, for development)   |
| Hosting    | AWS EC2 (systemd service)         |
| Build      | Maven                             |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/webapp/
│   │   ├── WebAppApplication.java     # Entry point
│   │   ├── config/SecurityConfig.java # Spring Security
│   │   ├── controller/WebController.java
│   │   ├── model/User.java
│   │   ├── repository/UserRepository.java
│   │   └── service/UserService.java
│   └── resources/
│       ├── application.yml            # Base config
│       ├── application-local.yml      # H2 (dev)
│       ├── application-prod.yml       # RDS (production)
│       ├── templates/                 # Thymeleaf HTML
│       └── static/css, js/
scripts/
└── deploy.sh                          # EC2 deployment script
```

---

## Local Development

### Prerequisites
- Java 17+
- Maven 3.8+

### Run locally

```bash
# Clone / open the project, then:
./mvnw spring-boot:run
# App starts at http://localhost:8080
# H2 console at http://localhost:8080/h2-console  (JDBC URL: jdbc:h2:mem:devdb)
```

---

## Build for Production

```bash
./mvnw clean package -P prod -DskipTests
# Output: target/java-web-app-1.0.0.jar
```

---

## AWS Setup Guide

### 1. Launch an EC2 Instance

- **AMI**: Amazon Linux 2023 or Ubuntu 22.04 LTS
- **Instance type**: t3.small (minimum recommended)
- **Security Group rules**:
  - Inbound: TCP 22 (SSH), TCP 8080 (or 80 via nginx)
  - Outbound: All traffic

### 2. Create an RDS Instance

- **Engine**: MySQL 8.0 or PostgreSQL 15
- **Instance class**: db.t3.micro (free tier eligible)
- **VPC**: Same VPC as your EC2 instance
- **Security Group**: Allow TCP 3306 (MySQL) or 5432 (PostgreSQL) **from your EC2 security group only**

### 3. Update `application-prod.yml`

Edit the DB dialect if using PostgreSQL:
```yaml
# Uncomment for PostgreSQL:
# url: jdbc:postgresql://${DB_HOST}:${DB_PORT:5432}/${DB_NAME}?sslmode=require
# database-platform: org.hibernate.dialect.PostgreSQLDialect
```

### 4. Deploy to EC2

```bash
# 1. Build the JAR locally
./mvnw clean package -P prod -DskipTests

# 2. Copy JAR to EC2
scp -i your-key.pem target/java-web-app-1.0.0.jar ec2-user@<EC2_PUBLIC_IP>:~/

# 3. SSH into EC2
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>

# 4. Copy deploy script and run it
chmod +x deploy.sh
./deploy.sh
```

### 5. Set environment variables on EC2

Edit `/etc/systemd/system/java-web-app.service` and fill in your RDS values:
```
Environment="DB_HOST=your-rds-endpoint.rds.amazonaws.com"
Environment="DB_NAME=your_database_name"
Environment="DB_USERNAME=your_user"
Environment="DB_PASSWORD=your_password"
```

Then reload:
```bash
sudo systemctl daemon-reload
sudo systemctl restart java-web-app
```

### 6. (Optional) Put Nginx in front on port 80

```bash
sudo yum install -y nginx
sudo tee /etc/nginx/conf.d/webapp.conf > /dev/null <<EOF
server {
    listen 80;
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
EOF
sudo systemctl enable --now nginx
```

---

## Useful Commands on EC2

```bash
# View live logs
sudo journalctl -u java-web-app -f

# Restart app
sudo systemctl restart java-web-app

# Stop app
sudo systemctl stop java-web-app

# Check health endpoint
curl http://localhost:8080/actuator/health
```

---

## Security Notes

- Passwords are hashed with **BCrypt**
- CSRF protection is enabled by default
- Database credentials are injected via **environment variables** (never hardcoded)
- RDS is in a **private subnet** / security group — accessible only from EC2
- Use **AWS Secrets Manager** or **Parameter Store** in production instead of env vars for better secret management
