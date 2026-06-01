# Copilot Instructions — Spring PetClinic

> 本檔案提供專案背景，讓 GitHub Copilot 了解技術棧、程式碼慣例與建置方式。

---

## 專案概述

- **名稱**：Spring PetClinic
- **用途**：示範 Spring Boot 生態系最佳實踐的寵物診所管理系統，用於展示 EY DnD DevOps 的開發流程
- **主要使用者**：開發人員（參考實作 / DevOps lab 練習用）

---

## 技術棧

| 層次 | 技術 |
|------|------|
| 語言 | Java 17 |
| 框架 | Spring Boot 4、Spring MVC、Thymeleaf |
| 資料庫 | H2（預設）、MySQL、PostgreSQL |
| 前端 | Thymeleaf（Server-side rendering）、Bootstrap、SCSS |
| 容器 / 部署 | Docker、Docker Compose |
| CI/CD | GitHub Actions、Maven Wrapper |

---

## 架構說明

Spring Boot 4 Web 應用程式，採用 Spring MVC + Thymeleaf 進行伺服器端渲染。**無 Service 層** — Repository（`JpaRepository`）直接注入 Controller。

```
org.springframework.samples.petclinic
├── model/          # 基礎 JPA 類別：BaseEntity、NamedEntity、Person
├── owner/          # Owner、Pet、PetType、Visit 實體 + Controller + Repository
├── vet/            # Vet、Specialty 實體 + Controller + Repository
└── system/         # CacheConfiguration、WebConfiguration、WelcomeController、CrashController
```

**Entity 繼承關係**：`BaseEntity`（id + `isNew()`）→ `NamedEntity` → `Person` → `Owner` / `Vet`

**Aggregate Root 模式**：`Pet` 與 `Visit` 不直接持久化，必須透過 `Owner` 操作（`CascadeType.ALL`）。請使用 `owner.addPet(pet)`、`owner.addVisit(petId, visit)`，再呼叫 `ownerRepository.save(owner)`。

**快取**：僅定義 `vets` cache（於 `CacheConfiguration`），由 Caffeine via JCache 支撐。

**資料庫 Profile**：

| Profile | 資料庫 | 說明 |
|---------|--------|------|
| 無（預設）| H2 in-memory | H2 console：`http://localhost:8080/h2-console` |
| `mysql` | MySQL | 需啟動 `docker compose up mysql` |
| `postgres` | PostgreSQL | 需啟動 `docker compose up postgres` |

Schema 與種子資料位於 `src/main/resources/db/{h2,mysql,postgres}/`。

---

## 程式碼慣例

### 命名規則

| 類型 | 規則 | 範例 |
|------|------|------|
| Class | PascalCase | `OwnerController` |
| Method / Variable | camelCase | `findOwnerById` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| DB Table / Column | snake_case | `pet_type` |

### Controller 慣例

- Controller 類別使用 **package-private**（不加 `public`）
- 使用 `@InitBinder` 搭配 `dataBinder.setDisallowedFields("id", "*.id")` 防止 mass-assignment
- 使用 `@ModelAttribute` 方法搭配 `@PathVariable` 在 handler 執行前從 DB 載入 Entity
- 成功 POST 後重導向（`redirect:/owners/{ownerId}`）；驗證錯誤則回傳表單 View
- Flash attributes（`RedirectAttributes`）以 `"message"`（成功）或 `"error"` 傳遞訊息至下一頁

### Entity 慣例

- 所有 Entity 繼承 `BaseEntity`，以 `entity.isNew()`（即 `id == null`）判斷新增或更新
- 驗證注解（`@NotBlank`、`@Pattern`、Jakarta Validation）定義於 Entity 欄位上
- 自訂欄位錯誤訊息 key 對應 `messages/messages.properties`（例如 `{telephone.invalid}`）
- i18n 訊息檔：`messages/messages.properties`（預設）+ 各地區語言變體（`_de`、`_es`、`_ru` 等）。所有語言檔須同步，由 `I18nPropertiesSyncTest` 強制檢查

### 其他慣例

