# ⚡ Power Pulse 2.0 (Java Full Stack)

```
Power-Pulse-Java/
├── backend/                      <- Java (Spring Boot) server
│   ├── pom.xml                   <- Maven config / dependencies
│   ├── mvnw, mvnw.cmd, .mvn/     <- Maven wrapper (no Maven install needed)
│   └── src/main/
│       ├── java/com/powerpulse/
│       │   ├── PowerPulseApplication.java
│       │   ├── config/SecurityConfig.java
│       │   ├── controller/  (Auth, Dashboard, Reading)
│       │   ├── model/       (AppUser, Reading)
│       │   ├── repository/  (UserRepository, ReadingRepository)
│       │   └── service/CustomUserDetailsService.java
│       └── resources/application.properties
├── frontend/                     <- Pages (HTML + CSS + JS)
│   ├── index.html   (dashboard)
│   ├── login.html
│   ├── register.html
│   └── edit.html
├── .gitignore
└── README.md
```

## Run
1. Install JDK 21 (https://adoptium.net) and VS Code + "Extension Pack for Java".
2. Open the **Power-Pulse-Java** folder in VS Code, open a terminal and run:
   - Windows: `cd backend` then `.\mvnw.cmd spring-boot:run`
   - Mac/Linux: `cd backend` then `./mvnw spring-boot:run`
3. Open http://localhost:8080

The database (H2 file) is created automatically in `backend/data/`.

## Settings (`backend/src/main/resources/application.properties`)
- `server.port` - change if 8080 is busy
- `app.rate-per-unit` - price per kWh (default 7)
- `app.high-usage-limit` - monthly kWh above which status is "High Usage" (default 100)
