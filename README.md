# springboot-qna-board — KDT 심화 실습 (로그인/세션 + Q&A 게시판)

Spring Boot + JPA + Thymeleaf + MySQL 기반의 학습용 웹 애플리케이션입니다.
회원 CRUD와 세션 로그인 위에 질문/답변 게시판을 얹은 구조이며, 게시판2 단계에서 **수정·삭제, 페이징, 검색, 에러 페이지**까지 확장했습니다.

- **문서 기준 커밋**: `c967580` (게시판2 완성분)
- **문서 세트**: [docs/design/ERD.md](docs/design/ERD.md) · [docs/design/component-SA.md](docs/design/component-SA.md) · [docs/design/sequence-diagrams.md](docs/design/sequence-diagrams.md) · [docs/design/IS.md](docs/design/IS.md)
  > 위 설계 문서 4종은 **게시판1 시점(`aef9d98`) 기준**입니다. 게시판2 변경분(수정/삭제·페이징·검색)은 아직 반영돼 있지 않습니다.
- 본 README의 모든 표는 `c967580` 시점 실제 소스에서 추출한 것입니다. 코드에 없는 내용은 `[TBD: 미구현]`으로 표기합니다.

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
| 프런트 | Bootstrap 5 (CDN 아님 — `src/main/resources/static/` 에 CSS/JS 동봉) |
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

### 2.4 첫 사용 시나리오

1. `/add`에서 회원가입 → `grade`는 코드에서 **`"user"`로 고정** 세팅됩니다.
2. `/login`으로 로그인 → 홈에 "질문 등록하기 / 질문 목록" 버튼이 나타납니다.
3. **회원 목록(`/members`) 버튼은 `grade == "admin"`인 계정에만 홈에 노출**됩니다. 관리자 계정을 만들려면 DB에서 직접 `UPDATE do_member SET grade='admin' WHERE login_id='...';` 로 승격해야 합니다(가입 화면에는 등급 선택이 없습니다).

### 2.5 ⚠️ `ddl-auto: update` 주의

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
springboot-qna-board/
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
    │   │   ├── GlobalModelAdvice.java     # @ControllerAdvice — 모든 모델에 loginMember 주입
    │   │   └── SessionConst.java          # ⚠️ 중복본
    │   ├── domain/
    │   │   ├── DoMember.java
    │   │   ├── Question.java              # createDate, modifyDate, author, answerList(cascade=REMOVE)
    │   │   └── Answer.java                # createDate, modifyDate, author, question
    │   ├── dto/
    │   │   ├── LoginForm.java
    │   │   ├── MemberForm.java
    │   │   ├── QuestionForm.java
    │   │   └── AnswerForm.java
    │   ├── repository/
    │   │   ├── MemberRepository.java
    │   │   ├── QuestionRepository.java    # JpaSpecificationExecutor (검색)
    │   │   └── AnswerRepository.java
    │   └── service/
    │       ├── LoginService.java
    │       ├── MemberService.java
    │       ├── QuestionService.java       # 페이징 + Specification 검색
    │       └── AnswerService.java
    └── resources/
        ├── application.yaml
        ├── application.yaml.example
        ├── static/
        │   ├── css/  (bootstrap.css, style.css)
        │   ├── js/   (bootstrap.bundle.js)
        │   └── index.html
        └── templates/
            ├── layout.html · navbar.html · footer.html · home.html
            ├── error/
            │   ├── 4xx.html
            │   └── 5xx.html
            ├── admin/
            │   ├── memberList.html
            │   └── updateMemberForm.html
            └── user/
                ├── addMemberForm.html · loginForm.html
                ├── questionForm.html · questionList.html
                ├── questionDetail.html
                └── answerForm.html
```

---

## 4. 기능 목록

| 도메인 | 기능 | 구현 클래스 |
|---|---|---|
| 회원 | 회원가입(중복 loginId 검증), 전체 조회, 단건 조회, 수정, 삭제 | `MemberController` / `MemberService` |
| 인증 | 로그인(세션 생성), 로그아웃(세션 무효화) | `LoginController` / `LoginService` |
| 질문 | 등록, 목록(페이징·검색), 상세, **수정, 삭제(답변 cascade)** | `QuestionController` / `QuestionService` |
| 답변 | 등록, **수정, 삭제** | `AnswerController` / `AnswerService` |
| 홈 | 로그인 여부에 따른 홈 화면, **admin 등급에만 회원목록 버튼 노출** | `HomeController` / `home.html` |
| 공통 | 모든 모델에 `loginMember` 자동 주입, **4xx/5xx 에러 페이지** | `GlobalModelAdvice` / `templates/error/` |

### 4.1 목록 — 페이징·정렬·번호·댓글수

`QuestionService.getList(page, kw)` 기준입니다.

- **페이지 크기 10**, `createDate` **DESC** 정렬 (`PageRequest.of(page, 10, Sort.by(DESC, "createDate"))`)
- 게시글 번호는 DB 값이 아니라 화면에서 계산: `전체건수 - (현재페이지 × 페이지크기) - 반복인덱스`
- 페이지 네비게이션은 현재 페이지 **±5**만 노출하며, 링크가 아니라 hidden form 전송이라 **검색어(`kw`)가 페이지 이동 후에도 유지**됩니다.
- 제목 옆 빨간 숫자는 답변 개수(`#lists.size(question.answerList)`), 0건이면 표시하지 않습니다.

