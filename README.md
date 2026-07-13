# login — KDT 심화 실습 (로그인/세션 + 게시판1)

Spring Boot + JPA + Thymeleaf + MySQL 기반의 학습용 웹 애플리케이션입니다.
회원 CRUD와 세션 로그인 위에 질문/답변 게시판(게시판1)을 얹은 구조입니다.

- **as-is 기준선 커밋**: `aef9d98` (게시판1 완성분)
- **문서 세트**: [docs/design/ERD.md](docs/design/ERD.md) · [docs/design/component-SA.md](docs/design/component-SA.md) · [docs/design/sequence-diagrams.md](docs/design/sequence-diagrams.md) · [docs/design/IS.md](docs/design/IS.md)
- 본 문서의 모든 표·다이어그램은 `aef9d98` 시점 실제 소스에서 추출한 것입니다. 코드에 없는 내용은 `[TBD: 미구현]`으로 표기합니다.

---

## 1. 기술 스택

`build.gradle` 기준 실제 버전입니다.

| 구분 | 내용 |
|---|---|
| 빌드 | Gradle (`java` 플러그인) |
| 프레임워크 | Spring Boot **4.1.0** (`org.springframework.boot`) |
| 의존성 관리 | `io.spring.dependency-management` **1.1.7** |
| Java | toolchain **17** |
| 웹 | `spring-boot-starter-webmvc` (Boot 4.x 정식 아티팩트명) |
| 영속성 | `spring-boot-starter-data-jpa` (Hibernate) |
| 뷰 | `spring-boot-starter-thymeleaf` |
| 검증 | `spring-boot-starter-validation` (Jakarta Bean Validation) |
| DB 드라이버 | `com.mysql:mysql-connector-j` (runtimeOnly) |
| 보조 | Lombok, spring-boot-devtools |
| 테스트 | `*-test` 스타터 4종, JUnit Platform |

> Spring Security는 **의존성에 없습니다**. 인증은 `HttpSession` + `@SessionAttribute`로 직접 구현되어 있습니다.

---

## 2. 실행 방법

### 2.1 사전 준비 — MySQL

`src/main/resources/application.yaml`의 접속 정보입니다. **비밀번호는 환경변수 `DB_PASSWORD`로 주입**하며, 파일에 평문으로 두지 않습니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

로컬 MySQL에 `test` 스키마가 존재해야 합니다.

```sql
CREATE DATABASE test DEFAULT CHARACTER SET utf8mb4;
```

### 2.2 환경변수 설정 — `DB_PASSWORD`

MySQL 접속 비밀번호를 환경변수로 지정해야 합니다. 설정하지 않으면 빈 문자열로 접속을 시도해 기동에 실패합니다.

**IntelliJ IDEA — 실행 구성(Run Configuration)**

1. 상단 실행 구성 드롭다운 → **Edit Configurations…**
2. `LoginApplication` 선택
3. **Environment variables** 항목에 입력:
   ```
   DB_PASSWORD=your_password
   ```
4. **Apply** → **OK** 후 실행

**터미널에서 실행하는 경우**

```bash
# PowerShell
$env:DB_PASSWORD="your_password"; ./gradlew bootRun

# bash
DB_PASSWORD=your_password ./gradlew bootRun
```

> 설정 예시는 `src/main/resources/application.yaml.example` 참고.

### 2.3 실행

```bash
./gradlew bootRun          # 또는 IDE에서 LoginApplication 실행
```

기동 후 <http://localhost:8080> 접속.

### 2.4 ⚠️ `ddl-auto: update` 주의

```yaml
spring.jpa.hibernate.ddl-auto: update
```

- `update`는 **테이블·컬럼 추가만** 반영하고, 컬럼 삭제·타입 변경·FK 재정의 등 **파괴적 변경은 반영하지 않습니다.**
- 따라서 엔티티의 **연관관계를 변경**(예: `@ManyToOne` FK 추가/제거, `@JoinColumn` 이름 변경)한 뒤에는 기존 스키마와 어긋나 기동 실패나 잘못된 컬럼이 남을 수 있습니다. 이 경우 스키마를 드롭하거나 일시적으로 `ddl-auto: create`로 재생성해야 합니다.
- `create`는 **기존 데이터를 전부 삭제**하므로 운영 환경에서는 절대 사용하지 마십시오. 본 프로젝트는 학습용이라 `update`를 그대로 둡니다.

