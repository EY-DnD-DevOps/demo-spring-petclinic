# Copilot Instructions — Spring PetClinic

## Build & Run

```bash
# Run (Maven)
./mvnw spring-boot:run

# Run (Gradle)
./gradlew bootRun

# Build + test + lint (Maven)
./mvnw verify

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=OwnerControllerTests

# Run a single test method
./mvnw test -Dtest=OwnerControllerTests#processCreationFormSuccess

# Recompile CSS from SCSS (Maven only)
./mvnw package -P css

# Build container image
./mvnw spring-boot:build-image
```

Lint runs automatically during `validate` phase via two plugins:
- **spring-javaformat**: enforces Spring Java Format (use `./mvnw spring-javaformat:apply` to auto-fix)
- **nohttp-checkstyle**: rejects plain `http://` URLs in any file (must use `https://`)

## Architecture

Spring Boot 4 web app using Spring MVC + Thymeleaf for server-side rendering. **There is no service layer** — repositories (`JpaRepository`) are injected directly into controllers.

```
org.springframework.samples.petclinic
├── model/          # Base JPA classes: BaseEntity, NamedEntity, Person
├── owner/          # Owner, Pet, PetType, Visit entities + controllers + repos
├── vet/            # Vet, Specialty entities + controller + repo
└── system/         # CacheConfiguration, WebConfiguration, WelcomeController, CrashController
```

**Entity hierarchy**: `BaseEntity` (id + `isNew()`) → `NamedEntity` → `Person` → `Owner` / `Vet`.

**Aggregate root pattern**: `Pet` and `Visit` are never persisted directly — they are always modified through `Owner` (which has `CascadeType.ALL` on its `pets` collection). Use `owner.addPet(pet)`, `owner.addVisit(petId, visit)`, then `ownerRepository.save(owner)`.

**Caching**: only the `vets` cache is defined (in `CacheConfiguration`). It is backed by Caffeine via JCache.

**Database profiles**:
- Default (no profile): H2 in-memory; H2 console at `http://localhost:8080/h2-console`
- `spring.profiles.active=mysql` — requires MySQL (or `docker compose up mysql`)
- `spring.profiles.active=postgres` — requires PostgreSQL (or `docker compose up postgres`)

Schema and seed data live in `src/main/resources/db/{h2,mysql,postgres}/`.

## Key Conventions

### Controllers
- Controller classes are **package-private** (no `public` modifier).
- Use `@InitBinder` with `dataBinder.setDisallowedFields("id", "*.id")` to prevent mass-assignment of IDs.
- Use `@ModelAttribute` methods on `@PathVariable` to load entities from the DB before handler methods run.
- Redirect after successful POST (`redirect:/owners/{ownerId}`); return form view on validation errors.
- Flash attributes (`RedirectAttributes`) carry `"message"` (success) or `"error"` keys to the next page.

### Entities
- All entities extend `BaseEntity`. Check `entity.isNew()` (i.e., `id == null`) to decide between insert and update.
- `@NotBlank` / `@Pattern` / Jakarta Validation annotations live on entity fields.
- Custom field-level error keys reference `messages/messages.properties` (e.g., `{telephone.invalid}`).
- i18n message files: `messages/messages.properties` (default) + locale variants (`_de`, `_es`, `_ru`, etc.). Keep all locale files in sync — `I18nPropertiesSyncTest` enforces this.

### Testing
- **`@WebMvcTest`** — controller slice tests using `MockMvc`; mock the repository with `@MockitoBean`. Annotate with `@DisabledInNativeImage` and `@DisabledInAotMode`.
- **`@DataJpaTest`** — repository/service integration tests against H2; each test is `@Transactional` and auto-rolled back.
- **`MySqlIntegrationTests`** — uses Testcontainers to spin up MySQL in Docker.
- **`PostgresIntegrationTests`** — uses Docker Compose to start Postgres.
- **`PetClinicIntegrationTests`** — full `@SpringBootTest` with a real HTTP server on a random port; can also be run as a `main()` for interactive dev testing.
- Use `EntityUtils.getById(collection, Type.class, id)` (in `src/test`) to look up entities from a collection by id.

### Commits
All commits must include a `Signed-off-by` trailer (DCO requirement):
```
Signed-off-by: Your Name <your@email.com>
```
