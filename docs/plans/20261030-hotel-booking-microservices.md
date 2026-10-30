# План: микросервисная система бронирования отелей (итоговый проект SkillFactory)

Дата: 2026-10-30
Дедлайн сдачи: 2026-11-09 23:59 MSK
Источник требований (проверено в браузере Orca):
- Введение: `.../block@8771c03f0a6c4cb1b94e0c0ea5cb01ec`
- Что нужно сделать: `.../block@a20379c6182547c7ada9476946dbc039`
- Сдача проекта: `.../block@8433a3c35fa449e9a1dc070d2a7aa838`

## Прогресс

- [x] Этап 0. Сборка, версии, структура — Boot 3.5.16 + Cloud 2025.0.3, Java 21,
      MapStruct-интерфейсы, `mvn package` зелёный.
- [x] Этап 1. hotel-service: CRUD, статистика, recommend, room_locks,
      confirm/release (идемпотентность, пессимистичная блокировка, 409/400/404).
      Проверено HTTP-запросами.
- [x] Этап 2. Eureka: регистрация hotel-service подтверждена (`/eureka/apps`, UP).
- [x] Этап 3. booking-service: пользователи, JWT, Resource Server; hotel-service
      Resource Server, `@PreAuthorize` + URL-правила, единые 401/403.
      Проверено: register/auth/CRUD, USER→403 на админских, 401 без токена,
      TTL=3600, токен booking-service принимается hotel-service.
- [x] Этап 4. booking-service: бронирования, autoSelect через Feign,
      проверка пересечений, `room_guards` + pessimistic lock, идемпотентность
      по `requestId`, пагинация, владелец/ADMIN. Проверено HTTP + Eureka.
- [x] Этап 5. Сага + Feign + устойчивость: PENDING→confirm→CONFIRMED,
      компенсация release→CANCELLED, retry/backoff, Resilience4j, timeout Feign.
      Проверено: успех, 409 + CANCELLED, идемпотентный повтор, отмена/rebook.
- [x] Этап 6. Gateway: MVC-маршруты `lb://`, JWT на входе, единые 401/403,
      CORS, `X-Request-Id`/MDC. Проверено: маршруты 200/201, INTERNAL 404,
      без токена 401, stats USER→403, CORS preflight 200.
- [x] Этап 7. Сквозные вещи и seed: springdoc + Bearer в hotel/booking,
      seed 3 отеля / 5 номеров, admin/admin123 и user/user123, Actuator,
      структурные JSON-логи (ECS). Swagger и openapi отдают 200.
- [ ] Этап 8. Тесты.
- [ ] Этап 9. README/ADR/демо.

## 1. Что реально требуют (краткий конспект)

### Компоненты

| Компонент | Ответственность |
|---|---|
| `gateway-service` | Spring Cloud Gateway: маршрутизация, проксирование JWT, проверка доступа на входе и/или в сервисах, трассировка |
| `hotel-service` | CRUD отелей и номеров, статистика загруженности, рекомендация номеров, временная блокировка слота (INTERNAL) |
| `booking-service` | Регистрация/авторизация (JWT), CRUD пользователей (ADMIN), бронирования, сага с `hotel-service` |
| `eureka-service` | Service Registry / discovery |

Каждый сервис — отдельное Spring Boot приложение с **in-memory H2**.
Рекомендации: Java 17+, Spring Boot **3.5.x**, Spring Cloud BOM, JPA, Security + JWT,
Eureka, Gateway, Lombok + MapStruct.

### Роли

- **USER**: просмотр отелей/номеров, создание брони, своя история, отмена своей брони.
- **ADMIN**: CRUD отелей, номеров, пользователей; статистика загруженности.
- **INTERNAL**: служебные эндпойнты hotel-service (confirm-availability/release).

### Ключевые сценарии

