FROM ubuntu:26.04

RUN chown -R ubuntu:ubuntu /usr/local/lib/
RUN chown -R ubuntu:ubuntu /usr/local/bin/
RUN chown -R ubuntu:ubuntu /usr/local/share/

RUN apt update
RUN apt install nodejs npm -y

USER ubuntu
RUN npm install -g opencode-ai

COPY --chown=ubuntu:ubuntu ./docker/opencode/opencode.json /home/ubuntu/.config/opencode/opencode.json

# TODO
# WORKDIR /workspace

# CMD [ "opencode", "--print-logs", "--log-level", "DEBUG" ]
CMD ["sleep", "infinity"]
