use backend_api::UserDto;
use serde::{Deserialize, Serialize};

const AUTH_SESSION_STORAGE_KEY: &str = "simple-split.auth-session";

#[derive(Clone, Debug, Deserialize, Serialize)]
pub struct AuthSession {
    pub user: UserDto,
}

impl AuthSession {
    pub fn load() -> Option<Self> {
        let storage = web_sys::window()?.local_storage().ok()??;
        let serialized = storage.get_item(AUTH_SESSION_STORAGE_KEY).ok()??;

        serde_json::from_str(&serialized).ok()
    }

    pub fn save(&self) {
        let Some(storage) = web_sys::window()
            .and_then(|window| window.local_storage().ok())
            .flatten()
        else {
            return;
        };
        let Ok(serialized) = serde_json::to_string(self) else {
            return;
        };

        let _ = storage.set_item(AUTH_SESSION_STORAGE_KEY, &serialized);
    }

    pub fn clear() {
        let Some(storage) = web_sys::window()
            .and_then(|window| window.local_storage().ok())
            .flatten()
        else {
            return;
        };

        let _ = storage.remove_item(AUTH_SESSION_STORAGE_KEY);
    }
}
