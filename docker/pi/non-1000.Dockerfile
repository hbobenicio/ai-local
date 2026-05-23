FROM node:24.15.0-trixie-slim

ARG UID
ARG GID

RUN addgroup --gid ${GID} ai-code
RUN adduser  --gid ${GID} --uid ${UID} --disabled-password ai-code

USER ai-code

RUN mkdir -p /home/ai-code/.npm-global
RUN npm config set prefix /home/ai-code/.npm-global
RUN npm config set --global min-release-age 7
ENV PATH="/home/ai-code/.npm-global/bin:$PATH"

VOLUME  /home/ai-code/workspace
WORKDIR /home/ai-code/workspace

ENV PI_SKIP_VERSION_CHECK=1
ENV PI_OFFLINE=1
RUN npm install -g '@earendil-works/pi-coding-agent@0.74.0'
RUN pi install npm:pi-permission-system

COPY --chown=ai-code:ai-code ./agent /home/ai-code/.pi/agent/

# CMD ["pi"]
CMD ["bash"]

