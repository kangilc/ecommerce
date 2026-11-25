# ecommerce

> **✨ Gemini API 연동 추가 (`shop-product` AI 상품 카피 생성 & 멀티턴 Q&A)**  
> 변경된 아키텍처, API 명세, 환경변수(`GOOGLE_API_KEY`) 및 실행 방법은 [docs/GEMINI_AI.md](docs/GEMINI_AI.md)에서 확인할 수 있습니다.

Prompt
쇼핑몰 backend를 spring boot로 구축하고자 한다.
Kafka를 이용한 이벤트 기반 시스템으로 개발 진행 예정.
skill or spec: spring boot 3.x 사용, jdk 21, Mysql 8.x, hibernate, jpa, feign client, Kafka
프로젝트를 구축하기위해서 필요한 것이 있으면 알려줘

쇼핑몰 백엔드를 이벤트 기반 아키텍처(EDA)와 마이크로서비스(또는 모듈러 모놀리스) 형태로 구축하시려는 멋진 계획이네요! 제시해주신 기술 스택(Spring Boot 3.x, JDK 21, MySQL 8.x, JPA, Feign Client, Kafka)은 현재 가장 트렌디하고 안정적인 조합입니다.

성공적인 프로젝트 구축을 위해 준비하고 고려해야 할 사항들을 단계별로 정리해 드립니다.

---

### 1. 개발 환경 및 필수 도구 (Prerequisites)
* **Java 21**: JDK 21 (LTS) 설치 및 환경 변수 설정 (Virtual Threads, Record 등 활용 권장)
* **IDE**: IntelliJ IDEA (Ultimate 권장이나 Community도 가능) 또는 Eclipse
* **빌드 툴**: Gradle (최근 트렌드) 또는 Maven
* **Docker & Docker Compose**: 로컬 환경에서 MySQL과 Kafka를 쉽게 띄우고 테스트하기 위해 필수적입니다.
* **API 테스트 도구**: Postman, Insomnia, 또는 IntelliJ 내장 HTTP Client

### 2. Spring Boot 초기 프로젝트 구성 (Dependencies)
Spring Initializr(`start.spring.io`)를 통해 프로젝트를 생성할 때 다음 의존성(Dependencies)을 추가해야 합니다. (여러 개의 마이크로서비스로 나눈다면 각 서비스 성격에 맞게 추가)

* **Spring Web**: REST API 구축
* **Spring Data JPA**: Hibernate 기반 데이터베이스 연동
* **MySQL Driver**: MySQL 8.x 연결
* **Spring for Apache Kafka**: Kafka 프로듀서 및 컨슈머 연동
* **Spring Cloud OpenFeign**: 서비스 간 동기식(HTTP) 통신 (※ Feign을 사용하려면 Spring Cloud 버전 관리가 필요합니다)
* **Lombok**: 보일러플레이트 코드 제거
* **Validation**: DTO 검증용 (`Jakarta Bean Validation`)

### 3. 로컬 인프라 구축 (Docker Compose)
MySQL과 Kafka를 로컬에 직접 설치하는 것보다 Docker를 사용하는 것이 훨씬 편리합니다. 다음과 같은 `docker-compose.yml` 파일이 필요합니다.

* **MySQL 8.x**: 쇼핑몰 데이터(회원, 상품, 주문 등) 저장
* **Kafka & Zookeeper (또는 KRaft 모드)**: 이벤트 브로커. 최근에는 Zookeeper를 제거한 KRaft 모드의 Kafka를 많이 사용합니다.
* *(선택)* **Kafka UI 또는 Dropdrop**: Kafka 토픽과 메시지를 시각적으로 모니터링하기 위한 웹 도구

### 4. 아키텍처 및 도메인 설계 (핵심)
기술 스택으로 보아 마이크로서비스 아키텍처(MSA)를 지향하시는 것 같습니다.

* **서비스 분리**: User, Product, Order, Payment 등의 도메인으로 서비스를 나눌지 (MSA), 아니면 하나의 프로젝트 안에서 모듈로 나눌지 결정해야 합니다.
* **동기 vs 비동기 통신 설계**:
    * **Feign Client (동기)**: 실시간으로 데이터 조회가 꼭 필요한 경우 (예: 주문 전 상품 재고 리얼타임 확인)
    * **Kafka (비동기)**: 결과적 일관성(Eventual Consistency)으로 처리해도 되는 경우 (예: 주문 완료 후 결제 처리 모듈로 이벤트 발행, 알림 발송 등)
* **데이터베이스 분리**: MSA라면 원칙적으로 각 서비스는 자신만의 독립적인 DB 스키마를 가져야 합니다.

### 5. 추가적으로 고려하면 좋은 기술/패턴
* **트랜잭셔널 아웃박스 패턴 (Transactional Outbox Pattern)**: DB에 데이터를 저장하는 트랜잭션과 Kafka로 이벤트를 발행하는 동작의 원자성을 보장하기 위해 필요합니다. (JPA + Kafka 조합에서 매우 중요)
* **문제 해결 및 추적 (Distributed Tracing)**: 여러 서비스와 Kafka를 거치는 요청을 추적하기 위해 `Micrometer Tracing`과 `Zipkin`을 도입하는 것을 추천합니다.
* **분산 트랜잭션 (Saga 패턴)**: 주문 생성 -> 결제 실패 시 주문 취소(보상 트랜잭션)와 같은 흐름을 어떻게 제어할지(Choreography vs Orchestration) 설계가 필요합니다.

---

### 📝 앞으로의 진행 계획 제안 (Work-In-Progress & Plans)

사용자님의 규칙에 따라 앞으로 제가 도와드릴 수 있는 작업 계획을 제안 드립니다. 원하시는 단계를 말씀해주시면 설계부터 코드 작성까지 함께 진행하겠습니다.

* [ ] **Step 1: 인프라 구성 준비**
    * 로컬 개발용 `docker-compose.yml` (MySQL 8, Kafka, UI 연동) 작성 및 실행 가이드
* [ ] **Step 2: 기본 프로젝트 골격 생성**
    * Spring Boot + Gradle 기반의 멀티 모듈 또는 단일 프로젝트 스캐폴딩 설정
    * `application.yml`에 DB, Kafka, Feign 초기 설정 작성
* [ ] **Step 3: 아키텍처 및 도메인 설계 심화**
    * 주문(Order) 시스템을 예시로 한 Kafka 프로듀서/컨슈머 코드 작성
    * Feign Client를 이용한 서비스 간 동기 통신 인터페이스 작성
* [ ] **Step 4: 데이터 모델링 (JPA/Hibernate)**
    * 쇼핑몰 핵심 엔티티(User, Item, Order) 구조 설계 및 JPA 매핑

위의 내용 중 어떤 것부터 시작할까요? 혹은 로컬에 바로 적용할 수 있는 `docker-compose.yml` 파일이나 전체 프로젝트 디렉터리 아키텍처 구조를 먼저 짜드릴까요?

