# Kafka

## Section - 1 Theory

#### Topics partitions and offsets

#### Brokers and topics

#### Topic replication

#### Producers and message keys

#### Consumer and consumer group

#### Consumer offsets and delivery semantics

#### Kafka broker discovery

#### ZooKeeper

#### Kafka guarantees

## Section - 2 Kafka CLI

### Downloading and Starting Kafka

##### Downloading Kafka and adding to PATH
 To start working with Kafka, download Kafka binary and extract it from below link:
 
 https://kafka.apache.org/downloads
 
 To run the kafka commands from any location we will add the extracted kafka path to PATH in profile. For this
 we will edit .profile file
 
 Add below line in .profile file
  * export PATH="$PATH:<Path to Kafka bin folder>"

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


