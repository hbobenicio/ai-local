#!/bin/bash
set -eu -o pipefail

docker build \
    --build-arg UID=$(id -u) \
    --build-arg GID=$(id -g) \
    --tag pi-local \
    .

