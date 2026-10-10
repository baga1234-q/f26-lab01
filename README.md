# Lab 1 Starter: Booking Service

A small room-booking service. Users book rooms for time intervals, and if a room is
taken they land on a waitlist. It is the codebase you work in for Lab 1.

**Read `ARCHITECTURE.md` first.** It maps the three layers (domain / service / repo)
so you do not have to cold-read every file.

## Build and test

```
mvn test
```

One test fails on purpose. Diagnosing and fixing it is Milestone 1.
By the way: the fix may not be where the failing test first points you. :-)

## Where things are

- Source: `src/main/java/edu/cmu/cs214/booking/` (`domain/`, `service/`, `repo/`)
- Tests: `src/test/java/edu/cmu/cs214/booking/`
- Setup: `SETUP.md`
- Your Milestone 2 task: `TASK.md`
- A proposed change you will review in Milestone 3: `changes/agent-attempt.patch` (the handout tells you how to apply it)
- Transcript export script (for Claude Code; modify if using a different tool): `tools/export-transcripts.sh` (the handout tells you when to run it)

See the Lab 1 handout on the course page for the three milestones you show a TA.

## Tooling and models used for this lab

- Agent: Doubao (Doubao Work, agent mode) — driving diagnosis, patch review,
  cancelBooking implementation, and test authoring.
- Toolchain: Microsoft OpenJDK 25 (`.jdks/ms-25.0.3`), Apache Maven 3.9.6,
  JUnit Jupiter 5.10.2.
- Local transcripts are exported under `transcripts/` and are git-ignored per
  `.gitignore`; they are shown to the TA at recitation but not pushed.

