package com.aasaanshiksha.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class SimpleKafkaProducer {

    public static void main(String[] args) {
        String topicName = "demo_topic";
        Properties kafkaProperties = getKafkaProperties();
        KafkaProducer<String, String> kafkaProducer = getKafkaProducer(kafkaProperties);

        for (int i = 0; i < 10; i++) {
            sendDataToKafka(kafkaProducer, "Sample Data " + i, topicName);
        }
        kafkaProducer.close();
    }

    private static void sendDataToKafka(KafkaProducer<String, String> kafkaProducer, String data, String topicName) {
        ProducerRecord<String,String> producerRecord = new ProducerRecord<String, String>(topicName,data);
        kafkaProducer.send(producerRecord); // This is asynchronous process
    }

    public static KafkaProducer<String, String> getKafkaProducer(Properties kafkaProperties) {
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(kafkaProperties);
        return kafkaProducer;
    }

    public static Properties getKafkaProperties() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }
}
