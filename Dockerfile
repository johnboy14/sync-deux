FROM ubuntu:14.04
MAINTAINER john
ENV REFRESHED_AT 2015-08-02

RUN apt-get update
RUN apt-get -y install software-properties-common

#Install Rsync to pull data from govtrack
RUN apt-get install rsync

# Install Java.
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Install Leiningen 2.5.0 and make executable
RUN apt-get install wget

# Leiningen
ENV LEIN_ROOT true

RUN wget -q -O /usr/bin/lein \
    https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
    && chmod +x /usr/bin/lein

RUN lein

#Create folder for congress information
RUN cd && mkdir -p congress/bills/114/local congress/bills/114/copy congress/votes/114/local congress/votes/114/copy congress/114/legislators

# Install the cron service
RUN apt-get install cron -y

#Add code
ADD . /root/code

#Use the crontab file
RUN crontab /root/code/scripts/crons.conf

#Run Job
#ENTRYPOINT ["/root/code/scripts/startup.sh"]