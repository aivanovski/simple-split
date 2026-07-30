use crate::api::client::ApiClient;
use backend_api::{ExpenseDto, GroupDto, PostExpenseRequest, PutExpenseRequest, UserUidDto};
use leptos::ev::SubmitEvent;
use leptos::prelude::*;
use leptos::task::spawn_local;
use std::sync::Arc;

#[derive(Clone, Copy)]
struct ExpenseFormState {
    title: RwSignal<String>,
    description: RwSignal<String>,
    amount: RwSignal<String>,
    paid_by: RwSignal<String>,
    split_between: RwSignal<Vec<String>>,
    error: RwSignal<Option<String>>,
    is_submitting: RwSignal<bool>,
}

#[component]
pub(super) fn ExpenseDialog(
    group: GroupDto,
    expense: Option<ExpenseDto>,
    client: Arc<ApiClient>,
    group_state: RwSignal<Option<GroupDto>>,
    dialog: RwSignal<Option<Option<ExpenseDto>>>,
) -> impl IntoView {
    let member_uids = group
        .members
        .iter()
        .map(|member| member.uid.clone())
        .collect::<Vec<_>>();
    let is_editing = expense.is_some();
    let expense_uid = expense.as_ref().map(|item| item.uid.clone());
    let state = ExpenseFormState {
        title: RwSignal::new(
            expense
                .as_ref()
                .map(|item| item.title.clone())
                .unwrap_or_default(),
        ),
        description: RwSignal::new(
            expense
                .as_ref()
                .map(|item| item.description.clone())
                .unwrap_or_default(),
        ),
        amount: RwSignal::new(
            expense
                .as_ref()
                .map(|item| format!("{:.2}", item.amount))
                .unwrap_or_default(),
        ),
        paid_by: RwSignal::new(
            expense
                .as_ref()
                .and_then(|item| item.paid_by.first())
                .map(|member| member.uid.clone())
                .or_else(|| member_uids.first().cloned())
                .unwrap_or_default(),
        ),
        split_between: RwSignal::new(
            expense
                .as_ref()
                .map(|item| {
                    item.split_between
                        .iter()
                        .map(|member| member.uid.clone())
                        .collect()
                })
                .unwrap_or(member_uids),
        ),
        error: RwSignal::new(None),
        is_submitting: RwSignal::new(false),
    };
    let group_uid = group.uid.clone();
    let close = move |_| {
        if !state.is_submitting.get() {
            dialog.set(None);
        }
    };
    let eyebrow = if is_editing {
        "Edit expense"
    } else {
        "New expense"
    };
    let heading = if is_editing {
        "Modify expense"
    } else {
        "Add an expense"
    };
    let submit_label = if is_editing {
        "Save changes"
    } else {
        "Add expense"
    };
    let loading_label = if is_editing {
        "Saving expense"
    } else {
        "Adding expense"
    };

    view! {
        <div class="dialog-backdrop">
            <section
                class="create-group-dialog expense-dialog"
                role="dialog"
                aria-modal="true"
                aria-labelledby="add-expense-title"
            >
                <div class="dialog-header">
                    <div>
                        <p class="eyebrow">{eyebrow}</p>
                        <h2 id="add-expense-title">{heading}</h2>
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
                    on:submit=move |event| submit_expense(
                        event,
                        state,
                        group_uid.clone(),
                        expense_uid.clone(),
                        client.clone(),
                        group_state,
                        dialog,
                    )
                >
                    <div class="form-row expense-main-fields">
                        <label class="field">
                            <span class="field-label">"Expense title"</span>
                            <input
                                class="field-input"
                                type="text"
                                autofocus
                                placeholder="Dinner"
                                prop:value=move || state.title.get()
                                on:input=move |event| {
                                    state.title.set(event_target_value(&event));
                                    state.error.set(None);
                                }
                            />
                        </label>
                        <label class="field">
                            <span class="field-label">
                                {format!("Amount ({})", group.currency.symbol)}
                            </span>
                            <input
                                class="field-input"
                                type="number"
                                min="0.01"
                                step="0.01"
                                inputmode="decimal"
                                placeholder="0.00"
                                prop:value=move || state.amount.get()
                                on:input=move |event| {
                                    state.amount.set(event_target_value(&event));
                                    state.error.set(None);
                                }
                            />
                        </label>
                    </div>

                    <label class="field">
                        <span class="field-label">"Description"</span>
                        <textarea
                            class="field-input field-textarea"
                            placeholder="Optional details"
                            prop:value=move || state.description.get()
                            on:input=move |event| state.description.set(event_target_value(&event))
                        ></textarea>
                    </label>

                    <label class="field">
                        <span class="field-label">"Paid by"</span>
                        <select
                            class="field-input field-select"
                            prop:value=move || state.paid_by.get()
                            on:change=move |event| state.paid_by.set(event_target_value(&event))
                        >
                            {group.members.iter().map(|member| {
                                view! {
                                    <option value=member.uid.clone()>{member.name.clone()}</option>
                                }
                            }).collect_view()}
                        </select>
                    </label>

                    <fieldset class="split-fieldset">
                        <legend class="field-label">"Split between"</legend>
                        <div class="split-member-grid">
                            {group.members.iter().map(|member| {
                                let uid = member.uid.clone();
                                let checked_uid = uid.clone();
                                let change_uid = uid.clone();
                                view! {
                                    <label class="split-member-option">
                                        <input
                                            type="checkbox"
                                            prop:checked=move || state.split_between.get().contains(&checked_uid)
                                            on:change=move |event| {
                                                let is_checked = event_target_checked(&event);
                                                state.split_between.update(|selected| {
                                                    selected.retain(|selected_uid| selected_uid != &change_uid);
                                                    if is_checked {
                                                        selected.push(change_uid.clone());
                                                    }
                                                });
                                                state.error.set(None);
                                            }
                                        />
                                        <span>{member.name.clone()}</span>
                                    </label>
                                }
                            }).collect_view()}
                        </div>
                    </fieldset>

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
                                fallback=move || view! {
                                    <span class="button-loader" aria-label=loading_label></span>
                                }
                            >
                                {submit_label}
                            </Show>
                        </button>
                    </div>
                </form>
            </section>
        </div>
    }
}

