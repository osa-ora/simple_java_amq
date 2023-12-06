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
