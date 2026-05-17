#!/bin/bash
set -eu -o pipefail

# -H "Accept: application/json, text/event-stream"
curl \
    -X POST http://localhost:8083/mcp \
    -H "Accept: application/json, text/event-stream" \
    -H "Content-Type: application/json" \
    -d '{
      "jsonrpc": "2.0",
      "id": 1,
      "method": "tools/list",
      "params": {}
    }'
