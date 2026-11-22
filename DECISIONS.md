# Decisions and assumptions

The brief scores judgement, not feature count. This file records what I assumed, what I cut
and why, and how the code is meant to grow.

## The shape of the solution

```
Compose screen ──intent──▶ ViewModel (MVI) ──▶ use case / interface ──▶ data implementation ──▶ DummyJSON
      ▲                         │                  (domain: plain Kotlin)      (Retrofit, DTOs)
      └──────── state ──────────┘
```

- **Single app module, package by feature** (`catalogue`, `enquiry`), each with `domain`, `data`,
  `di` and `ui` packages. Multi-module would be overkill for a 2–3 hour PoC, but the package
  boundaries are the future module boundaries (see "How it grows").
- **MVI per screen.** Each screen has an immutable `State`, a sealed `Intent` and a ViewModel
  that is the only thing allowed to change state. One-off events (a snackbar after a failed
  send) go through an `Effect` channel so they are not replayed on rotation. This is pragmatic
  MVI: state changes are `setState { copy(...) }` calls inside the ViewModel, not a separate
  pure reducer function. That keeps 5 small screens readable. A reducer can be pulled out if a
  screen's logic grows.
- **Domain is plain Kotlin** with no Android, Retrofit or JSON types. DummyJSON's response shapes
  stay in `data/remote` and are mapped to domain models, so the API's quirks never reach the UI.
- **Use cases only where there is a rule:** `FeaturedProductsProvider` (what "featured" means),
  `GetCategoryProductsUseCase` (top-rated vs. the rest) and `SubmitEnquiryUseCase` (never send an
  invalid enquiry). Search and product details call the repository directly, because a
  pass-through use case would add a file and no behaviour.
- **Errors are values.** Every network call goes through `safeApiCall`, which turns exceptions
  into a `DataResult` with a `DataError` (`NETWORK`, `NOT_FOUND`, `SERVER`, `UNEXPECTED`).
  Cancellation is rethrown. Each screen shows a message and a retry button, except for
  `NOT_FOUND`, where retrying cannot help.

## Swap points for what the client said will change

| Expected change | What to do | What stays untouched |
|---|---|---|
| **Product data source** (own backend, a PIM, Shopify...) | Write a new `ProductRepository` implementation, bind it in `CatalogueModule`, set `officeworks.apiBaseUrl` | Every screen, ViewModel and use case |
| **Email provider** | Backend change only: the app posts to an `EnquiryGateway`, and the provider lives behind the backend | The whole app, once the gateway points at the backend |
| **Look and feel** | Edit `ui/theme/Color.kt` and `Type.kt`. Screens only use `MaterialTheme` tokens. Dynamic (wallpaper) colour is off so Android 12+ doesn't override the brand | Screen code |
| **Hand-picked featured products** | New `FeaturedProductsProvider` that reads the owner's list, bound in `CatalogueModule` | Home screen |

## Assumptions

The acceptance criteria leave these open on purpose. Where unclear, I picked the simplest
reasonable reading and moved on.

