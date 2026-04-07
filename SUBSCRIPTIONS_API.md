# Subscriptions and Monetization API Guide

This document is the single source of truth for frontend integration of subscription plans, limits, and seller monetization.

## Overview

Base URLs:
- Users API: http://localhost:8080/api/users
- Publications API: http://localhost:8080/api/publications

Current auth model:
- Private endpoints require header X-User-Id: <USER_ID>
- Subscription endpoints are owner-only:
  - Path userId must match X-User-Id
  - If mismatch, backend returns 400

Business model:
- FREE: max 3 ACTIVE publications, USD 0/month
- Extra post slot: +1 publication, USD 1/month each (max suggested: 5 before recommending HOBBY)
- HOBBY: max 15 ACTIVE publications, USD 5/month
- BUSINESS: unlimited ACTIVE publications, USD 18/month
- Paid plan duration: 1 month
- Grace period: 5 days after paid plan expiration
- After grace period: automatic downgrade to FREE

Important rules:
- Publication limits apply to ACTIVE publications only.
- Limit is validated both when creating a publication and when reactivating a publication to ACTIVE.
- The real limit is: maxActivePosts = planLimit + extraPostsPurchased
- extraPostsPurchased is a field on the user object (default 0). Future feature: users will be able to purchase extra slots.

## Enums and Contracts

Plan values:
- FREE
- HOBBY
- BUSINESS

Subscription status values:
- NONE: free user without active paid subscription
- ACTIVE: paid and valid
- GRACE_PERIOD: paid plan expired, still inside grace period
- EXPIRED: grace period ended, downgraded to FREE
- CANCELLED: user manually cancelled and is now FREE

## Common Response Model

SubscriptionResponse:

```json
{
  "userId": 2,
  "plan": "HOBBY",
  "status": "ACTIVE",
  "maxActivePublications": 15,
  "monthlyPriceUsd": 5,
  "subscriptionStartDate": "2026-04-06T20:00:00",
  "subscriptionEndDate": "2026-05-06T20:00:00",
  "gracePeriodEndDate": null,
  "canCreateMorePublications": true
}
```

Notes:
- Dates are ISO-8601 LocalDateTime.
- canCreateMorePublications currently returns true in backend response model; frontend should rely on publication-create errors for hard blocking.
- maxActivePublications reflects planLimit only. To get the real limit, calculate: planLimit + user.extraPostsPurchased.

## Error Format

Current error format for business errors:

```text
Error: <message>
```

Most business validation errors return HTTP 400.

## Endpoints

### 1) Get subscription plan catalog (public)

Endpoint:
- GET /api/users/subscription/plans

Headers:
- None

Success response (200):

```json
[
  {
    "plan": "FREE",
    "maxActivePublications": 3,
    "monthlyPriceUsd": 0,
    "description": "Free plan with up to 3 active publications"
  },
  {
    "plan": "HOBBY",
    "maxActivePublications": 15,
    "monthlyPriceUsd": 5,
    "description": "Hobby plan with up to 15 active publications"
  },
  {
    "plan": "BUSINESS",
    "maxActivePublications": -1,
    "monthlyPriceUsd": 18,
    "description": "Business plan with unlimited active publications"
  }
]
```

Frontend use:
- Render pricing cards dynamically from backend values.
- Do not hardcode prices and limits in frontend.
- If maxActivePublications is -1, render label "Unlimited" (or "Ilimitado").

### 2) Get my subscription status

Endpoint:
- GET /api/users/{userId}/subscription

Headers:
- X-User-Id: 2

Success response (200):
- SubscriptionResponse (see Common Response Model)

Common errors:
- 400: owner mismatch
- 400: user not found

Recommended frontend behavior:
- Call on app boot/login refresh.
- Use plan/status to show current tier badge and CTA state.

### 3) Change subscription plan

Endpoint:
- PUT /api/users/{userId}/subscription/plan

Headers:
- Content-Type: application/json
- X-User-Id: 2

Body:

```json
{
  "plan": "HOBBY"
}
```

Allowed plan values:
- FREE
- HOBBY
- BUSINESS

Success response (200):
- SubscriptionResponse

Behavior:
- FREE:
  - Plan switches to FREE
  - Status becomes NONE
  - Paid dates are cleared
- HOBBY / BUSINESS:
  - Status becomes ACTIVE
  - Start date = now
  - End date = now + 1 month

Important:
- This endpoint currently simulates successful payment (no payment gateway yet).

Common errors:
- 400: Plan is required
- 400: owner mismatch
- 400: user not found

### 4) Renew paid subscription

Endpoint:
- POST /api/users/{userId}/subscription/renew

Headers:
- X-User-Id: 2

Body:
- No body

Success response (200):
- SubscriptionResponse

