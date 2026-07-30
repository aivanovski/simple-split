use crate::session::AuthSession;
use leptos::prelude::*;
use leptos_router::{NavigateOptions, hooks::use_navigate};

#[component]
pub fn DashboardPage() -> impl IntoView {
    let session = expect_context::<RwSignal<Option<AuthSession>>>();
    
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

                <button class="secondary-button" type="button" on:click=logout>
                    "Logout"
                </button>
            </div>
        </main>
    }
}