또한 SQL 로그가 켜져 있습니다(`org.hibernate.SQL: debug`, `org.hibernate.orm.jdbc.bind: trace`) — 콘솔에 쿼리와 바인딩 파라미터가 모두 출력됩니다.

---

## 3. 디렉토리 구조

```
login/
├── build.gradle
├── README.md
├── JUDGMENT_LOG.md
├── docs/
│   └── design/
│       ├── ERD.md
│       ├── component-SA.md
│       ├── sequence-diagrams.md
│       └── IS.md
└── src/main/
    ├── java/com/example/login/
    │   ├── LoginApplication.java
    │   ├── constant/
    │   │   └── SessionConst.java          # 정본 (private 생성자)
    │   ├── controller/
    │   │   ├── HomeController.java
    │   │   ├── LoginController.java
    │   │   ├── MemberController.java
    │   │   ├── QuestionController.java
    │   │   ├── AnswerController.java
    │   │   ├── GlobalModelAdvice.java     # @ControllerAdvice (횡단 관심사)
    │   │   └── SessionConst.java          # ⚠️ 중복본
    │   ├── domain/
    │   │   ├── DoMember.java
    │   │   ├── Question.java
    │   │   └── Answer.java
    │   ├── dto/
    │   │   ├── LoginForm.java
    │   │   ├── MemberForm.java
    │   │   ├── QuestionForm.java
    │   │   └── AnswerForm.java
    │   ├── repository/
    │   │   ├── MemberRepository.java
    │   │   ├── QuestionRepository.java
    │   │   └── AnswerRepository.java
    │   └── service/
    │       ├── LoginService.java
    │       ├── MemberService.java
    │       ├── QuestionService.java
    │       └── AnswerService.java
    └── resources/
        ├── application.yaml
        ├── static/
        │   ├── css/style.css
        │   └── index.html
        └── templates/
            ├── layout.html · navbar.html · footer.html · home.html
            ├── admin/
            │   ├── memberList.html
            │   └── updateMemberForm.html
            └── user/
                ├── addMemberForm.html · loginForm.html
                ├── questionForm.html · questionList.html
                └── questionDetail.html
```

---

## 4. 기능 목록

| 도메인 | 기능 | 구현 클래스 |
|---|---|---|
| 회원 | 회원가입(중복 loginId 검증), 전체 조회, 단건 조회, 수정, 삭제 | `MemberController` / `MemberService` |
| 인증 | 로그인(세션 생성), 로그아웃(세션 무효화) | `LoginController` / `LoginService` |
| 질문 | 등록, 목록, 상세 | `QuestionController` / `QuestionService` |
| 답변 | 질문 상세 화면에서 답변 등록 | `AnswerController` / `AnswerService` |
| 홈 | 로그인 여부에 따른 홈 화면 | `HomeController` |

**[TBD: 미구현]** — 질문 수정/삭제, 답변 수정/삭제, 페이징, 검색, 비밀번호 암호화, 권한(`grade`) 기반 접근 제어.

---

## 5. URL 매핑 표

컨트롤러에 클래스 레벨 `@RequestMapping`이 **없어** 모든 경로가 루트 기준입니다. 총 **15개 핸들러 메서드**입니다.

