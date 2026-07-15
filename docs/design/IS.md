# IS — 인터페이스 명세 (Interface Specification)

> ⚠️ **현행 코드와 일부 불일치** — 해당 설계 문서는 Spring Security 및 OAuth2 도입 전 코드(기준 커밋 `aef9d98`) 기준이며, 현재 인증 구조 및 엔드포인트(소셜 로그인 `/oauth/**` 4종 추가)와 일부 불일치합니다. 현행 흐름은 [`../../README.md`](../../README.md)와 [`../oauth-login-flow.md`](../oauth-login-flow.md)를 참고하세요.

- 기준 커밋: `aef9d98`
- 근거: `controller` 패키지 5개 컨트롤러의 실제 핸들러 시그니처, `dto` 패키지 4종
- 클래스 레벨 `@RequestMapping`은 **없습니다** — 모든 URL은 루트 기준 절대 경로입니다.
- **핸들러 총 15개**입니다. (WO 문언의 "엔드포인트 12종"과 개수가 다릅니다 — JUDGMENT_LOG `[A]-1` 참조)

---

## 1. 엔드포인트 명세

### 1.1 HomeController

| 항목 | 내용 |
|---|---|
| **Method / URL** | `GET /` |
| 핸들러 | `home(...)` |
| PathVariable · Param | 없음 |
| 요청 DTO | 없음 |
| 세션 요구 | `@SessionAttribute(LOGIN_MEMBER, required = false)` — **선택** |
| 응답 | view `home` |
| 검증 규칙 | 없음 |
| 비고 | `loginMember == null`이면 그대로 `home` 반환, 있으면 model에 담아 `home` 반환 (분기해도 뷰는 동일) |

### 1.2 LoginController

| 항목 | `GET /login` | `POST /login` | `POST /logout` |
|---|---|---|---|
| 핸들러 | `loginForm()` | `login()` | `logout()` |
| PathVariable · Param | 없음 | 없음 | 없음 |
| 요청 DTO | 없음 (빈 `LoginForm`을 model에 세팅) | **`LoginForm`** (`@Valid @ModelAttribute("loginForm")`) | 없음 |
| 세션 요구 | 없음 | 없음 (성공 시 **생성**) | 없음 (있으면 무효화) |
| 응답 | view `user/loginForm` | 성공 → `redirect:/`<br/>검증 실패·로그인 실패 → view `user/loginForm` | `redirect:/` |
| 검증 규칙 | — | `@NotEmpty loginId` / `@NotEmpty password`<br/>+ 인증 실패 시 `bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다")` (글로벌 에러) | — |
| 세션 처리 | — | `request.getSession()` → `setAttribute(SessionConst.LOGIN_MEMBER, loginMember)` | `request.getSession(false)` → `session.invalidate()` |

### 1.3 MemberController

| 항목 | `GET /add` | `POST /add` | `GET /members` |
|---|---|---|---|
| 핸들러 | `creatForm()` | `create()` | `list()` |
| PathVariable · Param | 없음 | 없음 | 없음 |
| 요청 DTO | 없음 (빈 `MemberForm`) | **`MemberForm`** (`@Valid @ModelAttribute("memberForm")`) | 없음 |
| 세션 요구 | 없음 | 없음 | `@SessionAttribute(LOGIN_MEMBER)` — **필수** (`required` 기본 true) |
| 응답 | view `user/addMemberForm` | 성공 → `redirect:/`<br/>실패 → view `user/addMemberForm` | view `admin/memberList` |
| 검증 규칙 | — | `@NotEmpty loginId/name/password` + 서비스단 중복 검증(`IllegalStateException`) | — |
| 비고 | — | `grade`는 **`"user"`로 하드코딩** 세팅 | model: `members`(전체 회원), `loginMember` |

| 항목 | `GET /members/{memberId}/edit` | `POST /members/{memberId}/edit` | `GET /members/{memberId}/delete` |
|---|---|---|---|
| 핸들러 | `updateMemberForm()` | `updateMemberSave()` | `delete()` |
| PathVariable | `memberId` (Long) | `memberId` (Long) — **선언만 되고 미사용**, 실제로는 `memberForm.id` 사용 | `memberId` (Long) |
| 요청 DTO | 없음 (조회 결과로 `MemberForm` 채워 전달) | **`MemberForm`** (`@Valid @ModelAttribute("memberForm")`) | 없음 |
| 세션 요구 | `@SessionAttribute(LOGIN_MEMBER)` — **필수** | `@SessionAttribute(LOGIN_MEMBER)` — **필수** | ⚠️ **없음** |
| 응답 | view `admin/updateMemberForm` | 성공 → `redirect:/members`<br/>실패 → view `admin/updateMemberForm` | `redirect:/members` |
| 검증 규칙 | — | `MemberForm` 어노테이션 동일. **중복 검증 없음**(`save()`는 `validateDuplicateMember` 미호출) | 없음 |
| 비고 | 미존재 id → `orElseThrow()` 예외 | 새 `DoMember`를 만들어 `id` 포함 세팅 후 `save()` → JPA merge(전체 덮어쓰기) | 미존재 id → `orElseThrow()`.<br/>**부수효과 있는 GET + 인증 없음** |

### 1.4 QuestionController

