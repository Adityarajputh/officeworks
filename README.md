# Product Catalogue (Android proof of concept)

A small product catalogue built as a foundation that can grow, not a throwaway prototype.
Customers can browse featured products and categories, search, open product details and send an
enquiry.

- **Client:** Android app in Kotlin with Jetpack Compose (MVI, Hilt, Retrofit, Coil).
- **Product data:** [DummyJSON](https://dummyjson.com/docs/products), a free public API that needs no account or key.

Design decisions, assumptions and cuts are in [DECISIONS.md](DECISIONS.md). How AI was used is in
[AI_USAGE.md](AI_USAGE.md).

## Why this stack, and the one deviation from the brief

The brief asks for a client plus a backend (Node.js or Laravel), with all product data going
through that backend. For this Android submission the recruiter asked for **an Android app with
DummyJSON as the backend** instead, so there is no backend in this repo and the app calls
DummyJSON directly.

I kept the boundary the backend rule is there to protect:

- Screens only talk to the `ProductRepository` and `EnquiryGateway` interfaces.
- The API base URL is configuration (`officeworks.apiBaseUrl`), not code.
- Pointing the app at our own backend means one new repository class, one new gateway class and a
  URL change. No screen changes.

DummyJSON was picked over the Fake Store API because Fake Store has no search endpoint, and
DummyJSON has search, categories and a `rating` field to sort by.

## Run it

### Requirements

- Android Studio 2026.1 or newer (built and tested with 2026.1; the project uses Android Gradle Plugin 9.2).
- Android SDK Platform 37 (Android Studio offers to install it on first sync).
- An Android Emulator, API 24 or newer. The UI tests were run on a Pixel 9 / API 36 emulator.
- Internet access (the app loads products from dummyjson.com).

You don't need a JDK of your own. Gradle downloads JDK 21 for its daemon if needed (see
`gradle/gradle-daemon-jvm.properties`).

### Android Studio

1. Clone the repo and open the folder in Android Studio.
2. Wait for Gradle sync to finish. Install SDK Platform 37 if prompted.
3. Start an emulator from Device Manager.
4. Select the `app` run configuration and press **Run**.

### Command line

Gradle needs to know where the Android SDK is. Either open the project once in Android Studio
(it writes `local.properties`), or set `ANDROID_HOME`, for example
`export ANDROID_HOME=$HOME/Library/Android/sdk` on macOS. Then, with an emulator already running:

```bash
./gradlew :app:installDebug
```

```bash
adb shell am start -n moc.itivo.officeworks/.MainActivity
```

## Tests

```bash
./gradlew :app:testDebugUnitTest
```

50 JVM tests: mappers, the DummyJSON repository against MockWebServer (paths, query
parameters, parsing, and error mapping for 404, 5xx, bad JSON and offline), the use cases,
enquiry validation and every ViewModel.

```bash
./gradlew :app:connectedDebugAndroidTest
```

3 Compose UI tests for the enquiry flow on a device: success shows "Thank you", an invalid email
is flagged and nothing is sent, and a failed send keeps the form. Needs a running emulator.

## Configuration

There are no secrets and no `.env` file to copy, because the app needs no keys. The only
setting is the API base URL, in `gradle.properties`:

```properties
officeworks.apiBaseUrl=https://dummyjson.com/
```

To override it without editing files:

```bash
./gradlew :app:installDebug -Pofficeworks.apiBaseUrl=https://my-backend.example.com/
```

## Acceptance criteria and where they live

| Criterion | Behaviour | Code |
|---|---|---|
| Home page: 5 featured products | The 5 highest-rated products, plus category links and the search bar | `catalogue/ui/home`, `catalogue/domain/FeaturedProductsProvider.kt` |
| Category page | Tap a category: the 3 top-rated products, then the rest of the category (no duplicates) | `catalogue/ui/category`, `catalogue/domain/GetCategoryProductsUseCase.kt` |
| Search | Type a term and tap **Search**: only products whose title or description match | `catalogue/ui/search`, `catalogue/ui/components/ProductSearchBar.kt` |
| Product page | Tap any product tile: the details page, loaded by id | `catalogue/ui/product` |
| Enquiry | **Enquire**, then fill in the form and submit: the enquiry is sent and "Thank you" shows | `enquiry/` (see the email caveat below) |

**Email caveat.** The app does not send a real email. Sending email from a mobile app would mean
shipping email-provider credentials inside the APK. The enquiry goes through an
`EnquiryGateway`. Today that gateway is a stand-in that makes a real POST to a DummyJSON test
endpoint, which stores nothing. In production the gateway posts to our backend, and the backend
emails the sales inbox. See [DECISIONS.md](DECISIONS.md#dropped-or-simplified).

## Project structure

```
app/src/main/java/moc/itivo/officeworks/
├── catalogue/                 # browsing: home, category, search, product details
│   ├── domain/                # Product, ProductRepository, FeaturedProductsProvider, GetCategoryProductsUseCase
│   ├── data/                  # DummyJSON API, DTOs, mappers, DummyJsonProductRepository
│   ├── di/                    # Hilt bindings: the data-source swap point
│   └── ui/                    # one package per screen: Contract (State/Intent), ViewModel, Screen
├── enquiry/                   # enquiry form and delivery
│   ├── domain/                # EnquiryForm, EnquiryValidator, EnquiryGateway, SubmitEnquiryUseCase
│   ├── data/                  # DummyJsonEnquiryGateway (stand-in)
│   ├── di/                    # Hilt bindings: the email-provider swap point
│   └── ui/
├── core/                      # DataResult, safeApiCall, MviViewModel, shared UI, NetworkModule
├── navigation/                # type-safe destinations and the NavHost
└── ui/theme/                  # the look and feel, in one place
```
