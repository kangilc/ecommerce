# ecommerce
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
