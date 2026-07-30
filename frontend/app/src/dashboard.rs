use crate::api::client::ApiClient;
use crate::session::AuthSession;
use backend_api::{CurrencyDto, GetGroupsResponse, GroupDto, PostGroupRequest};
use leptos::ev::SubmitEvent;
use leptos::prelude::*;
use leptos::task::spawn_local;
use leptos_router::{NavigateOptions, hooks::use_navigate};
use std::sync::Arc;

#[component]
pub fn DashboardPage() -> impl IntoView {
    let session = expect_context::<RwSignal<Option<AuthSession>>>();
    let client = expect_context::<Arc<ApiClient>>();
    let response = RwSignal::new(None::<GetGroupsResponse>);
    let currencies = RwSignal::new(Vec::<CurrencyDto>::new());
    let error = RwSignal::new(None::<String>);
    let is_create_dialog_open = RwSignal::new(false);

    let initial_client = client.clone();
    spawn_local(async move {
        match initial_client.get_groups().await {
            Ok(groups) => response.set(Some(groups)),
            Err(request_error) => error.set(Some(request_error.to_string())),
        }
    });

    let currency_client = client.clone();
    spawn_local(async move {
        if let Ok(currency_response) = currency_client.get_currencies().await {
            currencies.set(currency_response.currencies);
        }
    });

    let navigate = use_navigate();
    let user_name = move || {
        session
            .get()
            .map(|active_session| active_session.user.name)
            .unwrap_or_default()
    };

    let logout = move |_| {
        AuthSession::clear();
        session.set(None);
        navigate("/", NavigateOptions::default());
    };

    view! {
        <main class="dashboard-shell">
            <header class="dashboard-header">
                <a class="brand" href="/dashboard" aria-label="Simple Split dashboard">
                    <span class="brand-mark">"S"</span>
                    <span>"Simple Split"</span>
                </a>

                <div class="account-actions">
                    <span class="account-name">{user_name}</span>
                    <button class="secondary-button compact-button" type="button" on:click=logout>
                        "Log out"
                    </button>
                </div>
            </header>

            <section class="dashboard-content" aria-labelledby="groups-title">
                <div class="dashboard-intro">
                    <div>
                        <p class="eyebrow">"Dashboard"</p>
                        <h1 id="groups-title">"Your groups"</h1>
                        <p class="intro-copy">
                            "Choose a group to see its members and shared expenses."
                        </p>
                    </div>
                    <div class="dashboard-intro-actions">
                        <span class="group-count">
                            {move || {
                                let count = response.get().map(|value| value.groups.len()).unwrap_or(0);
                                format!("{count} {}", if count == 1 { "group" } else { "groups" })
                            }}
                        </span>
                        <button
                            class="primary-button create-group-button"
                            type="button"
                            on:click=move |_| is_create_dialog_open.set(true)
                        >
                            <span aria-hidden="true">"+"</span>
                            "Create group"
                        </button>
                    </div>
                </div>

                {move || {
                    if let Some(message) = error.get() {
                        view! {
                            <div class="dashboard-message error-message" role="alert">
                                <strong>"We couldn't load your groups."</strong>
                                <span>{message}</span>
                            </div>
                        }.into_any()
                    } else if let Some(groups_response) = response.get() {
                        if groups_response.groups.is_empty() {
                            view! {
                                <div class="dashboard-message empty-message">
                                    <strong>"No groups yet"</strong>
                                    <span>"Groups shared with you will appear here."</span>
                                </div>
                            }.into_any()
                        } else {
                            let cards = groups_response
                                .groups
                                .into_iter()
                                .map(|group| view! { <GroupCard group=group /> })
                                .collect_view();

                            view! {
                                <div class="group-grid">{cards}</div>
                            }.into_any()
                        }
                    } else {
                        view! {
                            <div class="group-grid" aria-label="Loading groups">
                                <div class="group-card skeleton-card"></div>
                                <div class="group-card skeleton-card"></div>
                            </div>
                        }.into_any()
                    }
                }}
            </section>

            <Show when=move || is_create_dialog_open.get()>
                <CreateGroupDialog
                    client=client.clone()
                    currencies=currencies
                    groups_response=response
                    is_open=is_create_dialog_open
                />
            </Show>
        </main>
    }
}

