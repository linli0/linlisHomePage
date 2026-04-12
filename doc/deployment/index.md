# Deployment Documentation

## 🚀 Deployment Options

CoffeeCookie's HomePage supports multiple deployment scenarios from local development to production environments.

### Local Development
- **Backend**: Spring Boot embedded server (port 8080)
- **Frontend**: Vite development server (port 3000) with proxy to backend
- **Database**: H2 in-memory database
- **AI Service**: Ollama running locally on default port

### Production Deployment
- **Monolithic**: Single Spring Boot JAR serving both backend API and frontend static files
- **Containerized**: Docker Compose with separate frontend and backend containers
- **Cloud**: Cloudflare Tunnel for public access with automatic HTTPS

## 🔧 Prerequisites

### System Requirements
- **Java**: JDK 17 or higher
- **Node.js**: Version 20 or higher  
- **Maven**: Version 3.8 or higher
- **npm**: Version 8 or higher
- **Docker**: Version 20.10 or higher (for containerized deployment)
- **Ollama**: Latest version (for AI features)

### Environment Variables

#### Backend Required Variables
```bash
# JWT Secret (required in production)
JWT_SECRET=your-very-secure-jwt-secret-here

# MetalpriceAPI Configuration (for gold price tracking)
METALPRICE_API_KEY=your-metalpriceapi-key
METALPRICE_ENABLED=true
METALPRICE_SERVER=us  # or eu

# Database Configuration (production only)
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/homepage
SPRING_DATASOURCE_USERNAME=homepage_user
SPRING_DATASOURCE_PASSWORD=secure_password
```

#### Frontend Optional Variables
```bash
# API Base URL (defaults to /api for production, http://localhost:8080/api for development)
VITE_API_BASE_URL=/api
```

## 📦 Build Process

### Full Build Script
The project includes a comprehensive build script that handles all deployment scenarios:

```bash
# Windows
start.bat

# Linux/macOS  
./start.sh
```

### Manual Build Steps

#### Step 1: Build Frontend
```bash
cd frontend
npm install
npm run build
# Output: frontend/dist/ directory
```

#### Step 2: Build Backend
```bash
cd backend
mvn clean package -DskipTests
# Output: backend/target/homepage-backend-1.0.0.jar
```

#### Step 3: Run Application
```bash
java -jar backend/target/homepage-backend-1.0.0.jar
```

## 🐳 Docker Deployment

### Docker Compose Setup
The project includes a `docker-compose.yml` file for containerized deployment:

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: homepage
      MYSQL_USER: homepage
      MYSQL_PASSWORD: homepagepassword
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"

  backend:
    build: ./backend
    depends_on:
      - mysql
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JWT_SECRET=your-jwt-secret
      - METALPRICE_API_KEY=your-api-key
      - METALPRICE_ENABLED=true
    ports:
      - "8080:8080"

volumes:
  mysql_data:
```

### Build and Run
```bash
# Build images
docker-compose build

# Start services
docker-compose up -d

# View logs
docker-compose logs -f
```

## ☁️ Cloudflare Tunnel Deployment

### Automatic Tunnel Setup
The one-click deployment script automatically sets up Cloudflare Tunnel:

```bash
# The start.bat script handles this automatically
# It downloads cloudflared if not present
# Creates tunnel configuration
# Starts tunnel pointing to localhost:8080
```

### Manual Tunnel Setup

#### Step 1: Install Cloudflared
```bash
# Windows (via Winget)
winget install Cloudflare.cloudflared

# macOS (via Homebrew)  
brew install cloudflare/cloudflare/cloudflared

# Linux
curl -L --output cloudflared https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64
chmod +x cloudflared
sudo mv cloudflared /usr/local/bin/
```

#### Step 2: Start Tunnel
```bash
# Windows
cloudflared tunnel --url http://localhost:8080

