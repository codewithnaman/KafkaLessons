package com.aasaanshiksha.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

@Slf4j

public class KafkaProducerWithKeys {

    public static void main(String[] args) {
        String topicName = "demo_topic";
        Properties properties = SimpleKafkaProducer.getKafkaProperties();
        KafkaProducer<String, String> kafkaProducer = SimpleKafkaProducer.getKafkaProducer(properties);
        for (int i = 0; i < 10; i++) {
            sendDataToKafka(kafkaProducer, i, topicName);
        }
        kafkaProducer.close();
    }

    private static void sendDataToKafka(KafkaProducer<String, String> kafkaProducer, int dataPart, String topicName) {
        String key = "key_" + dataPart % 3;
        String data = "Sample data " + dataPart;
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, key, data);
        kafkaProducer.send(producerRecord, (recordMetadata, exception) -> {
            if (exception != null) {
                log.info("Key     : " + key);
                log.info("Data     : " + data);
                log.info("Topic     : " + recordMetadata.topic());
                log.info("Partition : " + recordMetadata.partition());
                log.info("Offset    : " + recordMetadata.offset());
                log.info("TimeStamp : " + recordMetadata.timestamp());
            } else {
                log.error("Error while sending data to Kafka", exception);
            }
        }); // This is asynchronous process
    }
}
