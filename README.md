# interview-management-service

Interview-Management-System
Technology Focus: Spring Boot, JPA, Design Patterns, Microservices ________________________________________ Instructions • Code should follow SOLID principles, be modular, and demonstrate enterprise-grade quality. • Avoid using any deprecated or legacy Spring/Hibernate features. • Assume the use of: o Java 21 o Spring Boot 3.5 o Hibernate o Liquibase o H2 • No need to provide UI code or frontend logic. • Submit all code snippets in Java. Include class names and packages where applicable.

Use Case Design a microservice-based Interview Scheduling System. The system manages candidates, interviewers, , interviews, and feedback. It should allow users to schedule interviews, assign interviewers, record feedback, and notify participants.

Section A: System Design & Entity Modeling DB & DAO Design Design the following entities using JPA annotations (omit audit fields, nullable, and column definitions): • Candidate • Interviewer • Interview • Feedback Define appropriate relationships and Enums where needed.

REST DESIGN Create a Spring Boot @RestController for managing interview schedules. Include: Scheduling an interview. Submitting feedback Search API: Implement pagination and dynamic filtering for listing interviews by: • Interviewer name • Candidate Note: Use appropriate status codes and exception handling strategy.

SERVICE DESIGN Design a Service class method for the rest of the APIs
