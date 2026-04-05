# Favorites API - Frontend Integration Guide

Base URL:
- http://localhost:8080/api/publications

Auth model used today:
- Header required in private endpoints: X-User-Id: <USER_ID>
- Note: this backend still uses header-based identity (no JWT validation yet).

## 1) Add publication to favorites

Endpoint:
- POST /api/publications/{publicationId}/favorite

Headers:
- Content-Type: application/json
- X-User-Id: 2

Body:
- No body

Success response (200):
```json
{
  "publicationId": 10,
  "userId": 2,
  "favorite": true,
  "favoritesCount": 4
}
```

Validation rules:
- Publication must exist.
- User must exist.
- User cannot favorite own publication.
- If it was already favorited, endpoint is idempotent: favorite stays true.

Common error response (400):
```text
Error: You cannot favorite your own publication
```

## 2) Remove publication from favorites

Endpoint:
- DELETE /api/publications/{publicationId}/favorite

Headers:
- X-User-Id: 2

Body:
- No body

Success response (200):
```json
{
  "publicationId": 10,
  "userId": 2,
  "favorite": false,
  "favoritesCount": 3
}
```

Behavior:
- If the favorite did not exist, result is still favorite=false (safe to call multiple times).

## 3) Get favorite status for one publication

Endpoint:
- GET /api/publications/{publicationId}/favorite/status

Headers:
- X-User-Id: 2

Body:
- No body

Success response (200):
```json
{
  "publicationId": 10,
  "userId": 2,
  "favorite": true,
  "favoritesCount": 4
}
```

Use cases in frontend:
- Render active/inactive heart icon.
- Render total favorites counter for publication detail.

## 4) List favorites of logged user

Endpoint:
- GET /api/publications/my-favorites

Headers:
- X-User-Id: 2

Success response (200):
```json
[
  {
    "id": 10,
    "title": "Fender Stratocaster Mexico",
    "description": "Very good condition",
    "price": 1200.00,
    "category": "ELECTRIC_GUITAR",
    "condition": "USED",
    "brand": "Fender",
    "year": 2019,
    "location": "Buenos Aires",
    "images": [
      "https://res.cloudinary.com/..."
    ],
    "status": "ACTIVE",
    "userId": 7,
    "userName": "Seller Name",
    "createdAt": "2026-04-04T18:12:33",
    "updatedAt": null,
    "soldAt": null,
    "viewsCount": 30
  }
]
```

Behavior:
- Returns publications in favorite order (most recent favorite first).
- DELETED publications are filtered out.

## 5) Toggle favorite (recommended)

Endpoint:
- POST /api/publications/{publicationId}/favorite/toggle

Headers:
- X-User-Id: 2

Body:
- No body

Success response (200) when publication becomes favorite:
```json
{
  "publicationId": 10,
  "userId": 2,
  "favorite": true,
  "favoritesCount": 4
}
```

Success response (200) when publication is removed from favorites:
```json
{
  "publicationId": 10,
  "userId": 2,
  "favorite": false,
  "favoritesCount": 3
}
```

Behavior:
- If favorite exists, toggle removes it.
- If favorite does not exist, toggle creates it.
- Same business rules as add/remove apply (cannot favorite own publication, publication must exist).

## Frontend flow recommendation

Detail screen (simplest with toggle):
1. GET /api/publications/{id}
2. GET /api/publications/{id}/favorite/status
3. On heart click call POST /api/publications/{id}/favorite/toggle
4. Update icon and count from returned favorite and favoritesCount

Alternative without toggle:
1. GET /api/publications/{id}/favorite/status
2. If favorite=false -> POST /api/publications/{id}/favorite
3. If favorite=true -> DELETE /api/publications/{id}/favorite

My Favorites screen:
1. GET /api/publications/my-favorites
2. Render cards exactly like marketplace cards
3. Optional: remove with DELETE /{id}/favorite or use POST /{id}/favorite/toggle

## cURL examples

Add favorite:
```bash
curl -X POST "http://localhost:8080/api/publications/10/favorite" \
  -H "X-User-Id: 2"
```

Remove favorite:
```bash
curl -X DELETE "http://localhost:8080/api/publications/10/favorite" \
  -H "X-User-Id: 2"
```

Get status:
```bash
curl "http://localhost:8080/api/publications/10/favorite/status" \
  -H "X-User-Id: 2"
```

List my favorites:
```bash
curl "http://localhost:8080/api/publications/my-favorites" \
  -H "X-User-Id: 2"
```

Toggle favorite:
```bash
curl -X POST "http://localhost:8080/api/publications/10/favorite/toggle" \
  -H "X-User-Id: 2"
```
