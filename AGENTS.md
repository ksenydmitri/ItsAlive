# Repository guidance

- ItsAlive is a multi-module Kotlin/Android app using Jetpack Compose, Clean Architecture, MVI, Hilt, and Room.
- Modules include `:app`, `:core:network`, `:core:local`, `:shared`, and feature modules under `:feature`.
- For a task, inspect only the relevant module and its direct dependencies/callers; avoid loading whole modules or large files when targeted search is enough.
- Follow existing module boundaries and local patterns for presentation, domain, data, and dependency injection.
- Run the narrowest relevant Gradle test or build task, and report what was run and any failure.
- Do not read, print, or commit `local.properties`, `app/google-services.json`, signing credentials, or other secrets.