Behavior:
- Extends current paid plan by +1 month.
- If expired long ago, renew starts from now.

Common errors:
- 400: Free plan cannot be renewed. Choose a paid plan first
- 400: owner mismatch
- 400: user not found

### 5) Cancel subscription (downgrade to FREE)

Endpoint:
- POST /api/users/{userId}/subscription/cancel

Headers:
- X-User-Id: 2

Body:
- No body

Success response (200):
- SubscriptionResponse

Behavior:
- Plan becomes FREE immediately.
- Status becomes CANCELLED.
- Paid dates are cleared.

Common errors:
- 400: owner mismatch
- 400: user not found

## Publication Limit Enforcement

Limit applies in these operations:
- Create publication: POST /api/publications
- Reactivate publication: PATCH /api/publications/{id}/status?status=ACTIVE

If limit is reached, backend returns 400:

```text
Error: Plan FREE limit reached: maximum 3 active publications. Upgrade your subscription to publish more.
```

Note: the number in the message reflects planLimit + extraPostsPurchased, so it may be higher than 3 if the user has purchased extra slots.

Frontend recommendation:
- Catch this message and open paywall modal.
- Offer upgrade paths from GET /api/users/subscription/plans.
- On successful plan change, retry the failed action.

## Frontend Integration Flows

### Flow A: Create publication with paywall fallback

1. Optional pre-check:
   - GET /api/users/{id}/subscription
2. User submits publication form:
   - POST /api/publications
3. If success:
   - show success UI
4. If 400 limit error:
   - open paywall modal
   - load plan cards from GET /api/users/subscription/plans
   - user selects new plan
   - call PUT /api/users/{id}/subscription/plan
   - on success, retry POST /api/publications

### Flow B: Subscription management screen

1. Load plans:
   - GET /api/users/subscription/plans
2. Load current user subscription:
   - GET /api/users/{id}/subscription
3. Render:
   - current plan card
   - expiration info
   - status badge
   - actions (upgrade, renew, cancel)
4. Action buttons:
   - Upgrade/Downgrade: PUT /api/users/{id}/subscription/plan
   - Renew: POST /api/users/{id}/subscription/renew
   - Cancel: POST /api/users/{id}/subscription/cancel

### Flow C: Handle expired subscriptions

1. User opens app after long inactivity.
2. Frontend calls GET /api/users/{id}/subscription.
3. Backend may normalize state to EXPIRED and downgrade to FREE.
4. Frontend should:
   - show plan FREE
   - show message: plan expired, upgrade to publish more

## UI Copy Suggestions

Limit reached modal title:
- You reached your publication limit

Limit reached modal body:
- Your current plan allows up to X active publications. Upgrade to publish more.
- Note: X should be planLimit + extraPostsPurchased, not just planLimit.

Success toast after upgrade:
- Plan updated successfully. You can continue publishing.

## cURL Testing Commands

Get plans:

```bash
curl "http://localhost:8080/api/users/subscription/plans"
```

Get my subscription:

```bash
curl "http://localhost:8080/api/users/2/subscription" \
  -H "X-User-Id: 2"
```

Change plan to hobby:

```bash
curl -X PUT "http://localhost:8080/api/users/2/subscription/plan" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 2" \
  -d '{"plan":"HOBBY"}'
```

Change plan to business:

```bash
curl -X PUT "http://localhost:8080/api/users/2/subscription/plan" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 2" \
  -d '{"plan":"BUSINESS"}'
```

Downgrade to free:

```bash
curl -X PUT "http://localhost:8080/api/users/2/subscription/plan" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 2" \
  -d '{"plan":"FREE"}'
```

Renew:

```bash
curl -X POST "http://localhost:8080/api/users/2/subscription/renew" \
  -H "X-User-Id: 2"
```

Cancel:

```bash
curl -X POST "http://localhost:8080/api/users/2/subscription/cancel" \
  -H "X-User-Id: 2"
```

Create publication with free-plan user (limit test):

```bash
curl -X POST "http://localhost:8080/api/publications" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 2" \
  -d '{"title":"Publicacion de prueba","description":"Descripcion de prueba","price":1000,"category":"ACOUSTIC_GUITAR","condition":"GOOD","location":"Buenos Aires"}'
```

## Troubleshooting

If GET /api/users/subscription/plans returns 404:
- Backend running instance is outdated.
- Restart backend with latest code.

If user can still exceed FREE limit:
- Verify user really has plan FREE with GET /api/users/{id}/subscription.
- Verify publications are ACTIVE (only ACTIVE counts).
- Verify you restarted backend after latest changes.
- Remember: effective limit is planLimit + extraPostsPurchased. If the user bought extra slots, the limit will be higher than 3.
