from app.plugins.xiaomi.dialogue import keywords


def test_wake_and_exit():
    assert keywords.match_wake("你好小爱小爱在吗")
    assert keywords.match_exit_multi("小爱同学再见")
    assert keywords.match_enter_codex("请进入codex模式")
    assert keywords.match_enter_codex("codex模式")
    assert keywords.match_exit_codex("退出codex模式")
    assert keywords.match_enter_panel("进入panel")
    assert keywords.match_enter_debug("进入调试模式")


def test_session_idle():
    from app.plugins.xiaomi.dialogue.session import Route, get_state
    import time

    st = get_state()
    with st.lock:
        st.route = Route.MULTI
        st.last_input_at = time.time() - 999
        assert st.idle_expired(120)
        st.route = Route.IDLE
