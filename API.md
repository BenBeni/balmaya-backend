# API Documentation (balmaya)

## Auth & Access
- Public: `GET /actuator/health`, `POST /auth/login`, `POST /auth/register`
- All other endpoints require `Authorization: Bearer <JWT>`
- Admin read endpoints require roles `admin` or `assistant`

## Endpoints

### POST `/auth/login` (query param: `lang=english|french`, optional; default `en`)
Request `AuthLoginRequest`:
- `username` string, required
- `password` string, required
Response `AuthTokenResponse`:
- `access_token`, `refresh_token`, `expires_in`, `refresh_expires_in`, `token_type`, `scope`
Note: Successful login also generates an email OTP if the user has an email in Keycloak.

### POST `/auth/register`
Request `AuthRegisterRequest`:
- `username`, `email`, `password`, `phoneNumber` required
- `firstName`, `lastName` optional
Response `AuthRegisterResponse`:
- `userId`

### POST `/auth/otp` (query param: `lang=english|french`, optional; default `en`)
Request `EmailOtpRequest`:
- `email`, `userId` required
Response `EmailOtpResponse`:
- `email`, `code`, `expiresAt`

### POST `/auth/otp/resend` (query param: `lang=english|french`, optional; default `en`)
Request `EmailOtpRequest`:
- `email`, `userId` required
Response `EmailOtpResponse`:
- `email`, `code`, `expiresAt`

### POST `/auth/otp/validate`
Request `EmailOtpValidateRequest`:
- `email`, `userId`, `code` required
Response `EmailOtpValidateResponse`:
- `valid`

### GET `/auth/me`
Response `UserProfileResponse`:
- `userId`, `username`, `email`, `firstName`, `lastName`, `phoneNumber`

### GET `/beneficiaries`
Response `List<BeneficiaryResponse>`

### POST `/beneficiaries`
Request `BeneficiaryCreateRequest`:
- `fullName`, `phoneE164`, `provider`, `country`, `categoryRecipients` required
- `relationship`, `changeReason` optional
Response `BeneficiaryResponse`

### PUT `/beneficiaries/{logicalId}`
Request `BeneficiaryUpdateRequest`:
- `fullName`, `phoneE164`, `provider`, `country`, `categoryRecipients`, `changeReason` required
- `relationship`, `status` optional (`ACTIVE`/`INACTIVE`)
Response `BeneficiaryResponse`

### GET `/schedules`
Response `List<ScheduleResponse>`

### POST `/schedules`
Request `ScheduleCreateRequest`:
- `beneficiaryId` (beneficiary logicalId), `frequency`, `startAt`, `timezone`, `currency`, `changeReason`, `items` required
- `weeklyDay`, `monthlyDay` optional
Response `ScheduleResponse`

### PUT `/schedules/{logicalId}`
Request `ScheduleUpdateRequest`:
- `status`, `frequency`, `startAt`, `timezone`, `currency`, `changeReason`, `items` required
- `weeklyDay`, `monthlyDay` optional
Response `ScheduleResponse`

### GET `/payments`
Response `List<PaymentResponse>`

### POST `/payments`
Request `PaymentCreateRequest`:
- `beneficiaryId`, `kind`, `executeAt`, `currency`, `changeReason`, `items` required
- `scheduleId`, `note` optional
Response `PaymentResponse`

### GET `/admin/read/beneficiaries/{logicalId}/versions`
Response `List<BeneficiaryResponse>`

### GET `/admin/read/schedules/{logicalId}/versions`
Response `List<ScheduleResponse>`

### GET `/admin/read/payments/{logicalId}/versions`
Response `List<PaymentResponse>`

## Schemas

### `UserProfileResponse`
- `userId`, `username`, `email`, `firstName`, `lastName`, `phoneNumber`

### `EmailOtpRequest`
- `email`, `userId`

### `EmailOtpResponse`
- `email`, `code`, `expiresAt`

### `EmailOtpValidateRequest`
- `email`, `userId`, `code`

### `EmailOtpValidateResponse`
- `valid`

### `BeneficiaryResponse`
- `id`, `logicalId` (UUID), `version` (int), `isCurrent` (bool)
- `createdAt` (OffsetDateTime), `createdBy`, `changeReason`
- `fullName`, `relationship`, `country`, `provider`, `phoneE164`, `status`
- `categoryRecipients[]` with `category`, `recipientName`, `recipientPhoneE164`, `recipientEmail`, `provider`

### `BeneficiaryCategoryRecipient`
- `category`, `recipientName`, `recipientPhoneE164`, `recipientEmail`, `recipientAccountNumber`, `provider`

### `ScheduleResponse`
- `id`, `logicalId`, `version`, `isCurrent`
- `beneficiaryId` (beneficiary logicalId), `status`, `frequency`, `timezone`, `startAt`, `weeklyDay`, `monthlyDay`, `nextRunAt`
- `netAmount` (long), `currency`
- `items[]` with `id`, `category`, `amount`, `currency`, `method`

### `PaymentResponse`
- `id`, `logicalId`, `version`, `isCurrent`
- `beneficiaryId`, `kind`, `scheduleId`, `executeAt`
- `netAmount`, `feeAmount`, `grossAmount`, `currency`
- `status`, `note`
- `items[]` with `id`, `category`, `amount`, `currency`, `method`, `status`
- `fees[]` with `id`, `code`, `amount`, `currency`, `direction`

## Enums
- `Provider`: `WAVE`, `ORANGE_MONEY`
- `BeneficiaryStatus`: `ACTIVE`, `INACTIVE`
- `ScheduleStatus`: `ACTIVE`, `PAUSED`, `CANCELLED`
- `Frequency`: `WEEKLY`, `MONTHLY`
- `PaymentKind`: `ONE_SHOT`, `SCHEDULED_RUN`
- `PaymentStatus`: `CREATED`, `AWAITING_FUNDS`, `FUNDED`, `PROCESSING`, `COMPLETED`, `PARTIAL`, `FAILED`, `CANCELLED`
- `ItemCategory`: `CASH`, `FOOD`, `RENT`, `UTILITIES`, `ELECTRICITY`, `WATER`, `SCHOOL`, `OTHER`
- `ItemMethod`: `WALLET_PAYOUT`, `BILL_PAY`
- `ItemStatus`: `CREATED`, `PROCESSING`, `SUCCEEDED`, `FAILED`
- `FeeCode`: `SERVICE_FEE`, `PROCESSOR_FEE`, `FX_MARGIN`, `NETWORK_FEE`, `DISPUTE_FEE`, `OTHER`
- `FeeDirection`: `CHARGE`, `COST`, `REVENUE`


