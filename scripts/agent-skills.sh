#!/usr/bin/env bash
# Manage phase-specific agent-skills rules for Cursor.
# Essential skills are always in .cursor/rules/; use this script to add/remove others on demand.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RULES_DIR="$REPO_ROOT/.cursor/rules"
SKILLS_SRC="${AGENT_SKILLS_PATH:-/tmp/agent-skills}/skills"

declare -A PHASE_SKILLS=(
  [spec-development]=spec-driven-development
  [frontend-ui]=frontend-ui-engineering
  [security]=security-and-hardening
  [performance]=performance-optimization
)

usage() {
  cat <<'EOF'
Usage: scripts/agent-skills.sh <command> [skill...]

Commands:
  add <skill>     Copy a phase-specific skill into .cursor/rules/
  remove <skill>  Remove a phase-specific skill from .cursor/rules/
  list            Show available phase-specific skills and load status
  sync            Re-copy essential skills from agent-skills source

Phase-specific skills:
  spec-development   spec-driven-development
  frontend-ui        frontend-ui-engineering
  security           security-and-hardening
  performance        performance-optimization

Environment:
  AGENT_SKILLS_PATH  Path to cloned addyosmani/agent-skills repo (default: /tmp/agent-skills)

Examples:
  scripts/agent-skills.sh add security performance
  scripts/agent-skills.sh remove security
  scripts/agent-skills.sh list
EOF
}

ensure_source() {
  if [[ ! -d "$SKILLS_SRC" ]]; then
    echo "agent-skills source not found at: $SKILLS_SRC" >&2
    echo "Clone it with: git clone https://github.com/addyosmani/agent-skills.git /tmp/agent-skills" >&2
    exit 1
  fi
}

copy_skill() {
  local rule_name="$1"
  local skill_dir="$2"
  local src="$SKILLS_SRC/$skill_dir/SKILL.md"
  local dest="$RULES_DIR/$rule_name.md"

  if [[ ! -f "$src" ]]; then
    echo "Missing skill file: $src" >&2
    exit 1
  fi

  mkdir -p "$RULES_DIR"
  cp "$src" "$dest"
  echo "Added $dest"
}

remove_skill() {
  local rule_name="$1"
  local dest="$RULES_DIR/$rule_name.md"

  if [[ -f "$dest" ]]; then
    rm "$dest"
    echo "Removed $dest"
  else
    echo "Not loaded: $dest"
  fi
}

sync_essential() {
  ensure_source
  copy_skill test-driven-development test-driven-development
  copy_skill code-review-and-quality code-review-and-quality
  copy_skill incremental-implementation incremental-implementation
}

list_skills() {
  echo "Essential (always loaded):"
  for name in test-driven-development code-review-and-quality incremental-implementation; do
    if [[ -f "$RULES_DIR/$name.md" ]]; then
      echo "  [loaded] $name"
    else
      echo "  [missing] $name"
    fi
  done

  echo
  echo "Phase-specific (load on demand):"
  for rule_name in "${!PHASE_SKILLS[@]}"; do
  skill_dir="${PHASE_SKILLS[$rule_name]}"
    if [[ -f "$RULES_DIR/$rule_name.md" ]]; then
      echo "  [loaded] $rule_name -> $skill_dir"
    else
      echo "  [available] $rule_name -> $skill_dir"
    fi
  done
}

cmd="${1:-}"
shift || true

case "$cmd" in
  add)
    [[ $# -gt 0 ]] || { usage; exit 1; }
    ensure_source
    for rule_name in "$@"; do
      skill_dir="${PHASE_SKILLS[$rule_name]:-}"
      if [[ -z "$skill_dir" ]]; then
        echo "Unknown phase skill: $rule_name" >&2
        exit 1
      fi
      copy_skill "$rule_name" "$skill_dir"
    done
    ;;
  remove)
    [[ $# -gt 0 ]] || { usage; exit 1; }
    for rule_name in "$@"; do
      if [[ -z "${PHASE_SKILLS[$rule_name]:-}" ]]; then
        echo "Unknown phase skill: $rule_name" >&2
        exit 1
      fi
      remove_skill "$rule_name"
    done
    ;;
  list)
    list_skills
    ;;
  sync)
    sync_essential
    ;;
  ""|-h|--help|help)
    usage
    ;;
  *)
    echo "Unknown command: $cmd" >&2
    usage
    exit 1
    ;;
esac
