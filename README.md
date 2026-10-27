# YikYak Clone

A location-aware, community-driven Android social application inspired by YikYak, developed as a team project for NJIT's CS 673 graduate Software Engineering course.

The application aims to help users connect through local conversations, share thoughts anonymously within communities, and participate in discussions relevant to their surroundings.

## Overview

YikYak Clone explores the design and implementation of a modern mobile social platform with an emphasis on location-based content, community interaction, and maintainable software architecture.

The project is also an opportunity to apply software engineering practices throughout the development lifecycle, including requirements analysis, system design, collaborative development, testing, and documentation.

## Project Goals

- Build a native Android application with a modern, responsive interface.
- Support community discussions through posts, comments, and voting.
- Explore location-aware content discovery.
- Organize the application into maintainable, well-defined components.
- Practice collaborative software development using Git and GitHub.
- Apply testing, documentation, and code review throughout development.

## Planned Features

The following describes the intended product scope. Individual features may be under development and are not necessarily available in the current build.

- **Accounts:** User account creation and authentication.
- **Location-based feeds:** Discover discussions associated with a user's surrounding area.
- **Posts:** Create and browse short-form community posts.
- **Comments and replies:** Participate in conversations.
- **Voting:** Express support for or disagreement with posts.
- **Channels and communities:** Organize discussions around shared interests or locations.
- **Notifications:** Keep users informed about relevant interactions.
- **Community safety:** Explore reporting, blocking, and other moderation controls.
- **Additional interactions:** Potential support for polls, trending posts, photos, and campus events.

## Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary application programming language |
| Android SDK | Native Android application platform |
| Jetpack Compose | Declarative user interface development |
| Material 3 | UI components and visual styling |
| Navigation Compose | Navigation between application screens |
| Gradle Kotlin DSL | Build configuration and dependency management |
| Supabase | Backend services, including authentication, database access, and real-time functionality |
| Google Play Services Location | Android location services |
| JUnit | Unit testing |
| AndroidX Test and Compose UI Test | Instrumented and UI testing |

## Architecture

The application uses a native Android structure with Jetpack Compose for UI rendering.

The main application entry point is `MainActivity.kt`, which initializes the application theme and navigation.

The codebase is organized under the app module, with responsibilities separated into UI, navigation, and supporting application logic. As development progresses, feature-specific components and data-access abstractions should remain modular to make the application easier to test and maintain.

Key architectural principles:

- **Separation of concerns:** Keep presentation, application logic, and data access distinct.
- **Reusable UI:** Prefer shared Compose components over duplicated screen implementations.
- **Testability:** Design application logic so that it can be tested independently where practical.
- **Maintainability:** Keep files and components focused on clearly defined responsibilities.
- **Configuration management:** Keep local development settings out of committed environment-specific files.

## Getting Started

### Prerequisites

Install the following before building the application:

- Android Studio with support for the project's configured Android Gradle Plugin.
- A compatible JDK and Android SDK.
- Git.
- An Android emulator or a physical Android device.
- Supabase configuration for the selected backend build variant.

### 1. Clone the repository

```bash
git clone https://github.com/Moonm4nn/cs673-yikyak.git
cd cs673-yikyak
```

### 2. Open the project

Open the cloned project directory in Android Studio and allow Gradle to synchronize dependencies.

Resolve any missing Android SDK or JDK requirements reported by Android Studio.

### 3. Configure the local backend

For local backend development, copy the provided example configuration:

```bash
cp dev.properties.example dev.properties
```

On Windows, copy `dev.properties.example` to `dev.properties` using File Explorer or PowerShell.

Update `dev.properties` with the local Supabase URL and publishable key:

```properties
supabase.local.url=YOUR_LOCAL_SUPABASE_URL
supabase.local.publishableKey=YOUR_LOCAL_PUBLISHABLE_KEY
```

Use the actual connection details for your local Supabase instance. If using an Android emulator to reach a service running on the development machine, configure the URL and networking according to the emulator and Supabase setup.

Do not commit `dev.properties` or private credentials to version control.

### 4. Select a build variant

The project defines two backend product flavors:

- `local`: Uses values supplied through `dev.properties`.
- `production`: Uses the production backend configuration defined in the Gradle build file.

In Android Studio, open **Build > Select Build Variant** and select the desired variant, such as `localDebug`.

Choose the appropriate backend before running or testing the application.

### 5. Run the application

Select an Android emulator or connected device in Android Studio, then run the `app` configuration.

The first build may take additional time while Gradle downloads dependencies.

## Testing

Run unit tests from the project root:

```bash
./gradlew test
```

On Windows:

```powershell
.\gradlew.bat test
```

To run Android instrumented tests on a configured emulator or device:

```bash
./gradlew connectedAndroidTest
```

On Windows:

```powershell
.\gradlew.bat connectedAndroidTest
```

Tests should cover application logic, important user interactions, navigation, and relevant backend integration behavior as these features are implemented.

## Development Workflow

The team uses Git and GitHub to coordinate development.

Recommended workflow:

1. Pull the latest changes from the shared development branch.
2. Create a focused feature or bug-fix branch.
3. Implement the change and add or update tests where appropriate.
4. Commit changes with clear, descriptive messages.
5. Open a pull request for review.
6. Address review feedback and resolve conflicts.
7. Merge changes after the agreed checks and reviews pass.

Avoid committing generated build output, machine-specific configuration, credentials, or unrelated changes.

## Project Status

This project is under active development as part of CS 673.

Features, architecture, and setup instructions may evolve as the team implements and tests the application. Refer to the repository's current implementation and project tracking materials for the latest status.

## Academic Context

Developed for CS 673 — Software Engineering at the New Jersey Institute of Technology (NJIT).

The project emphasizes the practical application of software engineering principles to a collaborative Android application, including design, implementation, verification, documentation, and team-based development.
