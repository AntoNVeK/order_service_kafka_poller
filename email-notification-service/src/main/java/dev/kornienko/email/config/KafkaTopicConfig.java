package dev.kornienko.email.config;

import dev.kornienko.email.dto.OrderCompletedEvent;
import dev.kornienko.email.dto.OrderFailedEvent;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaTopicConfig {

    // ================= TOPICS =================

    @Bean
    public NewTopic orderCompletedTopic() {
        return TopicBuilder.name("order-completed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderFailedTopic() {
        return TopicBuilder.name("order-failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // ================= COMPLETED =================

    @Bean
    public ConsumerFactory<String, OrderCompletedEvent> completedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        JsonDeserializer<OrderCompletedEvent> deserializer =
                new JsonDeserializer<>(OrderCompletedEvent.class);

        deserializer.addTrustedPackages("dev.kornienko.email.dto");

        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "email-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer)
        );
    }

    @Bean(name = "completedFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OrderCompletedEvent> completedFactory(
            ConsumerFactory<String, OrderCompletedEvent> cf) {

        ConcurrentKafkaListenerContainerFactory<String, OrderCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(cf);

        return factory;
    }

    // ================= FAILED =================

    @Bean
    public ConsumerFactory<String, OrderFailedEvent> failedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        JsonDeserializer<OrderFailedEvent> deserializer =
                new JsonDeserializer<>(OrderFailedEvent.class);

        deserializer.addTrustedPackages("dev.kornienko.email.dto");

        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "email-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer)
        );
    }

    @Bean(name = "failedFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OrderFailedEvent> failedFactory(
            ConsumerFactory<String, OrderFailedEvent> cf) {

        ConcurrentKafkaListenerContainerFactory<String, OrderFailedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(cf);

        return factory;
    }
}