1. Регистрация/вход → JWT (срок 1 час), роли USER/ADMIN.
2. Просмотр отелей и свободных номеров.
3. Создание брони:
   - `autoSelect=true` → подбор номера по возрастанию `times_booked` (при равенстве — по id),
   - `autoSelect=false` → бронируем указанный `roomId`.
4. Сага:
   - booking-service локально сохраняет бронь в `PENDING`;
   - вызывает hotel-service `POST /api/rooms/{id}/confirm-availability` (временная блокировка периода);
   - успех → `CONFIRMED`;
   - ошибка/таймаут → `CANCELLED` + компенсация `POST /api/rooms/{id}/release`.
5. Отмена подтверждённой брони → `CANCELLED` + release (чтобы снять блокировку).
6. Конкурентные брони на один номер/пересекающиеся даты → без дублей, 409.

### Явно оговорено

- `rooms.available` — **операционный** статус (ремонт/вывод из эксплуатации), **не** занятость по датам.
- Занятость по датам определяется по `bookings.start_date/end_date` в booking-service.
- Распределённых транзакций нет: только локальные `@Transactional` + компенсация.
- Обязательны таймауты и ограниченные повторы с backoff.
- Обязательна идемпотентность по `requestId`/`bookingId` (повторная доставка не создаёт дубль).
- Сквозная корреляция логов (`requestId`/`bookingId`).
- Схема БД (минимум):
  - booking: `users(id, username, password, role)`, `bookings(id, user_id, room_id, start_date, end_date, status, created_at)`
  - hotel: `hotels(id, name, address)`, `rooms(id, hotel_id, number, available, times_booked)`
- Swagger/OpenAPI + тесты JUnit/MockMvc.
- Сдача: GitHub + README (запуск, эндпойнты, как тестировать), архитектурная схема, ADR.
- Оценка: 50 баллов по 15 критериям (см. раздел 6).

## 2. Аудит текущего состояния

| # | Где | Проблема |
|---|---|---|
| B1 | `hotel-service/pom.xml` | `maven-compiler-plugin` объявлен дважды → Maven не читает POM (проверено) |
| B2 | `hotel-service/pom.xml` | `source/target 1.8` при `java.version=17`; `record` не компилируется |
| B3 | корневой POM + модули | Boot 4.0.7 (корень) vs 4.1.1 (модули) vs рекомендация 3.5.x; Cloud 2025.1.3 рассчитан на Boot 4.0.8 |
| B4 | корневой POM | Модули не наследуют агрегатор, `dependencyManagement` из корня не работает |
| B5 | `mapper/HotelMapper.java`, `RoomMapper.java` | `class` вместо `interface`, пустые `@Mapping(target="", source="")`, нет `componentModel=spring` |
| B6 | `entity/Room.java` | `@GeneratedValue` на `number` (не `@Id`) — так не работает |
| B7 | `HotelService` / `HotelDto` | `rooms` всегда пустой |
| B8 | `eureka-service` | Нет `@EnableEurekaServer` |
| B9 | `gateway-service/application.yaml` | Префикс `server.webflux` при MVC-стартере; маршруты не применятся |
| B10 | `booking-service` | Есть `spring-boot-starter-security`, но нет конфига; эндпойнтов нет |
| B11 | `hotel-service` | `confirmAvailability` пустой; нет locks, статистики, рекомендаций |
| B12 | инфраструктура | Нет контроллеров, `@RestControllerAdvice`, OpenAPI, datasource/JPA-конфига, seed-данных |
| B13 | git | `.idea/*` в индексе, `.gitignore` не покрывает весь `.idea/` |
| B14 | сборка | `mvnw` на машине не стартует; локальный Maven 3.10 есть в `~/.m2/wrapper/dists` |

## 3. Технические решения (и почему)

### 3.1. Версии: Spring Boot 3.5.16 + Spring Cloud 2025.0.3

