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
import osa.ora.amq.beans.AMQReciever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

@Configuration
@EnableJms
public class RecieverConfig {

    @Value("${my.amq.url}")
    private String broker;

    @Value("${my.amq.user}")
    private String user;

    @Value("${my.amq.password}")
    private String password;

    @Value("${use.topic}")
    private Boolean useTopic;


    @Bean
    public ActiveMQConnectionFactory receiverActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(broker);
        activeMQConnectionFactory.setUser(user);
        activeMQConnectionFactory.setPassword(password);
        if(useTopic) {
           activeMQConnectionFactory.setClientID("my-broker-id");
        }
        return activeMQConnectionFactory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(receiverActiveMQConnectionFactory());
        if(useTopic) {
           factory.setPubSubDomain(true);
           factory.setSubscriptionDurable(true);
        }
        return factory;
    }


    @Bean
    public AMQReciever receiver() {
        return new AMQReciever();
    }

}
