# Development

## Project structure

* `/domain` - data models and interfaces representing core business rules, independent of framework or infrastructure choices
  * `/domain/constants` - core service constants and enums
  * `/domain/entities` - core data models for the consent service
  * `/domain/exceptions` - exception classes
  * `/domain/pagination` - pagination utility classes
  * `/domain/parsers` - parser utility classes
  * `/domain/repositories` - repository interfaces defining supported backend data operations
  * `/domain/validators` - input validation utility classes
* `/usecases` - application-level business logic leveraging `/domain` data models and interfaces, independent of framework and infrastructure choices
  * `/usecases/activities` - API activities interacting with repository interfaces
  * `/usecases/requesthandlers` - request handlers for each API operation, interacting with API activities
* `/infrastructure` - implementations of domain logic dependent on backend/infrastructure choices
  * `/infrastructure/repositories` - infrastructure-specific implementations of repository interfaces

Key concepts borrowed from Clean Architecture:

* Decouple layers: domain logic, use cases, interface adapters, frameworks, and infrastructure.
  * This allows us to easily test each layer and replace elements with minimal effort.  For example, we could switch frameworks, migrate from SQL to NoSQL, or switch cloud providers without needing to change any domain or activity code.
* No objects in inner layers should know about outer layers.
  * Entities in the domain layer should know nothing about use cases, frameworks, or infrastructure choices.
  * Use cases will reference entities, but should know nothing about frameworks or infrastructure.
  * Interface adapters will reference use cases, but should know nothing about infrastructure.
* Use [Dependency Inversion](https://en.wikipedia.org/wiki/Dependency_inversion_principle) for crossing from inner to outer layers, using interfaces that abstract details that outer layers implement.
  * For example, the `GetServiceUserConsentActivity` should reference a data access interface, eg. `interface ServiceUserConsentRepository`, without having any dependency on how the database is implemented.
  * We can then make infrastructure decisions later, or migrate to other options with minimal effort, which for complex projects can save months of developer time.

## Building the project

### First-time set-up
Follow [GitHub's "Managing your personal access tokens" guide](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) to set up a GitHub personal access token.

Set up a `GITHUB_USERNAME` environment variable storing your GitHub username.

Set up a `GITHUB_TOKEN` environment variable storing your GitHub personal access token.

To build the project for the first time, run

```sh
./gradlew build
```

and validate that it completes successfully.

### Subsequent builds
In order to clean up stale build artifacts and rebuild the API models based on your latest changes, run

```sh
./gradlew clean build
```

If you do not clean before building, your local environment may continue to use stale, cached artifacts in builds.

## Helpful commands

* `./gradlew build` - build project, run checkstyle, and run unit tests
* `./gradlew clean build` - clear build artifacts, rebuild project, run checkstyle, and run unit tests
* `./gradlew tasks` - list available Gradle tasks
* `./gradlew test` - run unit tests
* `./gradlew test --tests TestClass --info` - run unit tests from a specific test class with info-level logging, helpful when debugging errors
* `./gradlew test --tests TestClass.TestMethod --info` - run a specific unit test with info-level logging, helpful when debugging errors

## Troubleshooting

#### My local tests failed but the output doesn't include logs or stack traces needed to debug

Run `./gradlew build --info` to rerun the tests with info logging enabled, which will include logs and stack traces for failed tests.

#### My local builds are not picking up Gradle dependency changes

Run `./gradlew clean build --refresh-dependencies` to ignore your Gradle environment's cached entries for modules and artifacts, and download new versions if they have different published SHA1 hashsums.
