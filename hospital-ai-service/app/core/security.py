import base64
import binascii
from typing import Annotated, Any

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import (
    HTTPAuthorizationCredentials,
    HTTPBearer,
)
from jwt import ExpiredSignatureError, InvalidTokenError
from pydantic import BaseModel

from app.core.config import settings


class CurrentUser(BaseModel):
    user_id: int
    username: str
    role: str
    patient_id: int | None = None
    doctor_id: int | None = None


bearer_scheme = HTTPBearer(auto_error=False)


def create_unauthorized_exception(message: str) -> HTTPException:
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=message,
        headers={
            "WWW-Authenticate": "Bearer",
        },
    )


def decode_signing_key() -> bytes:
    secret = settings.jwt_secret.get_secret_value()

    try:
        signing_key = base64.b64decode(
            secret,
            validate=True,
        )
    except (ValueError, binascii.Error) as exception:
        raise RuntimeError(
            "JWT_SECRET不是有效的Base64密钥"
        ) from exception

    if len(signing_key) < 32:
        raise RuntimeError(
            "JWT_SECRET解码后不能少于32字节"
        )

    return signing_key


jwt_signing_key = decode_signing_key()


def optional_positive_int(value: Any) -> int | None:
    if value is None:
        return None

    number = int(value)

    if number <= 0:
        return None

    return number


async def get_current_user(
        credentials: Annotated[
            HTTPAuthorizationCredentials | None,
            Depends(bearer_scheme),
        ],
) -> CurrentUser:
    if credentials is None:
        raise create_unauthorized_exception("请先登录")

    try:
        payload = jwt.decode(
            credentials.credentials,
            jwt_signing_key,
            algorithms=[settings.jwt_algorithm],
            options={
                "require": [
                    "exp",
                    "iat",
                    "sub",
                    "userId",
                    "role",
                ],
            },
        )

        user_id = int(payload["userId"])
        username = str(payload["sub"]).strip()
        role = str(payload["role"]).strip().upper()

        patient_id = optional_positive_int(
            payload.get("patientId")
        )

        doctor_id = optional_positive_int(
            payload.get("doctorId")
        )

    except ExpiredSignatureError as exception:
        raise create_unauthorized_exception(
            "登录状态已过期，请重新登录"
        ) from exception

    except (
            InvalidTokenError,
            KeyError,
            TypeError,
            ValueError,
    ) as exception:
        raise create_unauthorized_exception(
            "登录令牌无效"
        ) from exception

    if user_id <= 0 or not username:
        raise create_unauthorized_exception(
            "登录身份信息不完整"
        )

    allowed_roles = {
        "ADMIN",
        "DOCTOR",
        "PATIENT",
    }

    if role not in allowed_roles:
        raise create_unauthorized_exception(
            "登录角色无效"
        )

    if role == "PATIENT" and patient_id is None:
        raise create_unauthorized_exception(
            "患者身份信息不完整"
        )

    if role == "DOCTOR" and doctor_id is None:
        raise create_unauthorized_exception(
            "医生身份信息不完整"
        )

    return CurrentUser(
        user_id=user_id,
        username=username,
        role=role,
        patient_id=patient_id,
        doctor_id=doctor_id,
    )