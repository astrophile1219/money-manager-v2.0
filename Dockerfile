FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/money_manager-0.0.1-SNAPSHOT.jar moneymanager-v2.0.jar
EXPOSE 1010
ENTRYPOINT["java","-jar","moneymanager-v2.0.jar"]