#[derive(Clone, Copy)]
struct CreateGroupState {
    title: RwSignal<String>,
    description: RwSignal<String>,
    password: RwSignal<String>,
    currency_iso_code: RwSignal<String>,
    error: RwSignal<Option<String>>,
    is_submitting: RwSignal<bool>,
}

impl CreateGroupState {
    fn new(default_currency: String) -> Self {
        Self {
            title: RwSignal::new(String::new()),
            description: RwSignal::new(String::new()),
            password: RwSignal::new(String::new()),
            currency_iso_code: RwSignal::new(default_currency),
            error: RwSignal::new(None),
            is_submitting: RwSignal::new(false),
        }
    }
}

#[component]
fn CreateGroupDialog(
    client: Arc<ApiClient>,
    currencies: RwSignal<Vec<CurrencyDto>>,
    groups_response: RwSignal<Option<GetGroupsResponse>>,
    is_open: RwSignal<bool>,
) -> impl IntoView {
    let available_currencies = currencies.get_untracked();
    let default_currency = available_currencies
        .iter()
        .find(|currency| currency.iso_code == "EUR")
        .or_else(|| available_currencies.first())
        .map(|currency| currency.iso_code.clone())
        .unwrap_or_else(|| "EUR".to_owned());
    let state = CreateGroupState::new(default_currency);

    let close = move |_| {
        if !state.is_submitting.get() {
            is_open.set(false);
        }
    };

    view! {
        <div class="dialog-backdrop">
            <section
                class="create-group-dialog"
                role="dialog"
                aria-modal="true"
                aria-labelledby="create-group-title"
            >
                <div class="dialog-header">
                    <div>
                        <p class="eyebrow">"New group"</p>
                        <h2 id="create-group-title">"Create a group"</h2>
                    </div>
                    <button
                        class="dialog-close"
                        type="button"
                        aria-label="Close dialog"
                        on:click=close
                    >
                        "×"
                    </button>
                </div>

                <form
                    class="create-group-form"
                    on:submit=move |event| submit_create_group(
                        event,
                        state,
                        client.clone(),
                        groups_response,
                        is_open,
                    )
                >
                    <label class="field">
                        <span class="field-label">"Group name"</span>
                        <input
                            class="field-input"
                            type="text"
                            autofocus
                            placeholder="Weekend in Berlin"
                            prop:value=move || state.title.get()
                            on:input=move |event| {
                                state.title.set(event_target_value(&event));
                                state.error.set(None);
                            }
                        />
                    </label>

                    <label class="field">
                        <span class="field-label">"Description"</span>
                        <textarea
                            class="field-input field-textarea"
                            placeholder="What are you splitting?"
                            prop:value=move || state.description.get()
                            on:input=move |event| state.description.set(event_target_value(&event))
                        ></textarea>
                    </label>

                    <div class="form-row">
                        <label class="field">
                            <span class="field-label">"Currency"</span>
                            <select
                                class="field-input field-select"
                                prop:value=move || state.currency_iso_code.get()
                                on:change=move |event| {
                                    state.currency_iso_code.set(event_target_value(&event))
                                }
                            >
                                {move || currencies.get().into_iter().map(|currency| {
                                    let value = currency.iso_code.clone();
                                    view! {
                                        <option value=value>
                                            {format!("{} ({})", currency.iso_code, currency.symbol)}
                                        </option>
                                    }
                                }).collect_view()}
                            </select>
                        </label>

                        <label class="field">
                            <span class="field-label">"Group password"</span>
                            <input
                                class="field-input"
                                type="password"
                                autocomplete="new-password"
                                placeholder="At least 4 characters"
                                prop:value=move || state.password.get()
                                on:input=move |event| {
                                    state.password.set(event_target_value(&event));
                                    state.error.set(None);
                                }
                            />
                        </label>
                    </div>

                    <Show when=move || state.error.get().is_some()>
                        <p class="form-error" role="alert">
                            {move || state.error.get().unwrap_or_default()}
                        </p>
                    </Show>

                    <div class="dialog-actions">
                        <button
                            class="secondary-button compact-button"
                            type="button"
                            disabled=move || state.is_submitting.get()
                            on:click=close
                        >
                            "Cancel"
                        </button>
                        <button
                            class="primary-button dialog-submit"
                            type="submit"
                            disabled=move || state.is_submitting.get()
                        >
                            <Show
                                when=move || !state.is_submitting.get()
                                fallback=|| view! {
                                    <span class="button-loader" aria-label="Creating group"></span>
                                }
                            >
                                "Create group"
                            </Show>
                        </button>
                    </div>
                </form>
            </section>
        </div>
    }
}

