FROM eclipse-temurin:21

ENV SPRING_PROFILE="default"
ENV SERVER_PORT=8300

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

RUN mkdir /opt/app
COPY target/auth.jar /opt/app
CMD ["java", "-Dspring.profiles.active=${SPRING_PROFILE}", "-Dserver.port=${SERVER_PORT}", "-Duser.timezone=Asia/Seoul", "-Xms192m", "-Xmx192m", "-jar", "/opt/app/auth.jar"]
