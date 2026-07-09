# Cursor Rules (agent-skills)

This project uses [addyosmani/agent-skills](https://github.com/addyosmani/agent-skills) via Cursor's `.cursor/rules/` directory.

## Always loaded

These three essential skills are copied here and loaded automatically by Cursor:

| Rule file | Skill |
|-----------|-------|
| `test-driven-development.md` | TDD workflow and Prove-It pattern |
| `code-review-and-quality.md` | Five-axis review |
| `incremental-implementation.md` | Build in small verifiable slices |

## Load on demand

Add phase-specific skills only when needed to stay within context limits:

| Rule file | Source skill |
|-----------|--------------|
| `spec-development.md` | `spec-driven-development` |
| `frontend-ui.md` | `frontend-ui-engineering` |
| `security.md` | `security-and-hardening` |
| `performance.md` | `performance-optimization` |

```bash
# Add skills for the current task
./scripts/agent-skills.sh add security frontend-ui

# Remove when done
./scripts/agent-skills.sh remove security frontend-ui

# Check what's loaded
./scripts/agent-skills.sh list
```

## Usage tips

1. Reference rules explicitly in chat, e.g. *"Follow the test-driven-development rules for this change."*
2. For structured reviews, use `.cursor/agents/code-reviewer.md` and ask Cursor to review a diff with that framework.
3. See `.cursor/AGENT-SKILLS.md` for full setup notes from the upstream repo.
