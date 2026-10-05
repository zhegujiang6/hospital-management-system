from fastapi import APIRouter

from app.api.routes.health import router as health_router

from app.api.routes.conversations import router as conversation_router

from app.api.routes.admin_conversations import (
    router as admin_conversation_router,
)

api_router = APIRouter()

api_router.include_router(health_router)

api_router.include_router(conversation_router)
api_router.include_router(admin_conversation_router)