Причина: это ровно то, что рекомендует курс; Spring Cloud 2025.0.3 содержит
Gateway 4.3.5, Netflix/Eureka 4.3.3, OpenFeign 4.3.3 — совместимые с Boot 3.5.x.
Текущая связка Boot 4.1.1 + Cloud 2025.1.3 не гарантирована (Cloud 2025.1.x
объявляет Boot 4.0.8). Downgrade делаем один раз на этапе 0, пока кода мало.
Альтернатива — остаться на Boot 4.1.1, но тогда выше риск несовместимости и
отличий от материалов курса (Spring Security 7, новые имена стартеров).

### 3.2. Gateway: MVC-вариант (`spring-cloud-starter-gateway-mvc`)

Плюсы: сервлетный стек, знакомый Spring Security, проще отладка, тот же стиль,
что в сервисах. Конфиг: `spring.cloud.gateway.mvc.routes` (не `webflux` и не
`gateway.routes`). Маршруты — `lb://hotel-service` / `lb://booking-service`.
Это всё ещё Spring Cloud Gateway и закрывает критерий 5.

### 3.3. JWT: HS256, выпускает booking-service

- `POST /user/register` и `POST /user/auth` возвращают токен.
- Claims: `sub` (username), `userId`, `roles`, `iat`, `exp` (1 час).
- Один общий секрет (`JWT_SECRET`) во всех сервисах.
- Каждый сервис — Resource Server (`spring-boot-starter-oauth2-resource-server`),
  `JwtDecoder` на общем секрете, `@PreAuthorize` на эндпойнтах.
- Gateway тоже валидирует токен (ранний 401) и проксирует его дальше.
- Альтернатива для «взрослого» варианта — RSA/JWKS, но для сдачи HS256 проще и
  полностью удовлетворяет требованиям.

### 3.4. Безопасность INTERNAL-эндпойнтов

`confirm-availability` и `release` не маршрутизируются через Gateway.
booking-service вызывает их напрямую по Eureka и пробрасывает исходный
`Authorization` + `X-Request-Id` через Feign `RequestInterceptor`.
На эндпойнтах — `isAuthenticated()`; в README фиксируем, что в проде это
закрывается сетью/mTLS или service-token.

### 3.5. Сага и блокировки

- `bookings.status`: `PENDING → CONFIRMED` или `PENDING → CANCELLED`.
- В hotel-service добавляем таблицу `room_locks(id, room_id, booking_id, start_date,
  end_date, created_at)`:
  - `confirm-availability` идемпотентен по `bookingId`: повтор возвращает успех
    без повторного инкремента `times_booked`;
  - проверяет операционный статус номера и пересечение с другими блокировками;
  - создаёт блокировку и атомарно увеличивает `times_booked` (`UPDATE ... SET
    times_booked = times_booked + 1`);
  - `release` идемпотентно удаляет блокировку по `booking_id`.
- `times_booked` — исторический счётчик подтверждённых броней; при отмене не
  уменьшаем (это «количество бронирований», а не текущая загрузка).
- В booking-service добавляем `room_guards(room_id)` — пессимистичная блокировка
  строки на время проверки пересечений и вставки `PENDING`. Это сериализует
  конкурентные попытки на один номер в рамках одного инстанса и между ними.
- Удалённый вызов выполняется **после** коммита `PENDING` (отдельная транзакция),
  чтобы сага была видима и компенсация работала.

### 3.6. Идемпотентность и повторы

- `bookings.request_id` UNIQUE: повторный `POST /booking` с тем же `requestId`
  возвращает существующую бронь.
- Feign: connect/read timeout, `Retryer` с экспоненциальным backoff (3 попытки),
  повтор только для timeout/5xx, **не** для 409.
- Resilience4j CircuitBreaker на вызове hotel-service; fallback → компенсация.
- Все переходы состояний логируются с `bookingId` и `requestId`.

### 3.7. Трассировка

- Gateway: если нет `X-Request-Id` — генерирует UUID, кладёт в MDC и заголовок.
- Каждый сервис: servlet-фильтр читает `X-Request-Id` → MDC → очистка.
- Feign interceptor пробрасывает `X-Request-Id` и `Authorization`.
- В логах booking-service дополнительно `bookingId`.