알겠습니다! 제안해 드린 계획의 **Step 1(인프라 구성 준비)**과 전체적인 **프로젝트 디렉터리 아키텍처 구조**부터 바로 시작하겠습니다.

### 🚧 [현재 작업 진행 상태: Work-In-Progress]
* **[진행 중] Step 1: 인프라 구성 준비 (Docker Compose 및 아키텍처)**
* [대기] Step 2: Spring Boot 기본 프로젝트 골격 생성 (의존성 및 설정)
* [대기] Step 3: 도메인 설계 및 통신 (Kafka / Feign 코드 예시)
* [대기] Step 4: 데이터 모델링 (JPA/Hibernate)

---

### 1. 로컬 환경 `docker-compose.yml` (MySQL 8 + Kafka)

로컬에서 개발하실 때 MySQL과 Kafka를 손쉽게 띄우고 테스트하기 위한 설정입니다. 프로젝트 최상위 디렉터리에 `docker-compose.yml` 파일을 만들고 아래 내용을 작성해 주시면 됩니다. (요즘은 Zookeeper 없이 더 가볍게 띄울 수 있는 Kafka KRaft 모드를 많이 사용합니다.)

```yaml
version: '3.8'

services:
  # 1. MySQL 8.x 설정
  mysql:
    image: mysql:8.0
    container_name: shop_mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: shop_db
      MYSQL_USER: shop_user
      MYSQL_PASSWORD: shop_password
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
    volumes:
      - mysql_data:/var/lib/mysql

  # 2. Kafka 설정 (KRaft 모드 - Zookeeper 불필요)
  kafka:
    image: bitnami/kafka:3.5
    container_name: shop_kafka
    ports:
      - "9092:9092"
    environment:
      # KRaft 설정
      KAFKA_ENABLE_KRAFT: 'yes'
      KAFKA_CFG_PROCESS_ROLES: broker,controller
      KAFKA_CFG_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_BROKER_ID: 1
      KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: 1@127.0.0.1:9093
      ALLOW_PLAINTEXT_LISTENER: 'yes'

  # 3. Kafka UI (선택사항, 브라우저에서 Kafka 토픽과 메시지를 볼 수 있는 툴)
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: shop_kafka_ui
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
    depends_on:
      - kafka

volumes:
  mysql_data:
```

> **[💡 실행 방법]**
> 위 파일이 있는 경로에서 터미널을 열고 `docker-compose up -d` 명령어를 실행하면 DB와 Kafka가 백그라운드에서 실행됩니다. Kafka UI는 브라우저에서 `http://localhost:8080`으로 접속하여 확인 가능합니다.

---

### 2. 프로젝트 통합 아키텍처 및 패키지 구조 (MSA 또는 모듈러 모놀리스 관점)

Kafka와 Feign Client를 모두 사용하시기 때문에, 서비스 간 경계(Bounded Context)가 명확해야 합니다.
처음부터 완전한 MSA로 리포지토리를 여러 개 만들면 관리가 어려울 수 있으므로, **초기엔 하나의 리포지토리 안에서 멀티 모듈 구조(모듈러 모놀리스)**를 가져가는 것을 추천합니다.

#### 🏗️ 추천 패키지 구조 (Domain 기반)

```text
shop-backend (Root)
 ┣ 📂 shop-common        # 공통 로직 모듈 (예외 처리, 공통 유틸, BaseEntity 등)
 ┣ 📂 shop-order         # 주문 서비스 모듈 (포트: 8081)
 ┃ ┣ 📂 controller      # REST API 엔드포인트
 ┃ ┣ 📂 service         # 비즈니스 로직
 ┃ ┣ 📂 domain          # JPA 엔티티 (예: Order, OrderLine) & Repository
 ┃ ┣ 📂 infrastructure  # 외부 연동 구체화 클래스
 ┃ ┃ ┣ 📂 kafka        # Kafka 프로듀서 (결제 성공 이벤트 발행 등)
 ┃ ┃ ┗ 📂 feign        # Feign Client (결제 모듈, 상품 모듈 호출 등)
 ┃ ┗ 📂 application     # 파사드(Facade) 또는 외부 API 호출/이벤트 조합 계층
 ┣ 📂 shop-product       # 상품 서비스 모듈 (포트: 8082)
 ┃ ┗ ... (동일한 구조의 JPA, Kafka 컨슈머 등)
 ┗ 📂 shop-payment       # 결제 서비스 모듈 (포트: 8083)
   ┗ ... (요청은 Feign으로 받고 처리는 Kafka로 주문 모듈에 이벤트 던짐)
```

이러한 구조를 따르면,
1. **Feign Client**로는 `Order` -> `Product`의 **재고 확인**(동기성 요구)을 처리하고,
2. **Kafka**로는 `Payment` 완료 후 -> `Order` 의 **주문 상태 변경 및 알림 발행**(비동기성 적용)을 처리할 수 있어 역할이 명확히 분리됩니다.

### 🚀 다음 진행 계획 (Step 2)
1. 현재 계신 디렉터리(`c:\git\nebp\admin`)에 이 프로젝트 구조를 만드시겠습니까? 아니면 다른 디렉터리에 새로운 Spring Boot 멀티 모듈 뼈대를 생성 및 세팅(Gradle 설정, 디렉터리 생성)을 도와드릴까요?
2. 괜찮으시다면 제가 명령어(`run_command`, `write_to_file`)를 실행하여 초기 `build.gradle` 세팅과 기본 디렉터리를 구성해 드릴 수 있습니다. 어떻게 하시겠어요?

Searched for "net.javaf.ecommerce"
Listed directory ecommerce_be
Viewed settings.gradle:1-6
Listed directory common
Viewed build.gradle:1-48
Viewed build.gradle:1-12
Listed directory order
Listed directory shop-order
Viewed docker-compose.yml:1-49
Viewed build.gradle:1-9
Created ApiResponse.java
Created OrderApplication.java
Created ProductApplication.java
Created PaymentApplication.java
Created application.yml
Created application.yml
Created application.yml

수정하신 내용을 기반으로 E-commerce Backend 프로젝트의 다음 단계 스캐폴딩 작업을 진행했습니다!

현재 프로젝트는 **MSA(Microservices Architecture) 및 Event-Driven Architecture**를 목표로 하고 있으므로, 각 도메인(주문, 상품, 결제) 모듈이 정상적인 Spring Boot 애플리케이션으로 동작하도록 기본 뼈대를 만들었습니다.

### 🛠️ 작업 진행 내역 (Work-in-progress)
1. **`shop-common` 공통 모듈 구성:**
    - 모든 서비스에서 공통으로 사용할 `ApiResponse<T>` 스펙(성공/에러 래핑) 모델을 `com.ecommerce.common.response` 패키지에 생성했습니다.

