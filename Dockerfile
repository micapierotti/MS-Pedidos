FROM openjdk:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ms-pedidos.jar
ENTRYPOINT ["java","-jar","/ms-pedidos.jar"]