# TODO / 확인 목록

> 최종 정리: 2026-07-22
> 이 파일은 나중에 이어서 볼 때를 위한 체크리스트입니다. 완료하면 `[x]`로 바꾸세요.

## ✅ 지금까지 완료된 것
- [x] 컨트롤러 계층 테스트 (`@WebMvcTest` 슬라이스) + **Spring REST Docs API 명세서**
      - 문서 소스: `src/docs/asciidoc/index.adoc`
      - 생성: `./gradlew asciidoctor` → `build/docs/asciidoc/index.html`
      - `bootJar`에 번들 → 앱 실행 시 `/docs/index.html`로 서빙
- [x] 도메인 단위 테스트 (SlotTimes, Invitation, WorkGroup, Member)
- [x] 서비스 단위 테스트 (Mockito, 아웃바운드 포트 목킹)
- [x] 영속성 통합 테스트 (`@DataJpaTest` + **Testcontainers MySQL**, 7개 어댑터)
- 전체 102개 테스트 통과 · 실행: `./gradlew test`

---

## 🧪 테스트로 아직 안 덮인 부분
- [ ] **세션/Redis E2E 미검증** — 로그인 → Spring Session(Redis) 저장 → 후속 요청 인증까지의
      실제 흐름은 테스트가 없음. `@SpringBootTest` + Testcontainers(MySQL + Redis)로 전 구간 E2E 추가 검토.
- [ ] **AssignShiftService 전 구간 통합 테스트** — 현재는 서비스 단위(목킹) + 어댑터 단위만 있음.
      컨트롤러→서비스→실제 DB로 이어지는 배정 시나리오 통합 검증.
- [ ] **동시성(quota) 경쟁 조건 검토** — `AssignShiftService`가 `countConfirmed` 확인 후 저장하는데,
      서로 다른 알바가 동시에 배정되면 필요 인원을 초과할 여지가 있음.
      (같은 알바·같은 슬롯 중복은 UNIQUE 제약이 막아주지만, **총원 초과**는 못 막음)
      → 트랜잭션 격리 수준 / 비관적 락 / 슬롯 카운트 방식 재검토.
- [ ] Docker 없는 CI를 위해 영속성 테스트를 `@Tag("integration")`으로 분리하고
      `./gradlew test`(단위)와 `integrationTest`(통합)로 나누는 것 검토.

## 🔐 운영 / 보안 확인
- [ ] **`spring.jpa.hibernate.ddl-auto: update`** (application.yaml) — 운영에서는 위험.
      **Flyway / Liquibase 마이그레이션** 도입 후 운영은 `validate`로 전환 (yaml 주석에도 언급됨).
- [ ] **DB 기본 자격증명** (`root` / `1234`)이 dev 기본값으로 들어가 있음 —
      운영 배포 시 `DB_USERNAME` / `DB_PASSWORD` 환경변수로 반드시 주입되는지 확인.
- [ ] **CSRF 비활성화** (`SecurityConfig`) — SPA + same-site 쿠키 전제. 크로스사이트 요청 시나리오가
      생기면 재검토 (코드 주석에 "Revisit if browsers post cross-site" 있음).
- [ ] 세션 타임아웃 / 쿠키 설정(`same-site: lax`, `http-only`) 운영 요구사항과 맞는지 확인.

## 📄 문서 / 배포
- [ ] 실제 앱 기동 후 `/docs/index.html`이 정상 서빙되는지 확인 (`bootJar` 실행).
- [ ] API 문서에 인증(세션 쿠키) 흐름 예시 / 에러 코드 표가 최신인지 주기적으로 점검.

## 🧰 참고 (이 프로젝트의 Boot 4 특이사항)
- Jackson 3 (`tools.jackson.*`), `@WebMvcTest`·`@DataJpaTest` 등 테스트 슬라이스가
  기술별 모듈로 분리됨 (`spring-boot-webmvc-test`, `spring-boot-data-jpa-test` 등).
- Testcontainers 2.x 좌표: `org.testcontainers:testcontainers-mysql`,
  클래스 `org.testcontainers.mysql.MySQLContainer`.
- 영속성 테스트는 **Docker 데몬이 떠 있어야** 실행됨.
