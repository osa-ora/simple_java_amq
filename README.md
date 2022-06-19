# Simple Java SpringBoot AMQ Broker Example

This project will expose REST services to send JMS messages to AMQ Broker and listen to these messages to demostrate both operations in simple way.
We are going to use Red Hat AMQ Broker which is based on open source Apache ActiveMQ Artemis. 
The maven POM file contains the required dependency for using AMQ. 

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-artemis</artifactId>
</dependency>
```

We have added the configurations to the application.properties which is broker URL, username and password required for establishing the connection, these values can be overriden by environment variables (and configMap/Secrets in OpenShift)
```
my.amq.url=${amq.url:tcp://127.0.0.1:61616}
my.amq.user=${amq.user:amq}
my.amq.password=${amq.password:topSecret}
```

## Local Testing

First, you need to download Red Hat AMQ Broker, once downloaded, extract it and then create your first broker
```
cd amq-broker-7.10.0 //or your own version
cd bin
./artemis create mybroker
//set username and password (default in our app is amq as username and topSecret as password)
cd mybroker/bin
./artemis run
```
Now, we have the broker up and running and we can run our application:
```
mvn test // to run the unit tests
mvn clean test spring-boot:run // to build, test and run the application
mvn package //to build the jar file of this SpringBoot app
//or package then run
mvn package
java -jar target/amq-0.0.1-SNAPSHOT.jar
//or run a specifc listening port
java -jar -Dserver.port=8083 target/amq-0.0.1-SNAPSHOT.jar

```
Use the REST services to send/get messages or use it directly from the browser
```
curl http://localhost:8080/amq/v1/send/Hello%20Mr%20Osama%20Oransa
curl http://localhost:8080/amq/v1/list
curl http://localhost:8080/amq/v1/reset
```
You can navigae into the management console as well and check the different configurations using URL such as http://localhost:8161/console/auth/login which you can spot from the AMQ Broker logs.


## Deploy to OpenShift
- Provision AMQ Broker using AMQ Broker Operator
- Create ConfigMap/Secret
- Deploy our Application into OpenShift
- Test the application




