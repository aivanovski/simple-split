use crate::api::client::ApiClient;
use crate::session::AuthSession;
use leptos::task::spawn_local;
use leptos::{ev::SubmitEvent, prelude::*};
use leptos_router::{NavigateOptions, hooks::use_navigate};
use std::sync::Arc;

#[derive(Clone, Copy)]
struct SignupState {
    name: RwSignal<String>,
    email: RwSignal<String>,
    password: RwSignal<String>,
    error: RwSignal<Option<String>>,
    is_loading: RwSignal<bool>,
}

impl SignupState {
    fn new() -> Self {
        Self {
            name: RwSignal::new(String::new()),
            email: RwSignal::new(String::new()),
            password: RwSignal::new(String::new()),
            error: RwSignal::new(None),
            is_loading: RwSignal::new(false),
        }
    }
}

#[component]
pub fn SignupPage() -> impl IntoView {
    let state = SignupState::new();
    let navigate = use_navigate();
    let session = expect_context::<RwSignal<Option<AuthSession>>>();
    let client = expect_context::<Arc<ApiClient>>();

    view! {
        <main class="auth-shell">
            <form
                class="signup-form"
                on:submit=move |event| submit_signup(
                    event,
                    state,
                    client.clone(),
                    session,
                    navigate.clone(),
                )
                novalidate
            >
                <h1 class="auth-heading">"Create your account"</h1>
                <p class="auth-subtitle">"Start splitting expenses with your group."</p>

                <label class="field">
                    <span class="field-label">"Name"</span>
                    <input
                        class="field-input"
                        id="name"
                        name="name"
                        type="text"
                        autocomplete="name"
                        autofocus
                        placeholder="Your name"
                        prop:value=move || state.name.get()
                        on:input=move |event| {
                            state.name.set(event_target_value(&event));
                            state.error.set(None);
                        }
                    />
                </label>

                <label class="field">
                    <span class="field-label">"Email"</span>
                    <input
                        class="field-input"
                        id="email"
                        name="email"
                        type="email"
                        autocomplete="username"
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
                        autocomplete="new-password"
                        placeholder="Create a password"
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
                        fallback=|| view! { <span class="button-loader" aria-label="Creating account"></span> }
                    >
                        "Create account"
                    </Show>
                </button>

                <p class="auth-switch">
                    "Already have an account? "
                    <a href="/">"Log in"</a>
                </p>
            </form>
        </main>
    }
}

fn submit_signup(
    event: SubmitEvent,
    state: SignupState,
    client: Arc<ApiClient>,
    session: RwSignal<Option<AuthSession>>,
    navigate: impl Fn(&str, NavigateOptions) + Clone + 'static,
) {
    event.prevent_default();

    let name = state.name.get().trim().to_owned();
    let email = state.email.get().trim().to_owned();
    let password = state.password.get();

    if name.is_empty() {
        state.error.set(Some("Enter your name.".to_owned()));
        return;
    }

    if email.is_empty() {
        state.error.set(Some("Enter your email.".to_owned()));
        return;
    }

    if password.trim().is_empty() {
        state.error.set(Some("Create a password.".to_owned()));
        return;
    }

    state.error.set(None);
    state.is_loading.set(true);

    spawn_local(async move {
        match client.signup(name, email, password).await {
            Ok(response) => {
                state.is_loading.set(false);
                let auth_session = AuthSession {
                    user: response.user,
                };
                auth_session.save();
                session.set(Some(auth_session));
                navigate("/dashboard", NavigateOptions::default());
            }
            Err(error) => {
                state.error.set(Some(error.to_string()));
                state.is_loading.set(false);
            }
        }
    });
}
