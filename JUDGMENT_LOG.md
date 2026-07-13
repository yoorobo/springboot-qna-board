# JUDGMENT LOG

문서화(WO 0713_05 v2) 과정에서 발생한 판단 지점 기록.

- 기준 커밋: `aef9d98`
- 작업 브랜치: `docs/architecture`
- 분류: **[A] 스펙이탈** — WO 문언/전제와 실제 코드·환경이 다른 지점 / **[B] 미정의** — 코드만으로 확정할 수 없어 판단이 필요했던 지점
- 원칙: 소스는 읽기만 했고, 아래 항목 중 어느 것도 **코드로 수정하지 않았습니다**. 전부 "현황 기록"입니다.

---

## [A] 스펙이탈

### [A]-0 — WO 지정 필독 자료 2건이 존재하지 않음
- WO는 `~/다운로드/INVENTORY_aef9d98.md`와 `~/다운로드/0713_05_SA_IS_문서화_WO레오_v2.md`를 "작업 전 반드시 읽을 것"으로 지정.
- 실제 `C:\Users\두드림\Downloads\`(= `~/다운로드`에 해당하는 실제 경로)에 **두 파일 모두 없음**. 존재하는 것은 `0713_04_SA_IS_문서화_WO레오_v1.md`, `0713_05_SA_IS_문서화확정_WO레오_v1.md` 2건.
- **판단**: (1) WO v2 전문은 사용자 프롬프트에 그대로 포함되어 있었고, (2) 인벤토리 내용은 직전 STEP 0 작업 결과 및 디스크의 `0713_05_..확정_WO레오_v1.md`의 "STEP 0에서 확정된 사실" 절과 일치하므로, **실소스를 직접 재확인하는 방식**으로 진행. 추측으로 채운 내용 없음.
- **영향**: 없음(모든 표·다이어그램은 `aef9d98` 실소스에서 추출). 다만 INVENTORY 문서와의 대조 검증은 불가능했음.

### [A]-1 — 엔드포인트 개수: WO "12종" vs 실제 **15개 핸들러**
- WO STEP 5는 "엔드포인트 12종 표"를 요구.
- 실제 `@GetMapping`/`@PostMapping` 카운트: Home 1 + Login 3 + Member 6 + Question 4 + Answer 1 = **15개**. (URL 패턴 기준으로 GET/POST를 한 줄로 합치면 11줄이 되며, 이 역시 12가 아님)
- **판단**: 실소스 기준 **15개 전부**를 IS.md·README에 기재. WO의 "12종"은 따르지 않음.

### [A]-2 — 답변 등록만 세션이 `required = false` (미로그인 답변 가능)
- `QuestionController.questionCreate()`(POST)는 `@SessionAttribute(LOGIN_MEMBER)` = **required=true**.
- `AnswerController.createAnswer()`는 같은 "쓰기" 동작인데 `required = false`.
- `AnswerService.create()`에 `author` null 검사가 없어, **미로그인 상태로 답변을 POST하면 `author = NULL`인 answer 행이 저장됨**.
- **판단**: 일관성 이탈로 보이나 **의도적 설계인지 실수인지 코드만으로는 알 수 없음**. 수정하지 않고 ERD·시퀀스·IS 세 문서에 현황으로 기록.

### [A]-3 — `GET /members/{memberId}/delete`에 인증이 전혀 없음
- 해당 핸들러에는 `@SessionAttribute`가 **아예 없어** 비로그인 상태에서도 회원 삭제가 가능.
- 같은 컨트롤러의 `/members`, `edit`(GET·POST)은 `required=true`로 보호됨.
- 부수효과가 있는 요청이 GET 메서드인 점도 함께 기록(링크 프리페치·크롤러에 취약).
- **판단**: 보안 결함으로 보이나 이번 WO 범위는 문서화 → 수정하지 않고 README "알려진 개선점"에 명시.

### [A]-4 — `answerList` fetch 전략: 이전 WO(v1) 기재 오류
- 디스크의 `0713_05_..확정_WO레오_v1.md` STEP 4는 "answerList(EAGER)"로 기재.
- 실제 `Question.answerList`는 `@OneToMany`이므로 fetch 기본값은 **LAZY**. (EAGER인 것은 `@ManyToOne`인 `author`, `Answer.question`)
- **판단**: 프롬프트의 WO v2가 이를 "LAZY + OSIV 기준으로 표현"으로 정정했고, 이것이 실제 코드와 일치하므로 **v2를 따름**.

### [A]-5 — ERD 카디널리티: WO v1의 `||--o{` vs 실제 nullability 미선언
- WO v1은 `do_member ||--o{ question` 등 필수 관계로 지정.
- STEP 0-A grep 결과 `domain` 패키지에 `optional = false` / `nullable = false` **0건** → DB상 FK는 전부 NULL 허용.
- **판단**: WO v2 지시(`미확인시 |o--o{ + 각주`)에 따라 ERD.md에 **양쪽 다 제시** — 코드 컨벤션 기준(`||--o{`)을 주 다이어그램으로, 스키마 실제(`|o--o{`)를 각주 다이어그램으로 병기하고 "필수 여부 미확정"을 명시.

---

## [B] 미정의

### [B]-1 — FK 필수 여부 미확정
- 위 [A]-5와 동일 근거. `question.member_id`, `answer.member_id`, `answer.question_id` 세 FK 모두 스키마상 nullable.
- 확정하려면 `@ManyToOne(optional = false)` 또는 `@JoinColumn(nullable = false)` 선언 필요. **현재 코드로는 판단 불가**.

### [B]-2 — 회원 삭제 시 cascade 연쇄 범위 미검증
- `DoMember`의 두 `@OneToMany`가 `cascade = ALL`(REMOVE 포함), `Question.answerList`는 `cascade = REMOVE`.
- 이론상 회원 삭제 → 그 회원의 질문 삭제 → 그 질문에 달린 **다른 회원의 답변까지** 연쇄 삭제.
- **런타임으로 실행해 확인하지 않았음**(read-only WO). `orphanRemoval`은 미지정(false).
- ERD.md 3절에 "이론적 동작 + 미검증" 명시.

### [B]-3 — OSIV 활성 여부는 "기본값" 근거
- `questionDetail.html`이 LAZY 컬렉션 `question.answerList`에 접근하는데 `LazyInitializationException` 없이 동작하려면 OSIV가 켜져 있어야 함.
- `application.yaml`에 `spring.jpa.open-in-view` 설정이 **없음** → Spring Boot 기본값 `true`로 판단.
- **실행해서 확인하지 않았음**. 기본값이 유지된다는 전제하의 서술임을 sequence-diagrams.md 흐름 3에 각주로 명시.

### [B]-4 — 테이블·컬럼 물리명은 기본 네이밍 전략 기준 추정
- 엔티티에 `@Table(name=...)`이 없고, 명시적 `@Column(name=...)`은 PK 3종(`member_id`, `question_id`, `answer_id`)뿐.
- 나머지(`login_id`, `create_date`, `modify_date` 등)는 Hibernate 기본 네이밍 전략(camelCase → snake_case)으로 **산출한 값**이며, 실제 생성된 DB 스키마를 조회해 확인하지 않았음.

### [B]-5 — `POST /members/{memberId}/edit`의 `memberId` 미사용
- 시그니처에 `@PathVariable`이 **선언되어 있지 않고**, 실제 갱신 대상은 `memberForm.getId()`(hidden 필드 추정)로 결정됨.
- URL의 `{memberId}`와 폼의 `id`가 불일치할 경우 **폼 값이 이긴다** — 의도적 설계인지 불명. 수정하지 않고 IS.md에 기록.

### [B]-6 — 회원 수정이 `save()` 전체 덮어쓰기 방식
- `updateMemberSave()`는 조회 없이 `new DoMember()`에 폼 값만 세팅 후 `memberService.save()` 호출 → JPA merge.
- 이때 `questionList`/`answerList`가 `null`인 상태로 merge되는데, `cascade = ALL`과 결합해 기존 연관 데이터에 어떤 영향이 있는지는 **런타임 미검증**.
- 또한 `save()` 경로에는 `validateDuplicateMember()`가 호출되지 않아 **수정 시 loginId 중복 검사가 없음**(DB UNIQUE 제약도 없음).

---

## 이번 WO에서 하지 않은 것 (명시)

- `SessionConst` 중복 통합 — 별도 WO 예정 (WO 전제사항)
- 위 [A]/[B] 항목의 코드 수정 일절 없음
- 소스(`.java`/`.html`/`.yaml`/`.gradle`) 무변경 — STEP 6에서 `git diff`로 검증