## 4. Итоговый API-контракт

Порты: gateway 8080, booking 8081, hotel 8082, eureka 8761.

### 4.1. booking-service (8081)

| Метод | Путь | Роль | Назначение |
|---|---|---|---|
| POST | `/user/register` | public | регистрация USER + JWT |
| POST | `/user/auth` | public | вход + JWT |
| POST | `/user` | ADMIN | создать пользователя (роль задаётся) |
| PATCH | `/user` | ADMIN | обновить пользователя (id в теле) |
| DELETE | `/user?id={id}` | ADMIN | удалить пользователя |
| POST | `/booking` | USER | создать бронь (`autoSelect`) |
| GET | `/bookings?page=&size=` | USER | своя история (пагинация) |
| GET | `/booking/{id}` | USER/ADMIN | бронь по id (чужая — 403) |
| DELETE | `/booking/{id}` | USER/ADMIN | отменить (PENDING/CONFIRMED) |

Тело `POST /booking`:
```json
{
  "hotelId": "uuid (опционально, для autoSelect)",
  "roomId": "uuid (игнорируется при autoSelect=true)",
  "startDate": "2026-11-10",
  "endDate": "2026-11-12",
  "autoSelect": true,
  "requestId": "uuid"
}
```
Ответ: `BookingDto(id, userId, roomId, hotelId, startDate, endDate, status, createdAt)`.

### 4.2. hotel-service (8082)

| Метод | Путь | Роль | Назначение |
|---|---|---|---|
| GET | `/api/hotels?name=&page=&size=&sort=` | USER/ADMIN | список отелей (фильтр/пагинация) |
| GET | `/api/hotels/{id}` | USER/ADMIN | отель |
| POST | `/api/hotels` | ADMIN | создать отель |
| PUT | `/api/hotels/{id}` | ADMIN | изменить отель |
| DELETE | `/api/hotels/{id}` | ADMIN | удалить отель |
| GET | `/api/rooms?hotelId=&page=&size=` | USER/ADMIN | свободные операционно номера |
| GET | `/api/rooms/recommend` | USER/ADMIN | свободные, сортировка `timesBooked ASC, id ASC` |
| GET | `/api/rooms/{id}` | USER/ADMIN | номер |
| POST | `/api/rooms` | ADMIN | добавить номер `{hotelId, number}` |
| PUT | `/api/rooms/{id}` | ADMIN | изменить номер (number, available) |
| DELETE | `/api/rooms/{id}` | ADMIN | удалить номер |
| GET | `/api/rooms/stats` | ADMIN | статистика загруженности |
| POST | `/api/rooms/{id}/confirm-availability` | INTERNAL | блокировка периода + `timesBooked++` |
| POST | `/api/rooms/{id}/release` | INTERNAL | снять блокировку |

`POST /api/rooms/{id}/confirm-availability`:
```json
{ "bookingId": "uuid", "startDate": "2026-11-10", "endDate": "2026-11-12", "requestId": "uuid" }
```
`POST /api/rooms/{id}/release`:
```json
{ "bookingId": "uuid", "requestId": "uuid" }
```

### 4.3. gateway-service (8080)

| Путь | Куда |
|---|---|
| `/api/hotels/**` | `lb://hotel-service` |
| `GET /api/rooms/**` | `lb://hotel-service` |
| `POST /api/rooms` | `lb://hotel-service` |
| `PUT/DELETE /api/rooms/{id}` | `lb://hotel-service` |
| `/user/**`, `/booking/**`, `/bookings/**` | `lb://booking-service` |

`POST /api/rooms/{id}/confirm-availability` и `/release` через Gateway **не**
публикуются (нет маршрута).

## 5. Схема БД (с расширениями)