fn submit_expense(
    event: SubmitEvent,
    state: ExpenseFormState,
    group_uid: String,
    expense_uid: Option<String>,
    client: Arc<ApiClient>,
    group_state: RwSignal<Option<GroupDto>>,
    dialog: RwSignal<Option<Option<ExpenseDto>>>,
) {
    event.prevent_default();

    let title = state.title.get().trim().to_owned();
    let amount = state.amount.get().trim().parse::<f64>();

    if title.is_empty() {
        state.error.set(Some("Enter an expense title.".to_owned()));
        return;
    }
    let Ok(amount) = amount else {
        state.error.set(Some("Enter a valid amount.".to_owned()));
        return;
    };
    if !amount.is_finite() || amount <= 0.0 {
        state
            .error
            .set(Some("Amount must be greater than zero.".to_owned()));
        return;
    }
    if state.paid_by.get().is_empty() {
        state.error.set(Some("Choose who paid.".to_owned()));
        return;
    }
    if state.split_between.get().is_empty() {
        state
            .error
            .set(Some("Choose at least one member to split with.".to_owned()));
        return;
    }

    let split_between = state
        .split_between
        .get()
        .into_iter()
        .map(UserUidDto::new)
        .collect::<Vec<_>>();
    let paid_by = vec![UserUidDto::new(state.paid_by.get())];
    let description = state.description.get().trim().to_owned();
    state.error.set(None);
    state.is_submitting.set(true);

    spawn_local(async move {
        let save_result = if let Some(expense_uid) = expense_uid {
            let mut request = PutExpenseRequest::new();
            request.title = Some(Some(title));
            request.description = Some(Some(description));
            request.amount = Some(Some(amount));
            request.paid_by = Some(paid_by);
            request.is_split_between_all = Some(Some(false));
            request.split_between = Some(split_between);
            client
                .update_expense(&expense_uid, request)
                .await
                .map(|_| ())
        } else {
            let mut request = PostExpenseRequest::new(
                split_between,
                description,
                paid_by,
                title,
                group_uid.clone(),
                amount,
            );
            request.is_split_between_all = Some(Some(false));
            client.create_expense(request).await.map(|_| ())
        };

        match save_result {
            Ok(_) => match client.get_groups().await {
                Ok(response) => {
                    if let Some(updated) = response
                        .groups
                        .into_iter()
                        .find(|candidate| candidate.uid == group_uid)
                    {
                        group_state.set(Some(updated));
                    }
                    state.is_submitting.set(false);
                    dialog.set(None);
                }
                Err(error) => {
                    state.error.set(Some(error.to_string()));
                    state.is_submitting.set(false);
                }
            },
            Err(error) => {
                state.error.set(Some(error.to_string()));
                state.is_submitting.set(false);
            }
        }
    });
}
