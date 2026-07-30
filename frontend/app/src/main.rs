use frontend::api::client::ApiClient;
use frontend::dashboard::DashboardPage;
use frontend::login::LoginPage;
use frontend::session::AuthSession;
use leptos::prelude::*;
use leptos_router::{components::*, path};
use std::sync::Arc;

fn main() {
    console_error_panic_hook::set_once();
    mount_to_body(App)
}

#[component]
pub fn App() -> impl IntoView {
    let session = RwSignal::new(None::<AuthSession>);
    let client = Arc::new(ApiClient::new());

    provide_context(session);
    provide_context(client);

    view! {
        <Router>
            <Routes fallback=|| view! { <Redirect path="/" /> }>
                <Route
                    path=path!("/")
                    view=move || view! { <LoginRoute /> }
                />
                <Route path=path!("/dashboard") view=DashboardRoute />
            </Routes>
        </Router>
    }
}

#[component]
fn LoginRoute() -> impl IntoView {
    let session = expect_context::<RwSignal<Option<AuthSession>>>();

    view! {
        <Show
            when=move || session.with(Option::is_none)
            fallback=|| view! { <Redirect path="/dashboard" /> }
        >
            <LoginPage />
        </Show>
    }
    .into_any()
}

#[component]
fn DashboardRoute() -> impl IntoView {
    let session = expect_context::<RwSignal<Option<AuthSession>>>();

    view! {
        <Show
            when=move || session.with(Option::is_some)
            fallback=|| view! { <Redirect path="/" /> }
        >
            <DashboardPage />
        </Show>
    }
    .into_any()
}
