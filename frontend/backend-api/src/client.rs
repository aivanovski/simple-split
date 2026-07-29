use crate::{
    ErrorMessageDto, LoginRequest, LoginResponse, RefreshTokenRequest, RefreshTokenResponse,
    SignupRequest, SignupResponse, auth,
};
use gloo_net::http::Request;
use serde::{Serialize, de::DeserializeOwned};
use thiserror::Error;

#[derive(Clone, Debug)]
pub struct ApiClient {
    base_url: String,
}

impl ApiClient {
    pub fn new(base_url: impl Into<String>) -> Self {
        Self {
            base_url: base_url.into().trim_end_matches('/').to_owned(),
        }
    }

    pub fn base_url(&self) -> &str {
        &self.base_url
    }

    pub async fn login(&self, request: &LoginRequest) -> Result<LoginResponse, ApiError> {
        self.post(auth::LOGIN_PATH, request).await
    }

    pub async fn signup(&self, request: &SignupRequest) -> Result<SignupResponse, ApiError> {
        self.post(auth::SIGNUP_PATH, request).await
    }

    pub async fn refresh_token(
        &self,
        request: &RefreshTokenRequest,
    ) -> Result<RefreshTokenResponse, ApiError> {
        self.post(auth::REFRESH_TOKEN_PATH, request).await
    }

    async fn post<Req, Resp>(&self, path: &str, body: &Req) -> Result<Resp, ApiError>
    where
        Req: Serialize + ?Sized,
        Resp: DeserializeOwned,
    {
        let payload =
            serde_json::to_string(body).map_err(|error| ApiError::Json(error.to_string()))?;
        let url = format!("{}{}", self.base_url, path);
        let response = Request::post(&url)
            .header("Content-Type", "application/json")
            .body(payload)
            .map_err(|error| ApiError::RequestBuild(error.to_string()))?
            .send()
            .await
            .map_err(|error| ApiError::Network(error.to_string()))?;
        let status = response.status();
        let payload = response
            .binary()
            .await
            .map_err(|error| ApiError::Network(error.to_string()))?;

        if (200..300).contains(&status) {
            return serde_json::from_slice::<Resp>(&payload)
                .map_err(|error| ApiError::Json(error.to_string()));
        }

        let message = serde_json::from_slice::<ErrorMessageDto>(&payload)
            .ok()
            .and_then(|dto| dto.message)
            .filter(|message| !message.trim().is_empty())
            .unwrap_or_else(|| "request failed".to_owned());

        Err(ApiError::Server { status, message })
    }
}

#[derive(Debug, Error)]
pub enum ApiError {
    #[error("failed to construct request: {0}")]
    RequestBuild(String),

    #[error("network request failed: {0}")]
    Network(String),

    #[error("failed to encode or decode json: {0}")]
    Json(String),

    #[error("server returned {status}: {message}")]
    Server { status: u16, message: String },
}
