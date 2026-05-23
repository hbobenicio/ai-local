#!/bin/bash
set -eu -o pipefail

if [ $# -ne 1 ]; then
    echo "error: bad args" >&2
    echo "usage: pi-start.sh <WORKSPACE>"
    exit 1
fi

# host network to access the model started by lm-studio
# in case you run llm with llama.cpp on another docker container, this can be avoided
docker run -it --rm --network host -v "$1:/home/ai-code/workspace" pi-local

