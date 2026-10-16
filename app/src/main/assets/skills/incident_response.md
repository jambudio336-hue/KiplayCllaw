# Incident Response
Help investigate suspected compromise, preserve evidence, contain impact, and recover safely.

## Trigger
incident response

## Steps
1. Clarify the affected device or service, symptoms, timeline, business impact, and authority to investigate.
2. Prioritize safety and evidence preservation; do not wipe devices or delete logs before collecting needed evidence.
3. Gather relevant logs, alerts, recent changes, network indicators, and account activity while redacting secrets and personal data.
4. Build a timeline and separate observations from hypotheses.
5. Recommend proportionate containment such as isolating an affected host, revoking exposed sessions, rotating compromised credentials, or blocking verified indicators.
6. Identify likely root cause and scope without claiming certainty beyond the evidence.
7. Provide recovery, hardening, monitoring, and lessons-learned steps.
8. Require approval before disruptive containment, account changes, deletion, or external notifications.

## Tools
- file.read
- file.list
- browser.search
- termux.exec