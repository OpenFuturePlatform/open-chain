FROM openjdk8-sqlite3

WORKDIR root/
ADD build/libs/*.jar ./application.jar

CMD java -client -Xmx512M -jar -Dspring.profiles.active=docker /root/application.jar