use backend_api::{
    DEFAULT_BASE_URL, ErrorMessageDto, GetGroupsResponse, LoginRequest, LoginResponse,
};
use gloo_net::http::{Request, Response};
use serde::de::DeserializeOwned;
use web_sys::RequestCredentials;

use crate::error::ApiError;

#[derive(Clone)]
pub struct ApiClient {
    base_url: String,
}

impl ApiClient {
    pub fn new() -> Self {
        Self {
            base_url: DEFAULT_BASE_URL.to_owned(),
        }
    }

    pub async fn login(&self, email: String, password: String) -> Result<LoginResponse, ApiError> {
        let request = LoginRequest::new(email, password);

        let response = Request::post(&format!("{}/api/login", self.base_url))
            .credentials(RequestCredentials::Include)
            .json(&request)
            .map_err(network_error)?
            .send()
            .await
            .map_err(network_error)?;

        decode_response(response).await
    }

    pub async fn get_groups(&self, ids: &[&str]) -> Result<GetGroupsResponse, ApiError> {
        let response = Request::get(&format!("{}/api/group", self.base_url))
            .query([("ids", ids.join(","))])
            .credentials(RequestCredentials::Include)
            .send()
            .await
            .map_err(network_error)?;

        decode_response(response).await
    }
}

async fn decode_response<T: DeserializeOwned>(response: Response) -> Result<T, ApiError> {
    if response.ok() {
        response.json().await.map_err(network_error)
    } else {
        let status = response.status();
        let message = response
            .json::<ErrorMessageDto>()
            .await
            .ok()
            .and_then(|body| body.message.flatten())
            .unwrap_or_else(|| format!("Request failed with status {status}"));

        Err(ApiError::Network(message))
    }
}

fn network_error(error: impl std::fmt::Display) -> ApiError {
    ApiError::Network(error.to_string())
}
