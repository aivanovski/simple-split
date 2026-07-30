use crate::api::client::ApiClient;
use crate::session::AuthSession;
use leptos::task::spawn_local;
use leptos::{ev::SubmitEvent, prelude::*};
use leptos_router::{NavigateOptions, hooks::use_navigate};
use std::sync::Arc;

#[derive(Clone, Copy)]
struct LoginState {
    email: RwSignal<String>,
    password: RwSignal<String>,
    error: RwSignal<Option<String>>,
    is_loading: RwSignal<bool>,
}

impl LoginState {
    fn new() -> Self {
        Self {
            email: RwSignal::new(String::new()),
            password: RwSignal::new(String::new()),
            error: RwSignal::new(None),
            is_loading: RwSignal::new(false),
        }
    }
}

#[component]
pub fn LoginPage() -> impl IntoView {
    let state = LoginState::new();
    let navigate = use_navigate();
    let session = expect_context::<RwSignal<Option<AuthSession>>>();
    let client = expect_context::<Arc<ApiClient>>();

    view! {
        <main class="auth-shell">
            <form
                class="login-form"
                on:submit=move |event| submit_login(
                    event,
                    state,
                    client.clone(),
                    session,
                    navigate.clone(),
                )
                novalidate
            >
                <label class="field">
                    <span class="field-label">"Email"</span>
                    <input
                        class="field-input"
                        id="email"
                        name="email"
                        type="email"
                        autocomplete="username"
                        autofocus
                        placeholder="name@example.com"
                        prop:value=move || state.email.get()
                        on:input=move |event| {
                            state.email.set(event_target_value(&event));
                            state.error.set(None);
                        }
                    />
                </label>

                <label class="field">
                    <span class="field-label">"Password"</span>
                    <input
                        class="field-input"
                        id="password"
                        name="password"
                        type="password"
                        autocomplete="current-password"
                        placeholder="Enter your password"
                        prop:value=move || state.password.get()
                        on:input=move |event| {
                            state.password.set(event_target_value(&event));
                            state.error.set(None);
                        }
                    />
                </label>

                <Show when=move || state.error.get().is_some()>
                    <p class="form-error" role="alert">
                        {move || state.error.get().unwrap_or_default()}
                    </p>
                </Show>

                <button class="primary-button" type="submit" disabled=move || state.is_loading.get()>
                    <Show
                        when=move || !state.is_loading.get()
                        fallback=|| view! { <span class="button-loader" aria-label="Signing in"></span> }
                    >
                        "Log in"
                    </Show>
                </button>
            </form>
        </main>
    }
}

fn submit_login(
    event: SubmitEvent,
    state: LoginState,
    client: Arc<ApiClient>,
    session: RwSignal<Option<AuthSession>>,
    navigate: impl Fn(&str, NavigateOptions) + Clone + 'static,
) {
    event.prevent_default();

    let email = state.email.get().trim().to_owned();
    let password = state.password.get();

    if email.is_empty() {
        state.error.set(Some(
            "Enter the email of an existing backend user.".to_owned(),
        ));
        return;
    }

    if password.trim().is_empty() {
        state
            .error
            .set(Some("Enter the password for that backend user.".to_owned()));
        return;
    }

    state.error.set(None);
    state.is_loading.set(true);

    spawn_local(async move {
        match client.login(email, password).await {
            Ok(response) => {
                state.is_loading.set(false);
                session.set(Some(AuthSession {
                    user: response.user,
                }));
                navigate("/dashboard", NavigateOptions::default());
            }
            Err(error) => {
                state.error.set(Some(error.to_string()));
                state.is_loading.set(false);
            }
        }
    });
}
