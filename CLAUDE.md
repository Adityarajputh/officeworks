# CLAUDE.md

Instructions for AI coding agents working in this repo. Humans: see README.md.

## What this is

An Android proof of concept for a product catalogue: home (5 featured products), category
pages, search, product details and an enquiry form. Kotlin, Jetpack Compose, MVI, Hilt.
Product data comes from DummyJSON (https://dummyjson.com). The client expects the data source,
the look and feel and the email provider to change, so the code is built around swap points.

## Commands

```bash
./gradlew :app:assembleDebug              # build
./gradlew :app:testDebugUnitTest          # JVM tests (fast, run after every change)
./gradlew :app:connectedDebugAndroidTest  # Compose UI tests, needs a running emulator
./gradlew :app:installDebug               # install on the running emulator
```

## Architecture rules

- Package by feature: `catalogue/`, `enquiry/`. Inside each: `domain/`, `data/`, `di/`, `ui/`.
  Shared code lives in `core/`. Navigation lives only in `navigation/`.
- `domain/` is plain Kotlin. No Android, Retrofit or serialization imports.
- DTOs (`*Dto`) never leave `data/`. Map them to domain models in `data/remote/*Mappers.kt`.
- Screens never call Retrofit or a repository directly. Flow: Composable -> ViewModel -> use case or
  repository interface -> data implementation.
- Every network call goes through `safeApiCall` and returns `DataResult`. Nothing below the
  ViewModel throws for expected failures.
- MVI per screen: `XContract.kt` holds `XState` (immutable data class) and `XIntent` (sealed).
  The ViewModel extends `MviViewModel` and is the only thing that changes state. Composables are
  split into `XRoute` (gets the ViewModel) and `XScreen` (stateless, takes state and an `onIntent`
  lambda).
- Things the UI shows once (a snackbar) are state, not events: a nullable field in `XState`
  plus an `XIntent.…Shown` intent the screen sends after showing it, inside
  `repeatOnLifecycle(STARTED)`. No `Channel` or `SharedFlow` events: they can be consumed and
  lost on rotation or in the background. See `EnquiryState.submitError`.
- Errors: screens use `ErrorView` (message plus retry, no retry for `NOT_FOUND`), `LoadingView`
  and `MessageView` from `core/ui`. Product images go through `ProductImage`, which shows a
  placeholder when an image is missing or fails to load.
- ViewModels that need navigation arguments get the typed `Destination` through Hilt assisted
  injection (`@AssistedInject` plus a `Factory`). Do not use `SavedStateHandle.toRoute()` in a
  ViewModel: it needs `android.os.Bundle` and breaks JVM unit tests.
- Add a use case only when it holds a rule (featured selection, category split, enquiry
  validation). No pass-through use cases.
- Colours and type only in `ui/theme/`. Screens use `MaterialTheme.colorScheme`, never raw colours.
- User-facing text goes in `res/values/strings.xml`.

## Swap points (keep them intact)

| Change | Where |
|---|---|
| Product data source | New `ProductRepository` impl, bind it in `catalogue/di/CatalogueModule.kt`; base URL in `gradle.properties` (`officeworks.apiBaseUrl`) |
| Email / enquiry delivery | New `EnquiryGateway` impl, bind it in `enquiry/di/EnquiryModule.kt` |
| Which products are featured | New `FeaturedProductsProvider` impl, bind it in `CatalogueModule` |
| Look and feel | `ui/theme/Color.kt`, `Type.kt`, `Theme.kt` |

## Never

- Never send email from the app or add email-provider keys, API keys or any secret to the repo or
  the APK. Enquiries must go through a backend in production.
- Never log request or response bodies: enquiries contain the customer's name, email and phone number.
  Log through Timber only (`safeApiCall` already logs failure causes); never `android.util.Log`.
- Never allow cleartext HTTP.
- Never commit `local.properties`.

## Tests

- Change a ViewModel, use case, validator, mapper or repository? Add or update its JVM test in
  `app/src/test`. Use the fakes in `testutil/` and `enquiry/FakeEnquiryGateway.kt`, not mocks.
- Repository tests use MockWebServer with JSON fixtures in `app/src/test/resources/dummyjson/`.
- `MainDispatcherRule` swaps `Dispatchers.Main` for ViewModel tests.
- Run `./gradlew :app:testDebugUnitTest` before saying a change is done.
