# REST API 문서

> **⚠️ 중요**: Battle과 Account API는 현재 테스트 모드입니다. 인증 없이 사용 가능하며, Account API는 DB의 첫 번째 사용자를 자동으로 사용합니다.

## 목차
- [Battle API](#battle-api)
- [Account API](#account-api)
- [Team API](#team-api)
- [Comment API](#comment-api)

---

## Battle API

Base URL: `/api/battles`

> **테스트 모드**: 현재 인증 없이 접근 가능합니다.

### 1. 모든 배틀 목록 조회

팀별 수익률 요약 정보를 포함한 배틀 목록을 조회합니다.

**Endpoint**
```
GET /api/battles
```

**Request**
- 인증: 필요 없음
- Query Parameters: 없음

**Response**
- Status: `200 OK`
- Body: `List<BattleListResponse>`

```json
[
  {
    "id": "uuid",
    "type": "ALL | NORMAL",
    "name": "배틀 이름",
    "ticker": "종목 티커",
    "startAt": "2026-01-30T09:00:00",
    "endAt": "2026-02-28T18:00:00",
    "status": "YET | PROGRESS | END",
    "createdAt": "2026-01-25T10:00:00",
    "teams": [
      {
        "id": 1,
        "name": "팀 이름",
        "rate": 15.5,
        "proceed": 155000,
        "memberCount": 5
      }
    ]
  }
]
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | 배틀 고유 ID |
| type | String | 배틀 유형 (ALL: 전체 대결, NORMAL: 일반 대결) |
| name | String | 배틀 이름 |
| ticker | String | 종목 티커 |
| startAt | DateTime | 배틀 시작 시간 |
| endAt | DateTime | 배틀 종료 시간 |
| status | String | 배틀 상태 (YET: 시작 전, PROGRESS: 진행 중, END: 종료) |
| createdAt | DateTime | 배틀 생성 시간 |
| teams | Array | 팀 요약 정보 목록 |
| teams[].id | Long | 팀 ID |
| teams[].name | String | 팀 이름 |
| teams[].rate | Float | 수익률 (%) |
| teams[].proceed | Integer | 수익금 |
| teams[].memberCount | Integer | 팀 멤버 수 |

---

### 2. 배틀 생성

새로운 배틀을 생성합니다.

**Endpoint**
```
POST /api/battles
```

**Request**
- 인증: 필요
- Content-Type: `application/json`

```json
{
  "type": "NORMAL",
  "name": "배틀 이름",
  "ticker": "005930",
  "startAt": "2026-02-01T09:00:00",
  "endAt": "2026-02-28T18:00:00",
  "metricType": "RATE",
  "valuationTime": "15:30:00",
  "initialCapital": 1000000,
  "memberCount": 10,
  "teamCount": 2
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| type | String | No | 배틀 유형 (ALL, NORMAL) |
| name | String | No | 배틀 이름 |
| ticker | String | No | 종목 티커 |
| startAt | DateTime | Yes | 배틀 시작 시간 |
| endAt | DateTime | Yes | 배틀 종료 시간 |
| metricType | String | No | 평가 기준 (RATE: 수익률, PROCEED: 수익금) |
| valuationTime | Time | No | 평가 시간 |
| initialCapital | Integer | Yes | 초기 금액 (최소 1) |
| memberCount | Integer | No | 최대 참가자 수 |
| teamCount | Integer | No | 팀 수 제한 (기본값: 2) |

**Response**
- Status: `201 CREATED`
- Body: `BattleResponse`

```json
{
  "id": "uuid",
  "type": "NORMAL",
  "name": "배틀 이름",
  "ticker": "005930",
  "startAt": "2026-02-01T09:00:00",
  "endAt": "2026-02-28T18:00:00",
  "status": "YET",
  "metricType": "RATE",
  "valuationTime": "15:30:00",
  "initialCapital": 1000000,
  "memberCount": 10,
  "teamCount": 2,
  "createdAt": "2026-01-30T10:00:00",
  "updatedAt": "2026-01-30T10:00:00"
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | 배틀 고유 ID |
| type | String | 배틀 유형 |
| name | String | 배틀 이름 |
| ticker | String | 종목 티커 |
| startAt | DateTime | 배틀 시작 시간 |
| endAt | DateTime | 배틀 종료 시간 |
| status | String | 배틀 상태 |
| metricType | String | 평가 기준 |
| valuationTime | Time | 평가 시간 |
| initialCapital | Integer | 초기 금액 |
| memberCount | Integer | 최대 참가자 수 |
| teamCount | Integer | 팀 수 |
| createdAt | DateTime | 생성 시간 |
| updatedAt | DateTime | 수정 시간 |

**Error Response**
```json
{
  "message": "대결 시작일은 필수입니다.",
  "status": 400
}
```

---

### 3. 배틀 상세 조회

특정 배틀의 상세 정보를 조회합니다.

**Endpoint**
```
GET /api/battles/{battleId}
```

**Request**
- 인증: 필요 없음
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID

**Response**
- Status: `200 OK`
- Body: `BattleResponse`

```json
{
  "id": "uuid",
  "type": "NORMAL",
  "name": "배틀 이름",
  "ticker": "005930",
  "startAt": "2026-02-01T09:00:00",
  "endAt": "2026-02-28T18:00:00",
  "status": "PROGRESS",
  "metricType": "RATE",
  "valuationTime": "15:30:00",
  "initialCapital": 1000000,
  "memberCount": 10,
  "teamCount": 2,
  "createdAt": "2026-01-30T10:00:00",
  "updatedAt": "2026-01-30T10:00:00"
}
```

**Response Fields**
- 배틀 생성 API의 Response Fields와 동일

---

## Account API

Base URL: `/api/accounts`

> **⚠️ 테스트 모드**: 현재 Account API는 인증을 비활성화한 테스트 모드입니다. DB의 첫 번째 사용자를 자동으로 사용합니다.

### 1. 개인 계좌 생성

사용자의 개인 투자 계좌를 생성합니다.

**Endpoint**
```
POST /api/accounts/personal
```

**Request**
- 인증: 필요 없음 (테스트 모드)
- Content-Type: `application/json`
- Body: 없음
- 참고: DB의 첫 번째 사용자 계좌를 생성합니다

**Response**
- Status: `200 OK`
- Body: String

```json
"개인 계좌 생성 완료"
```

**Error Response**
```json
{
  "status": 500,
  "message": "테스트용 사용자가 없습니다. 먼저 사용자를 생성해주세요."
}
```

---

### 2. 개인 계좌 조회

사용자의 개인 계좌 정보를 조회합니다.

**Endpoint**
```
GET /api/accounts/personal
```

**Request**
- 인증: 필요 없음 (테스트 모드)
- 참고: DB의 첫 번째 사용자 계좌를 조회합니다

**Response**
- Status: `200 OK`
- Body: `AccountResponse`

```json
{
  "id": "uuid",
  "name": "개인 계좌",
  "balance": 500000,
  "seedMoney": 1000000,
  "totalAsset": 1500000
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | 계좌 고유 ID |
| name | String | 계좌 이름 |
| balance | Long | 현재 잔액 |
| seedMoney | Long | 초기 시드머니 |
| totalAsset | Long | 총 자산 |

---

### 3. 배틀 계좌 조회

특정 배틀에서 사용자의 계좌 정보를 조회합니다.

**Endpoint**
```
GET /api/accounts/battle/{battleId}
```

**Request**
- 인증: 필요 없음 (테스트 모드)
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID
- 참고: DB의 첫 번째 사용자의 배틀 계좌를 조회합니다

**Response**
- Status: `200 OK`
- Body: `AccountResponse`

```json
{
  "id": "uuid",
  "name": "배틀 계좌 - 삼성전자 대결",
  "balance": 300000,
  "seedMoney": 1000000,
  "totalAsset": 1200000
}
```

**Response Fields**
- 개인 계좌 조회 API의 Response Fields와 동일

---

## Team API

Base URL: `/api`

### 1. 팀 생성

특정 배틀에 새로운 팀을 생성합니다.

**Endpoint**
```
POST /api/battles/{battleId}/teams
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID
- Content-Type: `application/json`

```json
{
  "name": "팀 이름",
  "description": "팀 설명"
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | 팀 이름 |
| description | String | No | 팀 설명 |

**Response**
- Status: `201 CREATED`
- Body: `TeamResponse`

```json
{
  "id": 1,
  "battleId": "uuid",
  "name": "팀 이름",
  "inviteCode": "uuid",
  "description": "팀 설명",
  "rate": 0.0,
  "proceed": 0,
  "memberCount": 1,
  "createdAt": "2026-01-30T10:00:00",
  "updatedAt": "2026-01-30T10:00:00"
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | Long | 팀 ID |
| battleId | UUID | 배틀 ID |
| name | String | 팀 이름 |
| inviteCode | UUID | 팀 초대 코드 |
| description | String | 팀 설명 |
| rate | Float | 팀 수익률 (%) |
| proceed | Integer | 팀 수익금 |
| memberCount | Integer | 현재 팀원 수 |
| createdAt | DateTime | 생성 시간 |
| updatedAt | DateTime | 수정 시간 |

---

### 2. 배틀의 팀 목록 조회 (팀별 수익률 비교)

특정 배틀의 모든 팀과 각 팀의 수익률 정보를 조회합니다.

**Endpoint**
```
GET /api/battles/{battleId}/teams
```

**Request**
- 인증: 필요 없음
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID

**Response**
- Status: `200 OK`
- Body: `List<TeamResponse>`

```json
[
  {
    "id": 1,
    "battleId": "uuid",
    "name": "불타는팀",
    "inviteCode": "uuid",
    "description": "우리가 1등이다",
    "rate": 25.5,
    "proceed": 255000,
    "memberCount": 5,
    "createdAt": "2026-01-30T10:00:00",
    "updatedAt": "2026-01-30T10:00:00"
  },
  {
    "id": 2,
    "battleId": "uuid",
    "name": "승리팀",
    "inviteCode": "uuid",
    "description": "무조건 승리",
    "rate": 18.3,
    "proceed": 183000,
    "memberCount": 4,
    "createdAt": "2026-01-30T11:00:00",
    "updatedAt": "2026-01-30T11:00:00"
  }
]
```

**Response Fields**
- 팀 생성 API의 Response Fields와 동일

---

### 3. 팀원 목록 및 순위 조회

특정 팀의 팀원 목록과 각 팀원의 수익률 및 순위를 조회합니다.

**Endpoint**
```
GET /api/teams/{teamId}/members
```

**Request**
- 인증: 필요 없음
- Path Parameters:
  - `teamId` (Long, required): 팀 ID

**Response**
- Status: `200 OK`
- Body: `List<TeamMemberResponse>`

```json
[
  {
    "id": 1,
    "userId": "uuid",
    "userNickname": "투자왕",
    "role": "LEADER",
    "rank": 1,
    "rate": 35.2,
    "status": "ACTIVE",
    "joinedAt": "2026-01-30T10:00:00"
  },
  {
    "id": 2,
    "userId": "uuid",
    "userNickname": "주식고수",
    "role": "MEMBER",
    "rank": 2,
    "rate": 28.7,
    "status": "ACTIVE",
    "joinedAt": "2026-01-30T10:30:00"
  }
]
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | Long | 팀원 ID |
| userId | UUID | 사용자 ID |
| userNickname | String | 사용자 닉네임 |
| role | String | 팀 역할 (LEADER, MEMBER) |
| rank | Integer | 팀 내 순위 |
| rate | Float | 개인 수익률 (%) |
| status | String | 상태 (ACTIVE, INACTIVE) |
| joinedAt | DateTime | 팀 가입 시간 |

---

### 4. 팀 가입 (초대 코드)

초대 코드를 사용하여 팀에 가입합니다.

**Endpoint**
```
POST /api/teams/join
```

**Request**
- 인증: 필요 (JWT Token)
- Content-Type: `application/json`

```json
{
  "inviteCode": "uuid"
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| inviteCode | UUID | Yes | 팀 초대 코드 |

**Response**
- Status: `200 OK`
- Body: `TeamResponse`

```json
{
  "id": 1,
  "battleId": "uuid",
  "name": "팀 이름",
  "inviteCode": "uuid",
  "description": "팀 설명",
  "rate": 15.5,
  "proceed": 155000,
  "memberCount": 6,
  "createdAt": "2026-01-30T10:00:00",
  "updatedAt": "2026-01-30T10:00:00"
}
```

**Response Fields**
- 팀 생성 API의 Response Fields와 동일

---

### 5. 팀 탈퇴

현재 소속된 팀에서 탈퇴합니다.

**Endpoint**
```
DELETE /api/teams/{teamId}/leave
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `teamId` (Long, required): 팀 ID

**Response**
- Status: `204 NO CONTENT`
- Body: 없음

---

## Comment API

Base URL: `/api/battles/{battleId}/comments`

### 1. 댓글 작성

특정 배틀에 댓글을 작성합니다. parentId가 있으면 대댓글로 작성됩니다.

**Endpoint**
```
POST /api/battles/{battleId}/comments
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID
- Content-Type: `application/json`

```json
{
  "battleId": "uuid",
  "content": "댓글 내용",
  "parentId": null
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| battleId | UUID | Yes | 배틀 ID |
| content | String | Yes | 댓글 내용 |
| parentId | Long | No | 부모 댓글 ID (대댓글인 경우) |

**Response**
- Status: `201 CREATED`
- Body: `CommentResponse`

```json
{
  "id": 1,
  "battleId": "uuid",
  "userId": "uuid",
  "userNickname": "사용자닉네임",
  "parentId": null,
  "content": "댓글 내용",
  "isDeleted": false,
  "createdAt": "2026-01-30T10:00:00",
  "updatedAt": "2026-01-30T10:00:00",
  "replies": []
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | Long | 댓글 ID |
| battleId | UUID | 배틀 ID |
| userId | UUID | 작성자 ID |
| userNickname | String | 작성자 닉네임 |
| parentId | Long | 부모 댓글 ID (원댓글이면 null) |
| content | String | 댓글 내용 |
| isDeleted | Boolean | 삭제 여부 |
| createdAt | DateTime | 작성 시간 |
| updatedAt | DateTime | 수정 시간 |
| replies | Array | 대댓글 목록 |

---

### 2. 배틀의 댓글 목록 조회

특정 배틀의 모든 댓글을 조회합니다. 대댓글도 포함됩니다.

**Endpoint**
```
GET /api/battles/{battleId}/comments
```

**Request**
- 인증: 필요 없음
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID

**Response**
- Status: `200 OK`
- Body: `List<CommentResponse>`

```json
[
  {
    "id": 1,
    "battleId": "uuid",
    "userId": "uuid",
    "userNickname": "사용자1",
    "parentId": null,
    "content": "첫 번째 댓글",
    "isDeleted": false,
    "createdAt": "2026-01-30T10:00:00",
    "updatedAt": "2026-01-30T10:00:00",
    "replies": [
      {
        "id": 2,
        "battleId": "uuid",
        "userId": "uuid",
        "userNickname": "사용자2",
        "parentId": 1,
        "content": "대댓글입니다",
        "isDeleted": false,
        "createdAt": "2026-01-30T10:05:00",
        "updatedAt": "2026-01-30T10:05:00",
        "replies": null
      }
    ]
  }
]
```

**Response Fields**
- 댓글 작성 API의 Response Fields와 동일

---

### 3. 댓글 삭제

댓글을 삭제합니다 (Soft Delete). 본인이 작성한 댓글만 삭제 가능합니다.

**Endpoint**
```
DELETE /api/battles/{battleId}/comments/{commentId}
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID
  - `commentId` (Long, required): 댓글 ID

**Response**
- Status: `204 NO CONTENT`
- Body: 없음

**참고**
- Soft Delete 방식으로, 삭제된 댓글은 "삭제된 댓글입니다."로 표시됩니다
- 본인이 작성한 댓글만 삭제할 수 있습니다

---

## 공통 에러 응답

### 인증 오류
```json
{
  "status": 401,
  "message": "인증이 필요합니다."
}
```

### 권한 오류
```json
{
  "status": 403,
  "message": "접근 권한이 없습니다."
}
```

### 리소스 없음
```json
{
  "status": 404,
  "message": "요청한 리소스를 찾을 수 없습니다."
}
```

### 유효성 검증 오류
```json
{
  "status": 400,
  "message": "입력값이 유효하지 않습니다.",
  "errors": [
    {
      "field": "startAt",
      "message": "대결 시작일은 필수입니다."
    }
  ]
}
```

### 서버 오류
```json
{
  "status": 500,
  "message": "서버 내부 오류가 발생했습니다."
}
```

---

## 인증

대부분의 API는 JWT(JSON Web Token) 기반 인증을 사용합니다.

**Authorization Header**
```
Authorization: Bearer {JWT_TOKEN}
```

**예시**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 데이터 타입 정의

### Enum Types

#### BattleType
- `ALL`: 전체 대결
- `NORMAL`: 일반 대결

#### BattleStatus
- `YET`: 대결 시작 전
- `PROGRESS`: 진행 중
- `END`: 종료

#### MetricType
- `RATE`: 수익률 기준
- `PROCEED`: 수익금 기준

#### TeamUserRole
- `LEADER`: 팀 리더
- `MEMBER`: 일반 팀원

#### TeamUserStatus
- `ACTIVE`: 활성 상태
- `INACTIVE`: 비활성 상태

---

## 참고사항

1. 모든 날짜/시간은 ISO 8601 형식을 사용합니다 (예: `2026-01-30T10:00:00`)
2. UUID는 하이픈으로 구분된 36자 형식입니다 (예: `550e8400-e29b-41d4-a716-446655440000`)
3. 금액은 정수형(Long/Integer)으로 처리되며, 원화 기준입니다
4. 수익률은 Float 타입이며, 퍼센트(%) 단위입니다
5. Time 형식은 `HH:mm:ss` 형식입니다 (예: `15:30:00`)
