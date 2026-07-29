use crate::{dashboard::DashboardPage, login::LoginPage, session::AuthSession};
use leptos::prelude::*;
use leptos_router::{components::*, path};

#[component]
pub fn App() -> impl IntoView {
    let session = RwSignal::new(None::<AuthSession>);

    provide_context(session);

    view! {
        <Router>
            <Routes fallback=|| view! { <Redirect path="/" /> }>
                <Route path=path!("/") view=LoginRoute />
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
