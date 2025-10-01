from enum import Enum
from typing import List

class OID(Enum):
    CPU_TOTAL_LOAD = "1_4"
    RAM_FREE = "2_1"

    @classmethod
    def values(cls) -> List[str]:
        return [member.value for member in cls]