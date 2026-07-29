# 📸 Smart Classroom Attendance System using AWS Serverless, IaC & CI/CD

## Overview

Traditional classroom attendance is time-consuming, manual, and susceptible to human error. This project automates classroom attendance verification using AWS serverless services and facial recognition.

The system compares student-submitted reference photos with classroom images captured during lectures to verify students' physical presence. The entire infrastructure is deployed using **Infrastructure as Code (IaC)**, while application deployment is automated through **Continuous Integration and Continuous Deployment (CI/CD)** pipelines.

---

## Problem Statement

Manual attendance tracking:

- Takes significant classroom time
- Requires instructor intervention
- Is difficult to scale
- Can be manipulated through proxy attendance

This project aims to automate attendance verification using facial recognition while demonstrating modern cloud engineering practices.

---

## Solution

The application uses a fully serverless architecture on AWS.

Students upload their reference images, while classroom photographs are processed through AWS Lambda. Amazon Rekognition compares faces between the uploaded images and classroom photos. Attendance results are stored in DynamoDB and displayed through a web application hosted on AWS Amplify.

All AWS resources are provisioned using AWS CDK (Java), eliminating manual AWS Console configuration.

---

# Architecture

```
                   Student Upload
                         │
                         ▼
                  AWS S3 Bucket
                         │
              S3 Event Trigger
                         │
                         ▼
                 AWS Lambda Function
                         │
         ┌───────────────┼────────────────┐
         │               │                │
         ▼               ▼                ▼
 Amazon Rekognition  Amazon Textract   DynamoDB
         │
         ▼
 Attendance Results
         │
         ▼
 API Gateway
         │
         ▼
 AWS Amplify Web Application
```

---

## Features

- Facial recognition based attendance verification
- Student image upload
- Classroom image processing
- Serverless architecture
- Infrastructure as Code using AWS CDK
- Automated deployment with GitHub Actions
- Continuous deployment through AWS Amplify
- REST API using API Gateway
- Attendance storage in DynamoDB
- Automatic AWS resource provisioning

---

# Technologies Used

## Programming

- Java
- HTML
- JavaScript
- CSS

## AWS Services

- AWS Lambda
- Amazon S3
- Amazon Rekognition
- Amazon Textract
- Amazon API Gateway
- Amazon DynamoDB
- Amazon Cognito
- AWS Amplify
- AWS CloudFormation
- AWS CDK
- AWS IAM
- AWS CLI

## DevOps

- Git
- GitHub
- GitHub Actions
- CI/CD
- Infrastructure as Code

## Development Tools

- Visual Studio Code
- Maven
- Node.js
- AWS SDK for Java

---

# Project Structure

```
project3-cicd-Iac/
│
├── cdk/
│   ├── Stack.java
│   ├── App.java
│   └── Infrastructure Resources
│
├── lambda/
│   ├── AttendanceLambda.java
│   └── Image Processing
│
├── frontend/
│   ├── index.html
│   ├── script.js
│   └── styles.css
│
├── .github/
│   └── workflows/
│       └── ci-cd.yml
│
├── pom.xml
├── cdk.json
└── README.md
```

---

# Infrastructure as Code

AWS resources are provisioned entirely using **AWS CDK (Java)**.

Resources created include:

- S3 Bucket
- Lambda Functions
- API Gateway
- DynamoDB Table
- Cognito Identity Pool
- IAM Roles
- CloudFormation Stack

Deployment commands:

```bash
cdk bootstrap
```

```bash
cdk synth
```

```bash
cdk deploy
```

To remove all resources:

```bash
cdk destroy
```

---

# CI/CD Pipeline

GitHub Actions automatically:

- Builds the Java project
- Packages Lambda functions
- Uploads deployment artifacts to S3
- Triggers AWS deployment
- Deploys the frontend using AWS Amplify

Workflow:

```
Developer Push
      │
      ▼
GitHub Repository
      │
      ▼
GitHub Actions
      │
      ▼
Build Project
      │
      ▼
Package Lambda
      │
      ▼
Upload to S3
      │
      ▼
AWS Amplify Deployment
      │
      ▼
Live Application
```

---

# Setup Instructions

## Prerequisites

- Java 8+
- Maven
- Node.js
- AWS CLI
- AWS CDK
- Git

Install AWS CDK globally:

```bash
npm install -g aws-cdk
```

Configure AWS credentials:

```bash
aws configure
```

Build the project:

```bash
mvn clean install
```

Deploy infrastructure:

```bash
cdk deploy
```

---

# AWS Services Used

| Service | Purpose |
|----------|----------|
| S3 | Store student and classroom images |
| Lambda | Image processing and attendance logic |
| Rekognition | Facial comparison |
| Textract | Text extraction (future enhancements) |
| API Gateway | REST APIs |
| DynamoDB | Attendance records |
| Cognito | Authentication |
| Amplify | Frontend hosting |
| CloudFormation | Infrastructure deployment |
| CDK | Infrastructure as Code |

---

# Results

- Successfully deployed a fully serverless attendance system
- Automated infrastructure deployment using AWS CDK
- Configured GitHub Actions for CI/CD
- Hosted frontend using AWS Amplify
- Verified automated Lambda deployments
- Eliminated manual AWS resource creation
- Demonstrated Infrastructure as Code best practices

---

# Key Learning Outcomes

- Infrastructure as Code with AWS CDK
- CloudFormation stack deployment
- GitHub Actions automation
- AWS Amplify CI/CD
- Serverless application architecture
- AWS Lambda development
- REST API integration
- Event-driven architecture
- Cloud-native deployment workflows

---

# Future Improvements

- Real-time classroom camera integration
- Multi-face attendance detection
- Attendance analytics dashboard
- Email notifications
- Mobile application
- Instructor portal
- Enhanced authentication
- Machine learning confidence threshold tuning

---

# Author

**Sri Lekhya Pulluru**

Master's in Computer Science

University of Central Oklahoma

---

## Skills Demonstrated

- AWS Cloud
- Serverless Computing
- Java
- Infrastructure as Code
- AWS CDK
- CloudFormation
- CI/CD
- GitHub Actions
- REST APIs
- DevOps
- Cloud Architecture
- Event-Driven Systems
- AWS Lambda
- Amazon Rekognition
- DynamoDB

---

## License

This project was developed for educational purposes as part of a Cloud Computing course at the University of Central Oklahoma.
