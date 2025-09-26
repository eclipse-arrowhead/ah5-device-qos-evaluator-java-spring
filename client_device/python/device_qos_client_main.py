from fastapi import FastAPI
from contextlib import asynccontextmanager
import uvicorn
import time

from cpu_total_load_1_4.cpu_total_load_1_4 import start_measuring, get_batch, stop_measuring

import logging

logger = logging.getLogger("uvicorn")
logger.info("Arrowhead DeviceQoSClient")

# SERVER
    
async def on_startup():
    logger.info("Application is initializing...")
    start_measuring()
    logger.info("Application has been initialized.")

async def on_shutdown():
    logger.info("Application is cleaning up...")
    stop_measuring()
    logger.info("Application has been cleaned up.")

app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None, on_startup=[on_startup], on_shutdown=[on_shutdown])

@app.get("/device-qos/cpu")
async def get_time():
    return get_batch()


def main():
    config = uvicorn.Config(app, host="0.0.0.0", port=9473)
    server = uvicorn.Server(config)
    server.run()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Server stopped by user.")