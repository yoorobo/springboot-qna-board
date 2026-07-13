# SA — 컴포넌트 아키텍처 (레이어드)

- 기준 커밋: `aef9d98`
- 근거: 각 클래스의 실제 `import` 문과 `@RequiredArgsConstructor` + `private final` 필드 주입
- 총 클래스 23종 (컨트롤러 6 + 상수 2 + 서비스 4 + 리포지토리 3 + 엔티티 3 + DTO 4 + 부트스트랩 1)

---

## 1. 컴포넌트 다이어그램

```mermaid
flowchart TD
    subgraph Client["브라우저 (Client)"]
        Browser["HTTP Request / Response"]
    end

    subgraph Presentation["Presentation Layer — @Controller"]
        HomeController["HomeController<br/>GET /"]
        LoginController["LoginController<br/>GET·POST /login, POST /logout"]
        MemberController["MemberController<br/>/add, /members/**"]
        QuestionController["QuestionController<br/>/question/**"]
        AnswerController["AnswerController<br/>POST /answer/create/{id}"]
    end

    subgraph CrossCutting["횡단 관심사 (Cross-cutting)"]
        GlobalModelAdvice["GlobalModelAdvice<br/>@ControllerAdvice<br/>@ModelAttribute('loginMember')"]
        HttpSession[("HttpSession<br/>key: loginMember")]
        SessionConstC["controller.SessionConst<br/>⚠️ 중복본"]
        SessionConstK["constant.SessionConst<br/>정본"]
    end

    subgraph Business["Business Layer — @Service"]
        LoginService["LoginService<br/>login()"]
        MemberService["MemberService<br/>join·save·find·delete"]
        QuestionService["QuestionService<br/>create·getList·getQuestion"]
        AnswerService["AnswerService<br/>create()"]
    end

    subgraph Persistence["Persistence Layer — JpaRepository"]
        MemberRepository["MemberRepository<br/>+ findByLoginId()"]
        QuestionRepository["QuestionRepository"]
        AnswerRepository["AnswerRepository"]
    end

    subgraph Domain["Domain — @Entity"]
        DoMember["DoMember"]
        Question["Question"]
        Answer["Answer"]
    end

    subgraph DTO["DTO — Form 객체"]
        LoginForm["LoginForm"]
        MemberForm["MemberForm"]
        QuestionForm["QuestionForm"]
        AnswerForm["AnswerForm"]
    end

    subgraph View["View — Thymeleaf"]
        Templates["home · user/loginForm · user/addMemberForm<br/>user/questionForm · questionList · questionDetail<br/>admin/memberList · admin/updateMemberForm"]
    end

    DB[("MySQL — test 스키마")]

    Browser --> HomeController
    Browser --> LoginController
    Browser --> MemberController
    Browser --> QuestionController
    Browser --> AnswerController

    LoginController --> LoginService
    HomeController --> MemberService
    MemberController --> MemberService
    QuestionController --> QuestionService
    AnswerController --> QuestionService
    AnswerController --> AnswerService

    LoginService --> MemberRepository
    MemberService --> MemberRepository
    QuestionService --> QuestionRepository
    AnswerService --> AnswerRepository

    MemberRepository --> DoMember
    QuestionRepository --> Question
    AnswerRepository --> Answer
    MemberRepository --> DB
    QuestionRepository --> DB
    AnswerRepository --> DB

    LoginController -.->|@Valid| LoginForm
    MemberController -.->|@Valid| MemberForm
    QuestionController -.->|@Valid| QuestionForm
    AnswerController -.->|@Valid| AnswerForm

    HomeController -.->|Model| Templates
    LoginController -.->|Model| Templates
    MemberController -.->|Model| Templates
    QuestionController -.->|Model| Templates
    AnswerController -.->|Model| Templates

    GlobalModelAdvice -.->|모든 요청 모델에 주입| Templates
    GlobalModelAdvice -->|@SessionAttribute| HttpSession
    GlobalModelAdvice --> SessionConstK
    LoginController -->|setAttribute / invalidate| HttpSession
    HomeController -.->|@SessionAttribute| HttpSession
    MemberController -.->|@SessionAttribute| HttpSession
    QuestionController -.->|@SessionAttribute| HttpSession
    AnswerController -.->|@SessionAttribute| HttpSession
    HomeController --> SessionConstC
    LoginController --> SessionConstC
    MemberController --> SessionConstC
    QuestionController --> SessionConstC
    AnswerController --> SessionConstC
```

> 실선 = 생성자 주입(`private final`) 또는 직접 호출 의존, 점선 = 데이터 바인딩·모델 전달·세션 참조.

