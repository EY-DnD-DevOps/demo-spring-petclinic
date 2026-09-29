# CLAUDE.md

> 本檔案提供專案背景，讓 Claude Code 了解技術棧、程式碼慣例與建置方式。

---

## 專案概述

- **名稱**：Spring PetClinic（EY DnD DevOps demo fork）
- **用途**：以 Spring Boot 實作的寵物診所管理系統，作為 EY DnD DevOps 開發流程（Issue → TDD → Review → PR）的示範與 lab 練習專案
- **主要使用者**：開發人員（參考實作 / DevOps lab 練習用）

---

## 語言標記

- **primary-language**: java
- **secondary-languages**: javascript <!-- src/main/resources/static 下的原生 HTML + JS 前端 -->

---

## 技術棧

| 層次 | 技術 |
|------|------|
| 語言 | Java 17 |
| 框架 | Spring Boot 4（Spring MVC REST、Spring Data JPA、Validation、Cache、Actuator） |
| 資料庫 | H2（預設）、MySQL、PostgreSQL；schema 由 Liquibase 管理 |
| 前端 | 靜態 HTML + 原生 JavaScript（`fetch` 呼叫 `/api/**`）、Bootstrap（webjars） |
| 快取 | Caffeine via JCache（`vets` cache） |
| 容器 / 部署 | Docker Compose（MySQL / PostgreSQL）、`spring-boot:build-image`、`k8s/` |
| CI/CD | GitHub Actions、Maven Wrapper、JaCoCo coverage 上報 |

---

## 架構說明

前後端分離：後端只提供 REST API（`/api/**`），前端為 `src/main/resources/static/` 下的靜態頁面。
**無 Service 層** — Repository（`JpaRepository`）直接注入 `*ApiController`。

```
org.springframework.samples.petclinic
├── model/    # 基礎 JPA 類別：BaseEntity、NamedEntity、Person
├── owner/    # Owner、Pet、PetType、Visit、SearchKeyword 實體 + *ApiController + Repository
├── vet/      # Vet、Specialty 實體 + VetApiController + VetRepository
└── system/   # ApiResponse、ApiErrorDetail、GlobalExceptionHandler、Cache/Async/Web 設定、
              # WelcomeController（/ → /index.html）、CrashController（/oups）

src/main/resources
├── static/                  # 前端頁面（*.html）與 js/（api.js 為共用 API client）
├── static/error/            # 4xx.html / 5xx.html 靜態錯誤頁
├── db/changelog/            # Liquibase master + changes/V00N__*.xml
└── messages/                # i18n 訊息檔
```

**Entity 繼承關係**：`BaseEntity`（id + `isNew()`）→ `NamedEntity` → `Person` → `Owner` / `Vet`

**Aggregate Root 模式**：`Pet` 與 `Visit` 不直接持久化，必須透過 `Owner` 操作（`CascadeType.ALL`）。
使用 `owner.addPet(pet)`、`owner.addVisit(petId, visit)`，再呼叫 `ownerRepository.save(owner)`。

**非同步**：搜尋關鍵字由 `SearchKeywordRecorder`（`@Async`）寫入，不阻塞搜尋回應。

**資料庫 Profile**：

| Profile | 資料庫 | 說明 |
|---------|--------|------|
| 無（預設）| H2 in-memory | H2 console：`/h2-console` |
| `mysql` | MySQL | `docker compose up mysql` |
| `postgres` | PostgreSQL | `docker compose up postgres` |

---

## 程式碼慣例

### 專案特定慣例

- Controller 類別與 handler method 使用 **package-private**（不加 `public`）
- REST controller 一律回傳 `ApiResponse<T>`（成功用 `ApiResponse.success(data)`）；建立資源回 `201 Created`
- 找不到資源時拋 `IllegalArgumentException`，由 `GlobalExceptionHandler` 統一轉成 404 `RESOURCE_NOT_FOUND`；不要在 controller 內自行組錯誤回應
- 防 mass-assignment：POST 時 `entity.setId(null)`，PUT 時以 path variable 覆寫 id（`entity.setId(pathId)`），不信任 request body 的 id
- 驗證注解（`@NotBlank`、`@Pattern` 等 Jakarta Validation）定義於 Entity 欄位上，controller 參數加 `@Valid`
- **DB schema 變更一律新增 Liquibase changeset**（`db/changelog/changes/V{NNN}__{desc}.xml`，並在 master 註冊），禁止修改已發布的 changeset；`spring.jpa.hibernate.ddl-auto=none`
- i18n 訊息檔（`messages/messages*.properties`）所有語言須同步，由 `I18nPropertiesSyncTest` 強制檢查
- 禁止 `System.out.println`，使用 SLF4J Logger
- 不可使用純文字 `http://` URL（`nohttp-checkstyle` 會擋）
- 程式碼格式由 `spring-javaformat` 強制（tab 縮排），提交前可執行 `./mvnw spring-javaformat:apply`
- 前端 JS 透過 `static/js/api.js` 呼叫 API，並依 `success` / `error` 欄位處理回應

