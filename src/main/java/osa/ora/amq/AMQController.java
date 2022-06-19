/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osa.ora.amq;

/**
 *
 * @author ooransa
 */
import osa.ora.amq.beans.AMQSender;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/amq/v1")
public class AMQController {

    @Autowired
    private AMQSender sender;

    //list of recieved messages
    public static ArrayList<String> messages = new ArrayList<String>();

    /**
     * Rest Service to send a JMS message
     *
     * @param msg the message content
     * @return true
     */
    @GetMapping(path = "/send/{msg}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String sendMessage(@PathVariable(value = "msg") String message) {
        System.out.println("Sending message: " + message);
        sender.send(message, AmqApplication.QUEUE_NAME);
        return "{\"Result\":true}";
    }

    /**
     * Rest Service to show all recieved messages
     *
     * @param msg the message content
     * @return all messages
     */
    @GetMapping(path = "/list",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMessages() {
        System.out.println("List all recieved messages: " + messages.size());
        String result = "{ \"Total Recieved\":" + messages.size();
        for (int i = 0; i < messages.size(); i++) {
            result += ", \"message " + i + "\":\"" + messages.get(i) + "\"";
        }
        result += "}";
        return result;
    }

    /**
     * Service to reset all recieved messages shouldn't be a 'get' method
     *
     * @return true
     */
    @GetMapping(path = "/reset",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String reset() {
        System.out.println("Clear all messages");
        messages.clear();
        return "{\"Result\":true}";
    }
}
