# RoboHire Backend - Industrial Standard Spring Boot Application

## Project Structure

```
src/main/java/com/robohire/
├── config/
│   ├── AppConfig.java          # RestTemplate Bean Configuration
│   └── CorsConfig.java          # CORS Configuration for Frontend
├── controller/
│   ├── UserController.java      # Authentication Endpoints
│   └── InterviewController.java # Interview Management Endpoints
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── UserResponse.java
│   ├── InterviewRequest.java
│   ├── QuestionResponse.java
│   ├── AnswerSubmission.java
│   └── FeedbackReport.java
├── exception/
│   ├── ApiException.java
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── model/
│   ├── User.java               # User Entity
│   └── Interview.java          # Interview Entity
├── repository/
│   ├── UserRepository.java     # JPA Repository for User
│   └── InterviewRepository.java # JPA Repository for Interview
├── service/
│   ├── UserService.java        # Authentication Business Logic
│   ├── InterviewService.java   # Interview Business Logic
│   └── GeminiService.java      # Gemini API Integration
└── RoboHireApplication.java    # Main Application Class
```

## Database Setup

1. Install MySQL Server
2. Create database (auto-created by application):
```sql
CREATE DATABASE robohire_db;
```

3. Update credentials in `application.yaml`:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

## API Endpoints

### Authentication APIs
- POST `/api/auth/register` - User Registration
- POST `/api/auth/login` - User Login

### Interview APIs
- POST `/api/interview/generate-questions` - Generate AI Questions
- POST `/api/interview/submit-answers` - Submit Answers & Get Feedback

## Environment Variables

Set Gemini API Key:
```bash
export GEMINI_API_KEY=your-gemini-api-key
```

## Run Application

```bash
mvn clean install
mvn spring-boot:run
```

Backend runs on: http://localhost:8080

## Frontend Connection

CORS configured for: http://localhost:3000
