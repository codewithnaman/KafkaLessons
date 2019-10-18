package com.aasaanshiksha.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
public class KafkaConsumerWithSeekAndAssign {

    public static void main(String[] args) {
        String topicName = "demo_application_topic";
        KafkaConsumer<String, String> kafkaConsumer = createKafkaConsumer(getKafkaProperties());
        assignAndSeek(kafkaConsumer, topicName);
        consumeDataFromKafka(kafkaConsumer);
    }

    private static void consumeDataFromKafka(KafkaConsumer<String, String> kafkaConsumer) {
        int messagesReadSoFar = 0;
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
            if (!records.isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Value          : "+record.value());
                    log.info("Partition      : "+record.partition());
                    log.info("Offset         : "+record.offset());
                    log.info("Timestamp      : "+record.timestamp());
                    messagesReadSoFar++;
                    if(messagesReadSoFar==5){
                        break;
                    }
                }
            }
        }
    }

    public static void assignAndSeek(KafkaConsumer<String, String> kafkaConsumer, String topic) {
        TopicPartition topicPartition = new TopicPartition(topic,0);
        kafkaConsumer.assign(Arrays.asList(topicPartition));
        kafkaConsumer.seek(topicPartition,10l);
    }

    public static KafkaConsumer<String, String> createKafkaConsumer(Properties properties) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        return consumer;
    }

    public static Properties getKafkaProperties() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.109.131:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }
}
