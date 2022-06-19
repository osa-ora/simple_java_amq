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

Now, let's create our broker on Openshift by using the following command:

```
oc apply -f https://raw.githubusercontent.com/osa-ora/simple_java_amq/main/amq-config/broker.yaml -n amq
```
This will create AMQ Broker instance based on common configurations and exposing the required port with default username/password.
You can access the management console by using the exposed console route.

<img width="1035" alt="Screen Shot 2022-06-19 at 10 33 13" src="https://user-images.githubusercontent.com/18471537/174472670-4422cc69-2244-497e-bfd9-8db788644da3.png">

- Create ConfigMap/Secret

Now we need to create ConfigMap and Secrets for our applicatons. If you didnt' change the username and password, then you can just update the broker URL, you can get it from the service name "amq-broker-sample-all-0-svc" (if you didn't change the broker name as per the previous step).

```
oc create configmap amq-settings --from-literal  amq.url="tcp://${service_ip}:61616" -n amq
```

- Deploy our Application into OpenShift

Either from the console or by command line.

```
oc new-app --name=amq-client java~https://github.com/osa-ora/simple_java_amq -n amq
oc expose svc/amq-client -n amq
oc set env deployment/amq-client --from configmap/amq-settings
```
In case you changed the AMQ Broker username and password, make sure to update the application deployment to reference the same credentials used in the provisioned broker as following:

<img width="1319" alt="Screen Shot 2022-06-19 at 11 29 52" src="https://user-images.githubusercontent.com/18471537/174474569-e9852abc-b686-4ea6-92d2-ec404a2cd17c.png">


- Test the application

Use the route to execute some test cases to make sure our application is sending and reciving messages to our deployed AMQ Broker.

```
curl {ROUTE_URL}/amq/v1/send/Hello%20Mr%20Osama%20Oransa
curl {ROUTE_URL}/amq/v1/list
curl {ROUTE_URL}/amq/v1/reset
```

And it should be working fine from both browser and in the application logs.


<img width="354" alt="Screen Shot 2022-06-19 at 11 32 50" src="https://user-images.githubusercontent.com/18471537/174474670-74d1b39d-f17a-4c1f-8ccb-0d57990b2cc4.png">

<img width="1471" alt="Screen Shot 2022-06-19 at 11 33 26" src="https://user-images.githubusercontent.com/18471537/174474692-c4690ef8-34c3-4a0e-af28-4ddd11db5c79.png">




