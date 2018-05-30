FROM openjdk:8-jre-alpine

WORKDIR root/

ADD build/libs/open-chain-*.jar ./application.jar

EXPOSE 8080

CMD java -server \
    -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled \
    -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 \
    -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark \
    -jar /root/application.jar