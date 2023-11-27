FROM amazoncorretto:17

COPY lib/libWeWorkFinanceSdk.so /usr/lib/libWeWorkFinanceSdk.so

CMD mkdir /usr/local/wisdech
WORKDIR /usr/local/wisdech

ARG JAR_FILE=target/Finance.jar

COPY ${JAR_FILE} Finance.jar
ENTRYPOINT ["java","-jar","Finance.jar"]
EXPOSE 8900