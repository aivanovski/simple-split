use backend_api::{
    DEFAULT_BASE_URL, ErrorMessageDto, GetCurrenciesResponse, GetGroupsResponse, LoginRequest,
    LoginResponse, PostExpenseRequest, PostExpenseResponse, PostGroupRequest, PostGroupResponse,
    PutExpenseRequest, PutExpenseResponse, SignupRequest, SignupResponse,
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

    pub async fn signup(
        &self,
        name: String,
        email: String,
        password: String,
    ) -> Result<SignupResponse, ApiError> {
        let request = SignupRequest::new(name, email, password);

        let response = Request::post(&format!("{}/api/signup", self.base_url))
            .credentials(RequestCredentials::Include)
            .json(&request)
            .map_err(network_error)?
            .send()
            .await
            .map_err(network_error)?;

        decode_response(response).await
    }

    pub async fn get_groups(&self) -> Result<GetGroupsResponse, ApiError> {
        let response = Request::get(&format!("{}/api/group", self.base_url))
            .credentials(RequestCredentials::Include)
            .send()
            .await
            .map_err(network_error)?;

        decode_response(response).await
    }

    pub async fn get_currencies(&self) -> Result<GetCurrenciesResponse, ApiError> {
        let response = Request::get(&format!("{}/api/currency", self.base_url))
            .send()
            .await
            .map_err(network_error)?;

        decode_response(response).await
    }

    pub async fn create_group(
        &self,
        request: PostGroupRequest,
    ) -> Result<PostGroupResponse, ApiError> {
        let response = Request::post(&format!("{}/api/group", self.base_url))
            .credentials(RequestCredentials::Include)
            .json(&request)
            .map_err(network_error)?
            .send()
            .await
            .map_err(network_error)?;

        decode_response(response).await
    }

    pub async fn create_expense(
        &self,
        request: PostExpenseRequest,
    ) -> Result<PostExpenseResponse, ApiError> {
        let response = Request::post(&format!("{}/api/expense", self.base_url))
            .credentials(RequestCredentials::Include)
            .json(&request)
            .map_err(network_error)?
            .send()
            .await
            .map_err(network_error)?;

        decode_response(response).await
    }

    pub async fn update_expense(
        &self,
        expense_uid: &str,
        request: PutExpenseRequest,
    ) -> Result<PutExpenseResponse, ApiError> {
        let response = Request::put(&format!("{}/api/expense/{}", self.base_url, expense_uid))
            .credentials(RequestCredentials::Include)
            .json(&request)
            .map_err(network_error)?
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
