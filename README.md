# Med_Help
# 1. Start Postgres + Redis
docker compose up -d

# 2. Set up your project from Spring Initializr
#    Replace pom.xml with our version

# 3. Copy all source files into src/main/java/com/pathlab/

# 4. Run the app
./mvnw spring-boot:run

# 5. Open Swagger UI
# http://localhost:8080/swagger-ui/index.html

# 6. Test login
# POST /api/v1/auth/login
# { "email": "demo@pathlab.com", "password": "Admin@123" }

# Architecture
![alt text](image.png)

# Modules
![alt text](image-1.png)

# API Design
![alt text](image-2.png)
![alt text](image-3.png)

# Tech Stack 
![alt text](image-4.png)