hotel-service:
```
hotels(id UUID PK, name, address)
rooms(id UUID PK, hotel_id UUID, number INT, available VARCHAR, times_booked INT)
room_locks(id UUID PK, room_id UUID, booking_id UUID UNIQUE, start_date DATE,
           end_date DATE, created_at TIMESTAMP)
UNIQUE(rooms.hotel_id, rooms.number)
INDEX(room_locks.room_id, start_date, end_date)
```
booking-service:
```
users(id UUID PK, username UNIQUE, password, role)
bookings(id UUID PK, user_id UUID, room_id UUID, start_date DATE, end_date DATE,
         status VARCHAR, created_at TIMESTAMP, request_id UUID UNIQUE, version BIGINT)
room_guards(room_id UUID PK)
INDEX(bookings.room_id, start_date, end_date, status)
INDEX(bookings.user_id)
```
`available`: `AVAILABLE | MAINTENANCE | DECOMMISSIONING`.

## 6. Матрица критериев приёмки → что делаем

| Критерий | Макс. | Реализация | Этап |
|---|---|---|---|
| 1. Планирование занятости | 5 | `times_booked`, `recommend` сортировка, autoSelect, pessimistic guard + идемпотентность | 2, 4, 5 |
| 2. Согласованность (сага) | 5 | PENDING→CONFIRMED/CANCELLED, release, Retry+Timeout+CircuitBreaker | 5 |
| 3. Booking CRUD | 5 | CRUD, пагинация, валидация дат, идемпотентность, «только свои» | 4, 5 |
| 4. Hotel CRUD/статистика | 5 | CRUD, фильтры/сортировки, stats | 1, 2 |
| 5. Gateway+Eureka | 5 | `lb://`, security-фильтры, traceId | 3, 6 |
| 6. Безопасность | 5 | Resource Server в каждом сервисе, `@PreAuthorize`, 401/403 | 4 |
| 7. Тестирование | 3 | JUnit+MockMvc, сага, негативные кейсы | 8 |
| 8. Схема БД | 2 | минимальная схема + locks/guards/индексы | 1–5 |
| 9. Предзаполнение | 2 | seed: отели, номера, admin, user, брони | 7 |
| 10–11. Качество/стиль | 5 | SOLID, слои, MapStruct, единый стиль | всё |
| 12. Ошибки | 2 | `ErrorDTO` + `@RestControllerAdvice` + статусы | 1, 5 |
| 13. Логи/trace | 2 | MDC traceId, bookingId, ключевые шаги | 6 |
| 14. README/ADR | 2 | README + mermaid-схема + ADR | 9 |
| 15. Тесты поддерживаемы | 2 | AAA, моки Feign, независимость от Eureka | 8 |

## 7. Этапы выполнения

### Этап 0. Сборка, версии, структура (0.5 дня)

- Корневой POM — родитель для всех модулей:
  - Boot `3.5.16`, Java 21, Cloud `2025.0.3`;
  - общий `maven-compiler-plugin` с annotation processors: Lombok, MapStruct,
    `lombok-mapstruct-binding`;
  - версии MapStruct 1.6.3, springdoc 2.8.x.
- Модульные POM: только свои зависимости, без своих parent/`dependencyManagement`.
- Заменить Boot 4-стартеры на Boot 3.5:
  `spring-boot-starter-web`, `spring-boot-starter-data-jpa`,
  `spring-boot-starter-security`, `spring-boot-starter-validation`,
  `spring-boot-starter-oauth2-resource-server`, `spring-boot-starter-test`.
- H2: `spring.datasource.url=jdbc:h2:mem:<db>;DB_CLOSE_DELAY=-1`,
  `ddl-auto=create-drop`, `h2.console.enabled=true`.
- `.gitignore`: `.idea/`; убрать `.idea` из индекса.
- Починить запуск Maven (wrapper или локальный Maven/IDE).

**Проверка:** `mvn clean package` по корню, 4 jar.

