FROM openjdk:8-jre

WORKDIR root/

ADD build/libs/*.jar ./application.jar

RUN apt-get update && \
    apt-get install -y \
        sqlite3 \
        libsqlite3-dev

COPY entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]

CMD [ "java", "-client", "-Xmx512M", "-jar", "-Dspring.profiles.active=docker", "/root/application.jar" ]