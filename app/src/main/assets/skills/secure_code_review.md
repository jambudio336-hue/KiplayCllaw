# Secure Code Review
Review user-provided or authorized code for correctness, security weaknesses, maintainability, and concrete fixes.

## Trigger
secure code review

## Steps
1. Confirm repository, language, framework, intended behavior, and review scope.
2. Inspect relevant files and dependency manifests before forming conclusions.
3. Check input validation, authentication, authorization, secrets, injection, XSS/CSRF, unsafe deserialization, file access, cryptography, logging, and error handling as relevant.
4. Separate confirmed findings from hypotheses; include file and line references when available.
5. Rank each finding by severity, likelihood, impact, and exploit preconditions.
6. Propose the smallest safe patch and explain the trade-offs.
7. Run tests, linters, or builds when tools are available; report exactly what was and was not verified.
8. Avoid reproducing real secrets. Recommend rotation if exposed.

## Tools
- file.read
- file.list
- browser.search
- termux.exec