| # | Method | URL | 컨트롤러 | 설명 | 세션 요구 |
|---|---|---|---|---|---|
| 1 | GET | `/` | HomeController | 홈 (로그인 시 회원명 표시) | 선택 (`required=false`) |
| 2 | GET | `/login` | LoginController | 로그인 폼 | 불필요 |
| 3 | POST | `/login` | LoginController | 로그인 처리 → `redirect:/` | 불필요 |
| 4 | POST | `/logout` | LoginController | 세션 무효화 → `redirect:/` | 불필요 |
| 5 | GET | `/add` | MemberController | 회원가입 폼 | 불필요 |
| 6 | POST | `/add` | MemberController | 회원가입 처리 → `redirect:/` | 불필요 |
| 7 | GET | `/members` | MemberController | 회원 목록 (관리자 화면) | **필수** |
| 8 | GET | `/members/{memberId}/edit` | MemberController | 회원 수정 폼 | **필수** |
| 9 | POST | `/members/{memberId}/edit` | MemberController | 회원 수정 저장 → `redirect:/members` | **필수** |
| 10 | GET | `/members/{memberId}/delete` | MemberController | 회원 삭제 → `redirect:/members` | ⚠️ **없음** |
| 11 | GET | `/question/create` | QuestionController | 질문 등록 폼 | 선택 (`required=false`) |
| 12 | POST | `/question/create` | QuestionController | 질문 저장 → `redirect:/question/list` | **필수** |
| 13 | GET | `/question/list` | QuestionController | 질문 목록 | 선택 (`required=false`) |
| 14 | GET | `/question/detail/{id}` | QuestionController | 질문 상세 + 답변 목록 | 선택 (`required=false`) |
| 15 | POST | `/answer/create/{questionId}` | AnswerController | 답변 등록 → `redirect:/question/detail/{id}` | 선택 (`required=false`) |

> "세션 필수"는 `@SessionAttribute(name = SessionConst.LOGIN_MEMBER)`(= `required` 기본값 `true`)를 뜻합니다. 미로그인 시 예외가 발생합니다. 엔드포인트별 상세 명세는 [docs/design/IS.md](docs/design/IS.md) 참고.

---

## 6. 알려진 개선점 (현황 기록)

문서화 시점(`aef9d98`)의 **사실 기록**이며, 이번 문서화 WO에서는 코드를 수정하지 않았습니다.

1. **`SessionConst` 중복 (2벌 존재)**
   - `com.example.login.constant.SessionConst` — 정본. `private` 생성자로 인스턴스화 차단.
   - `com.example.login.controller.SessionConst` — 중복본. `HomeController`, `LoginController`, `MemberController`, `QuestionController`, `AnswerController` 5종이 (동일 패키지라 import 없이) 이쪽을 사용.
   - `GlobalModelAdvice`만 `constant.SessionConst`를 명시적으로 import.
   - 상수값이 양쪽 모두 `"loginMember"`로 동일해 **런타임 동작은 같지만**, 동일 개념의 상수가 두 벌 존재하는 상태입니다. **`constant` 쪽을 정본으로 통합 권장** (별도 WO에서 진행).

2. **인증 인터셉터/필터 부재**
   - `HandlerInterceptor`나 `Filter`가 없어 인증이 **핸들러 파라미터의 `@SessionAttribute`에 개별 의존**합니다.
   - 결과적으로 보호 수준이 핸들러마다 제각각이며, 누락 시 무방비가 됩니다 — 실제로 `GET /members/{memberId}/delete`에는 세션 검사가 **아예 없습니다**.
   - 인터셉터로 인증을 한 곳에 모으는 것을 권장합니다.

3. **`required=true`인 `@SessionAttribute`의 실패 처리 부재**
   - 미로그인 상태로 `/members` 등에 접근하면 로그인 페이지로 리다이렉트되는 대신 예외가 발생합니다. `@ExceptionHandler`나 인터셉터 리다이렉트가 **[TBD: 미구현]**입니다.

4. **삭제가 GET 메서드**
   - `GET /members/{memberId}/delete`는 부수효과가 있는 요청을 GET으로 처리합니다. POST/DELETE 전환 권장.

5. **비밀번호 평문 저장·평문 비교**
   - `LoginService.login()`이 `getPassword().equals(password)`로 비교합니다. 해시(BCrypt 등) 미적용.

6. **`grade` 필드 미활용**
   - 회원가입 시 `"user"`로 고정 세팅되며, 권한 검사 로직은 **[TBD: 미구현]**입니다. `/members`(admin 화면)도 로그인만 하면 누구나 접근 가능합니다.
