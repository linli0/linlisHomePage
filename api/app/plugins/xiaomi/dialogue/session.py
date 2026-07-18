"""Global dialogue state machine (process singleton)."""

from __future__ import annotations

import enum
import threading
import time
from dataclasses import dataclass, field


class Route(str, enum.Enum):
    IDLE = "idle"
    MULTI = "multi"
    CODEX = "codex"
    PANEL = "panel"
    DEBUG = "debug"
    SPEAKING = "speaking"


@dataclass
class DialogueState:
    route: Route = Route.IDLE
    speaking: bool = False
    listening: bool = False
    last_input_at: float = field(default_factory=time.time)
    return_route: Route = Route.MULTI  # after speaking
    history: list[dict[str, str]] = field(default_factory=list)
    lock: threading.RLock = field(default_factory=threading.RLock)

    def touch(self) -> None:
        self.last_input_at = time.time()

    def idle_expired(self, idle_sec: int) -> bool:
        if self.route in (Route.IDLE,):
            return False
        return (time.time() - self.last_input_at) >= idle_sec

    def snapshot(self) -> dict:
        return {
            "route": self.route.value,
            "speaking": self.speaking,
            "listening": self.listening,
            "halfDuplex": self.speaking,
            "lastInputAt": self.last_input_at,
            "historyLen": len(self.history),
        }


_STATE = DialogueState()


def get_state() -> DialogueState:
    return _STATE
