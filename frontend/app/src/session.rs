use backend_api::{LoginResponse, UserDto};

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct AuthSession {
    pub token: String,
    pub refresh_token: String,
    pub user: UserDto,
}

impl From<LoginResponse> for AuthSession {
    fn from(response: LoginResponse) -> Self {
        Self {
            token: response.token,
            refresh_token: response.refresh_token,
            user: response.user,
        }
    }
}
