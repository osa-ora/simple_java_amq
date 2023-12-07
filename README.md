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
my.amq.expiry=5000
use.topic=false
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
//update application properties to use either topic or queue and AmqApplication.java with queue/topic name
mvn package //to build the jar file of this SpringBoot app
//or package then run
mvn package
java -jar target/amq-0.0.1-SNAPSHOT.jar
//or run a specific listening port
java -jar -Dserver.port=8083 target/amq-0.0.1-SNAPSHOT.jar

```
Use the REST services to send/get messages or use it directly from the browser
```
//for sending to queue
curl http://localhost:8080/amq/v1/send/Hello%20Mr%20Osama%20Oransa
//for publishing to a topic
curl http://localhost:8080/amq/v1/publish/Hello%20Mr%20Osama%20Oransa
//to list recievd messages
curl http://localhost:8080/amq/v1/list
//to clear the recived messages
curl http://localhost:8080/amq/v1/reset
```
You can navigate into the management console as well and check the different configurations using URL such as http://localhost:8161/console/auth/login which you can spot from the AMQ Broker logs.

Note: You can disable the message queue to see the messages, trace the expiration and movement of the messages into the expiration queue.

<img width="920" alt="Screenshot 2023-12-06 at 17 00 50" src="https://github.com/osa-ora/simple_java_amq/assets/18471537/a8c2df28-a44c-4414-9ee9-abdcc222433f">

This is the expiry queue:
<img width="1786" alt="Screenshot 2023-12-06 at 16 55 00" src="https://github.com/osa-ora/simple_java_amq/assets/18471537/9afdbd53-6ffe-49b6-9c57-1b8232b51262">

In the expiry queue, you can resend or retry the message, move the message to another queue or just delete it (them).

<img width="1187" alt="Screenshot 2023-12-06 at 16 58 40" src="https://github.com/osa-ora/simple_java_amq/assets/18471537/d31a11d9-d753-4f54-bd72-6e9173b171c2">

By default the queue or topic will be auto-created but you can explicitly configure them, go to the broker name folder/etc and update the broker file and add any queue (any-cast) or topic (multi-cast) and their configurations, then start the broker.  

For example:
```
<addresses>
         <address name="DLQ">
            <anycast>
               <queue name="DLQ" />
            </anycast>
         </address>
         <address name="ExpiryQueue">
            <anycast>
               <queue name="ExpiryQueue" />
            </anycast>
         </address>
        <address name="myConfigured">
            <anycast>
               <queue name="myConfigured" />
            </anycast>
         </address>
        <address name="my-topic">
            <multicast>
                 <queue name="my-topic">
                    <durable>true</durable>
                 </queue>
            </multicast>
        </address>
      </addresses>
```

Finally, you can create the queue or topic from the management console.  First create addreess then create the topic/queue.  

<img width="1785" alt="Screenshot 2023-12-06 at 18 22 25" src="https://github.com/osa-ora/simple_java_amq/assets/18471537/9b2015e4-9446-46d7-883c-eaa780b857f9">

<img width="1779" alt="Screenshot 2023-12-06 at 18 27 10" src="https://github.com/osa-ora/simple_java_amq/assets/18471537/af8aa29c-3e4e-4f3f-8367-fc4e8fb87292">

To test deadletter situation, where messages are failed to process and acknowledge by the client after trials to deliver it, it get moved to DLQ queue, simply send a message with the text "test-dead-letter"
```
curl http://localhost:8080/amq/v1/send/test-dead-letter
```
You will see, many delivery failure in the application logs, then if you check the console, you can see the DLQ with the message, where you can do further analysis and then move it, delete it or retry it.

<img width="1491" alt="Screenshot 2023-12-07 at 11 02 43" src="https://github.com/osa-ora/simple_java_amq/assets/18471537/22401c9f-ebcf-4a82-8c7b-db9ae7dd5dc7">

To control the deadletter queue name and redelivery settings, use the broker.xml to configure all these detals in the address settings section, for example for our my-queue
```
<address-setting match="my-queue">
    <dead-letter-address>DLQ</dead-letter-address>
    <expiry-address>ExpiryQueue</expiry-address>
    <redelivery-delay>2000</redelivery-delay>
    ...
 </address-setting>
```

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





