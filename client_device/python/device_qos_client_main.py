from typing import List
from fastapi import FastAPI, Query

import uvicorn
import logging
from utils.global_constants import PATH, WINDOW_SIZE
from cpu_total_load_1_4.cpu_total_load_1_4 import start_measuring_cpu, get_batch_cpu, stop_measuring_cpu
from ram_free_2_1.ram_free_2_1 import start_measuring_ram, get_batch_ram, stop_measuring_ram
from utils.oid import OID

logger = logging.getLogger("uvicorn")

# SERVER
    
async def on_startup():
    logger.info("Application is initializing...")
    start_measuring_cpu()
    start_measuring_ram()
    logger.info("Application has been initialized.")

async def on_shutdown():
    logger.info("Application is cleaning up...")
    stop_measuring_cpu()
    stop_measuring_ram()
    logger.info("Application has been cleaned up.")

app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None, on_startup=[on_startup], on_shutdown=[on_shutdown])

@app.get(PATH)
async def get(batch: int = Query(default=WINDOW_SIZE), oids: List[str] = Query(default=OID.values())):
    respone = {}
    if OID.CPU_TOTAL_LOAD.value in oids:
        respone[OID.CPU_TOTAL_LOAD.value] = get_batch_cpu(batch)
    if OID.RAM_FREE.value in oids:
        respone[OID.RAM_FREE.value] = get_batch_ram(batch)
    return respone

def main():
    logger.info("Arrowhead DeviceQoSClient")
    config = uvicorn.Config(app, host="0.0.0.0", port=9473)
    server = uvicorn.Server(config)
    server.run()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Server stopped by user.")