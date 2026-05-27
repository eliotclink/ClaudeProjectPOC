# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean install

# Run the application
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClaudeProjectPocApplicationTests

# Run a single test method
mvn test -Dtest=ClaudeProjectPocApplicationTests#contextLoads

# Package as executable JAR
mvn clean package
java -jar target/claudeprojectpoc-0.0.1-SNAPSHOT.jar
```

## Architecture

Spring Boot 3.3, Java 21, Maven. Entry point is `ClaudeProjectPocApplication` in `com.example.claudeprojectpoc`.

Uses `spring-boot-starter-webflux` (Reactor/Netty) — all controllers return `Mono`/`Flux`, services do the same. No persistence layer yet (in-memory `ConcurrentHashMap` in services). New features follow the layering: controller → service → repository, with reactive types throughout.
