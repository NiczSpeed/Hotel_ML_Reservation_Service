package com.ml.hotel_ml_reservation_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfiguration {

    @Bean
    public NewTopic checkReservationTopic(){
        return TopicBuilder.name("check_reservation_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic booleanReservationTopic(){
        return TopicBuilder.name("boolean_reservation_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic createReservationTopic(){
        return TopicBuilder.name("create_reservation_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic allUserReservationRequestTopic(){
        return TopicBuilder.name("all_user_reservation_request_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic allUserReservationResponseTopic(){
        return TopicBuilder.name("all_user_reservation_response_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic deleteReservationTopic(){
        return TopicBuilder.name("delete_reservation_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic updateReservationTopic(){
        return TopicBuilder.name("update_reservation_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic updateAllReservationTopic(){
        return TopicBuilder.name("update_all_reservation_topic")
                .partitions(12)
                .replicas(3)
                .build();
    }
}
