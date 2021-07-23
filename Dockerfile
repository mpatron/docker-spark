FROM ubuntu:20.04

LABEL Description="Ubuntu 20.04LTS + OpenJDK 8" authors="Mickael Patron"

# Update general
RUN apt-get update -y &&  apt-get upgrade -y && apt-get dist-upgrade -y && apt-get autoremove -y && apt-get upgrade -y && apt-get autoremove -y;

# This step will install java 8 on the image
RUN apt-get install software-properties-common -y && apt-get install openjdk-8-jdk -y && apt-get install curl -y

ENV SPARK_VERSION 2.4.8
ENV HADOOP_VERSION 2.7

# download and extract Spark 
RUN curl -OL https://archive.apache.org/dist/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz \
&&  tar -xzf spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz \
&&  mv spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION /opt/spark

# Installation of supervisor
RUN apt-get -y install supervisor

# Debugging
RUN apt-get install net-tools vim nmap -y

COPY log4fluentd.jar /opt
# Set spark home 
ENV SPARK_HOME /opt/spark
ENV PATH $SPARK_HOME/bin:$PATH

# adding conf files to all images. This will be used in supervisord for running spark master/slave
COPY conf/master.conf /opt/conf/master.conf
COPY conf/slave.conf /opt/conf/slave.conf
COPY conf/history-server.conf /opt/conf/history-server.conf

# Adding configurations for history server
COPY conf/spark-defaults.conf /opt/spark/conf/spark-defaults.conf
RUN  mkdir -p /opt/spark-events

# expose port 8080 for spark UI
EXPOSE 4040 6066 7077 8080 18080 8081

#default command: this is just an option 
CMD ["/opt/spark/bin/spark-shell", "--master", "local[*]"]
