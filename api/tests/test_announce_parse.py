from app.plugins.xiaomi.watchers.parse import (
    FileRollup,
    feed_codex_chunk,
    feed_cursor_chunk,
    format_announce,
    is_subagent_path,
)


def test_is_subagent_path():
    assert is_subagent_path(
        r"C:\x\agent-transcripts\abc\subagents\run.jsonl"
    )
    assert not is_subagent_path(r"C:\x\agent-transcripts\abc\abc.jsonl")


def test_cursor_parent_detailed_uses_last_text():
    st = FileRollup()
    chunk = "\n".join(
        [
            '{"role":"assistant","message":{"content":[{"type":"text","text":"已修复鉴权测试"}]}}',
            '{"type":"turn_ended","status":"success"}',
        ]
    )
    events = feed_cursor_chunk(st, chunk, subagent=False)
    assert len(events) == 1
    assert events[0].kind == "cursor"
    assert events[0].brief == "Cursor 任务完成"
    assert "鉴权" in events[0].detailed
    assert format_announce(events[0], detail="brief") == "Cursor 任务完成"
    assert "鉴权" in format_announce(events[0], detail="detailed")


def test_cursor_subagent_prefers_final_summary():
    st = FileRollup()
    chunk = "\n".join(
        [
            '{"role":"assistant","message":{"content":[{"type":"text","text":"长文"},{"type":"tool_use","name":"UpdateCurrentStep","input":{"final_summary":"测过了十个用例","completed_subtitle":"tests ok"}}]}}',
            '{"type":"turn_ended","status":"success"}',
        ]
    )
    events = feed_cursor_chunk(st, chunk, subagent=True)
    assert len(events) == 1
    assert events[0].kind == "subagent"
    assert events[0].detailed == "测过了十个用例"
    assert format_announce(events[0], detail="brief") == "子任务完成"


def test_ignores_substring_turn_ended_in_tool_input():
    st = FileRollup()
    chunk = (
        '{"role":"assistant","message":{"content":[{"type":"tool_use","name":"Grep",'
        '"input":{"pattern":"turn_ended"}}]}}'
    )
    assert feed_cursor_chunk(st, chunk, subagent=False) == []


def test_codex_task_complete():
    chunk = (
        '{"type":"event_msg","payload":{"type":"task_complete",'
        '"last_agent_message":"已完成 PR 创建"}}'
    )
    events = feed_codex_chunk(chunk)
    assert len(events) == 1
    assert events[0].kind == "codex"
    assert "PR" in format_announce(events[0], detail="detailed")
