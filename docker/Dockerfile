FROM tomcat:9

ENV DEBIAN_FRONTEND noninteractive
ENV APP_CONTEXT  webtex.war

RUN apt-get update -q && apt-get install -qy \
    texlive texlive-latex-extra texlive-math-extra dvipng \
    && rm -rf /var/lib/apt/lists/*

ADD run.sh /opt/local/bin/run.sh
RUN chmod +x /opt/local/bin/run.sh; rm -rf /usr/local/tomcat/webapps/*
ADD webtex.war /usr/local/tomcat/webapps/$APP_CONTEXT

ENTRYPOINT ["/opt/local/bin/run.sh"]
CMD ["start"]

#FROM ubuntu:16.04

#RUN add-apt-repository ppa:jonathonf/texlive; apt-get install tomcat texlive texlive-latex-extra texlive-math-extra dvipng