### 4.2 검색 — Specification

`kw`가 비어 있으면 `findAll(pageable)`, 값이 있으면 `Specification`으로 **5개 필드를 OR 검색**합니다 — 질문 제목, 질문 내용, 질문 작성자 `loginId`, 답변 내용, 답변 작성자 `loginId`. 답변 조인 때문에 질문이 중복되므로 `query.distinct(true)`를 겁니다.

### 4.3 수정·삭제

- 질문 수정은 **등록 폼(`questionForm.html`)을 재사용**해 기존 값을 채워 보여줍니다. 답변 수정은 전용 `answerForm.html`을 씁니다.
- 수정 시 `modifyDate`가 기록됩니다.
- 삭제는 **Bootstrap 모달로 한 번 확인**한 뒤 진행합니다. 질문 삭제 시 `Question.answerList`의 `cascade = REMOVE`로 답변도 함께 삭제됩니다.
- **수정/삭제 버튼은 `loginMember.loginId == author.loginId`일 때만 화면에 렌더링**됩니다. 단, 서버 측 검증은 없습니다 — 6절 1번 항목 참조.

**[TBD: 미구현]** — 비밀번호 암호화, 인증 인터셉터, 서버 측 작성자 권한 검증, 추천/조회수, 마크다운.

---

## 5. URL 매핑 표

컨트롤러에 클래스 레벨 `@RequestMapping`이 **없어** 모든 경로가 루트 기준입니다. 총 **21개 핸들러 메서드**입니다.

| # | Method | URL | 컨트롤러 | 설명 | 세션 요구 |
|---|---|---|---|---|---|
| 1 | GET | `/` | HomeController | 홈 (로그인 시 회원명 표시) | 선택 |
| 2 | GET | `/login` | LoginController | 로그인 폼 | 불필요 |
| 3 | POST | `/login` | LoginController | 로그인 처리 → `redirect:/` | 불필요 |
| 4 | POST | `/logout` | LoginController | 세션 무효화 → `redirect:/` | 불필요 |
| 5 | GET | `/add` | MemberController | 회원가입 폼 | 불필요 |
| 6 | POST | `/add` | MemberController | 회원가입 처리 → `redirect:/` | 불필요 |
| 7 | GET | `/members` | MemberController | 회원 목록 (관리자 화면) | **필수** |
| 8 | GET | `/members/{memberId}/edit` | MemberController | 회원 수정 폼 | **필수** |
| 9 | POST | `/members/{memberId}/edit` | MemberController | 회원 수정 저장 → `redirect:/members` | **필수** |
| 10 | GET | `/members/{memberId}/delete` | MemberController | 회원 삭제 → `redirect:/members` | ⚠️ **없음** |
| 11 | GET | `/question/create` | QuestionController | 질문 등록 폼 | 선택 |
| 12 | POST | `/question/create` | QuestionController | 질문 저장 → `redirect:/question/list` | **필수** |
| 13 | GET | `/question/modify/{id}` | QuestionController | 질문 수정 폼 | 선택 |
| 14 | POST | `/question/modify/{id}` | QuestionController | 질문 수정 저장 → `redirect:/question/detail/{id}` | 선택 |
| 15 | GET | `/question/delete/{id}` | QuestionController | 질문 삭제 → `redirect:/question/list` | 선택 |
| 16 | GET | `/question/list` | QuestionController | 질문 목록 (`?page=0&kw=`) | 선택 |
| 17 | GET | `/question/detail/{id}` | QuestionController | 질문 상세 + 답변 목록 | 선택 |
| 18 | POST | `/answer/create/{questionId}` | AnswerController | 답변 등록 → `redirect:/question/detail/{id}` | 선택 |
| 19 | GET | `/answer/modify/{id}` | AnswerController | 답변 수정 폼 | 선택 |
| 20 | POST | `/answer/modify/{id}` | AnswerController | 답변 수정 저장 → `redirect:/question/detail/{qid}` | 선택 |
| 21 | GET | `/answer/delete/{id}` | AnswerController | 답변 삭제 → `redirect:/question/detail/{qid}` | 선택 |

