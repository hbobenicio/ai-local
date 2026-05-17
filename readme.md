# README

```bash
LLM_MODELS_DIR=$HOME/llm/models/ LLM_MODEL=Qwen3-4B-Q4_K_M.gguf docker compose up --build llamacpp opencode
```

```bash
LLM_MODELS_DIR=$HOME/llm/models/ LLM_MODEL=Qwen3-4B-Q4_K_M.gguf docker compose exec opencode bash
```

```bash
LLM_MODELS_DIR=$HOME/llm/models/ LLM_MODEL=Qwen3-4B-Q4_K_M.gguf docker compose down --remove-orphans --volumes
```
