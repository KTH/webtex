FROM ubuntu:16.04

RUN add-apt-repository ppa:jonathonf/texlive; apt-get install tomcat texlive texlive-latex-extra texlive-math-extra dvipng

ENV APP_CONTEXT  webtex.war

ADD run.sh /usr/local/bin/run.sh
RUN chmod +x /usr/local/bin/run.sh; rm -rf /usr/local/tomcat/webapps/*
ADD office365.war /usr/local/tomcat/webapps/$APP_CONTEXT

ENTRYPOINT ["/usr/local/bin/run.sh"]
CMD ["start"]