2. **메인 애플리케이션 구동 클래스 생성:**
    - `shop-order`: [OrderApplication.java](cci:7://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/OrderApplication.java:0:0-0:0) 생성
    - `shop-product`: [ProductApplication.java](cci:7://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/ProductApplication.java:0:0-0:0) 생성
    - `shop-payment`: [PaymentApplication.java](cci:7://file:///c:/git/ecommerce_be/shop-payment/src/main/java/com/ecommerce/payment/PaymentApplication.java:0:0-0:0) 생성
    - *각 클래스에 `@EnableFeignClients`를 추가하여 추후 서비스 간 동기 호출(FeignClient) 준비를 마쳤습니다.*

3. **서비스별 설정 파일([application.yml](cci:7://file:///c:/git/ecommerce_be/shop-order/src/main/resources/application.yml:0:0-0:0)) 구성:**
    - `shop-order` 포트: `8081`
    - `shop-product` 포트: `8082`
    - `shop-payment` 포트: `8083`
    - [docker-compose.yml](cci:7://file:///c:/git/ecommerce_be/docker-compose.yml:0:0-0:0) 컨테이너 정보를 바탕으로 **MySQL (3306)**, **Kafka (9092)** 연결 설정을 각 서비스에 모두 매핑했습니다.
    - JPA `ddl-auto: update` 및 포맷 로깅 설정을 반영했습니다.

---

### 📋 향후 진행 계획 (Plans for future tasks)
기본적인 세팅이 완료되었으므로 이제 핵심 도메인 설계와 비즈니스 로직 작성으로 넘어갈 수 있습니다. 다음 중 원하시는 방향을 선택해 주시면 이어서 작업하겠습니다.

1. **JPA Base Entity 및 공통 예외 처리기(GlobalExceptionHandler) 설정:**
   `shop-common`에 생성일자/수정일자 자동 생성을 위한 엔티티와 에러 핸들링 추가
2. **도메인 모델(Entity) 설계 및 생성:**
   비즈니스 요구사항에 따라 [Order](cci:2://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/OrderApplication.java:6:0-12:1), [Product](cci:2://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/ProductApplication.java:6:0-12:1), [Payment](cci:2://file:///c:/git/ecommerce_be/shop-payment/src/main/java/com/ecommerce/payment/PaymentApplication.java:6:0-12:1) 엔티티와 Repository 계층 생성
3. **Kafka Event Producer/Consumer 기본 연동 구성:**
   '주문 생성 이벤트'를 브로커에 던지고 결제/상품 서비스가 수신하는 테스트 로직 작성

어느 부분부터 작업을 시작할까요? 혹은 바로 DB와 Kafka를 띄워 애플리케이션 실행 테스트를 먼저 해보시겠어요?

Viewed build.gradle:1-12
Edited build.gradle
Created BaseEntity.java
Created JpaAuditConfig.java
Created ErrorCode.java
Created CustomException.java
Created GlobalExceptionHandler.java
Created Order.java
Created OrderStatus.java
Created OrderRepository.java
Created Product.java
Created ProductRepository.java
Created Payment.java
Created PaymentStatus.java
Created PaymentRepository.java
Viewed build.gradle:1-9
Created OrderCreatedEvent.java
Created OrderEventProducer.java
Created OrderEventConsumer.java
Created OrderEventConsumer.java
Viewed build.gradle:1-9

요청하신 대로 1번부터 3번까지의 기본 스캐폴딩과 엔티티 설정을 순서대로 모두 완료했습니다!

### ✅ 완료된 작업 내역 (Work-in-progress)

**1. JPA 공통 엔티티 및 예외 처리 설정**
- `shop-common`에 [BaseEntity.java](cci:7://file:///c:/git/ecommerce_be/shop-common/src/main/java/com/ecommerce/common/entity/BaseEntity.java:0:0-0:0)를 생성하여 생성일자(`createdAt`), 수정일자(`updatedAt`)가 자동으로 매핑되도록 JPA Auditing을 적용했습니다.
- [JpaAuditConfig.java](cci:7://file:///c:/git/ecommerce_be/shop-common/src/main/java/com/ecommerce/common/config/JpaAuditConfig.java:0:0-0:0)를 설정하여 Auditing 기능을 활성화했습니다.
- 공통 에러 처리를 위해 [ErrorCode.java](cci:7://file:///c:/git/ecommerce_be/shop-common/src/main/java/com/ecommerce/common/exception/ErrorCode.java:0:0-0:0) (Enum), [CustomException.java](cci:7://file:///c:/git/ecommerce_be/shop-common/src/main/java/com/ecommerce/common/exception/CustomException.java:0:0-0:0), 그리고 HTTP 요청 시 커스텀 예외를 잡아낼 [GlobalExceptionHandler.java](cci:7://file:///c:/git/ecommerce_be/shop-common/src/main/java/com/ecommerce/common/exception/GlobalExceptionHandler.java:0:0-0:0)를 `shop-common`에 구성했습니다.

**2. 도메인 모델(Entity) 및 Repository 설계**
- **`shop-order`**: 주문과 관련된 정보(`productId`, `quantity`, `status`)를 담는 [Order.java](cci:7://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/entity/Order.java:0:0-0:0) 매핑 및 `OrderStatus` (PENDING, COMPLETED, CANCELED), 저장소인 [OrderRepository.java](cci:7://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/repository/OrderRepository.java:0:0-0:0) 인터페이스 생성
- **`shop-product`**: 상품 정보(`name`, `price`, `stockQuantity`)를 갖는 [Product.java](cci:7://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/entity/Product.java:0:0-0:0) 매핑 및 재고 감소 비즈니스 로직(예외 처리 포함) 추가, [ProductRepository.java](cci:7://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/repository/ProductRepository.java:0:0-0:0) 생성
- **`shop-payment`**: 결제 내역(`orderId`, `amount`, `status`)을 담는 [Payment.java](cci:7://file:///c:/git/ecommerce_be/shop-payment/src/main/java/com/ecommerce/payment/entity/Payment.java:0:0-0:0) 매핑 및 `PaymentStatus` (SUCCESS, FAILED), [PaymentRepository.java](cci:7://file:///c:/git/ecommerce_be/shop-payment/src/main/java/com/ecommerce/payment/repository/PaymentRepository.java:0:0-0:0) 생성

**3. Kafka Event Producer/Consumer 기본 연동 구성**
- 마이크로서비스 간 메시징을 위해 `shop-common`에 전달될 이벤트 DTO인 [OrderCreatedEvent.java](cci:7://file:///c:/git/ecommerce_be/shop-common/src/main/java/com/ecommerce/common/event/OrderCreatedEvent.java:0:0-0:0)를 생성했습니다.
- **`shop-order`**: 주문 생성 시 Kafka로 이벤트를 발행(`"order-created-topic"`)하는 [OrderEventProducer.java](cci:7://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/producer/OrderEventProducer.java:0:0-0:0) 생성
- **`shop-product`, `shop-payment`**: 해당 토픽 이벤트를 수신하여 각각 재고를 감소시키고 결제를 진행하기 위해 준비된 [OrderEventConsumer.java](cci:7://file:///c:/git/ecommerce_be/shop-payment/src/main/java/com/ecommerce/payment/consumer/OrderEventConsumer.java:0:0-0:0)를 생성했습니다. (내부 구현은 `// TODO`로 열어두었습니다)

---

### 📋 향후 진행 계획 (Plans for future tasks)
엔티티 및 이벤트 흐름을 위한 준비가 매우 깔끔하게 끝났습니다. 다음으로는 실제 비즈니스 로직 연동을 진행하면 됩니다.

1. **OrderService 및 API 구현**: 주문 생성 API를 만들고 Producer를 통해 이벤트를 발생시키는 로직 구현
2. **Product / Payment Consumer 로직 상세 구현**: Product 재고 감소 처리 및 Payment 테이블 생성 처리 연동
3. **Docker Compose 실행 및 API 통합 테스트**: 로컬 MySQL과 Kafka(UI 포함) 컨테이너를 구동한 후 실제 트랜잭션 정상 생성 및 이벤트 수신 확인

어떤 작업부터 이어서 진행하는 것이 좋을까요? 바로 주문 생성 API(1번) 부터 만들어볼까요?

Created OrderRequest.java
Created OrderService.java
Created OrderController.java
Created ProductService.java
Edited OrderEventConsumer.java
Created PaymentService.java
Edited OrderEventConsumer.java

말씀하신 1번부터 3번까지([OrderService](cci:2://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/service/OrderService.java:13:0-45:1) API 구현, [Product](cci:2://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/entity/Product.java:11:0-42:1) & [Payment](cci:2://file:///c:/git/ecommerce_be/shop-payment/src/main/java/com/ecommerce/payment/entity/Payment.java:9:0-38:1) Consumer 로직 구현)를 모두 완료했습니다!

### ✅ 완료된 작업 내역 (Work-in-progress)

**1. `shop-order`: 주문 생성 API 및 서비스 로직 구현**
- [OrderRequest.java](cci:7://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/dto/OrderRequest.java:0:0-0:0) DTO 생성 (`productId`, `quantity`, `amount` 포함)
- [OrderService.java](cci:7://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/service/OrderService.java:0:0-0:0) 구성:
    - 주문 접수 시 DB에 상태를 `PENDING`으로 자동 저장
    - 저장된 주문 정보를 모아 [OrderEventProducer](cci:2://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/producer/OrderEventProducer.java:8:0-20:1)를 통해 [OrderCreatedEvent](cci:2://file:///c:/git/ecommerce_be/shop-common/src/main/java/com/ecommerce/common/event/OrderCreatedEvent.java:6:0-14:1) 카프카 이벤트 발행
- [OrderController.java](cci:7://file:///c:/git/ecommerce_be/shop-order/src/main/java/com/ecommerce/order/controller/OrderController.java:0:0-0:0) (`POST /api/orders`) REST API 생성

**2. `shop-product`: 상품 재고 검증 및 차감 로직 연동**
- [ProductService.java](cci:7://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/service/ProductService.java:0:0-0:0) 생성: [decreaseStock()](cci:1://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/service/ProductService.java:18:4-26:5) 메서드를 통해 재고를 차감하고, 만약 재고가 부족할 경우 즉시 실패시키는 트랜잭션 구현
- [OrderEventConsumer.java](cci:7://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/consumer/OrderEventConsumer.java:0:0-0:0) 수정: 카프카로 들어오는 주문 생성 메시지를 받아 바로 [ProductService.java](cci:7://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/service/ProductService.java:0:0-0:0)와 연결하도록 처리

**3. `shop-payment`: 결제 생성 로직 구현**
- [PaymentService.java](cci:7://file:///c:/git/ecommerce_be/shop-payment/src/main/java/com/ecommerce/payment/service/PaymentService.java:0:0-0:0) 생성: [processPayment()](cci:1://file:///c:/git/ecommerce_be/shop-payment/src/main/java/com/ecommerce/payment/service/PaymentService.java:17:4-31:5) 메서드로 주문 ID와 결제 금액을 전달받아 상태 `SUCCESS`로 결제 테이블에 insert 하는 로직을 구현
- [OrderEventConsumer.java](cci:7://file:///c:/git/ecommerce_be/shop-product/src/main/java/com/ecommerce/product/consumer/OrderEventConsumer.java:0:0-0:0) 수정: 들어오는 카프카 이벤트를 읽어와 백그라운드에서 결제 생성을 하도록 연결

---

### 🚀 향후 진행 계획 및 통합 테스트 안내 (Plans for future tasks)

모든 데이터 파이프라인(주문 API 호출 ➡️ DB 저장 ➡️ Kafka 이벤트 발행 ➡️ 상품(재고 차감) 및 결제(결제 내역 생성) 컨슈머 수신)이 코드로 완성되었습니다.

다음 단계는 **실제 환경에서 서비스들을 띄우고 통합 테스트를 실행해 보는 것**입니다! 아래 단계를 통해 직접 테스트해 보실 수 있습니다:

**Step 1. 인프라 실행 (MySQL, Kafka)**:
프로젝트 최상단 폴더(`c:\git\ecommerce_be`)에서 아래 명령어로 먼저 DB와 Kafka를 가동해 주세요.
```bash
docker-compose up -d
```

**Step 2. 애플리케이션 실행**:
각각의 독립된 마이크로서비스를 실행해주세요.
```bash
# 터미널 창을 여러 개 열어 따로 가동하시면 편합니다.
./gradlew :shop-order:bootRun
./gradlew :shop-product:bootRun
./gradlew :shop-payment:bootRun
```

**Step 3. 테스트 방법**:
Postman이나 cURL을 사용하여 포트 `8081` (`shop-order`) 로 아래 데이터를 전송하면 한 번의 요청으로 3개 서비스가 연동되는 걸 확인하실 수 있습니다. 테스트 전에 `shop_db` 안의 `product` 테이블에 임의의 상품 데이터를 하나 넣어두시면 좋습니다.

```bash
# 상품 1번에 대해 2개 주문 (총 결제액 5000원 가정)
curl -X POST http://localhost:8081/api/orders \
-H "Content-Type: application/json" \
-d "{\"productId\": 1, \"quantity\": 2, \"amount\": 5000}"
```

위 로직이 정상 작동되는지 테스트해 보시고, 오류가 발생하거나 보완이 필요한 부분(Saga 패턴 적용, 실패 보상 트랜잭션 처리 등)이 있다면 말씀해 주세요!

# 직렬화/역직렬화 ClassCastException
```
이벤트(kafka)  기반 e-commerce를 진행하고 있다. 다음으로 프로젝트가 나뉘어져 있는데
rootProject.name = 'ecommerce_be'
include 'shop-common'
include 'shop-order'
include 'shop-product'
include 'shop-payment'
order, product, payment의 yml 설정에 consumer만 있는데. order 프로젝트 OrderEventProducer 클래스에서 kafkaTemplate.send(TOPIC, event); 보낸다.
이 때 직렬화/역직렬화 불일치 발생 yml 설정을 어디에 어떻게 변경해야 하는가
```

좋은 질문입니다. **멀티모듈(ecommerce\_be)에서 Producer/Consumer 직렬화 설정이 어긋나 생기는 문제**는, 보통 다음 두 경우 중 하나 때문에 발생합니다.

1.  `shop-order`(Producer) 쪽에 **producer 설정이 없거나 기본값(StringSerializer)** 으로 되어 있음
2.  `shop-product`, `shop-payment` 등 Consumer 쪽은 **JsonDeserializer**인데, Producer가 **StringSerializer**로 보내는 등 **불일치**

아래에 **최소 수정 패치**와 **구조별 권장 배치**를 함께 드릴게요.

***

## 결론부터: 어디에 어떻게 바꿔야 하나?

**원칙**:

*   **보내는 곳(Producer: `shop-order`)에 `spring.kafka.producer`를 명확히 설정**
*   **받는 곳(Consumer: `shop-order` 자체의 내부 소비가 없다면 X), `shop-product`, `shop-payment`에 `spring.kafka.consumer`를 맞춰 설정**
*   **포맷은 일치**(예: 모두 JSON)시키고, Listener 시그니처(`OrderCreatedEvent`)와 ContainerFactory 제네릭 타입도 일치

### 옵션 A. 각 모듈별로 yml을 분리 유지 (가장 직관적)

*   `shop-order` (Producer) — `application.yml`에 **producer** 설정 추가
*   `shop-product`, `shop-payment` (Consumer) — `application.yml`에 **consumer** 설정 유지/정비

#### `shop-order/src/main/resources/application.yml` (Producer)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        # 타입 정보 헤더를 추가해서 Consumer가 자동 타입 매핑 가능
        spring.json.add.type.headers: true
        # 순수 JSON만 보내고 싶다면 false로, 이 경우 Consumer에 default.type 지정 필요
        # spring.json.add.type.headers: false
```

> `kafkaTemplate.send(TOPIC, event)`를 이 설정으로 보내면 **value가 JSON으로 직렬화**됩니다.

#### `shop-product/src/main/resources/application.yml` (Consumer)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: product-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.ecommerce.common.event
        spring.json.use.type.headers: true
        # 만약 Producer가 타입 헤더를 붙이지 않는다면 아래 기본 타입 지정
        # spring.json.value.default.type: com.ecommerce.common.event.OrderCreatedEvent
```

#### `shop-payment/src/main/resources/application.yml` (Consumer)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: payment-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.ecommerce.common.event
        spring.json.use.type.headers: true
        # 필요시 default.type 지정
        # spring.json.value.default.type: com.ecommerce.common.event.OrderCreatedEvent
```

*   Listener는 다음처럼 **도메인 타입**으로 받는 것이 깔끔합니다.

```java
@KafkaListener(topics = "order.created", containerFactory = "orderEventContainerFactory")
public void onMessage(OrderCreatedEvent event) { ... }
```

*   그리고 Consumer 측 `ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>`가 **해당 타입**으로 생성되도록 설정합니다.

***

### 옵션 B. 공통 설정을 `shop-common`으로 끌어올리기 (재사용/표준화)

*   `shop-common`을 **Auto-Configuration 모듈**(예: `@Configuration` + `spring.factories`/`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`)로 만들어,
    *   **Producer 표준 설정 빈** (KafkaTemplate, ProducerFactory)
    *   **Consumer 표준 설정 빈** (JsonDeserializer, ListenerContainerFactory)
        를 제공
*   각 서비스(`shop-order`, `shop-product`, `shop-payment`)는 **shop-common 의존성만 추가**하고 **yml에는 최소한의 오버라이드만** 적는 방식

> 장점: 표준화/일관성, 설정 중복 제거  
> 단점: Auto-config 작성/유지보수 필요

***

## 자주 발생하는 실수와 빠른 점검 포인트

*   [ ] `shop-order`에 **producer 설정이 없다** → 기본값은 `StringSerializer`; Consumer가 `JsonDeserializer`면 **타입 불일치**
*   [ ] Producer `spring.json.add.type.headers: true`인데 Consumer `spring.json.use.type.headers: false` → 타입 매핑 실패
*   [ ] Consumer에 `spring.json.trusted.packages` 누락 → 역직렬화 거부
*   [ ] Listener 파라미터 타입이 `String`인데 ContainerFactory는 `<String, OrderCreatedEvent>` → **시그니처-팩토리 불일치**
*   [ ] 하나의 토픽에 여러 이벤트 타입을 섞어 보낸다면 → **타입 헤더 필수** 혹은 **메시지 헤더 기반 라우팅/별도 default.type** 필요
*   [ ] `KafkaTemplate<String, OrderCreatedEvent>` 제네릭과 ProducerFactory 제네릭/설정 불일치

***

## 최소 변경 패치 (당장 에러 해소용)

1.  **`shop-order`에 Producer 설정 추가** (JSON 직렬화)

```yaml
# shop-order/application.yml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: true
```

2.  **Consumer 쪽에서 타입 헤더 사용** (이미 있다면 확인만)

```yaml
# shop-product/application.yml, shop-payment/application.yml
spring:
  kafka:
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.ecommerce.common.event
        spring.json.use.type.headers: true
```

> 만약 Producer에서 타입 헤더를 붙이기 어렵다면:  
> Consumer에 `spring.json.value.default.type: com.ecommerce.common.event.OrderCreatedEvent`를 지정해도 됩니다(단일 타입 토픽일 때 권장).

***

## 참고: Listener/ContainerFactory 예시 (Consumer 쪽)

```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderEventConsumerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ecommerce.common.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        // props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.ecommerce.common.event.OrderCreatedEvent");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderCreatedEvent.class, false)
        );
    }

    @Bean(name = "orderEventContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderEventContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> cf) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}
```

***

## 요약

*   **보내는 모듈(`shop-order`)에 Producer(JSON) 설정을 반드시 넣어주세요.**  
    → `value-serializer: JsonSerializer`, `spring.json.add.type.headers: true`
*   **받는 모듈(`shop-product`, `shop-payment`)은 Consumer(JSON) 설정을 맞추세요.**  
    → `value-deserializer: JsonDeserializer`, `trusted.packages`, `use.type.headers: true`
*   이 조합이면 `OrderEventProducer`의 `kafkaTemplate.send(TOPIC, event)`에서 보낸 이벤트가 **타입 안전하게 역직렬화**됩니다.

***

필요하시면 **현재 각 모듈의 `application.yml`과 `@KafkaListener`/`KafkaTemplate` 코드**를 주시면, 딱 거기에 맞춰 **diff 형태 패치**로 정리해 드리겠습니다.

공유해주신 `docker-compose.yml` 구성이라면 **한 개 브로커 + Zookeeper** 환경이고, 내부 통신도 PLAINTEXT라 비교적 단순합니다.  
지금 경고는 **컨트롤러 스레드가 브로커(`shop_kafka:9092`)로 인터브로커 연결을 시도했으나 실패**했다는 뜻이라서, 보통 아래 둘 중 하나가 원인입니다.

1.  **advertised.listeners / listeners / inter.broker.listener.name 불완전 설정**
2.  **DNS/네트워크에서 `shop_kafka` 해석 또는 포트 접근 문제**

현재 compose에는 `KAFKA_ADVERTISED_LISTENERS`만 지정되어 있고, 나머지가 **암묵적 기본값**에 의존 중입니다. 컨테이너/호스트 상황에 따라 기본값이 예상과 다르면 지금 같은 연결 실패가 날 수 있습니다.  
**명시적으로 리스너들을 고정**하는 것을 강력 추천드립니다.

***

## ✅ 권장 수정안 (내부용 9092 / 호스트용 29092 이중 리스너)

> 내부 컨테이너들(예: `kafka-ui`, 애플리케이션)은 `shop_kafka:9092`로 붙고,  
> 로컬 호스트(개발자 PC)에서 테스트할 땐 `localhost:29092`로 붙는 패턴입니다.  
> 인터브로커는 내부 리스너(INTERNAL, 9092)를 사용하게 고정합니다.

```yaml
version: "3.9"
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: shop_zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: shop_kafka
    ports:
      - "9092:9092"   # 내부용(컨테이너 네트워크) 리스너도 노출 가능
      - "29092:29092" # 호스트에서 접근할 외부용 리스너
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: shop_zookeeper:2181

      # 1) 리스너 정의(바인딩)
      #    - INTERNAL: 컨테이너 내부 네트워크용
      #    - EXTERNAL: 호스트에서 접근용
      KAFKA_LISTENERS: INTERNAL://0.0.0.0:9092,EXTERNAL://0.0.0.0:29092

      # 2) 광고 주소(클라이언트/브로커에게 알려줄 주소)
      #    - INTERNAL은 컨테이너 간 DNS 이름 사용 (shop_kafka)
      #    - EXTERNAL은 호스트에서 접속 가능한 주소/포트 사용
      #      로컬 테스트면 localhost, 다른 PC에서 접속이면 해당 호스트 IP로 바꿔주세요.
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://shop_kafka:9092,EXTERNAL://localhost:29092

      # 3) 보안 프로토콜 매핑 (둘 다 PLAINTEXT)
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT

      # 4) 인터브로커 리스너는 INTERNAL로 고정
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL

      # 옵션: 단일 브로커 환경에서 시스템 토픽 복제 팩터 경고 방지
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1

      # 편의 옵션
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
      ALLOW_PLAINTEXT_LISTENER: 'yes'
    depends_on:
      - zookeeper
    volumes:
      - kafka_data:/var/lib/kafka/data

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: shop_kafka_ui
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      # 내부 네트워크에서 브로커를 볼 수 있도록 INTERNAL 주소로
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: shop_kafka:9092
    depends_on:
      - kafka

volumes:
  kafka_data:
```

> **중요 포인트**
>
> *   `KAFKA_LISTENERS` / `KAFKA_ADVERTISED_LISTENERS` / `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP` / `KAFKA_INTER_BROKER_LISTENER_NAME` 네 가지를 **명시적으로** 세팅.
> *   인터브로커는 **INTERNAL(9092)** 를 사용 → 컨트롤러 스레드가 `shop_kafka:9092`로 정상 접속.
> *   외부 접속이 필요하면 `EXTERNAL://localhost:29092` (또는 실제 호스트 IP/도메인)로 광고.
> *   `kafka-ui` 같은 컨테이너는 INTERNAL 주소(`shop_kafka:9092`)로 붙이기.

***

## 🔍 즉시 점검 순서 (수정 후 재기동)

```bash
docker compose down -v
docker compose up -d
```

1.  **브로커 포트 리스닝 확인**

```bash
docker exec -it shop_kafka bash -lc "lsof -i -P -n | grep -E '9092|29092'"
```

2.  **컨테이너 내부에서 DNS/포트 확인**

```bash
docker exec -it shop_kafka bash -lc "getent hosts shop_kafka && nc -vz shop_kafka 9092"
```

3.  **메타데이터 조회 (컨테이너 내부)**

```bash
# kcat이 없으면 kafka-console-producer/consumer로 메타데이터만 확인해도 됩니다.
docker exec -it shop_kafka bash -lc "kafka-broker-api-versions --bootstrap-server shop_kafka:9092 || true"
```

4.  **호스트에서 외부 리스너 확인**

```bash
# 호스트 터미널
nc -vz localhost 29092
```

5.  **로그 동시 확인 (타임스탬프 맞춰보기)**

```bash
docker logs -f shop_kafka | egrep -i "RequestSendThread|controller|connection|error|warn"
```

*   `UnknownHostException`, `Connection refused`, `Timed out`, `Handshake failed` 같은 키워드가 있으면 해당 방향으로 추가 진단.

***

## 자주 걸리는 함정 3가지

1.  **EXTERNAL만 설정하고 INTERNAL을 생략**  
    → 브로커가 **인터브로커 채널도 EXTERNAL**로 붙으려다, 호스트 경로를 통해 NAT/포트포워딩을 거치며 실패하는 경우가 있습니다. 반드시 `INTER_BROKER_LISTENER_NAME=INTERNAL`로 고정하세요.

2.  **`KAFKA_ADVERTISED_LISTENERS`에 잘못된 호스트/포트**
    *   INTERNAL에 `localhost`를 넣으면 **다른 컨테이너에서 접근 불가**합니다. INTERNAL은 **컨테이너 DNS 이름**(`shop_kafka`)을 쓰세요.
    *   EXTERNAL은 **호스트에서 실제로 접근 가능한 주소**(localhost/호스트 IP/도메인)로 맞추세요.

3.  **포트 충돌/방화벽**
    *   호스트에서 이미 9092/29092를 쓰고 있으면 리스닝 실패 → 다른 포트를 쓰거나 충돌 프로세스를 종료하세요.

***

## 계속 실패한다면 알려주세요 (필요 정보)

*   `docker exec -it shop_kafka bash -lc "cat /etc/hosts; env | egrep 'KAFKA_|ALLOW_|LISTENER'"`
*   동일 시각대의 `docker logs shop_kafka` 워닝/에러 라인 몇 줄
*   호스트 OS/방화벽 상태(회사 망/보안툴로 포트 차단 여부)

공유해주신 Spring Boot **producer(yml)** 설정을 기준으로, 지금의 Kafka 브로커 구성(내부 `shop_kafka:9092` / 외부 `localhost:29092` 권장안)과 **정확히 맞물리게** 만드는 방법, 추가로 **안전한 프로듀서 튜닝/직렬화 설정/트러블슈팅 포인트**까지 한 번에 정리했습니다.

***

## 1) 먼저, 프로듀서가 어디에서 동작하나요?

*   **컨테이너 내부(같은 docker-compose 네트워크)** 에서 동작 ⟶ `bootstrap-servers: shop_kafka:9092` 가 **정답**
*   **호스트(로컬 PC)** 에서 동작 ⟶ `bootstrap-servers: localhost:29092` 로 바꿔야 **정상 접속**

> 앞서 드린 compose 권장안(INTERNAL/EXTERNAL 이중 리스너)을 적용했다면,
>
> *   컨테이너는 **INTERNAL(shop\_kafka:9092)** 로,
> *   호스트는 **EXTERNAL(localhost:29092)** 로 붙어야 합니다.

***

## 2) Spring Boot(Kafka) 프로듀서 권장 설정 예시

> **컨테이너 내부에서 실행**한다는 가정(= `shop_kafka:9092` 유지).  
> 안전성과 처리량을 균형 있게 맞춘 프로덕션 기본값을 제안합니다.

```yaml
server:
  port: 8081

spring:
  application:
    name: order-service

  datasource:
    url: jdbc:mysql://localhost:3306/shop_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: shop_user
    password: shop_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  kafka:
    bootstrap-servers: shop_kafka:9092  # ← 컨테이너 내부에서 실행 시
    # (호스트에서 실행 시) bootstrap-servers: localhost:29092

    producer:
      # --- 직렬화 ---
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

      # --- 내구성/정확성 ---
      # 정확히 한 번 전송(멱등) + 최소 중복 방지
      properties:
        enable.idempotence: true           # 멱등성 활성화 (acks=all 자동 요구)
        acks: all                          # 모든 ISR에 기록될 때 성공 처리
        retries: 2147483647                # 무한대 수준 재시도
        max.in.flight.requests.per.connection: 5  # 멱등성과 함께 안전한 최대 동시 요청

        # --- 지연·배치·압축: 처리량 최적화 ---
        linger.ms: 5                       # 약간 지연 후 배치
        batch.size: 65536                  # 64KB 배치
        compression.type: lz4              # (선호: lz4 or zstd) 브로커/컨슈머와 호환

        # --- 타임아웃 ---
        delivery.timeout.ms: 120000        # 전체 전송 타임아웃(ack 포함)
        request.timeout.ms: 30000

        # --- JSON Serializer 관련 ---
        # Consumer가 타입 매핑을 자동으로 하려면 true(기본), 서로 다른 서비스 간엔 false 권장
        spring.json.add.type.headers: false
        # (false일 경우) Consumer 쪽에서 default.value.type 또는 trusted.packages/TypeMapper 설정 필요

      # 레코드 전송 실패 시 콜백/리트라이용
      properties.spring.kafka.producer.transaction-id-prefix: order-tx-  # 트랜잭션 쓸 때만
      # (트랜잭션 미사용이면 위 라인 제거)

    # (선택) KafkaTemplate의 sync send timeout
    properties:
      spring.kafka.template.observation-enabled: true
```

> **왜 `spring.json.add.type.headers: false`?**  
> 팀 간(서로 다른 서비스) 이벤트 교환 시 클래스명이 헤더로 섞이면 **의존성 결합**이 생겨요.  
> 컨슈머에서 `JsonDeserializer`에 **명시 타입 매핑(default.type / type.mapper / trusted.packages)** 을 두고,  
> 이벤트 payload는 **스키마(필드) 기준 계약**으로 관리하는 게 안전합니다.  
> (반대로, **동일 코드베이스 내부**에서만 쓰면 `true` 도 편리합니다.)

***

## 3) (중요) 컨슈머/프로듀서 타입 불일치 이슈 방지

부장님이 어제 공유하신 예외:

    ClassCastException: com.ecommerce.common.event.OrderCreatedEvent cannot be cast to String

*   **원인**: 컨슈머가 `StringDeserializer`를 쓰거나, 메시지를 String으로 가정했는데 **프로듀서는 JSON(Object)** 을 보낸 상황.
*   **대응**:
    *   프로듀서: `JsonSerializer` 유지
    *   컨슈머: `JsonDeserializer` + 타입 지정
        ```yaml
        spring:
          kafka:
            consumer:
              key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
              value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
              properties:
                spring.json.trusted.packages: "com.ecommerce.common.event"  # 또는 "*"
                spring.json.value.default.type: "com.ecommerce.common.event.OrderCreatedEvent"
                # 만약 add.type.headers=true 라면 default.type 대신 헤더 기반 매핑 가능
        ```
    *   또는 **둘 다 String** 으로 맞춘 뒤, 애플리케이션에서 수동 JSON 파싱(Gson/Jackson)도 가능합니다.  
        (이 경우 프로듀서 value-serializer를 `StringSerializer` 로 변경)

***

## 4) 토픽/파티션/복제 관련 체크

*   `KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'` 이면 **첫 전송 시 토픽 자동 생성**됩니다.  
    다만 **파티션 수/복제 팩터**가 기본값으로 생성되기 때문에, 운영에서는 **명시 생성** 권장:
    ```bash
    kafka-topics --bootstrap-server shop_kafka:9092 \
      --create --topic order.created --partitions 6 --replication-factor 1
    ```
*   단일 브로커라면 `replication-factor=1` 고정. 복제를 늘리려면 브로커를 추가해야 합니다.

***

## 5) 운영/테스트 점검 루틴

### A) 연결/메타데이터 확인

```bash
# 컨테이너 내부
docker exec -it shop_kafka bash -lc "kafka-broker-api-versions --bootstrap-server shop_kafka:9092"
# 호스트에서 EXTERNAL 확인
nc -vz localhost 29092
```

### B) 간단 발행/구독 테스트

```bash
# 컨테이너 내부에서 콘솔 프로듀서
docker exec -it shop_kafka bash -lc "kafka-console-producer --bootstrap-server shop_kafka:9092 --topic quick-test"
> hello
> ^C

# 컨테이너 내부에서 콘솔 컨슈머
docker exec -it shop_kafka bash -lc "kafka-console-consumer --bootstrap-server shop_kafka:9092 --topic quick-test --from-beginning --timeout-ms 5000"
```

### C) 애플리케이션 측 로그/지표

*   `TimeoutException: Expiring 1 record(s) due to ...` → 브로커 접근/네트워크/acks 지연
*   `UnknownHostException: shop_kafka` → DNS(컨테이너 네트워크 밖에서 접근)
*   `NotEnoughReplicas` → 단일 브로커인데 acks=all + replication-factor>1 요구 시
*   `RecordTooLargeException` → `max.request.size`, 브로커 `message.max.bytes` 상향 필요

***

## 6) docker-compose와의 정합성(요약)

*   브로커:
    *   `LISTENERS`: `INTERNAL://0.0.0.0:9092,EXTERNAL://0.0.0.0:29092`
    *   `ADVERTISED_LISTENERS`: `INTERNAL://shop_kafka:9092,EXTERNAL://localhost:29092`
    *   `INTER_BROKER_LISTENER_NAME`: `INTERNAL`
*   프로듀서(Spring Boot):
    *   **컨테이너 내부 실행**: `bootstrap-servers: shop_kafka:9092`
    *   **호스트에서 실행**: `bootstrap-servers: localhost:29092`

> ※ `kafka-ui` 같은 도구는 **INTERNAL 주소(shop\_kafka:9092)** 로 설정해야 컨테이너 간 네트워크에서 바로 접근 가능합니다.

***

## 7) (옵션) 운영 안정성 보강 팁

*   **헤더/타입 계약**: `add.type.headers=false`로 느슨 결합 + 이벤트 스키마 문서화
*   **Key 전략**: 파티셔닝을 위해 `orderId` 등 비즈니스 키를 **message key** 로 사용
*   **관찰성**: `client.id`, `metrics.recording.level=INFO`, **Datadog/Prometheus**로 `record-error-rate`, `request-latency-avg` 모니터링
*   **에러 핸들링**: `KafkaTemplate` send 콜백에서 실패 로깅 + 재시도/보상 트랜잭션 설계

***

## 다음 확인 요청 (두 가지만 알려주시면 설정을 더 정밀 튜닝해드릴게요)

1.  **order-service가 컨테이너 내부에서 돌고 있는지**, 아니면 **호스트(로컬)에서 직접 실행하는지**
2.  사용 중인 **토픽 이름/메시지 키 전략**(예: `order.created`, key=`orderId`)

이 두 가지에 맞춰 `bootstrap-servers` 최종값과 프로듀서 옵션(배치/압축/타임아웃)을 딱 맞춰 드리겠습니다.

다음 중 하나 때문에 나타나는 현상입니다:

1.  **이미 Git에 추적(Tracked)되었기 때문**  
    `.gitignore`는 *새로 생성되는 “미추적(untracked)” 파일*에만 적용됩니다. 예전에 한 번이라도 `shop-payment/build/tmp/compileJava/previous-compilation-data.bin`이 커밋/스테이징 되었다면, `.gitignore`를 추가해도 계속 커밋 후보에 뜹니다.

2.  **패턴/위치가 맞지 않음**

*   `.gitignore`가 **루트**에 있는데 패턴이 `shop-payment/build`로 정확히 안 맞거나, 앞에 `/` 여부로 의도와 다르게 동작할 수 있습니다.
*   **서브모듈(모듈별)** 디렉터리 `shop-payment/` 안에 별도의 `.gitignore`가 없어서, 루트 규칙이 제대로 적용되지 않을 수도 있습니다(대부분은 적용되지만, 상대 경로 실수로 누락되는 경우가 잦습니다).
*   상위 `.gitignore`나 다른 규칙에서 `!`(negation)으로 **예외**가 걸렸을 가능성도 있습니다.

***

## 빠른 해결책

### A. 이미 추적 중이라면: 인덱스에서 제거

```bash
# 특정 파일만 인덱스에서 제거
git rm --cached shop-payment/build/tmp/compileJava/previous-compilation-data.bin

# 전체 build 디렉터리를 인덱스에서 제거(이미 추적 중인 파일만)
git rm -r --cached shop-payment/build

git commit -m "Stop tracking shop-payment/build artifacts"
```

> 이후부터는 `.gitignore` 규칙이 제대로 되어 있으면 다시 등장하지 않습니다.

### B. .gitignore 패턴 확실히 하기

\*\*루트 `.gitignore`\*\*에서 프로젝트 전반에 적용하려면:

```gitignore
# 모든 모듈의 build 디렉터리 무시
**/build/
```

특정 모듈만(= shop-payment만) 무시하려면:

```gitignore
/shop-payment/build/
```

\*\*모듈 폴더 `shop-payment/` 내부의 `.gitignore`\*\*에 넣을 수도 있습니다(모듈 단위로 깔끔):

```gitignore
# shop-payment/.gitignore
/build/
```

> 팁: Gradle 프로젝트는 보통 `**/build/` 무시가 가장 편합니다. `tmp`, `generated`, `reports` 등도 모두 `build` 아래에 생기므로 한 번에 처리됩니다.

***

## 진단 체크리스트

1.  **이 파일이 추적 중인지 확인**
    ```bash
    git ls-files -- shop-payment/build/tmp/compileJava/previous-compilation-data.bin
    ```
    출력되면 이미 추적 중 → A 단계(인덱스 제거) 수행.

2.  **어떤 ignore 규칙이 적용되는지 확인**
    ```bash
    git check-ignore -v shop-payment/build/tmp/compileJava/previous-compilation-data.bin
    ```
    *   출력이 없으면 해당 파일은 ignore 규칙에 **걸리지 않음** → B 단계(패턴 수정).
    *   출력되면 어느 파일의 어떤 라인 규칙이 적용되는지 확인 가능.
    *   만약 `!something` 같은 **예외 규칙**이 보이면 그 라인이 원인.

3.  **로컬 전용 exclude도 확인(가끔 숨은 원인)**
    *   `.git/info/exclude` (로컬 전용)
    *   **글로벌 gitignore** (`git config --get core.excludesfile` 로 경로 확인)

4.  **IDE 캐시/표시 이슈 확인**
    *   IntelliJ 등에서 VCS 보기와 실제 Git 상태가 어긋나는 경우가 있습니다. `git status` 로 실제 상태를 확인하세요.

***

## 권장 `.gitignore` (Gradle/Java 공통)

루트 `.gitignore`에 아래를 포함하면 대부분의 바이너리를 방지할 수 있습니다:

```gitignore
# Gradle
**/build/
.gradle/
.gradle-cache/
!gradle/wrapper/gradle-wrapper.jar

# IntelliJ / Eclipse
.idea/
*.iml
*.ipr
*.iws
.project
.classpath
.settings/

# OS
.DS_Store
Thumbs.db

# Logs
*.log

# Java
*.class
```

> `!gradle/wrapper/gradle-wrapper.jar`는 래퍼 JAR을 **커밋에 포함**하려면 제외 해제(negation) 규칙을 둡니다. 조직 정책에 맞게 조정하세요.

***

## 최종 권장 절차 (한 번에 끝내기)

1.  루트 `.gitignore`에 `**/build/` 추가
2.  인덱스에서 기존 추적 제거:

```bash
git rm -r --cached shop-payment/build
```

3.  커밋:

```bash
git commit -m "Ignore Gradle build outputs"
```

4.  확인:

```bash
git status
git check-ignore -v shop-payment/build/tmp/compileJava/previous-compilation-data.bin
```

원하시면 현재 `.gitignore` 내용을 붙여주시면, **정확한 라인 레벨로** 무엇 때문에 예외 발생했는지 바로 짚어서 수정해 드릴게요.
