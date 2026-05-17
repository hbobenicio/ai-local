import os
from fastmcp import FastMCP

mcp = FastMCP("HelloMCP")

@mcp.tool
def pior_linguagem_de_programacao() -> str:
    """Responde qual é a pior linguagem de programação do mundo"""
    return "Java"

def main():
    server_host = os.environ.get('MCP_SERVER_HOST', '127.0.0.1')
    server_port = int(os.environ.get('MCP_SERVER_PORT', '8083'))
    mcp.run(transport='http', stateless_http=True, host=server_host, port=server_port)

if __name__ == "__main__":
    main()
