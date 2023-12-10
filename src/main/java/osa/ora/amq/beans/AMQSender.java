/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osa.ora.amq.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
/**
 *
 * @author ooransa
 */
public class AMQSender {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(String message,String queueName) {
        System.out.println("Sending message=" + message);
        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.convertAndSend(queueName, message);
    }

    public void publish(String message,String queueName) {
        System.out.println("Publish topic message=" + message);
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.convertAndSend(queueName, message);
    }

}