### Commit 規範

- 格式：`feat #{issue}: 說明` / `fix #{issue}: 說明`（由 gh-issue-workflow hook 強制）
- 不加任何 AI attribution trailer

---

## 測試規範

- 測試框架：JUnit 5、MockMvc、Mockito、AssertJ、Testcontainers
- 命名規則：`should_{預期結果}_when_{情境}`
- 測試目錄：`src/test/`
- Coverage 目標：line coverage ≥ 80%（JaCoCo，報告於 `target/site/jacoco/jacoco.xml`）
- **新增功能必須附帶對應的 unit test**

| 測試類型 | 說明 |
|----------|------|
| `@WebMvcTest` | Controller slice 測試，以 `@MockitoBean` mock Repository；標注 `@DisabledInNativeImage`、`@DisabledInAotMode` |
| `@DataJpaTest` | Repository 測試，使用 H2 |
| `MySqlIntegrationTests` / `PostgresIntegrationTests` | 需 Docker（Testcontainers / Docker Compose） |
| `PetClinicIntegrationTests` | 完整 `@SpringBootTest`（隨機 port） |

`EntityUtils.getById(collection, Type.class, id)`（`src/test/.../service`）可透過 id 從集合中查找 Entity。

---

## 建置與測試指令

```bash
# 建置 + 測試 + Lint（format / nohttp）+ JaCoCo 報告
./mvnw verify

# 執行測試
./mvnw test

# 執行單一測試類別 / 方法
./mvnw test -Dtest=OwnerApiControllerTests
./mvnw test -Dtest=OwnerApiControllerTests#should_returnOwner_when_ownerExists

# 本地啟動（http://localhost:8080）
./mvnw spring-boot:run

# 自動修正程式碼格式
./mvnw spring-javaformat:apply
```

---

## 外部整合

| 類型 | 說明 |
|------|------|
| 資料庫 | H2 / MySQL / PostgreSQL（Liquibase migration） |
| 快取 | Caffeine（JCache，in-process） |
| 外部 API | 無 |
| Queue / Event | 無（`@Async` 為 in-process 執行緒池） |

---

## 環境變數

| 變數名稱 | 說明 |
|----------|------|
| `SPRING_PROFILES_ACTIVE` | 資料庫 Profile（`mysql` / `postgres`，預設 H2） |
| `MYSQL_URL` | MySQL 連線字串（mysql profile） |
| `POSTGRES_URL` | PostgreSQL 連線字串（postgres profile） |

CI 使用：`vars.COVERAGE_PLATFORM_URL`（Org Variable）、`secrets.COVERAGE_TOKEN`（Org Secret）。

> 實際值請參考 `docker-compose.yml` 或 Secret Manager，不可寫入 repo。

---

## API 規範

> 所有 `/api/**` endpoint 遵循以下規範。
> 完整說明：[API Response Guideline](https://github.com/EY-DnD-DevOps/guidelines/blob/main/api_response_guideline.md)

### Error Code MODULE 清單

| MODULE | 說明 |
|--------|------|
| `OWNER` | 飼主 |
| `PET` | 寵物、寵物類型 |
| `VISIT` | 就診紀錄 |
| `VET` | 獸醫、專長 |
| `SEARCH` | 全域搜尋、搜尋關鍵字統計 |

既有通用錯誤碼（`GlobalExceptionHandler`）：`RESOURCE_NOT_FOUND`（404）、`VALIDATION_ERROR`（400）、`INTERNAL_SERVER_ERROR`（500）。

### Response 結構

由 `system/ApiResponse`（record）與 `system/ApiErrorDetail` 實作：

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2025-10-30T11:25:00+08:00"
}
```

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "OWNER_NOT_FOUND",
    "type": "IllegalArgumentException",
    "message": "找不到指定的飼主資料",
    "detail": "Owner not found with id: 99",
    "trace_id": "..."
  },
  "timestamp": "2025-10-30T11:25:00+08:00"
}
```

### 錯誤碼格式

`MODULE_CATEGORY_DETAIL`（全大寫、底線分隔、2~4 段），例如 `OWNER_NOT_FOUND`、`PET_TYPE_INVALID`

---

## 業務邏輯規範

- `Pet`、`Visit` 只能經由 `Owner` aggregate 新增／修改，不可繞過 Owner 直接存取
- 寵物新增時 name、type、birthDate 為必填（規則定義於 `PetValidator`，目前 REST controller 尚未套用，新增驗證時請一併對齊）
- 搜尋關鍵字記錄為非同步 best-effort，失敗不可影響搜尋結果回應
- Vet 清單有快取，異動 Vet 資料時須考慮 `vets` cache 失效

---

## 相關文件

- 開發流程指南：[EY-DnD-DevOps/guidelines](https://github.com/EY-DnD-DevOps/guidelines/blob/main/claude-code-workflow-guide.md)
- API Response 規範：[api_response_guideline.md](https://github.com/EY-DnD-DevOps/guidelines/blob/main/api_response_guideline.md)
- 原始 Spring PetClinic：[spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic)