| Gap in the brief | Assumption |
|---|---|
| "I have chosen 5 products to be top rated" | There is no admin tool, so "chosen" = the 5 highest-rated products in the catalogue. Isolated behind `FeaturedProductsProvider` so it can become a hand-picked list. |
| How many "top rated" on a category page | The 3 highest rated, then the rest of the category below. The rest never repeats the top 3. Both lists are sorted by rating. Sorting is done in the app, not the API query, so the rule holds even if a future data source can't sort. |
| Where category links live | On the home page, as chips under the featured products. |
| What counts as "text matching" in search | DummyJSON's server-side search, which matches title and description, case-insensitively. I checked this against the live API for `phone`, `red`, `apple` and `laptop`: every result contained the term in its title or description, so I did not add a second client-side filter. |
| When search runs | Only when **Search** is tapped (or the keyboard's search key is pressed), as the criterion says, not on every keystroke. A blank or whitespace-only term can't be submitted. All matches are shown; there is no paging. |
| Search bar placement | On the home page. Submitting opens a search results screen, which has its own bar for refining the search. |
| Product details source | Always fetched by id, not passed from the list, so the page behaves the same from home, a category, search or a future deep link. |
| Enquiry form fields | Name and email are required (sales needs a way to reply). Phone and message are optional. Basic format checks and length limits run on the device for instant feedback. A backend must validate again. |
| What "Thank you" looks like | The form is replaced by a thank-you message naming the product, with a button back to browsing. |
| Currency | DummyJSON doesn't say, so prices show as a plain `$` amount. Prices are `BigDecimal` in the domain to avoid floating-point rounding. |

## Dropped or simplified

| What | Why |
|---|---|
| **No backend in the repo**, so the app calls DummyJSON directly | The brief's rule is "all product data must go through your backend". For this Android submission the recruiter swapped the backend for DummyJSON. I kept the boundary instead: one repository interface and a configurable base URL. *Verbal waivers are easy to forget: confirm it in writing on the recruiter's email thread.* |
| **No real email is sent** | A mobile app can't send email safely: provider credentials would ship inside the APK, where anyone can extract them. DummyJSON has no email endpoint. `DummyJsonEnquiryGateway` makes a real `POST /posts/add` (DummyJSON answers like a real create call and stores nothing), so the flow has real network failure modes the UI must handle. Production needs a backend endpoint that sends the email; only the gateway binding changes in the app. **This stand-in sends the form contents to a public test API, so demos should use made-up contact details.** |
| No paging | The DummyJSON catalogue is ~200 products and a category is under 20. Search asks for all matches. Paging 3 is the next step with a real catalogue. |
| No offline cache | The PoC needs a connection. Errors are handled and retryable. |
| The enquiry form doesn't survive process death | It survives rotation (ViewModel). Saving it in `SavedStateHandle` is a small follow-up. |
| Only the enquiry flow has UI tests | The enquiry flow is the one with business consequences (a lost lead). Other screens are covered through their ViewModel tests; their composables are thin. |
| Single module | Speed. Package boundaries match the future modules. |

## Security basics

- No secrets anywhere: DummyJSON needs no key, and the email decision above keeps provider keys
  off the device.
- HTTPS only: `usesCleartextTraffic="false"`.
- HTTP logging only in debug builds, at `BASIC` level (request line and status). Never bodies,
  because enquiries carry personal data.
- Input is trimmed, validated and length-limited before it leaves the device. The backend must
  validate again; client checks are for user experience, not security.
- Hilt bindings are the only place implementations are chosen, so a test or a future backend
  can't be bypassed by a screen calling the API directly.

## Other technical choices

- **Hilt** for DI: the Android standard and compile-time checked. AGP 9 is new, so I built a
  Hilt + KSP spike before writing any features. It built first time with Kotlin 2.3.21, KSP 2.3.14
  and Hilt 2.60.1; the fallback would have been Koin.
- **The template didn't build.** core-ktx 1.19 and lifecycle 2.11 need compileSdk 37, and the
  template had 36.1. Raised compileSdk to 37; targetSdk stays 36.
- **Navigation arguments reach ViewModels via assisted injection**, not
  `SavedStateHandle.toRoute()`. `toRoute()` builds an `android.os.Bundle`, which made 16 JVM
  ViewModel tests fail. Now `AppNavHost` decodes the typed route and each ViewModel takes it as a
  constructor parameter, which is also simpler to read and to test.
- **Retrofit + kotlinx.serialization**, with `ignoreUnknownKeys` so new API fields can't break parsing.
- **Fakes over mocks** in tests: `FakeProductRepository` and `FakeEnquiryGateway` record calls and
  can hold a call open to test races, such as a slow old search losing to a newer one.

## How it grows beyond the PoC

1. **Own backend (BFF).** A small Node/TypeScript or Laravel service with `GET /products...` and
   `POST /enquiries`. It owns the product source, caching and the email provider (e.g. AWS SES).
   The app adds `BackendProductRepository` and `BackendEnquiryGateway` and swaps two bindings.
2. **Modules.** Split along existing packages: `:core:network`, `:core:ui` (design system),
   `:feature:catalogue`, `:feature:enquiry`. That gives faster builds and enforced boundaries.
3. **Offline and scale.** Room as a cache behind the same `ProductRepository` interface, plus
   Paging 3 for large categories and search.
4. **iOS later.** The domain layer is already plain Kotlin. Moving data to Ktor would let
   `domain` and `data` become Kotlin Multiplatform shared code.
5. **Brand.** When the creative work lands, it goes into `ui/theme` (and later a `:core:ui`
   design-system module) without touching screens.

## What I'd do next with another day

1. Build the BFF from step 1 (including a real email via SES or an Ethereal test inbox) and point
   the app at it. That closes the two biggest gaps: the backend rule and the real email.
2. CI: a GitHub Actions workflow that runs unit tests and lint on every PR.
3. Screenshot tests (Roborazzi or Paparazzi) for each screen, in light and dark.
4. Save the enquiry form in `SavedStateHandle`, and an accessibility pass with TalkBack and large fonts.
5. Let the owner hand-pick featured products: a remote config list behind `FeaturedProductsProvider`.
6. Paging for search and categories once the real catalogue size is known.
