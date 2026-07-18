"""Fixed dialogue keywords."""

from __future__ import annotations

WAKE = "小爱小爱"
EXIT_MULTI = "小爱同学"
ENTER_CODEX = ("进入codex模式", "codex模式")
EXIT_CODEX = "退出codex模式"
ENTER_PANEL = "进入panel"
EXIT_PANEL = "退出panel"
ENTER_DEBUG = "进入调试模式"
EXIT_DEBUG = "退出调试模式"


def norm(text: str) -> str:
    return "".join((text or "").strip().lower().split())


def contains(text: str, phrase: str) -> bool:
    return norm(phrase) in norm(text)


def match_enter_codex(text: str) -> bool:
    return any(contains(text, p) for p in ENTER_CODEX)


def match_exit_codex(text: str) -> bool:
    return contains(text, EXIT_CODEX)


def match_enter_panel(text: str) -> bool:
    return contains(text, ENTER_PANEL)


def match_exit_panel(text: str) -> bool:
    return contains(text, EXIT_PANEL)


def match_enter_debug(text: str) -> bool:
    return contains(text, ENTER_DEBUG)


def match_exit_debug(text: str) -> bool:
    return contains(text, EXIT_DEBUG)


def match_wake(text: str) -> bool:
    return contains(text, WAKE)


def match_exit_multi(text: str) -> bool:
    return contains(text, EXIT_MULTI)
