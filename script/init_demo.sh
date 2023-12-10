#!/bin/sh
if [ "$#" -ne 1 ];  then
  echo "Usage: $0 amq-project" >&2
  exit 1
fi

echo "Please Login to OCP using oc login ..... "  
echo "Make sure Openshift AMQ Broker (Multiarch) Operator is installed"
echo "Make sure oc and tkn commands are installed"
echo "AMQ Broker OpenShift Project $1"
echo "Press [Enter] key to resume..." 
read

echo "Creating Required OCP Projects … $1" 
oc new-project $1 

echo "Make sure Openshift AMQ Broker (Multiarch) Operator is installed in $1 project/namespace"
echo "Press [Enter] key to resume..."
read
echo "Creating Required AMQ Resources ..."
oc apply -f https://raw.githubusercontent.com/osa-ora/simple_java_amq/main/amq-config/broker.yaml -n $1
oc apply -f https://raw.githubusercontent.com/osa-ora/simple_java_amq/main/amq-config/address-config.yaml -n $1
oc apply -f https://raw.githubusercontent.com/osa-ora/simple_java_amq/main/amq-config/deadletter.yaml -n $1

echo "Press [Enter] key to resume..."
read

echo "Creating Sample SpringBoot Config/Secrets ..."
oc create configmap amq-settings --from-literal  AMQ_URL="tcp://amq-broker-sample-all-0-svc:61616" -n $1
oc create secret generic amq-secrets --from-literal=AMQ_USER="amq" --from-literal=AMQ_PASSWORD="topSecret" -n $1

echo "Deploy Sample SpringBoot App Demo ..."
oc new-app --name=amq-client java~https://github.com/osa-ora/simple_java_amq -n $1
oc expose svc/amq-client -n $1
oc set env deployment/amq-client --from secret/amq-secrets 
oc set env deployment/amq-client --from configmap/amq-settings

echo "Press [Enter] key to test the app..."
read
echo "Test sending some messages …"
curl $(oc get route amq-client -o jsonpath='{.spec.host}')/amq/v1/send/Hello%20Mr%20Osama%20Oransa
curl $(oc get route amq-client -o jsonpath='{.spec.host}')/amq/v1/send/Hello%20Mr%20Osama%20Oransa
curl $(oc get route amq-client -o jsonpath='{.spec.host}')/amq/v1/send/Hello%20Mr%20Osama%20Oransa
curl $(oc get route amq-client -o jsonpath='{.spec.host}')/amq/v1/send/test-dead-letter
curl $(oc get route amq-client -o jsonpath='{.spec.host}')/amq/v1/list

echo "Done!!"
