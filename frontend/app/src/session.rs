use backend_api::UserDto;

#[derive(Clone, Debug)]
pub struct AuthSession {
    pub user: UserDto,
}