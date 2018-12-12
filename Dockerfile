FROM openjdk:8-jre

WORKDIR /root
ADD build/libs/*.jar ./application.jar
COPY entrypoint.sh /

ENTRYPOINT ["/entrypoint.sh"]
CMD java -client -Xmx512M $JMX_OPTS -jar -Dspring.profiles.active=docker /root/application.jar