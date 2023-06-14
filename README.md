# Simple Java SpringBoot AMQ Broker Example

This project will expose REST services to send JMS messages to AMQ Broker and listen to these messages to demonstrate both operations in a simple way.

Red Hat AMQ contains many products one is the AMQ Broker which we are going to use and it is based on open source Apache ActiveMQ Artemis. 
There is also AMQ Streams which is based on the open source project Apache Kafka. 

<p align="center">
<img align="center" src="https://user-images.githubusercontent.com/18471537/174484814-3083c022-1b24-4247-bdd7-b388056ab60b.png">
</p>

The maven POM file contains the required dependency for using AMQ. 

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-artemis</artifactId>
</dependency>
```

We have added the configurations to the application.properties which is broker URL, username and password required for establishing the connection, these values can be overridden by environment variables (and configMap/Secret in OpenShift) these environment variables are: AMQ_URL, AMQ_USER and AMQ_PASSWORD.
```
my.amq.url=${AMQ_URL:tcp://127.0.0.1:61616}
my.amq.user=${AMQ_USER:amq}
my.amq.password=${AMQ_PASSWORD:topSecret}
```

## Local Setup
In this section, we will try to setup everything locally to test the developer experience, then we will switch to setup it over OpenShift which is the main purpose of this post.  

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
//or run a specific listening port
java -jar -Dserver.port=8083 target/amq-0.0.1-SNAPSHOT.jar

```
Use the REST services to send/get messages or use it directly from the browser
```
curl http://localhost:8080/amq/v1/send/Hello%20Mr%20Osama%20Oransa
curl http://localhost:8080/amq/v1/list
curl http://localhost:8080/amq/v1/reset
```
You can navigate into the management console as well and check the different configurations using URL such as http://localhost:8161/console/auth/login which you can spot from the AMQ Broker logs.


## Deploy to OpenShift
- Create OpenShift project 

Create a project for example "amq" to hold our AMQ broker and application, either from the Web Console or from the command line:
```
oc new-project amq
```
- Provision AMQ Broker using AMQ Broker Operator

Go to the OperatorHub section inside OpenShift and select Red Hat AMQ Broker

<img width="916" alt="Screen Shot 2022-06-19 at 09 42 59" src="https://user-images.githubusercontent.com/18471537/174470940-7bc349af-7278-4400-889f-e676f9b975b1.png">

Install it to the "amq" namespace only.

<img width="812" alt="Screen Shot 2022-06-19 at 09 46 51" src="https://user-images.githubusercontent.com/18471537/174471052-84f6817c-19b7-45a0-a8ea-99bc5f478d18.png">

Now, let's create our broker on Openshift by using the following SINGLE command:

```
oc apply -f https://raw.githubusercontent.com/osa-ora/simple_java_amq/main/amq-config/broker.yaml -n amq
```
This will create AMQ Broker instance based on common configurations and exposing the required port with default username/password. You may override any of the configurations or add to it whatever required.

You can have many options for acceptors, we selected the artemis one in our configurations:
```
acceptors:
    - anycastPrefix: jms.queue.
      expose: true
      multicastPrefix: /topic/
      name: all
      port: 61616
      protocols: openwire
      sniHost: localhost
```
We can have other acceptors as well such as:

```
<acceptors>
    <acceptor name="artemis">tcp://0.0.0.0:61616?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576</acceptor>
    <acceptor name="amqp">tcp://0.0.0.0:5672?protocols=AMQP</acceptor>
    <acceptor name="stomp">tcp://0.0.0.0:61613?protocols=STOMP</acceptor>
    <acceptor name="hornetq">tcp://0.0.0.0:5445?protocols=HORNETQ,STOMP</acceptor>
    <acceptor name="mqtt">tcp://0.0.0.0:1883?protocols=MQTT</acceptor>
</acceptors>
```

You can access the management console by using the exposed console route.

<img width="1035" alt="Screen Shot 2022-06-19 at 10 33 13" src="https://user-images.githubusercontent.com/18471537/174472670-4422cc69-2244-497e-bfd9-8db788644da3.png">

- Create ConfigMap/Secret

Now we need to create ConfigMap and Secrets for our application. If you didn't' change the username and password, then you can just update the broker URL, you can get it from the service name "amq-broker-sample-all-0-svc" (if you didn't change the broker name as per the previous step).

```
oc create configmap amq-settings --from-literal  AMQ_URL="tcp://${service_ip}:61616" -n amq
oc create secret generic amq-secrets --from-literal=AMQ_USER="amq" --from-literal=AMQ_PASSWORD="topSecret" -n amq
```

- Deploy our Application into OpenShift

Either from the console or by command line using a SINGLE command (oc new-app)

```
oc new-app --name=amq-client java~https://github.com/osa-ora/simple_java_amq -n amq
oc expose svc/amq-client -n amq
oc set env deployment/amq-client --from secret/amq-secrets 
oc set env deployment/amq-client --from configmap/amq-settings
```

- Test the application

Use the route to execute some test cases to make sure our application is sending and receiving messages to our deployed AMQ Broker.

```
curl {ROUTE_URL}/amq/v1/send/Hello%20Mr%20Osama%20Oransa
curl {ROUTE_URL}/amq/v1/list
curl {ROUTE_URL}/amq/v1/reset
```

And it should be working fine from browser, command line and in the application logs.


<img width="354" alt="Screen Shot 2022-06-19 at 11 32 50" src="https://user-images.githubusercontent.com/18471537/174474670-74d1b39d-f17a-4c1f-8ccb-0d57990b2cc4.png">

<img width="1471" alt="Screen Shot 2022-06-19 at 11 33 26" src="https://user-images.githubusercontent.com/18471537/174474692-c4690ef8-34c3-4a0e-af28-4ddd11db5c79.png">


## Conclusion
We have seen how simple it is to send and receive messages from AMQ Broker using the proper dependency in our SpringBoot application and how we can deploy AMQ Broker locally and test it with the proper configurations.
Then we saw how easily we can deploy the same AMQ Broker and our SpringBoot Application into OpenShift with the proper configurations to test the end-to-end functionality of Red Hat AMQ Broker without any hassles..





