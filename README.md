# Kafka

* [Section - 1 Theory](#section---1-theory)
* [Section - 2 Kafka CLI](#section---2-kafka-cli)
* [Section - 3 Kafka Java Programming](#section---3-kafka-java-programming)
* [Section - 4 Advance Configuration for Producer and Consumers](#section---4-advance-configuration-for-producer-and-consumers)
* [Section - 5 Best Practices, Partitions Count and Replication Factor](#section---5-best-practices-partitions-count-and-replication-factor)
* [Section - 6 Advanced Topic Configurations](#section---6-advanced-topic-configurations)

## Section - 1 Theory

### Topics partitions and offsets

In Kafka a topic is a particular stream of data. A topic is identified by its name. We can as many topic as we want.

Topics are split in partitions, a topic can have one or more partitions. Partitions are numbered and it starts with 0
and go to n-1 number, where n is number of partition in topic. 

Each partition contains number of messages. Each message get an incremental id for the partition, this incremental id 
called offset. It is not necessary each partition offset is at same point.

* Offset only have meaning for partition
* Order is guaranteed only within a partition
* Data is kept for for limited period of time, but the offset keep incrementing it will not go back to zero.
* Once data is written to a partition, it can't be changed. 
* Data is assigned randomly to a partition unless a key is provided.

### Brokers and topics

A kafka cluster is composed of multiple brokers (servers). Each broker has it's own unique integer id. Each broker
contains certain topic partitions. After connecting to any broker you will connected to entire partitions.

When we create a topic with multiple partitions, the partitions are distributed between brokers. 

### Topic replication

When we provide replication in kafka cluster, Then it will replicate partition to other server in cluster. In cluster
one broker is leader for the partition of a particular topic, and this leader can receive data for the partition. 
The rest broker partition are passive replica which synchronize the data from leader. Therefore each partition has
one leader and multiple ISR (in-sync replica). If leader broker goes down, then election between the leader happen
and a new leader will be assigned.

### Producers and message keys

Producers write data to topics. Producer automatically know to which broker and partition they have to write to. In
case of broker failure, Producer will automatically recover. If producer don't put the data with key, the data will
go to brokers in round robin fashioned and they are load balanced. If producer generates with the key it will send the
data to same broker and same partition.

Producers can choose to receive acknowledgement of data writes.
* ack = 0 Producer won't wait for acknowledgement (Possible data loss)
* ack = 1 Producer will wait for leader acknowledgement (Limited data loss)
* ack = all Leader + Replicas acknowledgement (No data loss)

### Consumer and consumer group

Consumer read data from topic. Consumers know which broker to read from. In case of broker failure, consumer know 
how to recover, no explicit mechanism is required. Data is read in order within each partition.

Consumer read data in consumer groups. Each consumer within a group reads from exclusive partitions. If we have
more consumers than some consumers will be inactive. In a consumer group a consumer can read data from unique partition
of the topic ex. if C1 has is reading data from Partition 0, then C2 in same group can't read data from partition 0; 
until C1 goes down. Each consumer group consume full data of the a topic i.e. if we have Consumer Group G1 and G2, 
G1 and G2 both receive full data for the topic.

If a consumer in a group goes down, consumer group balances itself and assign partitions to other consumer in group to
read the data.

### Consumer offsets and delivery semantics

Kafka stores the offsets at which a consumer group has been reading. The offsets committed live in a kafka topic named
__consumer_offsets. When a consumer in a group has processed data received from Kafka, it should be committing
the offsets. This is necessary because if a consumer dies it will be able to read back from where it left off.

*Delivery Semantics* -  Consumer can choose when to commit offsets. There are 3 delivery semantics for this which are
below:
* At Most Once 
    * Offsets are committed as soon as message received
    * If processing goes wrong the message will be lost. (It won't read again)
* At least Once (More preferred way)
    * Offsets are committed after the message is processed
    * If the processing goes wrong te message will be read again
    * This can result in duplicate processing of the messages, so make sure your processing is idempotent
* Exactly Once 
    * This can be achieved for Kafka => Kafka workflows using Kafka Streams API

### Kafka broker discovery

Every kafka broker is also called a "bootstrap server", that means that you only need to connect to one broker, and you
will be connected to entire cluster, i.e. Each broker knows about all brokers, topics and partitions (metadata).

### ZooKeeper

* Zookeeper manages broker (keeps list of brokers). 
* Zookeeper to perform the leader election for the partition if any broker goes down or joins the cluster. 
* Zookeeper sends notifications to Kafka in case of any changes Like topic creation, deletion or broker dies etc.
* Kafka can't work without zookeeper
* Zookeeper by design operates with an odd number of servers (3,5,7)
* Zookeeper has a leader (handle writes) the rest of the servers are followers (handle reads)
  

### Kafka guarantees

* Messages are appended to ta topic-partition in order they are sent
* Consumer read messaged in the order stored in a topic partition
* With a replication factor of N, producers and consumers can tolerate up to N-1 broker being down
* As long as the number of partitions remains constant for a topic (no new partitions), the same key will always go to 
same partition.

## Section - 2 Kafka CLI

### Downloading and Starting Kafka

##### Downloading Kafka and adding to PATH
 To start working with Kafka, download Kafka binary and extract it from below link:
 
 https://kafka.apache.org/downloads
 
 To run the kafka commands from any location we will add the extracted kafka path to PATH in profile. For this
 we will edit .profile file
 
 Add below line in .profile file
  * export PATH="$PATH:<Path_to Kafka bin folder>"

##### Starting kafka
Now we will see How to start how to start kafka. Before starting kafka we need to start zookeeper, so we will start 
zookeeper first and then Kafka.

We have some configuration files which we need to modify to keep data of zookeeper and kafka, They by default point
to tmp directory from which files can be deleted, so open the config folder and change following properties file
to point a directory where we can keep zookeeper and kafka data.

* Zookeeper properties file change
  - Open in vim or nano config/zookeeper.properties
  - Update dataDir property pointing to data directory for the zookeeper
  - Ex. : dataDir=/home/ngupta/kafka/data/zookeeper

Now we can start zookeeper. To start zookeeper use below command:
 * bin/zookeeper-server-start.sh config/zookeeper.properties

Kafka Server properties change
 * Kafka properties file change
    - Open in vim or nano config/server.properties
    - Update log.dirs property pointing to data directory for the zookeeper
    - Ex. : log.dirs=/home/ngupta/kafka/data/kafka-data

Now we can start kafka. To start kafka use below command:
 * bin/kafka-server-start.sh config/server.properties


### Kafka Topics

##### Create Kafka Topic
To Create kafka topic, we need below information.
1. Kafka Name
2. Partitions
3. Replication Factor
4. Zookeeper cluster 

Now to create topic we will use below command:
 * kafka-topics.sh --zookeeper <Zookeeper_servers> --topic <topic_name> --create --partitions <number_of_partition> --replication-factor <number_of_replication_copies>
 * Ex. : kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic example --create --partitions 3 --replication-factor 1

Now we create topic, we will look how we can see all the topics present on cluster.

**Note: Replication factor can not be grater than available brokers.**

##### List topics
Now to list all topics we will use below command:
 * kafka-topics.sh --zookeeper <Zookeeper_servers> --list
 * Ex. : kafka-topics.sh --zookeeper 127.0.0.1:2181 --list

Now we will describe a topic about it's partition and replications.

##### Describe topic
Now to describe topic we will use below command:
 * kafka-topics.sh --zookeeper <Zookeeper_servers> --topic <topic_name> --describe
 * Ex. : kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic example --describe

This will give table like below, which will provide you insight about topic, partition, leader
and ISR(In Sync Replicas)
```shell script
ngupta@node1:~/kafka$ kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic example --describe
Topic:example   PartitionCount:3        ReplicationFactor:1     Configs:
        Topic: example  Partition: 0    Leader: 0       Replicas: 0     Isr: 0
        Topic: example  Partition: 1    Leader: 0       Replicas: 0     Isr: 0
        Topic: example  Partition: 2    Leader: 0       Replicas: 0     Isr: 0
```
We can also delete a topic, Let's see how to delete a topic.
 
##### Delete topic
To delete a topic we need to use below code:
 * kafka-topics.sh --zookeeper <Zookeeper_servers> --topic <topic_name> --delete
 * Ex. : kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic example2 --delete

Output:
```shell script
ngupta@node1:~/kafka$ kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic example2 --delete
Topic example2 is marked for deletion.
Note: This will have no impact if delete.topic.enable is not set to true.
ngupta@node1:~/kafka$ kafka-topics.sh --zookeeper 127.0.0.1:2181 --list
example
ngupta@node1:~/kafka$
```

Let's put some data using CLI in topic.

##### Producing data to kafka
To produce data to kafka using CLI we need to use below command, which will gives the control to you for producing
message.
 * kafka-console-producer.sh --broker-list <kafka_broker_list> --topic <topic_name>
 * Ex. : kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic example

```shell script
ngupta@node1:~/kafka$ kafka-console-producer.sh --broker-list 127.0.0.1:9092 --topic example
>This is example topic
>working fine
```
To exit from producer press Ctrl+C. If we provide any topic which does not exist already it will create the topic with 
configurations defined in server.properties.

##### Consuming data from kafka
To consume data to kafka using CLI we need to use below command, which will gives the control to you for producing
message.
 * kafka-console-consumer.sh --bootstrap-server <kafka_broker_list> --topic <topic_name>
 * Ex. : kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic example

To exit from consumer press Ctrl+C. If we provide any topic which does not exist already it will create the topic with 
configurations defined in server.properties.

In case you want consumer read messages from beginning then we will use below command:
 *  kafka-console-consumer.sh --bootstrap-server <kafka_broker_list> --topic <topic_name> --from-beginning

##### Kafka Consumer Group
To provide a group from the cli we need to fire below command.
 * kafka-console-consumer.sh --bootstrap-server <kafka_broker_list> --topic <topic_name> --group <group_name>


So this will create a consumer group, To get the information of consumer group, we will use below commands:

 * To list all consumer Groups
    -  kafka-consumer-groups.sh --bootstrap-server <kafka_broker_list> --list
       ```shell script
        ngupta@node1:~$ kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --list
        consumerGroup2
        consumerGroup1
       ```
 * To see details about consumer
    -  kafka-consumer-groups.sh --bootstrap-server <kafka_broker_list> --describe --group <group_name>
       ```shell script
        ngupta@node1:~$ kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --describe --group consumerGroup1
        
        GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                     HOST            CLIENT-ID
        consumerGroup1  example         0          3               3               0               consumer-1-b3d78ec4-4bb5-452c-953b-4bfa43108d24 /127.0.0.1      consumer-1
        consumerGroup1  example         1          2               2               0               consumer-1-b3d78ec4-4bb5-452c-953b-4bfa43108d24 /127.0.0.1      consumer-1
        consumerGroup1  example         2          2               2               0               consumer-1-b3d78ec4-4bb5-452c-953b-4bfa43108d24 /127.0.0.1      consumer-1
       
       ngupta@node1:~$ kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --describe --group consumerGroup2
       
       Consumer group 'consumerGroup2' has no active members.
       
       GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
       consumerGroup2  example         2          2               3               1               -               -               -
       consumerGroup2  example         0          3               4               1               -               -               -
       consumerGroup2  example         1          2               4               2               -               -               -
       ```

The first oe is connected so it is showing connection details of consumer group and server ip from which it is connected.
The second consumer is not running, so it is not showing the details for connection. Also in each table it is showing
which consumer is lagging with which number of messages, CURRENT-OFFSET show the current offset of the consumer where it
reading, CURRENT-OFFSET show the where log offset end for the partition and LAG show how much message consumer is lagging
for partition. 

 * Resetting offset of a consumer
  -  kafka-consumer-groups.sh --bootstrap-server <kafka_broker_list> --group <group_name> --reset-offsets <reset_option> --execute --topic <topic_name>
  
There are several option available for resetting, like resetting from a time period, or start of stream which is present,
from few offset behind. For example i am going two offset back for consumerGroup1.
```shell script
ngupta@node1:~$ kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --describe --group consumerGroup1

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                     HOST            CLIENT-ID
consumerGroup1  example         0          4               4               0               consumer-1-41222ec4-b922-47ab-93d5-fc3a20807de4 /127.0.0.1      consumer-1
consumerGroup1  example         1          4               4               0               consumer-1-41222ec4-b922-47ab-93d5-fc3a20807de4 /127.0.0.1      consumer-1
consumerGroup1  example         2          3               3               0               consumer-1-41222ec4-b922-47ab-93d5-fc3a20807de4 /127.0.0.1      consumer-1

ngupta@node1:~$ kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --group consumerGroup1 --reset-offsets --shift-by -2 --execute --topic example

GROUP                          TOPIC                          PARTITION  NEW-OFFSET
consumerGroup1                 example                        0          2
consumerGroup1                 example                        2          1
consumerGroup1                 example                        1          2

```
So we had new offset nw which is two offset behind for each partition, now if we will start our consumer with group 6 
messages will be consumed.
```shell script
ngupta@node1:~$ kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic example --group consumerGroup1
Too
provided
This is a group message
what We can do
More Data
we can see this in consumer group

```

This section covers all about kafka CLI options, now we will see this in programming how to use kafka pub-sub model.

## Section - 3 Kafka Java Programming

To interact with kafka i.e. to produce or consume data from Kafka, we need the libraries. To get the libraries please
add below dependency in your pom or gradle.

```xml
<!-- https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Producer
To produce data in kafka we need to follow these below steps:
* Create producer properties
* Create the producer
* Send data

#### Simple Java Producer
We have Created one simple producer 
[SimpleKafkaProducer](kafkaSimpleConsumerProducerExample/src/main/java/com/aasaanshiksha/producer/SimpleKafkaProducer.java)
which contains exactly 3 methods corresponding to operation mentioned in above block.

In above solution we are just creating properties, initializing the kafka producer and sending data. Let's understand
send data part.

```java
 private static void sendDataToKafka(KafkaProducer<String, String> kafkaProducer, String data, String topicName) {
        ProducerRecord<String,String> producerRecord = new ProducerRecord<String, String>(topicName,data);
        kafkaProducer.send(producerRecord); // This is asynchronous process
    }
``` 

Kafka Producer takes the ProducerRecord to send data to kafka topic, ProducerRecord contains the topic name and data,
So KafkaProducer take this topicName and send the data to that topic. While declaring the properties we have used two
interesting properties which is serializer for key and value, where we provided the StringSerializer class name for 
key and value. These classes are used to convert the provided data type to byte. In case you want your custom data type
to transmit then you need to write your own Serializer. But as practices please use existing Serializer classes shipped
with kafka client library. As we are providing generics argument to the KafkaProducer and record we are mentioning
that the key and value are of type String. 

The KafkaProducer.send is a asynchronous process, so if you run the program and does not close the producer then it might
be possible you will not able to see the data in kafka as application exists before this method executes.

#### Producer with callbacks

In some cases you need the record metadata after sending purpose, mostly for logging or debug purpose. Then Kafka send
method provide a Callback interface with a method which provides you the metadata or exception if record failed due to
some reason. To log this record metadata you need to implement this and write your custom implementation for this, like
we did below and class [KafkaProducerWithCallBack](kafkaSimpleConsumerProducerExample/src/main/java/com/aasaanshiksha/producer/KafkaProducerWithCallBack.java):

```java
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
```

Since the Callback contains only one method, so we use the lambda function to log the details.  

#### Producer with keys

To show that with same keys values go to same partition we had implement class 
[KafkaProducerWithKeys](kafkaSimpleConsumerProducerExample/src/main/java/com/aasaanshiksha/producer/KafkaProducerWithKeys.java)
. Please run this class your own while practicing, this should give you output log like below:

```text
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_0
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 0
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 2
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 46
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823067
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_0
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 3
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 2
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 47
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823081
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_0
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 6
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 2
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 48
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823081
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_0
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 9
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 2
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 49
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823082
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_2
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 2
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 0
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 36
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823081
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_2
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 5
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 0
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 37
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823081
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_2
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 8
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 0
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 38
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823082
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_1
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 1
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 1
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 38
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823080
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_1
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 4
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 1
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 39
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823081
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Key     : key_1
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Data     : Sample data 7
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Topic     : demo_application_topic
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Partition : 1
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - Offset    : 40
[kafka-producer-network-thread | producer-1] INFO com.aasaanshiksha.producer.KafkaProducerWithKeys - TimeStamp : 1571371823081
```


### Consumer
To consume data from kafka we need to follow these below steps:
* Create consumer properties
* Create the consumer
* Subscribe consumer to our topic
* Poll for new data

#### Simple Java Consumer
We have Created one simple consumer
[SimpleKafkaConsumer](kafkaSimpleConsumerProducerExample/src/main/java/com/aasaanshiksha/consumer/SimpleKafkaConsumer.java)
 which contains exactly 4 methods corresponding to operation mentioned in above block.
 
 In above solution we are just creating properties, initializing the kafka consumer, subscribe to topics and sending data. 
 Let's understand consume data part.
 
 ```java
  private static void consumeDataFromKafka(KafkaConsumer<String, String> kafkaConsumer) {
         int noDataReceived = 0;
         while (noDataReceived < 5) {
             ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
             if (!records.isEmpty()) {
                 for (ConsumerRecord<String, String> record : records) {
                     log.info("Value          : "+record.value());
                     log.info("Partition      : "+record.partition());
                     log.info("Offset         : "+record.offset());
                     log.info("Timestamp      : "+record.timestamp());
                 }
             } else {
                 noDataReceived++;
             }
         }
     }
 ```

 Kafka Consumer polls the data from kafka topic using poll method in which we provide the timeout milliseconds. KafkaConsumer
 returns ConsumerRecord if any message to read is remaining for the consumer group, Otherwise Empty ConsumerRecord will
 be returned. The KafkaConsumer takes the Deserializer for key and value which converts the byte stream to meaningful content
 like for the example it converts to string. This should be reverse process from serialization which we understand in producer
 section. The additional property it takes is auto reset, which value is earliest, latest or none. These will be used if
 any consumer got out of sync due to not running due to some reason and offset is not contained by the kafka, Then at
 which point Consumer resume
    - earliest - For the earliest data present in kafka
    - latest - For the data which will come after starting the consumer
 To provide unique records to consumer group we provide group id in configuration, so if there are many consumers
 the consumers get the records unique to topic and partition is assigned to each consumer, In case any consumer comes
 up or goes down the consumers rebalanced.

#### Consumer replay using seek and assign 
Seek and Assign are used to replay data or fetch specific message. We have 
[KafkaConsumerWithSeekAndAssign](kafkaSimpleConsumerProducerExample/src/main/java/com/aasaanshiksha/consumer/KafkaConsumerWithSeekAndAssign.java)
as sample Implementation for the seek and assign where we are skipping first 5 messages from partition 0 and read next 
5 messages. We don't have any group id as we are replaying message.

#### Client Comparability Note
* As of Kafka 0.10.2, your clients & Kafka Brokers have capability called bi-directional compatibility i.e. Your Older
client can talk to newer version of the broker or newer client can talk to older broker
* Since Kafka has bi-directional compatibility it is best practice to use latest client library version if you can.
 
## Section - 4 Advance Configuration for Producer and Consumers

### Producer Configuration

#### Acks and min.insync.replicas
Let's first understand the acks different values and what are their result with some usecase on which they can use.

##### Ack = 0
* No response is requested
* If the broker goes offline or an exception happens, producer won't know and will lose data
* Useful for data where it's okay to potentially lose few messages like Metrics collection and Log Collection

##### Ack = 1
* Leader response is requested, but replication is not a guarantee (happens in the background)
* If an ack is not received the producer may retry
* If the leader broker goes offline but replicas haven't replicated the data yet, we have a data loss
* It is less recommended approach for transactional system where each record is critical.

##### Ack = all
* Leader + Replicas ack requested, it make sure the high availability of data in case if leader broker goes down.
* This causes added latency which will introduce a little delay in your transaction but ensures no data loss by
making sure having enough replicas.
* Acks=all must be used in conjunction with min.insync.replicas. min.insync.replicas implies that at least 2 brokers
that are ISR (including leader) must respond that they have the data
* For Ex. if you use replication.factor=3, min.insync.replicas=2 and acks=all, you can only tolerate 1 broker going down,
otherwise the producer will receive an exception on send.

#### retries and max.in.flight.requests.per.connection

* In case of transient failures, developers are expected to handle exceptions, otherwise the data will lost. Ex. of transient
failure are like BrokerDownException or NotEnoughReplicasException
 * To recover from transient failures kafka client library provide retry mechanism to send message again depending on the
retries parameter to provided.
 * By default the retries parameter value is 0, You can provide this to higher number like Integer.MAX_VALUE, so it will
 try to to send message again and again until the producer get feedback.
 * In case of retries, by default there is a chance that message will sent out of order (if a batch has failed to be sent).
 * If you rely on Key-based ordering, that can be an issue.
 * For this, you can set the setting while controls how many produce request can be made in parallel, for setting us this
 you can use max.in.flight.request.per.connection property.
 * By default the value of max.in.flight.request.per.connection property value is 5, to ensure retries and order of messages
 you can set value of this to 1, but it may impact throughput drastically.
 * Due to transient failures there are chances that data is committed in kafka and producer never get acknowledgement, due
 to which producer retries and try to send it again and a duplicate message will be pushed to topic 
 * To overcome all issue Kafka >= 1.0.0 comes with a better solution.

#### enable.idempotence

* In kafka>=0.11 you can define a "idempotent producer" which won't introduce duplicates on the network error.
* If any request reaches to kafka and it commits the data and it get a request for the same data then it will send the ack
not write the data to kafka, kafka de-duplicate the data using the produce-request-id. If the kafka receives multiple requests
with same producer-request-id, it will write for the one request and discard the subsequent request and send the ack for 
each request 

* Idempotent producers are great to guarantee a stable and safe pipeline.

* Idempotent producers come with below configuration
    - retries = Integer.MAX_VALUE
    - max.in.flight.request = 1 for Kafka>0.11 & < 1.1
    - max.in.flight.request = 5 for Kafka>=1.1 for higher performance and it guarantees your message will not go out of
    order while the max.in.flight request has the value greater than 1
    - acks = all
 
 * To make your producer idempotent your need provide enable.idempotence=true in your properties.
 
#### Message Compression (compression.type)
 * Producer usually send data that is text based for example with JSON.
 * In this case it is important to apply compression to producer, because plain text JSON is very heavy.
 * Compression is enabled at Producer level and doesn't require any configuration change in the brokers or in the consumers.
 * compression.type can be none, gzip, lz4, snappy. The default value for this property is none.
 * Compression is more effective the bigger the batch of message being sent to Kafka!
 * Compressed batch has following advantages:
    - Much smaller producer request size (compression ratio up to 4x)
    - Faster to transfer data over the network => less latency
    - Better throughput
    - Better disk utilization in kafka broker (stored messages on disk are smaller)
 * There are some disadvantage of the compression too but they are very minor, Let's see what are they:
    - Producers must commit some CPU cycles to compression
    - Consumers must commit some CPU cycles to decompression
 * Find a compression algorithm that gives you the bes performance for your specific data. Test all of them!
 * Always use compression in production and especially if you have high throughput
 * Consider tweaking linger.ms and batch.size to have bigger batches, and therefore more compression and higher throughput.

#### Producer batching (linger.ms & batch.size)
* By default Kafka try to minimize the latency so tries to send records as soon as possible
    - It will have up to 5 requests in flight meaning up to 5 messages individually sent at the same time
    - After this, if more messages have to be sent while others are in flight, Kafka is smart and will start batching
    them while they wait to send them all at once.
* This smart batching allows Kafka to increase throughput while maintaing very low latency.
* Batches have higher compression ratio so better efficency.
* To control this batching behaviour we have two attributes:
    - **linger.ms** : Number of milliseconds a producer is willing to wait before sending a batch out. Default value for 
    this attribute is 0 i.e. Kafka send the message as it arrives.
        - If we modify this value with some slight value like 5 milliseconds, we can increase chances of the message 
        being sent together as batch.
        - So at the expense of introducing a small delay, we can increase throughput, compression and efficiency of our
        producer.
        - If a batch is full (batch.size) before the end of linger.ms period, it will be sent to kafka right away!
    -  **batch.size** : Maximum number of bytes that will be included in a batch. The default value for this is 16 KB.
        -   Increasing a batch size to something like 32KB or 64KB can help increasing the compression, throughput and
        efficiency of requests.
        -   Any message that is bigger than the batch size will not be batched sent directly to Kafka
        -   A batch is allocated per partition, so make sure that you don't se it to a number that's too high, otherwise
        you will waste memory which can cause producer memory problems and bring down your broker performance.
        -   Check your average message size and try to keep your batch size between 16KB to 128KB, get performance stats
        for different batch size and choose which best fit your use case.
         
#### Producer default Partitioner and how keys are hashed
* By default your keys are hashed using "murmur2' algorithm
* It is most likely preferred to not override the behaviour of the partitioner, but it is possible to do so (partitioner.class)
* The formula currently used is:
    - targetpartition = Utils.abs(Utils.murmur2(record.key()))%numPartitions.
* This means that some key will go to same partition and adding partitions to a topic will completely alter the formula.

#### max.block.ms and buffer.memory
* If the producer produces faster than the broker can take the records will be buffered in memory
* buffer.memory = 33554432 (32MB) is default size of the buffer
* That buffer will fill up over time and fill back down when the throughput to the broker increases.

* If that buffer is full (All 32MB) then the .send() method will start to block (won't return right away)
* max.block.ms=60000 the time to .send() will block until throwing exception. Exceptions are basically thrown
when 
    -   The producer has filled up it's buffer
    -   The broker is not accepting any new data
    -   60 seconds has elapsed
* If you hit an exception hit that usually means brokers are down or overloaded a they can't respond to requests
 
### Consumer Configuration

#### Delivery Semantics

##### At Most Once
In this offsets are committed as soon as the message batch is received. If processing goes wrong, the message will be 
lost (it won't read again).

##### At Least Once
I this offsets are committed after the message is processed. If the processing goes wrong, the message will be read again.
This can result in duplicate processing of messages, so make sure your processing is idempotent (i.e. processing again
 the messages won't impact your system)
 
##### Exactly Once
It can be achieved in kafka to kafka workflow using Kafka Streams API. For Kafka => Sink workflow use an idempotent consumer.


By default Kafka consumer runs on "At Least Once".


#### Control Consumer poll behaviour

##### fetch.min.bytes (Default value 1 K)
 * Controls how much data you want to pull at least on each request
 * Helps improving throughput and decreasing request number
 * Sometimes hits on latency if bigger value of this is set.
 
##### max.poll.records (Default value 500)
 * Controls how many records to receive per poll request
 * Increase if your messages are small and have a lot of available RAM
 * Good to monitor how nay records are polled per request.

##### max.partitions.fetch.bytes (Default value 1 MB)
 *  Maximum data returned by the broker per partition
 *  If you read from 100 partitions, you'll need a lot memory. So, be careful when handling 
  this parameter your own

##### fetch.max.bytes (Default value 50 MB)
 * Maximum data returned for each fetch request(covers multiple partitions)
 * The consumer performs multiple fetches in parallel  
 
**Note: Chang these setting only if your consumer maxes out on throughput already**

#### Consumer Offset Commits Strategies

There are two most common patterns for committing offsets in a consumer application 
 * enable.auto.commit = true & synchronous processing of batches
    -    With auto-commit, offsets will be committed automatically for you at regular interval 
    (auto.commit.interval.ms=500 by default) every-time you call .poll
    -   If you don't use synchronous processing, you will be in "at-most-once" behaviour because offsets will be committed
    before your data is processed.
 * enable.auto.commit = false & manual commit of offsets
    -   You control when you commit offsets and what's the condition for commiting them.
    -   You can commit offset manually using commitSync method.

#### Controlling Consumer Liveliness

When Consumer runs in consumer group, we will see how they work and some of attributes that can used to find
consumer liveliness.
* In each consumer group consumer going to Poll to broker.
* Also Each consumer talks to Consumer Coordinator another broker for heartbeat thread.
* To detect consumers that are "down", there is a "heartbeat" mechanism and a "poll" mechanism.
* To avoid issues, consumers are encouraged to process data fast and poll often

##### session.timeout.ms (default 10 seconds)
* The heartbeat thread register themselves to broker than set this parameter to broker which mean if broker does not get
heartbeat for 10 second it consider this consumer to dead.
* Heartbeats are sent periodically to broker, if no heartbeat is sent during the period, the consumer is considered dead
* Set even lower to faster consumer rebalances.

##### heartbeat.interval.ms (Default 3 seconds)
* How often to send heartbeats to broker
* Usually set to 1/3rd of session.timeout.ms

##### max.poll.interval.ms (default 5 minutes)
* Maximum amount of time between two .poll() calls before declaring the consumer dead.
* If data processing is taking much more time between two poll calls then set this number higher.

## Section - 5 Best Practices, Partitions Count and Replication Factor

### Partitions Count and Replication Factor
The two most important parameters when creating a topic. They impact performance and durability of the system overall.

* It is best to get the parameters right at the first time!
    - If the partitions count increase during a topic lifecycle, you will break your keys ordering guarantees.
    - If the replication factor increases during a topic lifecycle, you put more pressure on your cluster, which can lead 
to unexpected performance decrease and cluster imbalance.
    - Each partition can handle a throughput of few MB/s (measure it for your setup!)
    - More partitions implies:
        - Better parallelism, better throughput
        - Ability to run more consumers in a group to scale
        - Ability to leverage more brokers if you have a large cluster
        - But more elections to perform for Zookeeper
        - But more files opened on Kafka
    - Guidelines for Partition Count (No hard solution, you need to test on your infrastructure for desired throughput, but a start point to measure)
        - If have small cluster (<6 Brokers): 2 * number of brokers partition
        - If have Big cluster (>12 Brokers): 1 * number of brokers partition
        - Adjust for number of consumers you need to run in parallel at peak throughput
        - Adjust for producer throughput (increase if super-high throughput or projected increase in next two years)
        - Test with different partition count for your use case, because use case and infrastructure performance varies cluster to cluster)
        - Don't create a topic with high number of partitions until you have really a use case for that.
    - Replication Factor
        - Should be at least 1, usually 3, maximum 4
        - The higher the replication factor (N)
            - Better resilience of your system (N-1 brokers can fail)
            - But more replication higher latency will be for producer if acks set to all
            - But more disk space on your system (50% more if RF is 3 instead of 2)
        - Set it to 3 to get started
        - If replication performance is an issue, get a better broker instead of less Replication Factor


### Cluster Guidelines

* It is pretty much accepted that a broker should not hold more than 2000 to 4000 partitions (across all topics of that broker).
* Additionally, a Kafka Cluster should have a maximum of 20000 partitions across all brokers.
* The reason is that in case of brokers going down, zookeeper need to perform a lot of leader elections.


* If you need more partitions in your cluster, add brokers instead or
* If you need more than 20000 partitions in your cluster (it will take time to get there!), follow the Netflix model and 
create more Kafka clusters.

* Overall, you don't need a topic with 1000 partitions to achieve high throughput. Start at reasonable number and test performance.

## Section - 6 Advanced Topic Configurations

We have some config cli option, which will help to add, modify or delete topic related config using cli. To use cli 
below can be used. 
```shell script
./kafka-configs.sh --zookeeper <zookeeper_servers> --entity-type topics --entity-name <topic_name> --alter --add-config <property>=<value>
```

### Partitions and Segments
* Topics are made of partitions (we already know that)
* Partitions are made of segments (files)
* There are multiple segments of a partition, but at a time only one active segment (the one data is being written to)
* There are two settings for segment are present:
    - log.segment.bytes : The max size of a single segment in bytes (Default value is 1 GB)
    - log.segment.ms : The time kafka will wait before committing segment if not full (Default value is 1 Week)

* Segments comes with two indexes (files):
    - An offset to position index: allows Kafka where to read to find a message
    - A timestamp to offset index: allows Kafka to find messages with a timestamp
* Therefore, Kafka know where to find data in a constant time.

```shell script
ngupta@node1:~/kafka/data/kafka-data/example-0$ pwd
/home/ngupta/kafka/data/kafka-data/example-0
ngupta@node1:~/kafka/data/kafka-data/example-0$ ls -lrt
total 20
-rw-rw-r-- 1 ngupta ngupta  10 Oct 12 13:08 00000000000000000002.snapshot
-rw-rw-r-- 1 ngupta ngupta 333 Oct 14 03:53 00000000000000000000.log
-rw-rw-r-- 1 ngupta ngupta  10 Oct 14 04:55 00000000000000000004.snapshot
-rw-rw-r-- 1 ngupta ngupta   8 Oct 18 03:55 leader-epoch-checkpoint
-rw-rw-r-- 1 ngupta ngupta  24 Oct 18 07:18 00000000000000000000.timeindex
-rw-rw-r-- 1 ngupta ngupta   0 Oct 18 07:18 00000000000000000000.index
ngupta@node1:~/kafka/data/kafka-data/example-0$
```

* A smaller log.segment.bytes means:
    - More segments per partitions
    - Log Compaction happens more often
    - But Kafka has to keep more files opened i.e. Too many open files
    - Best practice to set the data you expect in a day or two

* A smaller log.segment.ms means:
    - You set a max frequency for log compaction
    - Maybe you want daily compaction instead of weekly

### Log Cleanup policies

* Many Kafka cluster make data expire, according to a policy this concept called log cleanup.
* There are two log cleanup policies:
    - Policy I : log.cleanup.policy=delete (Kafka default for all user topics)
        - It can be based on the age of data (default is a week)
        - It can be based on max size of log (default is -1 which means infinite)
    - Policy II : log.cleanup.policy=compact (Kafka default for topic __consumer_offsets)
        - Delete based on keys of your messages
        - Will delete old duplicate keys after the active segment is committed
        - Infinite time and space retention 

#### Log Cleanup: Why and When?
* Delete data from Kafka allows you to:
    - Control the size of the data on the disk, delete obsolete data
    - Overall: Limit maintenance work on the Kafka Cluster

* How often does log cleanup happen?
    - Log cleanup happens on your segments!
    - Smaller / More segments means that log cleanup will happen more often
    - Log cleanup shouldn't happen too often because it takes CPU and RAM resources
    - The cleaner checks for work every 15 seconds (log.cleaner.backoff.ms)
    
#### Log Cleanup Policy : Delete
* log.retention.hours
    - number of hours to keep data for (default is 168 - one week)
    - Higher number means more disk space
    - Lower number means that less data is retained (if your consumers are down for too long, they can miss data)

* log.retention.bytes
    - Max size in Bytes for each partition (default is -1 means infinite)
    - Useful to keep the size of a log under a threshold

#### Log Cleanup Policy : Compact
* Log compaction ensures that your log contains at least the last known value for a specific key within a partition
* Very useful if we just require a SNAPSHOT instead of full history(such as for data table in database)
* The idea is that we only keep the latest "update" for a key in our log

##### Log compaction Guarantees
* Any consumer that is reading from the tail of a log (most current data) will still see all the messages sent to topic.
* Ordering of the message it kept, log compaction only removes some messages, but does not re-order them
* The offset of a message is immutable (it never changes). Offsets are just skipped if a message is missing.
* Delete records can still be seen by consumers for a period of delete.retention.ms (default is 24 hours).


* Log Compaction doesn't prevent you from pushing duplicate data to Kafka
    - De-duplication is done after a segment is committed
    - Your Consumers will still read from tail as soon as the data arrives
* Log Compaction doesn't prevent you from reading duplicate data from Kafka
* Log Compaction can fail from time to time
    - It is an optimization and the compaction thread might crash
    - Make sure you assign enough memory to it and that it gets triggered
    - Restart Kafka if log compaction is broken(this is bug and may get fixed in future)
* You can't trigger Log Compaction using an API call

##### Log compaction configurations

* segment.ms - Max amount of time to wait to close active segment (Default value 7 days)
* segment.byte - Max size of a segment (Default value 1GB)
* min.compaction.lag.ms - How long to wait before a message can be compacted. (Default value 0)
* delete.retention.ms - Wait before deleting data marked for compaction (Default value 24 hours)
* min.cleanable.dirty.ratio - Higher value of this more efficient cleaning, but uses CPU and RAM resources, too lower value 
means cleaning impacted and update happens rarely (Default value 0.5)


### unclean.leader.election
* If all your In sync replicas die (but you still have out of sync replicas up), you have following option:
    - Wait for an ISR to come back online (Default)
    - Enable unclean.leader.election=true
* If you enable unclean.leader.election=true, you improve availability, but you will lose data because other messages on
ISR will be discarded
* Overall this is a very dangerous setting and implications must be understood fully before enabling it.
* This setting will be use in cases where data loss is somewhat acceptable at the trade-off of availability.