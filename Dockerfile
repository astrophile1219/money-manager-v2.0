# ---------- BUILD STAGE ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests


# ---------- RUN STAGE ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/money_manager-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 1010

ENTRYPOINT ["java", "-jar", "app.jar"]
