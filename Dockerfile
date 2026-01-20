# ---------- BUILD STAGE ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# ---------- RUN STAGE ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# The *.jar allows it to work even if you change your version number later
COPY --from=build /app/target/*.jar app.jar

EXPOSE 1010

ENTRYPOINT ["java", "-jar", "app.jar"]