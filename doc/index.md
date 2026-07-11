# CoffeeCookie's HomePage Documentation

Welcome to the comprehensive documentation for CoffeeCookie's HomePage project.

## 📚 Documentation Structure

- **[Architecture](architecture/)** - System architecture and design decisions
- **[Backend](backend/)** - Spring Boot backend implementation details
- **[Frontend](frontend/)** - Vue 3 frontend implementation details  
- **[Design](design/)** - UI/UX design system and guidelines
- **[Testing](testing/)** - Test frameworks, strategies, and results
- **[Deployment](deployment/)** - Deployment guides and production setup

## 🚀 Quick Start

### Local Development
```bash
# Build and start backend
cd backend && mvn spring-boot:run

# Build and start frontend (development mode)
cd frontend && npm run dev
```

### Production Deployment
```bash
# One-click deployment
start.bat

# Manual deployment
cd frontend && npm install && npm run build && cd ..
cd backend && mvn clean package -DskipTests && java -jar target/*.jar
```

## 🔧 Project Overview

CoffeeCookie's HomePage is a full-stack personal homepage application featuring:

- **💰 Gold Price Tracking**: Real-time international gold price monitoring with historical charts
- **📝 Article Management**: Markdown-based article publishing with categories and tags
- **🛠️ Utility Tools**: JSON formatter, Base64 encoder/decoder, hash calculator, QR code generator
- **💬 AI Chat**: Local LLM integration with Ollama for AI conversations
- **🔐 User Authentication**: JWT-based authentication with role-based access control
- **📊 Tweets Monitor**: Real-time Twitter/Truth Social monitoring with WebSocket integration
- **🔊 Xiaomi Speaker**: Text-to-speech and AI chat integration with Xiaomi smart speakers
- **📈 Quant Trading**: Backtesting framework, trading signals, and technical indicators analysis

## 📋 Default Credentials

- **Admin**: `admin` / `admin123`
- **User**: `user` / `user123`

## 📞 Support

For questions or issues, please refer to the relevant documentation sections above or check the project's issue tracker.