### Этап 1. hotel-service: домен и API (1.5 дня)

1. `Hotel`, `Room` (без `@GeneratedValue` на number), `RoomLock`.
2. DTO + Jakarta Validation; MapStruct-интерфейсы.
3. Репозитории: `findAllByHotelId`, `findAllByAvailable`, `recommend`
   (`timesBooked ASC, id ASC`), `existsByHotelIdAndNumber`, статистика.
4. Сервисы: CRUD отелей/номеров, проверка существования отеля, уникальность номера,
   список свободных, рекомендации, статистика.
5. `RoomLockService`: confirm/release + идемпотентность по `bookingId`,
   атомарный инкремент `times_booked`, проверка пересечений.
6. Контроллеры `/api/hotels`, `/api/rooms`; коды 201/204/400/404/409.
7. `@RestControllerAdvice` + `ErrorDTO` (`timestamp, status, error, message, path,
   requestId`).
8. Пока без security (добавим на этапе 4, когда появятся токены) — но структуру
   пакета `config/security` закладываем.

**Поясню:** разницу между операционным `available` и занятостью по датам; почему
блокировка живёт в hotel-service, а брони — в booking-service; как считается
пересечение интервалов (`start < other.end && end > other.start`); почему
инкремент `times_booked` — атомарный UPDATE.

**Проверка:** Swagger/Postman: отель → номер → списки → recommend → confirm →
повторный confirm (идемпотентно) → release.

### Этап 2. Eureka (0.5 дня)

- `@EnableEurekaServer`, YAML сервера (`register-with-eureka=false`,
  `fetch-registry=false`).
- YAML клиентов (`defaultZone`, `prefer-ip-address`, lease).
- Дашборд 8761: hotel UP.

**Поясню:** как работает регистрация/обнаружение, что такое lease и почему
`lb://hotel-service` не резолвится без Eureka.

### Этап 3. booking-service: пользователи и JWT (1 день)

- `User` (BCrypt), `UserRepository`, `UserService`.
- `JwtService` (HS256, `JwtEncoder`/`JwtDecoder`, 1 час, claims).
- `POST /user/register`, `/user/auth`, admin CRUD пользователей.
- Security-конфиг booking-service: Resource Server, permit register/auth,
  `@PreAuthorize` для ADMIN/USER.
- В hotel-service добавляем такой же Resource Server + `@PreAuthorize`
  (INTERNAL — `isAuthenticated()`).
- Обработчики 401/403 в формате `ErrorDTO`.

**Поясню:** структуру JWT, подпись/claims/exp, почему секрет общий, как
`@PreAuthorize` читает роли, чем Resource Server отличается от login-формы.

**Проверка:** register → login → токен → доступ к защищённому эндпойнту;
без токена — 401; USER на админский — 403.

### Этап 4. booking-service: бронирования (1.5 дня)

- `Booking`, `BookingRepository` (пересечения, история, пагинация).
- `BookingService`: валидация дат, autoSelect через `GET /api/rooms/recommend`,
  проверка пересечений в своей БД, создание `PENDING`.
- `room_guards` + пессимистичная блокировка для конкурентных попыток.
- Идемпотентность по `requestId`.
- `GET /bookings`, `GET /booking/{id}`, `DELETE /booking/{id}` с проверкой владельца.

**Поясню:** почему нельзя проверить занятость в hotel-service (там нет дат
броней), как работает guard-строка, почему 409, а не 500.

### Этап 5. Сага + Feign + устойчивость (1.5 дня)

- `HotelClient` (`@FeignClient(name="hotel-service")`), DTO запросов/ответов.
- Feign interceptor: `Authorization`, `X-Request-Id`.
- Двухшаговая сага:
  - `PENDING` коммитится отдельной транзакцией;
  - confirm → `CONFIRMED`;
  - ошибка/timeout/409 → `release` → `CANCELLED`;
  - отмена `CONFIRMED` → `release` → `CANCELLED`.
