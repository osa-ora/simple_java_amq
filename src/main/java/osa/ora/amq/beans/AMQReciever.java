/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osa.ora.amq.beans;

/**
 *
 * @author ooransa
 */
import org.springframework.jms.annotation.JmsListener;
import osa.ora.amq.AMQController;
import osa.ora.amq.AmqApplication;

public class AMQReciever {

    @JmsListener(destination = AmqApplication.QUEUE_NAME)
    public void receive(String message) throws Exception {
        if(message.equals("test-dead-letter")) throw new Exception("Failed to process message!");
        System.out.println("Received message="+ message+" in "+AmqApplication.QUEUE_NAME);
        AMQController.messages.add(message);
    }

}
