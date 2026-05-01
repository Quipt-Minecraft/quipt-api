# Quipt API — REST Controller Documentation

---

## 1. `AccountController` — `/account`

Handles user account management including registration, email verification, and account editing.

---

### `GET /account/verify`

Verifies a user's email address using a token sent during registration.

| Parameter | Type        | Required | Description                  |
|-----------|-------------|----------|------------------------------|
| `token`   | Query param | ✅        | The verification token ID    |
| `email`   | Query param | ✅        | The email address to verify  |

**Behavior:**
- Looks up the token by its SHA-256 hash.
- Validates the token exists, is associated with an account, and the provided email matches.
- On success: saves the account to persistent storage, removes the token from the pending verification map, and returns a success message.

---

### `POST /account/register`

Registers a new user account and sends a verification email.

**Request Body (JSON):**

| Field      | Type   | Required | Description                                                                                      |
|------------|--------|----------|--------------------------------------------------------------------------------------------------|
| `username` | String | ✅        | Desired username                                                                                 |
| `password` | String | ✅        | Account password (stored as SHA-256 hash)                                                        |
| `email`    | String | ✅        | Email address (must end in an allowed TLD: `.com`, `.net`, `.org`, `.live`, `.io`, `.dev`, `.app`) |

**Behavior:**
- Validates email format and TLD.
- Checks for username and email uniqueness in account storage.
- Creates a pending `AccountData` object and a verification token.
- Sends a verification email. If sending fails, the pending token is cleaned up and an error is returned.
- The account is **not** saved until the email is verified via `GET /account/verify`.

---

### `* /account/edit` (any HTTP method)

Edits account data. Requires authentication via `Authorization` header.

**Request Body (JSON):**

| Field    | Type   | Required | Description                              |
|----------|--------|----------|------------------------------------------|
| `action` | String | ✅        | The edit action to perform (see below)   |

#### Action: `add_permission`

Grants a permission to another user. The caller must already have the permission themselves.

| Field        | Type   | Required | Description                  |
|--------------|--------|----------|------------------------------|
| `permission` | String | ✅        | The permission to grant      |
| `user`       | String | ✅        | The target username          |

#### Action: `create_token`

Creates an API token scoped to a subset of the caller's permissions.

| Field         | Type     | Required | Description                                                                           |
|---------------|----------|----------|---------------------------------------------------------------------------------------|
| `description` | String   | ✅        | A description for the token                                                           |
| `permissions` | String[] | ✅        | List of permissions to attach (only permissions the caller holds are applied)         |

Returns the raw token, description, permissions applied, and any invalid permissions in the response.

---

## 2. `TokenController` — `/token`

Handles API token validation.

---

### `GET /token/validate`

Validates an API token provided in the `Authorization` header.

| Header          | Required | Description               |
|-----------------|----------|---------------------------|
| `Authorization` | ✅        | The API token to validate |

**Behavior:**
- Delegates to `Utils.validateAuthorizationHeader()`.
- Returns success if the token is valid, or a failure response if not.

---

## 3. `FileController` — `/files`

Handles file uploads and downloads.

---

### `POST /files/upload`

Uploads a single file. Requires authentication.

| Input           | Type      | Required | Description                                                      |
|-----------------|-----------|----------|------------------------------------------------------------------|
| `Authorization` | Header    | ✅        | API token                                                        |
| `file`          | Multipart | ✅        | The file to upload                                               |
| `path`          | Query param | ❌      | Sub-directory within the uploads folder (defaults to root)       |

**Behavior:**
- Saves the file to `<app_folder>/uploads/<path>/<sanitized_filename>`.
- Filenames are sanitized: only alphanumeric characters, `.`, `_`, and `-` are allowed; all others are replaced with `_`.
- Returns the saved filename and path on success.

---

### `POST /files/upload-multiple`

Uploads multiple files in a single request. Requires authentication.

| Input           | Type        | Required | Description                                          |
|-----------------|-------------|----------|------------------------------------------------------|
| `Authorization` | Header      | ✅        | API token                                            |
| `files`         | Multipart[] | ✅        | One or more files to upload                          |
| `path`          | Query param | ❌        | Sub-directory within the uploads folder              |

**Behavior:**
- Attempts to save each file individually.
- Returns a list of successfully uploaded filenames and a list of failed filenames with error messages.

---

### `GET /files/download/**`

Downloads a file by path.

| Input             | Type     | Description                                                    |
|-------------------|----------|----------------------------------------------------------------|
| `**` (path)       | URL path | Relative path to the file within the uploads directory         |

**Behavior:**
- Resolves the file path against the uploads directory.
- Prevents path traversal attacks by verifying the resolved path stays within the uploads directory.
- Probes the file's content type and streams it as a download with the `Content-Disposition: attachment` header.
- Returns `404` if the file does not exist or is not readable.

---

## 4. `DataController` — `/data`

Handles server data/update operations.

---

### `* /data/update` (any HTTP method)

Triggers a server restart/update. Requires authentication.

| Header          | Required | Description   |
|-----------------|----------|---------------|
| `Authorization` | ✅        | API token     |

**Behavior:**
- Validates the authorization token.
- Schedules an async task to call `System.exit(0)` after 2 seconds, effectively restarting the process (presumably managed by an external process manager).
- Returns a success message immediately.

---

### `* /data/download` (any HTTP method)

> ⚠️ Currently a stub — returns the plain string `"download"`. Not yet implemented.