- Timeout, Retry с backoff, CircuitBreaker + fallback.
- Логи переходов с `bookingId`.
- Маппинг ошибок Feign в 409/502/503.

**Поясню:** почему нет распределённой транзакции; что такое компенсация и почему
release идемпотентен; как Retry не должен повторять 409; как CircuitBreaker
защищает booking-service.

**Проверка:** успех; остановленный hotel → timeout → CANCELLED; повтор с тем же
`requestId` → та же бронь; две параллельные брони → одна 201, вторая 409.

### Этап 6. Gateway (1 день)

- `spring-cloud-starter-gateway-mvc` + loadbalancer + eureka client.
- Маршруты из раздела 4.3, `lb://`.
- Spring Security: Resource Server, публичные register/auth, остальное — JWT;
  401/403 в `ErrorDTO`.
- Фильтр traceId; проброс `X-Request-Id`.
- CORS для Swagger/Postman.
- Внутренние маршруты не публикуем.

**Поясню:** чем MVC-gateway отличается от WebFlux; почему `lb://` лучше
`localhost:8082`; где проверять токен; как не открыть INTERNAL наружу.

### Этап 7. Сквозные вещи и seed (0.5 дня)

- springdoc + Bearer-схема в каждом сервисе.
- `CommandLineRunner`/`data.sql`: 2–3 отеля, номера с разным `times_booked`,
  admin/admin, user/user.
- Actuator health.
- Структурные логи (JSON) — если успеваем, для максимума по критерию 13.

### Этап 8. Тесты (1 день)

- Unit: `RoomLockService` (идемпотентность, пересечения), `BookingService`
  (валидация, autoSelect, сага), `JwtService`.
- MockMvc: коды 200/201/400/401/403/404/409, `ErrorDTO`.
- Сага: hotel вернул 500/timeout/409 → CANCELLED + release.
- Конкурентность: два потока на один номер/даты → без дублей.
- Тесты не поднимают Eureka, Feign замокан.

### Этап 9. README, ADR, демо (0.5 дня)

- README: схема (mermaid), порты, порядок запуска, эндпойнты, примеры curl,
  как прогнать тесты.
- ADR: Boot 3.5/Cloud 2025.0, MVC-gateway, HS256, сага+locks, идемпотентность.
- Чек-лист критериев 1–15.
- Git: осмысленные коммиты по этапам.

## 8. Порядок запуска и проверки

```
eureka-service  :8761
hotel-service   :8082
booking-service :8081
gateway-service :8080
```
Демо-сценарий:
1. `POST /user/register` → токен.
2. `POST /api/hotels` (ADMIN) → отель.
3. `POST /api/rooms` (ADMIN) → номера.
4. `GET /api/rooms/recommend` (USER).
5. `POST /booking` с `autoSelect=true`.
6. `GET /bookings` — история.
7. Остановить hotel-service → `POST /booking` → 503, бронь CANCELLED.
8. Поднять hotel → повтор с тем же `requestId` → без дубля.

## 9. Риски

| Риск | Митигация |
|---|---|
| Spring Security 7 (если остаться на Boot 4) | downgrade на 3.5.16 |
| H2 не умеет exclusion-constraint по датам | guard-строка + проверка в транзакции |
| Feign Retry повторяет неидемпотентный confirm | идемпотентность по `bookingId` на hotel |
| Блокировка осталась после падения | release идемпотентен; повтор при отмене/рестарте |
| Eureka не видит сервисы | `prefer-ip-address`, health-check, дашборд |
| Время до дедлайна (9 дней) | тесты и README — обязательный минимум; NFR по остатку |

## 10. Что дальше

Начинаем с этапа 0–1: чиним POM/версии и доводим hotel-service до рабочего API.
Перед этим подтвердить одно решение: **downgrade на Spring Boot 3.5.16 +
Spring Cloud 2025.0.3** (рекомендация курса) или остаться на текущем Boot 4.1.1.
