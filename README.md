# linlisHomePage

当前 `master` 分支整合全栈应用、开发计划与 Cursor agent-skills 配置。后续开发计划见 [ROADMAP.md](ROADMAP.md)。

## Agent skills (Cursor)

This repo is configured with [agent-skills](https://github.com/addyosmani/agent-skills) using **Option 1: Rules Directory**.

- **Always loaded:** `.cursor/rules/test-driven-development.md`, `code-review-and-quality.md`, `incremental-implementation.md`
- **On demand:** `./scripts/agent-skills.sh add <skill>` / `remove <skill>`
- **Docs:** `.cursor/AGENT-SKILLS.md` and `.cursor/rules/README.md`
- **Code review agent:** `.cursor/agents/code-reviewer.md`
