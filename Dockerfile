FROM openjdk:8-jre

WORKDIR root/

ADD build/libs/*.jar ./application.jar

EXPOSE 8080
EXPOSE 9190

CMD java -client -Xmx512M -jar /root/application.jar