| 항목 | `GET /question/create` | `POST /question/create` |
|---|---|---|
| 핸들러 | `questionCreateForm()` | `questionCreate()` |
| PathVariable · Param | 없음 | 없음 |
| 요청 DTO | 없음 (빈 `QuestionForm`) | **`QuestionForm`** (`@Valid @ModelAttribute("questionForm")`) |
| 세션 요구 | `@SessionAttribute(LOGIN_MEMBER, required = false)` — **선택** | `@SessionAttribute(LOGIN_MEMBER)` — **필수** |
| 응답 | view `user/questionForm` | 성공 → `redirect:/question/list`<br/>실패 → view `user/questionForm` |
| 검증 규칙 | — | `@NotEmpty subject` + `@Size(max = 200)` / `@NotEmpty content` |
| 비고 | 미로그인도 폼 열람 가능 | `createDate = LocalDateTime.now()`, `author = loginMember` 세팅.<br/>폼은 열리지만 제출은 미로그인 시 예외 → UX 불일치 |

| 항목 | `GET /question/list` | `GET /question/detail/{id}` |
|---|---|---|
| 핸들러 | `list()` | `detail()` |
| PathVariable · Param | 없음 | `id` (Long) |
| 요청 DTO | 없음 | 없음 (model에 빈 `AnswerForm` 전달 — 답변 폼용) |
| 세션 요구 | `required = false` — **선택** | `required = false` — **선택** |
| 응답 | view `user/questionList` | view `user/questionDetail` |
| 검증 규칙 | 없음 | 없음 |
| 비고 | model: `questionList`(`findAll()`), `loginMember`.<br/>정렬·페이징 **[TBD: 미구현]** | model: `question`, `answerForm`, `loginMember`.<br/>미존재 id → `orElseThrow()` (500, **[TBD: 404 처리 미구현]**).<br/>답변 목록은 템플릿에서 `question.answerList` 접근으로 로딩(LAZY/OSIV) |

### 1.5 AnswerController

| 항목 | 내용 |
|---|---|
| **Method / URL** | `POST /answer/create/{questionId}` |
| 핸들러 | `createAnswer()` |
| PathVariable | `questionId` (Long) |
| 요청 DTO | **`AnswerForm`** (`@Valid @ModelAttribute("answerForm")`) |
| 세션 요구 | `@SessionAttribute(LOGIN_MEMBER, required = false)` — **선택** ⚠️ |
| 응답 | 성공 → `redirect:/question/detail/{questionId}`<br/>검증 실패 → view `user/questionDetail` (리다이렉트 아님, 상세 재렌더링) |
| 검증 규칙 | `@NotEmpty content` ("내용은 필수 항목입니다.") |
| 비고 | 검증 실패 시 model에 `question`, `loginMember` 재세팅.<br/>미존재 `questionId` → `orElseThrow()`.<br/>**미로그인 시 `author = null`인 답변이 저장됨** |

---

## 2. DTO 명세 (4종)

STEP 0-A grep 결과 기준 — 전 DTO가 `@Getter @Setter`(Lombok)만 사용하며, 검증은 Jakarta Bean Validation 어노테이션입니다.

### 2.1 `LoginForm`

| 필드 | 타입 | 검증 어노테이션 | 메시지 |
|---|---|---|---|
| `loginId` | String | `@NotEmpty` | "회원 아이디는 필수입니다." |
| `password` | String | `@NotEmpty` | "비밀번호는 필수입니다." |

### 2.2 `MemberForm`

| 필드 | 타입 | 검증 어노테이션 | 메시지 |
|---|---|---|---|
| `id` | Long | **없음** | — (수정 폼에서 대상 식별용) |
| `loginId` | String | `@NotEmpty` | 기본 메시지 (커스텀 없음) |
| `name` | String | `@NotEmpty` | 기본 메시지 (커스텀 없음) |
| `password` | String | `@NotEmpty` | "비밀번호는 필수입니다." |
| `grade` | String | **없음** | — (가입 시 `"user"` 하드코딩, 수정 시 폼 값 사용) |

### 2.3 `QuestionForm`

| 필드 | 타입 | 검증 어노테이션 | 메시지 |
|---|---|---|---|
| `subject` | String | `@NotEmpty`, `@Size(max = 200)` | "제목은 필수 항목입니다." (`@Size`는 기본 메시지) |
| `content` | String | `@NotEmpty` | "내용은 필수 항목입니다." |

> `@Size(max = 200)`은 엔티티의 `@Column(length = 200)`과 일치합니다.

### 2.4 `AnswerForm`

| 필드 | 타입 | 검증 어노테이션 | 메시지 |
|---|---|---|---|
| `content` | String | `@NotEmpty` | "내용은 필수 항목입니다." |

---

## 3. 공통 규약

| 항목 | 내용 |
|---|---|
| 세션 키 | `"loginMember"` (`SessionConst.LOGIN_MEMBER`) — 값은 `constant`/`controller` 두 클래스 모두 동일 |
| 세션 저장 객체 | `DoMember` 엔티티 **그대로** (DTO 변환 없음 — 비밀번호 포함) |
| 전역 모델 속성 | `GlobalModelAdvice`가 모든 요청 model에 `loginMember` 주입 (미로그인 시 `null`) |
| 검증 실패 처리 | 모든 POST 핸들러가 `BindingResult.hasErrors()` → 해당 폼 뷰 재반환 |
| 응답 형식 | 전부 **서버사이드 렌더링(Thymeleaf)**. REST/JSON 엔드포인트 **없음** (`@RestController`·`@ResponseBody` 부재) |
| 인증 실패 응답 | **[TBD: 미구현]** — `required=true` 세션 속성 부재 시 예외. 로그인 페이지 리다이렉트 없음 |
| 에러 페이지 | **[TBD: 미구현]** — `@ControllerAdvice`의 `@ExceptionHandler`, 커스텀 error 템플릿 없음 |