> "세션 필수"는 `@SessionAttribute(name = SessionConst.LOGIN_MEMBER)`(= `required` 기본값 `true`)를 뜻하며, 미로그인 시 예외가 발생합니다. "선택"은 `required = false` — **미로그인이어도 핸들러가 실행됩니다.** 엔드포인트별 상세 명세는 [docs/design/IS.md](docs/design/IS.md) 참고(게시판1 기준).

---

## 6. 알려진 개선점 (현황 기록)

`c967580` 시점의 **사실 기록**입니다. 학습용 프로젝트이므로 아래 항목은 인지한 상태로 남겨 두었습니다.

1. **수정/삭제 권한 검사가 화면(뷰)에만 있고 서버에는 없음** — 가장 큰 결함
   - `questionDetail.html`은 `loginMember.loginId == author.loginId`일 때만 수정/삭제 버튼을 렌더링합니다.
   - 그러나 `QuestionController.questionModify/questionDelete`, `AnswerController.answerModify/answerDelete` 어디에도 **작성자 일치 검증이 없습니다**. 세션 파라미터도 `required = false`입니다.
   - 결과적으로 `GET /question/delete/3` 같은 URL을 **직접 입력하면 남의 글도, 심지어 비로그인 상태로도 삭제·수정할 수 있습니다.** 버튼을 숨기는 것은 보안이 아닙니다.
   - 서비스/컨트롤러 계층에서 작성자를 비교해 불일치 시 `403`을 던지도록 보완해야 합니다.

2. **삭제가 GET 메서드**
   - `/question/delete/{id}`, `/answer/delete/{id}`, `/members/{memberId}/delete` 모두 부수효과가 있는 요청을 GET으로 처리합니다. 모달로 확인을 받지만, 링크 프리페치·크롤러에는 무방비입니다. POST/DELETE 전환 권장.

3. **`grade` 기반 접근 제어가 화면 노출 수준에 그침**
   - `home.html`이 `grade == 'admin'`일 때만 "회원 목록" 버튼을 보여 주지만, `GET /members` 핸들러 자체는 **로그인만 하면 등급과 무관하게 통과**합니다. URL을 직접 치면 일반 사용자도 회원 목록·수정 화면에 접근합니다.
   - 회원가입은 `grade`를 `"user"`로 고정하므로, admin 계정은 DB에서 직접 승격해야 합니다.

4. **인증 인터셉터/필터 부재**
   - `HandlerInterceptor`나 `Filter`가 없어 인증이 **핸들러 파라미터의 `@SessionAttribute`에 개별 의존**합니다. 보호 수준이 핸들러마다 제각각이고, 누락 시 무방비가 됩니다 — `GET /members/{memberId}/delete`에는 세션 검사가 **아예 없습니다**.

5. **`required=true`인 `@SessionAttribute`의 실패 처리 부재**
   - 미로그인 상태로 `/members` 등에 접근하면 로그인 페이지로 리다이렉트되는 대신 예외가 발생합니다. 현재는 `error/4xx.html`이 받아 주지만, 인터셉터 리다이렉트가 정석입니다.

6. **비밀번호 평문 저장·평문 비교**
   - `LoginService.login()`이 `getPassword().equals(password)`로 비교합니다. 해시(BCrypt 등) 미적용.

7. **미로그인 답변 등록 시 `author = NULL`**
   - `AnswerController.createAnswer()`가 `required = false`이고 `AnswerService.create()`에 null 검사가 없어, 비로그인 POST 시 작성자가 NULL인 답변 행이 저장됩니다. 뷰는 이를 `'Unknown'`으로 표시해 가리고 있습니다.

8. **`SessionConst` 중복 (2벌 존재)**
   - `constant.SessionConst`(정본, `private` 생성자)와 `controller.SessionConst`(중복본)가 공존합니다. 컨트롤러 5종은 동일 패키지라 import 없이 후자를, `GlobalModelAdvice`만 전자를 씁니다. 값이 양쪽 다 `"loginMember"`라 **런타임 동작은 같지만** 통합이 필요합니다.

9. **검색 쿼리의 조인 비용**
   - `Specification` 검색이 `author`·`answerList`·답변 작성자까지 LEFT JOIN한 뒤 `distinct`로 중복을 제거합니다. 데이터가 늘면 페이징 카운트 쿼리와 함께 비용이 커집니다.