fn submit_create_group(
    event: SubmitEvent,
    state: CreateGroupState,
    client: Arc<ApiClient>,
    groups_response: RwSignal<Option<GetGroupsResponse>>,
    is_open: RwSignal<bool>,
) {
    event.prevent_default();

    let title = state.title.get().trim().to_owned();
    let password = state.password.get().trim().to_owned();
    let currency_iso_code = state.currency_iso_code.get();

    if title.is_empty() {
        state.error.set(Some("Enter a group name.".to_owned()));
        return;
    }
    if password.len() < 4 {
        state.error.set(Some(
            "Use at least 4 characters for the group password.".to_owned(),
        ));
        return;
    }
    if currency_iso_code.is_empty() {
        state.error.set(Some("Choose a currency.".to_owned()));
        return;
    }

    let request = PostGroupRequest::new(
        currency_iso_code,
        state.description.get().trim().to_owned(),
        Vec::new(),
        title,
        Vec::new(),
        password,
    );

    state.error.set(None);
    state.is_submitting.set(true);

    spawn_local(async move {
        match client.create_group(request).await {
            Ok(created) => {
                groups_response.update(|current| {
                    if let Some(response) = current {
                        response.groups.push(created.group);
                    } else {
                        *current = Some(GetGroupsResponse::new(vec![created.group], Vec::new()));
                    }
                });
                state.is_submitting.set(false);
                is_open.set(false);
            }
            Err(error) => {
                state.error.set(Some(error.to_string()));
                state.is_submitting.set(false);
            }
        }
    });
}

#[component]
fn GroupCard(group: GroupDto) -> impl IntoView {
    let group_url = format!("/groups/{}", group.uid);
    let member_count = group.members.len();
    let expense_count = group.expenses.len();
    let total = if group.expenses.is_empty() {
        0.0
    } else {
        group.expenses.iter().map(|expense| expense.amount).sum()
    };
    let symbol = group.currency.symbol.clone();
    let member_names = group
        .members
        .iter()
        .map(|member| member.name.as_str())
        .collect::<Vec<_>>()
        .join(", ");

    view! {
        <a class="group-card group-card-link" href=group_url>
            <div class="group-card-accent"></div>
            <div class="group-card-body">
                <div class="group-card-heading">
                    <span class="group-icon" aria-hidden="true">"👥"</span>
                    <span class="expense-total">{format!("{symbol}{total:.2}")}</span>
                </div>
                <div>
                    <h2>{group.title}</h2>
                    <p class="group-description">{group.description}</p>
                </div>
                <p class="member-list" title=member_names.clone()>{member_names.clone()}</p>
                <div class="group-meta">
                    <span>
                        {format!(
                            "{member_count} {}",
                            if member_count == 1 { "member" } else { "members" },
                        )}
                    </span>
                    <span aria-hidden="true">"•"</span>
                    <span>
                        {format!(
                            "{expense_count} {}",
                            if expense_count == 1 { "expense" } else { "expenses" },
                        )}
                    </span>
                </div>
            </div>
        </a>
    }
}
