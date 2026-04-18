#!/bin/bash
# =====================================================================
# deploy.sh — Deploy Spring Boot app to AWS EC2
# Run this on your EC2 instance (Amazon Linux 2023 / Ubuntu)
# =====================================================================

set -e

APP_NAME="java-web-app"
JAR_FILE="target/${APP_NAME}-1.0.0.jar"
APP_DIR="/opt/${APP_NAME}"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"

echo "=== Step 1: Install Java 17 ==="
# Amazon Linux 2023
sudo yum install -y java-17-amazon-corretto-headless || \
# Ubuntu
sudo apt-get install -y openjdk-17-jre-headless

echo "=== Step 2: Create app directory ==="
sudo mkdir -p $APP_DIR
sudo cp $JAR_FILE $APP_DIR/app.jar

echo "=== Step 3: Create systemd service ==="
sudo tee $SERVICE_FILE > /dev/null <<EOF
[Unit]
Description=${APP_NAME} Spring Boot Application
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=${APP_DIR}
ExecStart=/usr/bin/java -jar ${APP_DIR}/app.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=${APP_NAME}

# === AWS RDS Environment Variables ===
# Replace these with your actual RDS values
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DB_HOST=your-rds-endpoint.rds.amazonaws.com"
Environment="DB_PORT=3306"
Environment="DB_NAME=your_database_name"
Environment="DB_USERNAME=your_db_user"
Environment="DB_PASSWORD=your_db_password"
Environment="SERVER_PORT=8080"

[Install]
WantedBy=multi-user.target
EOF

echo "=== Step 4: Enable and start service ==="
sudo systemctl daemon-reload
sudo systemctl enable $APP_NAME
sudo systemctl restart $APP_NAME

echo "=== Step 5: Check status ==="
sudo systemctl status $APP_NAME

echo ""
echo "✅ Deployment complete!"
echo "   View logs: sudo journalctl -u ${APP_NAME} -f"
echo "   App URL:   http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
