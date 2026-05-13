# external-http-stub-service

HTTP-стаб для имитации внешних зависимостей order-processor-service. Экспонирует три эндпоинта на `/order-stub/...`, умеет управляемо задерживать ответы, генерировать ошибки/отказы и работает на виртуальных потоках (опционально).

## Эндпоинты
- `POST /order-stub/inventory/reserve?orderId={id}`  
  Ответ: `{ "orderId": "...", "stockReserved": true|false, "reserveCancellationReason": "...?" }`. Может вернуть отказ по вероятности.
- `POST /order-stub/pricing?orderId={id}`  
  Ответ: `{ "orderId": "...", "price": <number> }`. Может бросить исключение.
- `POST /order-stub/shipping/schedule?orderId={id}`  
  Ответ: `{ "orderId": "...", "etaDeliveryDays": <int> }`. Может бросить исключение.

## Конфигурация (env → `stub.*`)
См. `src/main/resources/application.yml`. Параметры через переменные окружения:
- `SPRING_THREADS_VIRTUAL_ENABLED` — включить виртуальные потоки.
- Inventory: `STUB_INVENTORY_REJECT_ENABLED`, `STUB_INVENTORY_LATENCY_MIN_MILLIS`, `STUB_INVENTORY_LATENCY_MAX_MILLIS`, `STUB_INVENTORY_REJECT_PROBABILITY`.
- Pricing: `STUB_PRICING_EXCEPTION_ENABLED`, `STUB_PRICING_LATENCY_MIN_MILLIS`, `STUB_PRICING_LATENCY_MAX_MILLIS`, `STUB_PRICING_EXCEPTION_PROBABILITY`.
- Shipping: `STUB_SHIPPING_EXCEPTION_ENABLED`, `STUB_SHIPPING_LATENCY_MIN_MILLIS`, `STUB_SHIPPING_LATENCY_MAX_MILLIS`, `STUB_SHIPPING_EXCEPTION_PROBABILITY`.
- `SERVER_PORT` — порт приложения (по умолчанию 8080; обычно 8081 вместе с order-processor-service).

## Локальный запуск (Gradle)
```bash
./gradlew bootRun                   # при необходимости SERVER_PORT=8081
```
Базовый URL после старта: `http://localhost:8080/order-stub` (или с вашим портом).

## Docker
- Быстрая сборка образа:  
  ```bash
  ./dev-build-image.sh   # соберет external-http-stub-service:latest
  ```
- Ручная сборка/запуск:  
  ```bash
  docker build -t external-http-stub-service:latest .
  docker run -p 8081:8081 -e SERVER_PORT=8081 external-http-stub-service:latest
  ```

## Проверка
- Тесты: `./gradlew test` (загрузка контекста).
- Ручные вызовы:  
  ```bash
  curl -X POST "http://localhost:8081/order-stub/inventory/reserve?orderId=1" -H "Accept: application/json"
  curl -X POST "http://localhost:8081/order-stub/pricing?orderId=1" -H "Accept: application/json"
  curl -X POST "http://localhost:8081/order-stub/shipping/schedule?orderId=1" -H "Accept: application/json"
  ```

## Заметки
- Сервис не использует БД; вся логика в памяти.