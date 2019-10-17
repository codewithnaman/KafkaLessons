# Kafka

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
We have Created one simple consumer 
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

#### Producer with callbabcks

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

```


### Consumer

#### Simple Java Consumer

#### Consumer with group 

#### Consumer replay using seek and assign 

#### Client Comparability Note

 

