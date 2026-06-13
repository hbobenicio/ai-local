FROM node:24.15.0-trixie-slim

RUN apt update \
 && apt install -y fd-find ripgrep git \
 && rm -rf /var/lib/apt/lists/*

USER node

RUN mkdir -p /home/node/.npm-global
RUN npm config set prefix /home/node/.npm-global
RUN npm config set --global min-release-age 7
ENV PATH="/home/node/.npm-global/bin:$PATH"

VOLUME  /home/node/workspace
WORKDIR /home/node/workspace

ENV PI_SKIP_VERSION_CHECK=1
ENV PI_OFFLINE=1
RUN npm install -g '@earendil-works/pi-coding-agent@0.78.0'
RUN pi  install    "npm:pi-permission-system"
RUN pi  install    "npm:@juicesharp/rpiv-ask-user-question"

# NOTE: plannotator extension is buggy, integrates with browser which is problematic inside a container.
#       so for plans, just write a skill your self or steer the model to write PLAN.md and execute each step at a time
#       use grill-me for better plans

RUN ln -s /usr/bin/fdfind /home/node/.npm-global/bin/fd

COPY --chown=node:node ./agent /home/node/.pi/agent/

CMD ["pi"]
# CMD ["bash"]

