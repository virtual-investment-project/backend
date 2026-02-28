# REST API 문서

## 목차
- [Auth API](#auth-api)
- [User API](#user-api)
- [Battle API](#battle-api)
  - [4. 배틀 계좌별 개인 수익률 조회](#4-배틀-계좌별-개인-수익률-조회)
  - [5. 배틀 팀별 합산 수익률 조회](#5-배틀-팀별-합산-수익률-조회)
- [Account API](#account-api)
  - [4. 내 개인 계좌 수익률 조회](#4-내-개인-계좌-수익률-조회)
  - [5. 내 배틀 계좌 수익률 조회](#5-내-배틀-계좌-수익률-조회)
  - [6. 계좌 ID로 수익률 조회](#6-계좌-id로-수익률-조회)
- [Team API](#team-api)
- [Comment API](#comment-api)
- [Order API](#order-api)
- [Rankings API](#rankings-api)
- [History API](#history-api)
- [MyPage API](#mypage-api)
  - [6. 내 전체 계좌 수익률 조회](#6-내-전체-계좌-수익률-조회)

---

## Auth API

Base URL: `/api/auth`

> 인증 없이 접근 가능합니다.

### 1. 구글 로그인

구글 ID Token을 사용하여 로그인합니다.

**Endpoint**
```
POST /api/auth/google
```

**Request**
- 인증: 필요 없음
- Content-Type: `application/json`

```json
{
  "idToken": "구글 ID 토큰"
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| idToken | String | Yes | 구글 ID Token |

**Response**
- Status: `200 OK`
- Body: `GoogleLoginResponse`

```json
{
  "accessToken": "JWT 액세스 토큰",
  "refreshToken": "JWT 리프레시 토큰",
  "role": "GUEST",
  "isNewUser": true
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| accessToken | String | JWT 액세스 토큰 |
| refreshToken | String | JWT 리프레시 토큰 |
| role | String | 사용자 역할 (GUEST, USER, ADMIN) |
| isNewUser | Boolean | 신규 사용자 여부 |

**Error Response**
- `401`: 유효하지 않은 구글 토큰
- `500`: 서버 내부 오류

---

### 2. 토큰 갱신

리프레시 토큰으로 새로운 액세스 토큰을 발급합니다.

**Endpoint**
```
POST /api/auth/refresh
```

**Request**
- 인증: 필요 없음
- Content-Type: `application/json`

```json
{
  "refreshToken": "리프레시 토큰"
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| refreshToken | String | Yes | 리프레시 토큰 |

**Response**
- Status: `200 OK`
- Body: `TokenResponse`

```json
{
  "accessToken": "새로운 JWT 액세스 토큰",
  "refreshToken": "새로운 JWT 리프레시 토큰"
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| accessToken | String | 새로운 JWT 액세스 토큰 |
| refreshToken | String | 새로운 JWT 리프레시 토큰 |

**Error Response**
- `401`: 유효하지 않은 리프레시 토큰
- `500`: 서버 내부 오류

---

## User API

Base URL: `/api/users`

### 1. 추가 정보 입력

신규 사용자(GUEST)가 추가 정보를 입력하여 회원 가입을 완료합니다 (GUEST → USER 등업).

**Endpoint**
```
POST /api/users/additional-info
```

**Request**
- 인증: 필요 (JWT Token)
- Content-Type: `application/json`

```json
{
  "nickname": "닉네임",
  "age": 25,
  "school": "학교명",
  "company": "회사명"
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| nickname | String | Yes | 닉네임 |
| age | Integer | Yes | 나이 (최소 1) |
| school | String | No | 학교명 |
| company | String | No | 회사명 |

**Response**
- Status: `200 OK`
- Body: `TokenResponse`

```json
{
  "accessToken": "새로운 JWT 액세스 토큰",
  "refreshToken": "새로운 JWT 리프레시 토큰"
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| accessToken | String | 새로운 JWT 액세스 토큰 (역할 변경 반영) |
| refreshToken | String | 새로운 JWT 리프레시 토큰 |

---

## Battle API

Base URL: `/api/battles`

### 1. 배틀 목록 조회

팀별 수익률 요약 정보를 포함한 배틀 목록을 조회합니다. 상태별 필터링과 조회 개수 제한이 가능합니다.

**Endpoint**
```
GET /api/battles
```

**Request**
- 인증: 필요 없음
- Query Parameters:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| status | String | No | - | 배틀 상태 필터 (YET, PROGRESS, END). 미지정 시 전체 조회 |
| limit | Integer | No | 10 | 최대 조회 개수 (1~100, status 지정 시에만 적용) |

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

새로운 배틀을 생성합니다. 생성 시 첫 번째 팀이 자동 생성되고, 생성자가 해당 팀의 LEADER로 등록됩니다.

**Endpoint**
```
POST /api/battles
```

**Request**
- 인증: 필요 (JWT Token)
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
  "teamCount": 2,
  "teamName": "우리팀"
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| type | String | No | 배틀 유형 (ALL, NORMAL). 기본값: NORMAL |
| name | String | No | 배틀 이름. 기본값: "battle" |
| ticker | String | No | 종목 티커. 기본값: "BTC" |
| startAt | DateTime | Yes | 배틀 시작 시간 |
| endAt | DateTime | Yes | 배틀 종료 시간 |
| metricType | String | No | 평가 기준 (RATE, PROCEED). 기본값: RATE |
| valuationTime | Time | No | 평가 시간. 기본값: 00:00:00 |
| initialCapital | Integer | Yes | 초기 금액 (최소 1) |
| memberCount | Integer | No | 팀별 최대 참가자 수. 기본값: 10 |
| teamCount | Integer | No | 팀 수 제한. 기본값: 2 |
| teamName | String | No | 생성자의 팀 이름. 미입력 시 "{사용자이름}의 팀" |

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
| memberCount | Integer | 팀별 최대 참가자 수 |
| teamCount | Integer | 팀 수 |
| createdAt | DateTime | 생성 시간 |
| updatedAt | DateTime | 수정 시간 |

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

### 4. 배틀 계좌별 개인 수익률 조회

특정 배틀에 참가한 모든 계좌의 개인 수익률을 수익률 내림차순으로 조회합니다.

**Endpoint**
```
GET /api/battles/{battleId}/profit/accounts
```

**Request**
- 인증: 필요 없음
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID

**Response**
- Status: `200 OK`
- Body: `List<AccountProfitResponse>`

```json
[
  {
    "accountId": "uuid",
    "userId": "uuid",
    "userName": "사용자 이름",
    "teamId": 1,
    "teamName": "불타는팀",
    "seedMoney": 1000000,
    "totalAsset": 1253000,
    "returnAmount": 253000,
    "returnRate": 25.30
  },
  {
    "accountId": "uuid",
    "userId": "uuid",
    "userName": "사용자2",
    "teamId": 2,
    "teamName": "승리팀",
    "seedMoney": 1000000,
    "totalAsset": 1183000,
    "returnAmount": 183000,
    "returnRate": 18.30
  }
]
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| accountId | UUID | 계좌 고유 ID |
| userId | UUID | 사용자 ID |
| userName | String | 사용자 이름 |
| teamId | Long | 팀 ID (배틀 계좌이므로 항상 존재) |
| teamName | String | 팀 이름 |
| seedMoney | Long | 초기 시드머니 |
| totalAsset | Long | 현재 총 자산 (현금 + 보유 주식 평가액) |
| returnAmount | Long | 수익금 (totalAsset − seedMoney) |
| returnRate | Double | 수익률 (%, 소수점 2자리 반올림) |

**참고**
- 수익률 내림차순으로 정렬됩니다
- `totalAsset`은 스케줄러에 의해 1초마다 갱신됩니다
- 수익금이 음수이면 손실을 의미합니다

---

### 5. 배틀 팀별 합산 수익률 조회

특정 배틀의 팀별 합산 수익률을 조회합니다. 각 팀의 수익률과 함께 팀원 개인 수익률도 포함됩니다.

**Endpoint**
```
GET /api/battles/{battleId}/profit/teams
```

**Request**
- 인증: 필요 없음
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID

**Response**
- Status: `200 OK`
- Body: `List<TeamProfitResponse>`

```json
[
  {
    "teamId": 1,
    "teamName": "불타는팀",
    "battleId": "uuid",
    "totalSeedMoney": 5000000,
    "totalAsset": 5842000,
    "returnAmount": 842000,
    "returnRate": 16.84,
    "memberCount": 5,
    "rank": 1,
    "members": [
      {
        "accountId": "uuid",
        "userId": "uuid",
        "userName": "투자왕",
        "teamId": 1,
        "teamName": "불타는팀",
        "seedMoney": 1000000,
        "totalAsset": 1253000,
        "returnAmount": 253000,
        "returnRate": 25.30
      },
      {
        "accountId": "uuid",
        "userId": "uuid",
        "userName": "주식고수",
        "teamId": 1,
        "teamName": "불타는팀",
        "seedMoney": 1000000,
        "totalAsset": 1170000,
        "returnAmount": 170000,
        "returnRate": 17.00
      }
    ]
  },
  {
    "teamId": 2,
    "teamName": "승리팀",
    "battleId": "uuid",
    "totalSeedMoney": 4000000,
    "totalAsset": 4492000,
    "returnAmount": 492000,
    "returnRate": 12.30,
    "memberCount": 4,
    "rank": 2,
    "members": []
  }
]
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| teamId | Long | 팀 ID |
| teamName | String | 팀 이름 |
| battleId | UUID | 배틀 ID |
| totalSeedMoney | Long | 팀원 전체 시드머니 합산 |
| totalAsset | Long | 팀원 전체 총 자산 합산 |
| returnAmount | Long | 팀 합산 수익금 (totalAsset − totalSeedMoney) |
| returnRate | Double | 팀 합산 수익률 (%, 소수점 2자리 반올림) |
| memberCount | Integer | 팀원 수 |
| rank | Integer | 배틀 내 팀 순위 (수익률 내림차순) |
| members | Array | 팀원 개인 수익률 목록 (수익률 내림차순) |
| members[].accountId | UUID | 계좌 ID |
| members[].userId | UUID | 사용자 ID |
| members[].userName | String | 사용자 이름 |
| members[].teamId | Long | 팀 ID |
| members[].teamName | String | 팀 이름 |
| members[].seedMoney | Long | 초기 시드머니 |
| members[].totalAsset | Long | 현재 총 자산 |
| members[].returnAmount | Long | 수익금 |
| members[].returnRate | Double | 수익률 (%, 소수점 2자리 반올림) |

**팀 수익률 계산 방식**

$$\text{팀 합산 수익률(\%)} = \frac{\sum totalAsset - \sum seedMoney}{\sum seedMoney} \times 100$$

**참고**
- 팀 수익률 내림차순으로 정렬되며, `rank` 필드로 순위를 확인할 수 있습니다
- `members` 배열도 수익률 내림차순으로 정렬됩니다

---

## Account API

Base URL: `/api/accounts`

### 1. 개인 계좌 생성

사용자의 개인 투자 계좌를 생성합니다.

**Endpoint**
```
POST /api/accounts/personal
```

**Request**
- 인증: 필요 (JWT Token)
- Body: 없음

**Response**
- Status: `200 OK`
- Body: String

```json
"개인 계좌 생성 완료"
```

**Error Response**
- `400`: 이미 개인 계좌가 존재합니다.

---

### 2. 개인 계좌 조회

사용자의 개인 계좌 정보를 조회합니다.

**Endpoint**
```
GET /api/accounts/personal
```

**Request**
- 인증: 필요 (JWT Token)

**Response**
- Status: `200 OK`
- Body: `AccountResponse`

```json
{
  "id": "uuid",
  "name": "Personal Account",
  "balance": 500000,
  "seedMoney": 100000,
  "totalAsset": 500000
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | 계좌 고유 ID |
| name | String | 계좌 이름 |
| balance | Long | 현재 잔액 (예수금) |
| seedMoney | Long | 초기 시드머니 |
| totalAsset | Long | 총 자산 (현금 + 보유 주식 평가액) |

---

### 3. 배틀 계좌 조회

특정 배틀에서 사용자의 계좌 정보를 조회합니다.

**Endpoint**
```
GET /api/accounts/battle/{battleId}
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID

**Response**
- Status: `200 OK`
- Body: `AccountResponse`

```json
{
  "id": "uuid",
  "name": "배틀 이름 Account",
  "balance": 300000,
  "seedMoney": 1000000,
  "totalAsset": 1200000
}
```

**Response Fields**
- 개인 계좌 조회 API의 Response Fields와 동일

---

### 4. 내 개인 계좌 수익률 조회

로그인한 사용자의 개인 계좌 수익금 및 수익률을 조회합니다.

**Endpoint**
```
GET /api/accounts/personal/profit
```

**Request**
- 인증: 필요 (JWT Token)
- Body: 없음

**Response**
- Status: `200 OK`
- Body: `AccountProfitResponse`

```json
{
  "accountId": "uuid",
  "userId": "uuid",
  "userName": "사용자 이름",
  "teamId": null,
  "teamName": null,
  "seedMoney": 100000,
  "totalAsset": 134200,
  "returnAmount": 34200,
  "returnRate": 34.20
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| accountId | UUID | 계좌 고유 ID |
| userId | UUID | 사용자 ID |
| userName | String | 사용자 이름 |
| teamId | Long | 팀 ID (개인 계좌이므로 항상 `null`) |
| teamName | String | 팀 이름 (개인 계좌이므로 항상 `null`) |
| seedMoney | Long | 초기 시드머니 |
| totalAsset | Long | 현재 총 자산 (현금 + 보유 주식 평가액) |
| returnAmount | Long | 수익금 (totalAsset − seedMoney, 음수이면 손실) |
| returnRate | Double | 수익률 (%, 소수점 2자리 반올림, 음수이면 손실) |

**Error Response**
- `400`: 개인 계좌가 존재하지 않습니다.

---

### 5. 내 배틀 계좌 수익률 조회

로그인한 사용자의 특정 배틀 계좌 수익금 및 수익률을 조회합니다.

**Endpoint**
```
GET /api/accounts/battle/{battleId}/profit
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `battleId` (UUID, required): 배틀 고유 ID

**Response**
- Status: `200 OK`
- Body: `AccountProfitResponse`

```json
{
  "accountId": "uuid",
  "userId": "uuid",
  "userName": "사용자 이름",
  "teamId": 1,
  "teamName": "불타는팀",
  "seedMoney": 1000000,
  "totalAsset": 1253000,
  "returnAmount": 253000,
  "returnRate": 25.30
}
```

**Response Fields**
- `4. 내 개인 계좌 수익률 조회` API의 Response Fields와 동일

**Error Response**
- `400`: 해당 배틀의 계좌가 존재하지 않습니다.

---

### 6. 계좌 ID로 수익률 조회

계좌 ID를 직접 지정하여 해당 계좌의 수익금 및 수익률을 조회합니다. 인증 없이 누구나 조회 가능하며, 랭킹 화면이나 타 사용자 계좌 조회 시 사용합니다.

**Endpoint**
```
GET /api/accounts/{accountId}/profit
```

**Request**
- 인증: 필요 없음
- Path Parameters:
  - `accountId` (UUID, required): 계좌 고유 ID

**Response**
- Status: `200 OK`
- Body: `AccountProfitResponse`

```json
{
  "accountId": "uuid",
  "userId": "uuid",
  "userName": "사용자 이름",
  "teamId": 1,
  "teamName": "불타는팀",
  "seedMoney": 1000000,
  "totalAsset": 1253000,
  "returnAmount": 253000,
  "returnRate": 25.30
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| accountId | UUID | 계좌 고유 ID |
| userId | UUID | 소유자 사용자 ID |
| userName | String | 소유자 이름 |
| teamId | Long | 팀 ID (개인 계좌이면 `null`) |
| teamName | String | 팀 이름 (개인 계좌이면 `null`) |
| seedMoney | Long | 초기 시드머니 |
| totalAsset | Long | 현재 총 자산 (현금 + 보유 주식 평가액) |
| returnAmount | Long | 수익금 (totalAsset − seedMoney, 음수이면 손실) |
| returnRate | Double | 수익률 (%, 소수점 2자리 반올림, 음수이면 손실) |

**Error Response**
- `400`: 계좌가 존재하지 않습니다.

**참고**
- 개인 계좌: `teamId`, `teamName`이 `null`로 반환됩니다
- 배틀 계좌: `teamId`, `teamName`이 포함됩니다
- `totalAsset`은 스케줄러에 의해 1초마다 갱신됩니다

---

## Team API

Base URL: `/api`

### 1. 팀 생성

특정 배틀에 새로운 팀을 생성합니다. 생성자는 자동으로 LEADER로 등록되며, 배틀 계좌가 자동 생성됩니다.

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

### 2. 배틀의 팀 목록 조회

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
| status | String | 상태 (ACTIVE, LEFT, KICKED) |
| joinedAt | DateTime | 팀 가입 시간 |

---

### 4. 팀 가입 (초대 코드)

초대 코드를 사용하여 팀에 가입합니다. 가입 시 배틀 계좌가 자동 생성됩니다.

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

현재 소속된 팀에서 탈퇴합니다. LEADER가 탈퇴할 경우 랜덤 팀원에게 LEADER가 이전됩니다.

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

특정 배틀에 댓글을 작성합니다. parentId가 있으면 대댓글로 작성됩니다. 해당 배틀에 참여한 팀원만 작성 가능합니다.

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
| parentId | Long | No | 부모 댓글 ID (대댓글인 경우). 대대댓글은 불가 |

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
| content | String | 댓글 내용 (삭제된 경우 "삭제된 댓글입니다.") |
| isDeleted | Boolean | 삭제 여부 |
| createdAt | DateTime | 작성 시간 |
| updatedAt | DateTime | 수정 시간 |
| replies | Array | 대댓글 목록 |

---

### 2. 배틀의 댓글 목록 조회

특정 배틀의 모든 댓글을 계층형으로 조회합니다. 대댓글도 포함됩니다.

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

## Order API

Base URL: `/api/orders`

### 1. 주문 생성

매수/매도 주문을 생성합니다.

**Endpoint**
```
POST /api/orders
```

**Request**
- 인증: 필요 (JWT Token)
- Content-Type: `application/json`

```json
{
  "accountId": "uuid",
  "stockCode": "BTCUSDT",
  "stockName": "비트코인",
  "orderPrice": 50000.00,
  "quantity": 0.5,
  "orderType": "BUY"
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| accountId | UUID | Yes | 계좌 ID |
| stockCode | String | Yes | 종목 코드 |
| stockName | String | Yes | 종목명 |
| orderPrice | BigDecimal | Yes | 주문 가격 (0보다 커야 함) |
| quantity | BigDecimal | Yes | 주문 수량 (0보다 커야 함) |
| orderType | String | Yes | 주문 유형 (BUY: 매수, SELL: 매도) |

**Response**
- Status: `201 CREATED`
- Body: `OrderResponse`

```json
{
  "id": "uuid",
  "accountId": "uuid",
  "stockCode": "BTCUSDT",
  "stockName": "비트코인",
  "orderPrice": 50000.00,
  "quantity": 0.5,
  "totalAmount": 25000.00,
  "orderType": "BUY",
  "status": "PENDING",
  "createdAt": "2026-01-30T10:00:00"
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | 주문 고유 ID |
| accountId | UUID | 계좌 ID |
| stockCode | String | 종목 코드 |
| stockName | String | 종목명 |
| orderPrice | BigDecimal | 주문 가격 |
| quantity | BigDecimal | 주문 수량 |
| totalAmount | BigDecimal | 주문 총액 (가격 × 수량) |
| orderType | String | 주문 유형 (BUY, SELL) |
| status | String | 주문 상태 (PENDING, FILLED, CANCELLED) |
| createdAt | DateTime | 주문 생성 시간 |

---

### 2. 계좌별 주문 목록 조회

특정 계좌의 모든 주문을 조회합니다.

**Endpoint**
```
GET /api/orders/account/{accountId}
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `accountId` (UUID, required): 계좌 고유 ID

**Response**
- Status: `200 OK`
- Body: `List<OrderResponse>`

```json
[
  {
    "id": "uuid",
    "accountId": "uuid",
    "stockCode": "BTCUSDT",
    "stockName": "비트코인",
    "orderPrice": 50000.00,
    "quantity": 0.5,
    "totalAmount": 25000.00,
    "orderType": "BUY",
    "status": "FILLED",
    "createdAt": "2026-01-30T10:00:00"
  }
]
```

**Response Fields**
- 주문 생성 API의 Response Fields와 동일

---

### 3. 주문 취소

미체결 주문을 취소합니다.

**Endpoint**
```
POST /api/orders/{orderId}/cancel
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `orderId` (UUID, required): 주문 고유 ID

**Response**
- Status: `200 OK`
- Body: String

```json
"주문이 취소되었습니다."
```

---

### 4. 주문 수동 체결 (테스트용)

주문을 수동으로 체결합니다. 테스트 목적의 API입니다.

**Endpoint**
```
POST /api/orders/{orderId}/fill
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `orderId` (UUID, required): 주문 고유 ID

**Response**
- Status: `200 OK`
- Body: String

```json
"주문이 체결되었습니다."
```

---

## Rankings API

Base URL: `/api/rankings`

> 인증 없이 접근 가능합니다.

### 1. 개인 계좌 수익률 랭킹

개인 계좌 수익률 상위 랭킹을 조회합니다.

**Endpoint**
```
GET /api/rankings/accounts/top
```

**Request**
- 인증: 필요 없음
- Query Parameters:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| limit | Integer | No | 10 | 조회할 상위 순위 수 (1~100) |

**Response**
- Status: `200 OK`
- Body: `List<AccountRankingResponse>`

```json
[
  {
    "accountId": "uuid",
    "userId": "uuid",
    "accountName": "Personal Account",
    "userName": "사용자 이름",
    "seedMoney": 100000,
    "totalAsset": 150000,
    "returnRate": 50.0
  }
]
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| accountId | UUID | 계좌 고유 ID |
| userId | UUID | 사용자 ID |
| accountName | String | 계좌 이름 |
| userName | String | 사용자 이름 |
| seedMoney | Long | 초기 시드머니 |
| totalAsset | Long | 총 자산 |
| returnRate | Double | 수익률 (%) |

---

## History API

Base URL: `/api/history`

### 1. 계좌별 거래 내역 조회

특정 계좌의 거래 내역을 조회합니다.

**Endpoint**
```
GET /api/history/account/{accountId}
```

**Request**
- 인증: 필요 (JWT Token)
- Path Parameters:
  - `accountId` (UUID, required): 계좌 고유 ID

**Response**
- Status: `200 OK`
- Body: `List<AccountHistoryResponse>`

```json
[
  {
    "id": "uuid",
    "accountId": "uuid",
    "tradeType": "BUY",
    "amount": 50000.00,
    "balanceSnapshot": 950000.00,
    "description": "BTCUSDT 매수",
    "createdAt": "2026-01-30T10:00:00"
  }
]
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | 거래 내역 ID |
| accountId | UUID | 계좌 ID |
| tradeType | String | 거래 유형 (BUY: 매수, SELL: 매도, SEED_MONEY: 시드머니 지급, PROFIT_SNAPSHOT: 수익률 스냅샷) |
| amount | BigDecimal | 거래 금액 |
| balanceSnapshot | BigDecimal | 거래 후 잔액 스냅샷 |
| description | String | 거래 설명 |
| createdAt | DateTime | 거래 시간 |

---

## MyPage API

Base URL: `/api/mypage`

### 1. 프로필 조회

사용자의 프로필 정보를 조회합니다.

**Endpoint**
```
GET /api/mypage/profile
```

**Request**
- 인증: 필요 (JWT Token)

**Response**
- Status: `200 OK`
- Body: `ProfileResponse`

```json
{
  "email": "user@example.com",
  "name": "사용자 이름",
  "nickname": "닉네임",
  "age": 25,
  "school": "학교명",
  "company": "회사명"
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| email | String | 이메일 |
| name | String | 이름 |
| nickname | String | 닉네임 |
| age | Integer | 나이 |
| school | String | 학교명 |
| company | String | 회사명 |

---

### 2. 프로필 수정

사용자의 프로필 정보를 수정합니다.

**Endpoint**
```
PATCH /api/mypage/profile
```

**Request**
- 인증: 필요 (JWT Token)
- Content-Type: `application/json`

```json
{
  "school": "새 학교명",
  "company": "새 회사명"
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| school | String | No | 학교명 |
| company | String | No | 회사명 |

**Response**
- Status: `200 OK`
- Body: `ProfileResponse`

```json
{
  "email": "user@example.com",
  "name": "사용자 이름",
  "nickname": "닉네임",
  "age": 25,
  "school": "새 학교명",
  "company": "새 회사명"
}
```

**Response Fields**
- 프로필 조회 API의 Response Fields와 동일

---

### 3. 로그아웃

사용자를 로그아웃합니다.

**Endpoint**
```
POST /api/mypage/logout
```

**Request**
- 인증: 필요 (JWT Token)
- Body: 없음

**Response**
- Status: `204 NO CONTENT`
- Body: 없음

---

### 4. 설정 조회

사용자의 테마 및 알림 설정을 조회합니다.

**Endpoint**
```
GET /api/mypage/settings
```

**Request**
- 인증: 필요 (JWT Token)

**Response**
- Status: `200 OK`
- Body: `SettingsResponse`

```json
{
  "darkMode": false,
  "orderExecution": true,
  "battleStart": true,
  "rankChange": true,
  "profitRate": true,
  "pushNotification": true,
  "dailySummary": false,
  "stockPriceAlert": false
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| darkMode | Boolean | 다크 모드 여부 |
| orderExecution | Boolean | 주문 체결 알림 |
| battleStart | Boolean | 배틀 시작 알림 |
| rankChange | Boolean | 순위 변동 알림 |
| profitRate | Boolean | 수익률 알림 |
| pushNotification | Boolean | 푸시 알림 |
| dailySummary | Boolean | 일일 요약 알림 |
| stockPriceAlert | Boolean | 주가 알림 |

---

### 5. 설정 변경

사용자의 테마 및 알림 설정을 변경합니다. 변경하지 않을 필드는 생략 가능합니다.

**Endpoint**
```
PATCH /api/mypage/settings
```

**Request**
- 인증: 필요 (JWT Token)
- Content-Type: `application/json`

```json
{
  "darkMode": true,
  "pushNotification": false
}
```

**Request Fields**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| darkMode | Boolean | No | 다크 모드 여부 |
| orderExecution | Boolean | No | 주문 체결 알림 |
| battleStart | Boolean | No | 배틀 시작 알림 |
| rankChange | Boolean | No | 순위 변동 알림 |
| profitRate | Boolean | No | 수익률 알림 |
| pushNotification | Boolean | No | 푸시 알림 |
| dailySummary | Boolean | No | 일일 요약 알림 |
| stockPriceAlert | Boolean | No | 주가 알림 |

**Response**
- Status: `200 OK`
- Body: `SettingsResponse`

**Response Fields**
- 설정 조회 API의 Response Fields와 동일

---

### 6. 내 전체 계좌 수익률 조회

로그인한 사용자의 개인 계좌 수익률과 참여 중인 모든 배틀 계좌 수익률을 한 번에 조회합니다. 마이페이지 수익률 대시보드에서 사용합니다.

**Endpoint**
```
GET /api/mypage/profit
```

**Request**
- 인증: 필요 (JWT Token)
- Body: 없음

**Response**
- Status: `200 OK`
- Body: `MyPageProfitResponse`

```json
{
  "personalAccount": {
    "accountId": "uuid",
    "userId": "uuid",
    "userName": "사용자 이름",
    "teamId": null,
    "teamName": null,
    "seedMoney": 100000,
    "totalAsset": 134200,
    "returnAmount": 34200,
    "returnRate": 34.20
  },
  "battleAccounts": [
    {
      "accountId": "uuid",
      "userId": "uuid",
      "userName": "사용자 이름",
      "teamId": 1,
      "teamName": "불타는팀",
      "seedMoney": 1000000,
      "totalAsset": 1253000,
      "returnAmount": 253000,
      "returnRate": 25.30
    },
    {
      "accountId": "uuid",
      "userId": "uuid",
      "userName": "사용자 이름",
      "teamId": 3,
      "teamName": "챌린저스",
      "seedMoney": 500000,
      "totalAsset": 482000,
      "returnAmount": -18000,
      "returnRate": -3.60
    }
  ]
}
```

**Response Fields**
| Field | Type | Description |
|-------|------|-------------|
| personalAccount | Object | 개인 계좌 수익률. 개인 계좌가 없으면 `null` |
| personalAccount.accountId | UUID | 계좌 고유 ID |
| personalAccount.userId | UUID | 사용자 ID |
| personalAccount.userName | String | 사용자 이름 |
| personalAccount.teamId | Long | 항상 `null` (개인 계좌) |
| personalAccount.teamName | String | 항상 `null` (개인 계좌) |
| personalAccount.seedMoney | Long | 초기 시드머니 |
| personalAccount.totalAsset | Long | 현재 총 자산 (현금 + 보유 주식 평가액) |
| personalAccount.returnAmount | Long | 수익금 (음수이면 손실) |
| personalAccount.returnRate | Double | 수익률 (%, 소수점 2자리 반올림, 음수이면 손실) |
| battleAccounts | Array | 참여 중인 배틀 계좌 수익률 목록 (수익률 내림차순) |
| battleAccounts[].accountId | UUID | 계좌 고유 ID |
| battleAccounts[].userId | UUID | 사용자 ID |
| battleAccounts[].userName | String | 사용자 이름 |
| battleAccounts[].teamId | Long | 소속 팀 ID |
| battleAccounts[].teamName | String | 소속 팀 이름 |
| battleAccounts[].seedMoney | Long | 초기 시드머니 |
| battleAccounts[].totalAsset | Long | 현재 총 자산 |
| battleAccounts[].returnAmount | Long | 수익금 (음수이면 손실) |
| battleAccounts[].returnRate | Double | 수익률 (%, 소수점 2자리 반올림) |

**참고**
- 개인 계좌가 없으면 `personalAccount`는 `null`로 반환됩니다
- 참여 중인 배틀이 없으면 `battleAccounts`는 빈 배열(`[]`)로 반환됩니다
- `totalAsset`은 스케줄러에 의해 1초마다 갱신됩니다
- 수익금/수익률이 음수이면 손실을 의미합니다

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

**공개 API (인증 불필요)**
- `POST /api/auth/**` (로그인, 토큰 갱신)
- `GET /api/battles`, `GET /api/battles/**` (배틀 조회, 팀 목록, 댓글 목록, 팀원 목록, 배틀 수익률 조회)
- `GET /api/accounts/{accountId}/profit` (계좌 ID로 수익률 조회)
- `GET /api/rankings/**` (랭킹 조회)

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
- `LEFT`: 탈퇴
- `KICKED`: 강퇴

#### OrderType
- `BUY`: 매수
- `SELL`: 매도

#### OrderStatus
- `PENDING`: 미체결
- `FILLED`: 체결
- `CANCELLED`: 취소

#### TradeType
- `BUY`: 매수
- `SELL`: 매도
- `SEED_MONEY`: 시드머니 지급
- `PROFIT_SNAPSHOT`: 수익률 스냅샷 (5분마다 자동 기록, PROGRESS 상태 배틀만 해당)

#### Role
- `GUEST`: 가입 대기 (추가 정보 입력 전)
- `USER`: 일반 회원 (가입 완료)
- `ADMIN`: 관리자

---

## 참고사항

1. 모든 날짜/시간은 ISO 8601 형식을 사용합니다 (예: `2026-01-30T10:00:00`)
2. UUID는 하이픈으로 구분된 36자 형식입니다 (예: `550e8400-e29b-41d4-a716-446655440000`)
3. 금액은 정수형(Long/Integer) 또는 BigDecimal로 처리됩니다
4. 수익률은 Float/Double 타입이며, 퍼센트(%) 단위입니다
5. Time 형식은 `HH:mm:ss` 형식입니다 (예: `15:30:00`)
