# AI-Log-Analysis-Dashboard
A full-stack, AI-powered log analysis dashboard that ingests application logs, detects error patterns, clusters failures, and generates intelligent debugging insights using an LLM (GPT-4o-mini or compatible model).

# Overview

Backend systems generate massive volumes of log data. Manually searching for root causes, identifying recurring issues, or spotting failure patterns is slow and error-prone.

AI-Log-Analysis-Dashboard solves this by providing a centralized platform that:

Ingests raw log text

Parses and stores logs in PostgreSQL

Sends logs to a Python FastAPI AI microservice

Uses an LLM to detect error clusters and root causes

Generates human-readable explanations and fixes

Displays everything in a clean React UI

This project showcases microservice communication, backend engineering, database design, and AI integration with LLMs.

# Features
🔹 Log Ingestion

Paste or enter log lines directly in the dashboard

Automatic parsing: timestamp, log level, service name, and message

Logs stored in PostgreSQL for persistence and querying

🔹 AI-Powered Log Analysis

The FastAPI service uses an LLM to:

Cluster repeating log patterns

Identify failure types

Explain root causes

Recommend actionable fixes

Return responses in strict JSON format

🔹 Interactive Dashboard

Clean modern UI for ingesting and viewing logs

One-click log analysis

Error clusters displayed as cards

Log table for viewing all stored entries

🔹 Scalable Architecture

Modular microservice design

Independent deployable components

LLM can be swapped (OpenAI, Azure, local LLMs like Ollama)

# Tech Stack

- Frontend

- React (Vite)

- JavaScript

- Fetch API

- Custom CSS styling

- Backend

- Java + Spring Boot

- Spring MVC, Spring Data JPA

- WebClient (to call FastAPI service)

- AI Service

- Python FastAPI

- OpenAI API / GPT-4o-mini

- JSON structured responses

- Database

- PostgreSQL

# Project Structure
```
AI-Log-Analysis-Dashboard/  
│
├── backend/          # Spring Boot Application
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── model/
│   └── resources/application.properties
│
├── ai-service/       # FastAPI + LLM Integration
│   ├── main.py
│   └── requirements.txt
│
└── frontend/         # React, Vite UI
    ├── src/App.jsx
    └── src/App.css
```
# Setup Instructions

## Clone the Repository
```
cd AI-Log-Analysis-Dashboard
```

## Configure PostgreSQL

Create the database:

```
CREATE DATABASE log_intel;
```

Add your postgres username and password in backend/src/resources/application.properties:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/log_intel
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Start the Spring Boot Backend
```
cd backend
mvn spring-boot:run
```

Backend URL: http://localhost:8080

## Start the AI Service (FastAPI)

### Install dependencies:
```
cd ai-service
pip install -r requirements.txt
```

### Set your OpenAI API key:
```
export OPENAI_API_KEY="your_openai_key_here"
```

### Start service:
```
uvicorn main:app --reload --port 8001
```

FastAPI URL: http://localhost:8001

## Start the React Frontend
```
cd frontend
npm install
npm run dev
```

Frontend URL: http://localhost:5173

## Testing the System

✔ Ingest Logs

Paste logs → Click Ingest Logs

✔ Analyze Logs

Click Analyze Logs → See AI generated clusters and explanations

✔ View Stored Logs

## Example Logs

2025-12-05 10:15:30 ERROR OrderService - Failed to connect to database

2025-12-05 10:15:45 ERROR AuthService - User authentication failed

2025-12-05 10:16:00 INFO OrderService - Retrying database connection

2025-12-05 10:16:30 ERROR OrderService - Timeout occurred while querying orders

## How the AI Analyzer Works

- Spring Boot fetches logs from PostgreSQL

- Sends logs to FastAPI

- FastAPI prepares a JSON-only prompt

- GPT-4o-mini analyzes logs and returns:

- Error clusters

- Explanations

- Suggested fixes

- Spring Boot sends results to frontend

- React displays AI insights in the dashboard

- Bottom table shows parsed logs with timestamp, level, service, message