# Linux/macOS
nohup cloudflared tunnel --url http://localhost:8080 > tunnel.log 2>&1 &
```

#### Step 3: Access Application
- **Local**: http://localhost:8080
- **Public**: https://[random-subdomain].trycloudflare.com

## 🏗️ Infrastructure Requirements

### Hardware Specifications

#### Minimum (Development)
- **CPU**: 2 cores
- **RAM**: 4 GB
- **Storage**: 2 GB free space
- **Network**: 10 Mbps

#### Recommended (Production)
- **CPU**: 4+ cores
- **RAM**: 8+ GB  
- **Storage**: 10+ GB SSD
- **Network**: 100+ Mbps

### Software Dependencies

#### Operating Systems
- **Windows**: 10/11 (64-bit)
- **Linux**: Ubuntu 20.04+, CentOS 7+, or any modern distribution
- **macOS**: 12.0 Monterey or higher

#### Runtime Dependencies
- **Java**: OpenJDK 17 or Oracle JDK 17
- **Node.js**: LTS version (20.x recommended)
- **Database**: MySQL 8.0 (production) or H2 (development)
- **AI Service**: Ollama (optional, for AI features)

## 🔒 Security Configuration

### Production Security Checklist

#### Backend Security
- [ ] Set strong `JWT_SECRET` environment variable
- [ ] Enable HTTPS/TLS termination
- [ ] Configure proper CORS origins
- [ ] Set up database connection pooling
- [ ] Enable application logging with log rotation
- [ ] Configure rate limiting for API endpoints

#### Frontend Security  
- [ ] Enable Content Security Policy (CSP)
- [ ] Implement proper error handling (no stack traces in production)
- [ ] Minify and obfuscate JavaScript
- [ ] Remove development-only code and comments

#### Network Security
- [ ] Use firewall to restrict access to necessary ports only
- [ ] Implement proper SSL/TLS certificates
- [ ] Regular security updates for all dependencies
- [ ] Monitor for suspicious activity

## 📊 Monitoring and Logging

### Application Logs
- **Backend**: Structured JSON logs with request correlation IDs
- **Frontend**: Console logs (removed in production builds)
- **System**: Docker/container logs for containerized deployments

### Health Checks
- **Backend**: `/actuator/health` endpoint
- **Frontend**: Static file serving health check
- **Database**: Connection pool health monitoring

### Performance Metrics
- **Response Times**: API endpoint response times
- **Memory Usage**: JVM heap usage and garbage collection
- **Database Queries**: Query execution times and connection counts
- **User Sessions**: Active user count and session duration

## 🔄 Update and Maintenance

### Application Updates
1. **Backup**: Create backup of database and configuration
2. **Stop**: Gracefully stop current application instance
3. **Update**: Pull latest code and rebuild
4. **Start**: Start updated application
5. **Verify**: Check health endpoints and functionality

### Database Migrations
- **Development**: Automatic schema updates (`ddl-auto: update`)
- **Production**: Manual schema management with proper backup strategy
- **Version Control**: Database schema changes tracked in version control

### Dependency Updates
- **Security Scanning**: Regular dependency vulnerability scanning
- **Version Pinning**: Specific versions pinned in package.json and pom.xml
- **Testing**: Full test suite execution after dependency updates

## 🆘 Troubleshooting

### Common Issues and Solutions

#### Application Won't Start
- **Check Java Version**: Ensure JDK 17+ is installed
- **Check Port Conflicts**: Ensure port 8080 is available
- **Check Dependencies**: Verify all npm and Maven dependencies are installed
- **Check Environment Variables**: Ensure required env vars are set

#### Database Connection Issues
- **H2 (Development)**: Verify H2 console is accessible at `/h2-console`
- **MySQL (Production)**: Check connection string, credentials, and network access
- **Connection Pool**: Monitor for connection leaks and pool exhaustion

#### Gold Price Data Not Updating
- **API Key**: Verify `METALPRICE_API_KEY` is valid and set
- **API Enabled**: Ensure `METALPRICE_ENABLED=true`
- **Network Access**: Check firewall/proxy settings for API access
- **Rate Limits**: Monitor for API rate limit exceeded errors

#### AI Features Not Working
- **Ollama Running**: Ensure Ollama service is running on default port (11434)
- **Model Downloaded**: Verify required models are downloaded in Ollama
- **Network Access**: Check if frontend can reach Ollama endpoint

### Support Resources
- **Documentation**: This deployment guide and other docs in `doc/` directory
- **Logs**: Application logs for detailed error information
- **Community**: GitHub issues for bug reports and feature requests
- **Professional Support**: Available for enterprise deployments

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] All tests pass (`mvn test && npm test`)
- [ ] Environment variables configured
- [ ] Database backup created (if migrating existing data)
- [ ] SSL certificates ready (for production)
- [ ] Monitoring and alerting configured

### Post-Deployment  
- [ ] Application health checks pass
- [ ] All features working as expected
- [ ] Performance metrics within acceptable ranges
- [ ] Security scans completed
- [ ] Backup and recovery procedures tested

This deployment documentation ensures consistent, reliable deployments across all environments while maintaining security and performance standards.