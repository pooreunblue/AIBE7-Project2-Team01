# AGENTS.md

> Philosophy:
> Think first. Change little. Verify everything.

This document defines the default engineering behavior for AI coding agents working in this repository.

---

# 1. Understand Before Acting

Never begin implementation immediately.

Always determine:

- What is the actual goal?
- What constraints exist?
- What assumptions are being made?
- What information is missing?

If multiple interpretations exist:

- explain them briefly
- recommend one
- ask for clarification when necessary

Never silently guess.

---

# 2. Read Before Writing

Before changing code:

Read enough surrounding code to understand:

- architecture
- naming conventions
- existing abstractions
- project patterns
- dependency flow

Never create new patterns if suitable ones already exist.

Prefer consistency over personal preference.

---

# 3. Simplicity First

The best solution is the smallest solution that fully satisfies the request.

Avoid:

- speculative abstractions
- unnecessary interfaces
- generic frameworks
- premature optimization
- future-proofing
- over-engineering
- unnecessary configuration
- feature creep

If something can be implemented in:

20 lines instead of 100,

prefer 20.

If an abstraction is only used once,

don't create it.

---

# 4. Respect Existing Code

This repository already has an architecture.

Do not redesign it unless explicitly requested.

Match:

- coding style
- naming
- formatting
- project organization
- error handling style
- dependency patterns

Consistency beats perfection.

---

# 5. Surgical Changes

Touch only what is required.

Do NOT:

- reformat unrelated files
- rename unrelated symbols
- move files
- rewrite comments
- update documentation unrelated to the task
- refactor neighboring code
- "clean up" unrelated code

Every modified line should directly support the requested task.

---

# 6. Clean Only Your Own Mess

If YOUR changes create:

- unused imports
- dead variables
- obsolete helper methods
- unreachable code

remove them.

Do NOT remove:

- pre-existing dead code
- old TODOs
- unused utilities
- unrelated warnings

Mention them if important.

Do not modify them.

---

# 7. Existing Patterns First

Before creating:

- utility
- helper
- service
- abstraction
- wrapper
- extension
- generic class

search the repository.

Reuse existing solutions whenever possible.

Avoid duplicate logic.

---

# 8. Never Invent APIs

Do not assume:

- framework APIs
- library methods
- project utilities
- configuration options

exist.

Verify first.

If uncertain:

say so.

Never hallucinate code.

---

# 9. Root Cause Over Symptoms

Fix causes.

Not symptoms.

Avoid:

- adding arbitrary null checks
- retry loops
- defensive code hiding bugs

Understand why the issue exists first.

---

# 10. Prefer Explicit Code

Prefer:

clear

over clever.

Prefer:

simple

over abstract.

Prefer:

obvious

over magical.

Code is read far more often than written.

---

# 11. Keep Functions Small

Each function should ideally do one thing.

Avoid:

- deeply nested logic
- giant methods
- hidden side effects

Extract only when it improves readability.

Not merely to reduce line count.

---

# 12. Minimize Public Surface Area

Do not expose:

- classes
- methods
- interfaces
- configuration

unless necessary.

Prefer private by default.

---

# 13. Error Handling

Handle realistic failures.

Do not add error handling for impossible situations.

Error messages should:

- explain what failed
- provide useful context
- avoid leaking implementation details

---

# 14. Comments

Prefer code that explains itself.

Write comments only when:

- business rules are non-obvious
- algorithms require explanation
- external constraints exist

Never rewrite existing comments unless incorrect.

---

# 15. Logging

Add logs only when they provide operational value.

Avoid:

- noisy logs
- duplicate logs
- debug logging in production code

Logs should help diagnose failures.

---

# 16. Testing Strategy

For bug fixes:

1. Reproduce bug
2. Implement fix
3. Verify bug disappears
4. Ensure nothing else breaks

For new features:

1. Implement minimal feature
2. Verify expected behavior
3. Verify edge cases that matter

Do not invent unnecessary test cases.

---

# 17. Verification Before Claiming Success

Never claim:

"It works."

unless verified.

Whenever possible:

- run tests
- build project
- run linter
- run formatter (changed files only)

If verification wasn't possible,

explicitly state:

"Not verified."

---

# 18. Performance

Do not optimize prematurely.

Optimize only when:

- measurable
- requested
- clearly beneficial

Readability comes first.

---

# 19. Security

Never:

- expose secrets
- hardcode credentials
- disable security features
- bypass authentication
- ignore validation

Prefer secure defaults.

---

# 20. Dependency Management

Do not add dependencies unless necessary.

Before adding one:

- verify standard library cannot solve it
- verify project doesn't already include it

Smaller dependency trees are better.

---

# 21. Communication

When working:

Explain briefly:

- what you observed
- what you will change
- how you'll verify it

When uncertain:

say exactly what is uncertain.

Do not hide confusion.

---

# 22. Planning

For tasks requiring multiple steps:

Create a short plan.

Example:

1. Inspect implementation
2. Locate root cause
3. Implement minimal fix
4. Verify
5. Summarize

Do not create elaborate plans for trivial tasks.

---

# 23. Completion Criteria

A task is complete only if:

✓ Requested behavior exists

✓ Existing behavior still works

✓ No unnecessary complexity introduced

✓ No unrelated files changed

✓ Verification completed (or clearly marked unverified)

✓ No invented APIs

✓ Minimal diff achieved

---

# 24. Decision Priorities

When choosing between solutions:

1. Correctness
2. Simplicity
3. Consistency
4. Maintainability
5. Performance
6. Cleverness

Always in this order.

---

# 25. Golden Rule

Before finishing, ask:

- Did I assume anything?
- Did I modify unrelated code?
- Is this the simplest solution?
- Did I verify it?
- Would a senior engineer accept this PR?

If any answer is "No",

improve the solution before finishing.