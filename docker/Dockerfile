FROM tomcat:9

ENV DEBIAN_FRONTEND noninteractive
ENV APP_CONTEXT  webtex.war

#ENV SSLCERTIFICATE_FILE /path/to/cert.p12
#ENV SSLCERTIFICATE_KEY  /path/to/file-containing-key-to-cert

RUN apt-get update -q && apt-get install -qy \
    texlive texlive-latex-extra texlive-math-extra dvipng \
    && rm -rf /var/lib/apt/lists/*

ADD run.sh /opt/local/bin/run.sh
RUN chmod +x /opt/local/bin/run.sh; rm -rf /usr/local/tomcat/webapps/*
ADD webtex.war /usr/local/tomcat/webapps/$APP_CONTEXT
ADD tomcat-access-logging-*.jar /usr/local/tomcat/lib/tomcat-access-logging.jar
ADD java-logging-bunyan-*.jar /usr/local/tomcat/bin/java-logging-bunyan.jar
ADD logging.properties /usr/local/tomcat/conf/logging.properties
ADD server.xml /usr/local/tomcat/conf/server.xml
ADD setenv.sh /usr/local/tomcat/bin/setenv.sh

ENTRYPOINT ["/opt/local/bin/run.sh"]
CMD ["start"]
