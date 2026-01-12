package com.fasttasker.notification.config;

import com.fasttasker.common.constant.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitMQConfig {

    public static final String QUEUE_NAME = "notification.queue";


    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange globalExchange) {
        return BindingBuilder.bind(queue)
                .to(globalExchange)
                .with(RabbitMQConstants.ROUTING_KEY_NOTIFICATION);
    }
}