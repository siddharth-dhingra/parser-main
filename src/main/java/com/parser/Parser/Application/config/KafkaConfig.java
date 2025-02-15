package com.parser.Parser.Application.config;

import com.parser.Parser.Application.dto.ParseAcknowledgement;
import com.parser.Parser.Application.dto.ScanParseEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ScanParseEvent> fileLocationEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Wrap the delegate deserializers in ErrorHandlingDeserializer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Point to the actual delegates
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // If you're using JSON, trust all packages or specify your model package
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.parser.Parser.Application.dto.ScanParseEvent");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        // group.id, etc.
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "parser-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new ErrorHandlingDeserializer<>(), // Key
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(ScanParseEvent.class))
        );
    }

    // @Bean
    // public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
    //     // Retry 3 times (interval=0), then DLT
    //     FixedBackOff backOff = new FixedBackOff(0L, 3L);
    //     DefaultErrorHandler handler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), backOff);
    //     return handler;
    // }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, ParseAcknowledgement> template) {
        FixedBackOff backOff = new FixedBackOff(0L, 3L);
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), backOff);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ScanParseEvent> fileLocationEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ScanParseEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(fileLocationEventConsumerFactory());
        return factory;
    }

    @Bean
    public ProducerFactory<String, ParseAcknowledgement> acknowledgementProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ParseAcknowledgement> acknowledgementKafkaTemplate() {
        return new KafkaTemplate<>(acknowledgementProducerFactory());
    }

}