- 禁止在程式碼中使用 `System.out.println`，改用 SLF4J Logger
- 不可使用純文字 `http://` URL（`nohttp-checkstyle` 會自動拒絕）
- 程式碼格式由 `spring-javaformat` 強制統一

---

## 測試規範

- **測試框架**：JUnit 5、MockMvc、Testcontainers
- **命名規則**：`should{預期結果}When{情境}` 或既有慣例格式
- **測試目錄**：`src/test/`
- **新增功能必須附帶對應的 unit test**

| 測試類型 | 說明 |
|----------|------|
| `@WebMvcTest` | Controller slice 測試，使用 MockMvc；以 `@MockitoBean` mock Repository；標注 `@DisabledInNativeImage` 與 `@DisabledInAotMode` |
| `@DataJpaTest` | Repository 整合測試，使用 H2；每個測試加 `@Transactional` 且自動 rollback |
| `MySqlIntegrationTests` | 使用 Testcontainers 啟動 MySQL Docker |
| `PostgresIntegrationTests` | 使用 Docker Compose 啟動 PostgreSQL |
| `PetClinicIntegrationTests` | 完整 `@SpringBootTest`，啟動真實 HTTP server（隨機 port）；可作為 `main()` 執行互動式測試 |

使用 `EntityUtils.getById(collection, Type.class, id)`（位於 `src/test`）透過 id 從集合中查找 Entity。

---

## 建置與測試指令

```bash
# 本地啟動（Maven）
./mvnw spring-boot:run

# 本地啟動（Gradle）
./gradlew bootRun

# 建置 + 測試 + Lint
./mvnw verify

# 執行所有測試
./mvnw test

# 執行單一測試類別
./mvnw test -Dtest=OwnerControllerTests

# 執行單一測試方法
./mvnw test -Dtest=OwnerControllerTests#processCreationFormSuccess

# 重新編譯 SCSS（Maven）
./mvnw package -P css

# 建置 Container Image
./mvnw spring-boot:build-image

# 自動修正程式碼格式
./mvnw spring-javaformat:apply
```

**Lint** 在 `validate` 階段自動執行：
- **spring-javaformat**：強制 Spring Java Format
- **nohttp-checkstyle**：禁止任何檔案中出現純文字 `http://` URL

---

## 環境變數

| 變數名稱 | 說明 |
|----------|------|
| `SPRING_PROFILES_ACTIVE` | 資料庫 Profile（`mysql` / `postgres`，預設為 H2） |
| `MYSQL_URL` | MySQL 連線字串（mysql profile 使用） |
| `POSTGRES_URL` | PostgreSQL 連線字串（postgres profile 使用） |

> 實際值請參考 `docker-compose.yml` 或各環境的 Secret Manager。

---

## API 規範

> 本專案以 Thymeleaf 伺服器端渲染為主，無獨立 REST API。若未來新增 REST endpoint，請遵循以下規範。  
> 完整說明：[API Response Guideline](https://github.com/EY-DnD-DevOps/guidelines/blob/main/api_response_guideline.md)

### Response 結構

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
    "type": "ResourceNotFound",
    "message": "找不到指定的飼主資料",
    "detail": "Owner with id=99 does not exist",
    "trace_id": "req-20251030-xyz"
  },
  "timestamp": "2025-10-30T11:25:00+08:00"
}
```

### 錯誤碼格式

`MODULE_CATEGORY_DETAIL`（全大寫、底線分隔、2~4 段）

範例：`OWNER_NOT_FOUND`、`PET_TYPE_INVALID`、`VET_SCHEDULE_CONFLICT`

---

## Commit 規範

所有 commit 必須包含 `Signed-off-by` trailer（DCO 要求）：

```
Signed-off-by: Your Name <your@email.com>
```

---

## 相關文件

- 開發流程指南：[EY-DnD-DevOps/guidelines](https://github.com/EY-DnD-DevOps/guidelines/blob/main/github-copilot/github-copilot-workflow-guide.md)
- API Response 規範：[api_response_guideline.md](https://github.com/EY-DnD-DevOps/guidelines/blob/main/api_response_guideline.md)
- 原始 Spring PetClinic：[spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic)
