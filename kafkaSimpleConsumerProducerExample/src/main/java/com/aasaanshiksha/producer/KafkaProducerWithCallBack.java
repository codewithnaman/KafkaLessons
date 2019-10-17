package com.aasaanshiksha.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

@Slf4j
public class KafkaProducerWithCallBack {

    public static void main(String[] args) {
        String topicName = "demo_topic";
        Properties properties = SimpleKafkaProducer.getKafkaProperties();
        KafkaProducer<String, String> kafkaProducer = SimpleKafkaProducer.getKafkaProducer(properties);
        for (int i = 0; i < 10; i++) {
            sendDataToKafka(kafkaProducer, "Sample Data " + i, topicName);
        }
        kafkaProducer.close();
    }

    private static void sendDataToKafka(KafkaProducer<String, String> kafkaProducer, String data, String topicName) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topicName, data);
        kafkaProducer.send(producerRecord, (recordMetadata, exception) -> {
            if (exception != null) {
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