의존 방향은 **Presentation → Business → Persistence → Domain** 단방향이며, 역방향 의존(서비스가 컨트롤러를 참조하는 등)은 **없습니다**.

---

## 2. 레이어별 책임 및 소속 클래스

| 레이어 | 책임 | 소속 클래스 | 의존 대상 |
|---|---|---|---|
| **Presentation** | HTTP 요청 수신, 폼 바인딩(`@ModelAttribute`)과 검증(`@Valid` + `BindingResult`), 세션에서 로그인 회원 획득, DTO ↔ 엔티티 변환, 뷰 이름/리다이렉트 반환 | `HomeController`, `LoginController`, `MemberController`, `QuestionController`, `AnswerController` | Service, DTO, Domain, HttpSession |
| **횡단 관심사** | 모든 요청 모델에 `loginMember` 자동 주입 (내비바 로그인 표시용) | `GlobalModelAdvice` (`@ControllerAdvice`) | HttpSession, `constant.SessionConst` |
| **Business** | 도메인 규칙 수행 — 로그인 인증, 회원 중복 검증, 엔티티 생성·저장 | `LoginService`, `MemberService`, `QuestionService`, `AnswerService` | Repository, Domain |
| **Persistence** | DB 접근. Spring Data JPA가 구현체 자동 생성 | `MemberRepository`(+`findByLoginId`), `QuestionRepository`, `AnswerRepository` | Domain, MySQL |
| **Domain** | JPA 엔티티 · 연관관계 정의 | `DoMember`, `Question`, `Answer` | — (최하위) |
| **DTO** | 폼 전송 객체 + Bean Validation 규칙 | `LoginForm`, `MemberForm`, `QuestionForm`, `AnswerForm` | — |
| **View** | Thymeleaf 템플릿 렌더링 | `layout`/`navbar`/`footer`/`home`, `user/*` 5종, `admin/*` 2종 | Model |
| **Constant** | 세션 키 상수 | `constant.SessionConst`(정본), `controller.SessionConst`(⚠️중복본) | — |

---

## 3. 컨트롤러별 주입 의존성 (실제 `private final` 필드 기준)

| 컨트롤러 | 주입받는 서비스 |
|---|---|
| `HomeController` | `MemberService` — **주입만 되고 핸들러에서 사용되지 않음** (`home()`은 세션만 사용) |
| `LoginController` | `LoginService` |
| `MemberController` | `MemberService` |
| `QuestionController` | `QuestionService` |
| `AnswerController` | `QuestionService`, `AnswerService` (2종 — 답변 저장 전 질문 조회 필요) |
| `GlobalModelAdvice` | 없음 (필드 주입 없음, 세션 파라미터만 사용) |

---

## 4. 횡단 관심사 상세 — `GlobalModelAdvice`

```java
@ControllerAdvice
public class GlobalModelAdvice {
    @ModelAttribute("loginMember")
    public DoMember loginMember(
        @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) DoMember loginMember) {
        return loginMember;
    }
}
```

- **역할**: 모든 컨트롤러 요청의 Model에 `loginMember`를 자동으로 담습니다(미로그인 시 `null`). `navbar.html`이 로그인 상태를 표시하는 데 사용합니다.
- **주의**: 각 컨트롤러가 `model.addAttribute("loginMember", loginMember)`를 **중복으로** 호출하고 있습니다(`HomeController`, `MemberController`, `QuestionController`, `AnswerController`). Advice가 이미 주입하므로 개별 호출은 사실상 불필요합니다 — 값이 같아 동작에는 영향이 없습니다.
- **이것은 인증이 아닙니다**: 모델에 담아줄 뿐, 접근을 차단하지 않습니다.

## 5. 인증 구조의 한계 (현황)

- `HandlerInterceptor` / `Filter` / Spring Security **모두 없습니다**.
- 인증은 각 핸들러 파라미터의 `@SessionAttribute` 유무와 `required` 값에 전적으로 의존합니다.
  - `required = true` (필수): `MemberController` 3개 핸들러(`/members`, edit GET·POST), `QuestionController.questionCreate`(POST)
  - `required = false` (선택): `HomeController`, `QuestionController`의 폼/목록/상세, `AnswerController`
  - **검사 없음**: `GET /members/{memberId}/delete`, `GET·POST /login`, `POST /logout`, `GET·POST /add`
- 즉 회원 삭제가 **비로그인 상태에서도 가능**한 구조입니다. 인터셉터 도입 시 이 구멍이 함께 막힙니다.
