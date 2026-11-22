# AI usage

## Tools and models

| Tool | Model | Used for |
|---|---|---|
| Claude (claude.ai, shared doc) | Claude | Reading the brief, risk analysis and the written plan |
| Claude Code (desktop app) | Claude Opus 5.5 (`claude-opus-5-5`) | Building: Gradle setup, all code, tests, these docs, running builds and tests, checking the live API and the emulator |

Agent instruction file: [CLAUDE.md](CLAUDE.md), committed. It holds the architecture rules, the
swap points that must not be broken, the "never" list (no secrets, no email from the app, no
body logging) and the test expectations, so any later agent session works to the same rules.

## The plan

I planned before building, in a shared doc. The main points:

1. **What they are really testing:** judgement, and code where the data source, look and feel
   and email provider can change.
2. **Two risks to handle on purpose:**
   - The reviewer scores against the written brief, which says "use a backend", while the
     recruiter said "Android + DummyJSON". So: state the deviation, and still build the seam (a
     `ProductRepository` interface plus a configurable base URL).
   - Email can't be done properly without a backend. Keys in an APK are a security failure. So:
     an `EnquiryGateway` interface with a stand-in that makes a real network call, documented
     honestly.
3. **Assumptions** for the gaps: featured = top 5 by rating, 3 top rated per category, search
   via the API, which enquiry fields are required.
4. **Architecture:** single module, package by feature, MVI + Compose, use cases only where
   they hold logic, all theming in one place, ViewModel tests with fakes.
5. **Open decisions** left for me before building: DI choice, and whether to add a small Node proxy.

## Key moments that shaped the solution

1. **Starting the build from the plan and the original brief, not a summary.** My prompt to
   Claude Code was to read the plan doc and complete the assessment. It read the plan, then
   found and read the original brief PDF, and checked the plan against the scoring table before
   writing code.
2. **The agent stopped for the two open decisions instead of guessing.** It recommended adding a
   small Node/TypeScript proxy, to meet the written brief's backend rule and to make the email
   real. **I overruled it:** the recruiter asked for Android + DummyJSON, so I kept the app calling
   DummyJSON and relied on the repository seam plus clear documentation. For DI I picked Hilt,
   with an explicit fallback to Koin if it didn't build on AGP 9.
3. **De-risking before building.** AGP 9 is new, so the first change was a Hilt + KSP spike with
   no feature code. It built, so no fallback was needed. The same baseline build showed the
   Android Studio template itself didn't compile (compileSdk 36.1 vs. libraries needing 37). That
   was fixed first, as its own commit.
4. **Checking an acceptance criterion against the real API.** "Only see products that have text
   matching" depends on DummyJSON's search. Instead of trusting it or adding a defensive filter
   by reflex, the agent queried the live API for four terms and checked every result. All
   matched in title or description, so no extra filter, and the check is written down in
   DECISIONS.md.
5. **Commits that tell the story:** the untouched template on `main`, then the build fix, then the
   feature, so the PR diff shows only the work.

## Where the AI got things wrong, and how it was caught

| What went wrong | How it was caught | Fix |
|---|---|---|
| Called `stringResource()` inside `LazyVerticalGrid`'s item builder in three screens. That lambda isn't composable, so this doesn't compile. | Compiler | Read the strings before building the grid |
| Read navigation arguments in ViewModels with `SavedStateHandle.toRoute()`, which needs `android.os.Bundle`. | 16 of 50 JVM tests failed with "Method putCharSequence in android.os.Bundle not mocked" | Each ViewModel now takes its typed route through Hilt assisted injection. No Robolectric needed, and the ViewModels are simpler. Added a rule to CLAUDE.md so it doesn't come back. |
| Set brand colours for primary/secondary only, so cards and image placeholders fell back to Material's default purple tones, which conflicts with the "look and feel in one place" goal. | A screenshot of the running app on the emulator | Added neutral surface tokens to the theme |
| (False alarm) An "app not responding" dialog appeared while scrolling the home screen on the emulator. | Logcat | Checked before calling it a bug. The emulator was overloaded: Play Store updates compiling in the background, load average around 30 on 4 cores, bytecode verification at 82 bytecodes/s. No app code was on the slow path. Needs re-checking on a quiet emulator. |

<!-- TODO (Aditya): add what you found and changed in your own review, e.g.
| Something you questioned or changed | How you noticed | What you did |
-->

## How I reviewed the output

<!-- TODO (Aditya): fill in honestly before submitting. Suggested points:
- Read every file in the PR diff. Note anything you changed or asked the agent to change.
- Ran the app on the emulator and walked through all five acceptance criteria.
- Ran ./gradlew :app:testDebugUnitTest and :app:connectedDebugAndroidTest.
- Decisions you would defend differently from the AI, if any.
-->

## Time spent

<!-- TODO (Aditya): replace with your real numbers. -->

| Phase | Time |
|---|---|
| Planning (reading the brief, plan doc, decisions) | _TODO_ |
| Building (agent sessions, directing, decisions) | _TODO_ |
| Testing (unit and UI tests, emulator walk-through) | _TODO_ |
| Reviewing (reading the diff, docs, PR) | _TODO_ |
| **Total** | _TODO_ |
