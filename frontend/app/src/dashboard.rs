use crate::api::client::ApiClient;
use crate::session::AuthSession;
use backend_api::GetGroupsResponse;
use leptos::prelude::*;
use leptos::task::spawn_local;
use leptos_router::{NavigateOptions, hooks::use_navigate};
use std::sync::Arc;

const FIRST_TEST_GROUP_ID: &str = "00000000-0000-0000-0000-b00000000001";

#[component]
pub fn DashboardPage() -> impl IntoView {
    let session = expect_context::<RwSignal<Option<AuthSession>>>();
    let client = expect_context::<Arc<ApiClient>>();
    let groups = RwSignal::new(None::<GetGroupsResponse>);
    let error = RwSignal::new(None::<String>);

    spawn_local(async move {
        match client.get_groups(&[FIRST_TEST_GROUP_ID]).await {
            Ok(response) => groups.set(Some(response)),
            Err(request_error) => error.set(Some(request_error.to_string())),
        }
    });

    let navigate = use_navigate();
    let active_session = move || {
        session
            .get()
            .expect("dashboard route is only mounted for authenticated sessions")
    };

    let logout = move |_| {
        session.set(None);
        navigate("/", NavigateOptions::default());
    };

    view! {
        <main class="dashboard-shell">
            <div class="dashboard-content">
                <p>{move || format!("Logged in as {}", active_session().user.name)}</p>

                <pre>
                    {move || {
                        if let Some(response) = groups.get() {
                            format!("{response:#?}")
                        } else if let Some(message) = error.get() {
                            format!("Failed to load groups: {message}")
                        } else {
                            "Loading groups...".to_owned()
                        }
                    }}
                </pre>

                <button class="secondary-button" type="button" on:click=logout>
                    "Logout"
                </button>
            </div>
        </main>
    }
}
