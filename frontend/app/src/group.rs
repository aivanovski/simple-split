mod expense_dialog;

use self::expense_dialog::ExpenseDialog;
use crate::api::client::ApiClient;
use crate::session::AuthSession;
use backend_api::{ExpenseDto, GroupDto};
use leptos::prelude::*;
use leptos::task::spawn_local;
use leptos_router::hooks::use_params_map;
use std::sync::Arc;

#[component]
pub fn GroupPage() -> impl IntoView {
    let session = expect_context::<RwSignal<Option<AuthSession>>>();
    let client = expect_context::<Arc<ApiClient>>();
    let params = use_params_map();
    let group_id = params.read().get("group_id").unwrap_or_default();
    let group_state = RwSignal::new(None::<GroupDto>);
    let error = RwSignal::new(None::<String>);

    let initial_client = client.clone();
    spawn_local(async move {
        match initial_client.get_groups().await {
            Ok(response) => match response
                .groups
                .into_iter()
                .find(|item| item.uid == group_id)
            {
                Some(found) => group_state.set(Some(found)),
                None => error.set(Some("This group could not be found.".to_owned())),
            },
            Err(request_error) => error.set(Some(request_error.to_string())),
        }
    });

    let user_name = move || {
        session
            .get()
            .map(|active_session| active_session.user.name)
            .unwrap_or_default()
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
                    <a class="secondary-button compact-button header-link" href="/dashboard">
                        "All groups"
                    </a>
                </div>
            </header>

            <section class="group-page-content">
                {move || {
                    if let Some(message) = error.get() {
                        view! {
                            <div class="dashboard-message error-message" role="alert">
                                <strong>"We couldn't load this group."</strong>
                                <span>{message}</span>
                                <a class="inline-link" href="/dashboard">"Back to dashboard"</a>
                            </div>
                        }.into_any()
                    } else if let Some(group) = group_state.get() {
                        view! {
                            <GroupDetails
                                group=group
                                group_state=group_state
                                client=client.clone()
                                user_name=user_name()
                            />
                        }.into_any()
                    } else {
                        view! {
                            <div class="group-detail-skeleton skeleton-card" aria-label="Loading group"></div>
                        }.into_any()
                    }
                }}
            </section>
        </main>
    }
}

#[component]
fn GroupDetails(
    group: GroupDto,
    group_state: RwSignal<Option<GroupDto>>,
    client: Arc<ApiClient>,
    user_name: String,
) -> impl IntoView {
    let expense_dialog = RwSignal::new(None::<Option<ExpenseDto>>);
    let member_count = group.members.len();
    let expense_count = group.expenses.len();
    let total_spent: f64 = group.expenses.iter().map(|expense| expense.amount).sum();
    let symbol = group.currency.symbol.clone();
    let balance = current_balance(&group, &user_name);
    let balance_class = if balance < 0.0 {
        "balance-value balance-negative"
    } else {
        "balance-value balance-positive"
    };
    let balance_caption = if balance > 0.005 {
        "You are owed"
    } else if balance < -0.005 {
        "You owe"
    } else {
        "You are settled up"
    };
    let expenses = group
        .expenses
        .clone()
        .into_iter()
        .map(|expense| {
            view! {
                <ExpenseRow expense=expense expense_dialog=expense_dialog />
            }
        })
        .collect_view();
    let members = group
        .members
        .iter()
        .map(|member| {
            let initial = member
                .name
                .chars()
                .next()
                .unwrap_or('?')
                .to_uppercase()
                .to_string();
            let name = member.name.clone();
            let is_you = name.trim().eq_ignore_ascii_case(user_name.trim());
            view! {
                <li class="member-row">
                    <span class="member-avatar">{initial}</span>
                    <span class="member-name">{name}</span>
                    <Show when=move || is_you>
                        <span class="you-badge">"You"</span>
                    </Show>
                </li>
            }
        })
        .collect_view();
    let settlements = group
        .payback_transactions
        .iter()
        .map(|transaction| {
            let debtor = member_name(&group, &transaction.debtor_uid);
            let creditor = member_name(&group, &transaction.creditor_uid);
            let amount = transaction.amount;
            let settlement_symbol = symbol.clone();

            view! {
                <li class="settlement-row">
                    <div class="settlement-people">
                        <span class="settlement-name">{debtor}</span>
                        <span class="settlement-arrow" aria-hidden="true">"→"</span>
                        <span class="settlement-name">{creditor}</span>
                    </div>
                    <strong class="settlement-amount">
                        {format!("{settlement_symbol}{amount:.2}")}
                    </strong>
                </li>
            }
        })
        .collect_view();
    let settlement_content = if group.payback_transactions.is_empty() {
        view! {
            <div class="settled-up">
                <span class="settled-up-icon" aria-hidden="true">"✓"</span>
                <div>
                    <strong>"All settled up"</strong>
                    <p>"There are no outstanding payments in this group."</p>
                </div>
            </div>
        }
        .into_any()
    } else {
        view! { <ul class="settlement-list">{settlements}</ul> }.into_any()
    };
    let expense_content = if expense_count == 0 {
        view! {
            <p class="panel-empty">"No expenses have been added yet."</p>
        }
        .into_any()
    } else {
        view! {
            <div class="expense-list">{expenses}</div>
        }
        .into_any()
    };

    view! {
        <a class="back-link" href="/dashboard">"← Back to groups"</a>
        <div class="group-detail-header">
            <div>
                <p class="eyebrow">"Group"</p>
                <h1>{group.title.clone()}</h1>
                <p class="intro-copy">{group.description.clone()}</p>
            </div>
            <div class="group-header-actions">
                <div class="group-summary-meta">
                    <span>{format!("{member_count} members")}</span>
                    <span aria-hidden="true">"•"</span>
                    <span>{format!("{expense_count} expenses")}</span>
                </div>
                <button
                    class="primary-button add-expense-button"
                    type="button"
                    on:click=move |_| expense_dialog.set(Some(None))
                >
                    <span aria-hidden="true">"+"</span>
                    "Add expense"
                </button>
            </div>
        </div>

        <div class="summary-grid">
            <article class="summary-card balance-card">
                <span class="summary-label">"Current balance"</span>
                <strong class=balance_class>
                    {format!("{symbol}{:.2}", balance.abs())}
                </strong>
                <span class="summary-caption">{balance_caption}</span>
            </article>
            <article class="summary-card">
                <span class="summary-label">"Total group spending"</span>
                <strong class="summary-value">{format!("{symbol}{total_spent:.2}")}</strong>
                <span class="summary-caption">
                    {format!("{expense_count} {}", if expense_count == 1 { "expense" } else { "expenses" })}
                </span>
            </article>
        </div>

        <section class="detail-panel settlements-panel" aria-labelledby="settlements-title">
            <div class="panel-heading">
                <div>
                    <p class="eyebrow">"Payments"</p>
                    <h2 id="settlements-title">"Settle debts"</h2>
                </div>
                <span class="panel-count">{group.payback_transactions.len()}</span>
            </div>
            {settlement_content}
        </section>

        <div class="group-detail-grid">
            <section class="detail-panel expenses-panel" aria-labelledby="expenses-title">
                <div class="panel-heading">
                    <div>
                        <p class="eyebrow">"Activity"</p>
                        <h2 id="expenses-title">"All expenses"</h2>
                    </div>
                    <span class="panel-count">{expense_count}</span>
                </div>
                {expense_content}
            </section>

            <section class="detail-panel members-panel" aria-labelledby="members-title">
                <div class="panel-heading">
                    <div>
                        <p class="eyebrow">"People"</p>
                        <h2 id="members-title">"Members"</h2>
                    </div>
                    <span class="panel-count">{member_count}</span>
                </div>
                <ul class="members-list">{members}</ul>
            </section>
        </div>

        <Show when=move || expense_dialog.get().is_some()>
            <ExpenseDialog
                group=group.clone()
                expense=expense_dialog.get().flatten()
                client=client.clone()
                group_state=group_state
                dialog=expense_dialog
            />
        </Show>
    }
}

fn member_name(group: &GroupDto, uid: &str) -> String {
    group
        .members
        .iter()
        .find(|member| member.uid == uid)
        .map(|member| member.name.clone())
        .unwrap_or_else(|| "Unknown member".to_owned())
}

#[component]
fn ExpenseRow(
    expense: ExpenseDto,
    expense_dialog: RwSignal<Option<Option<ExpenseDto>>>,
) -> impl IntoView {
    let payer_names = expense
        .paid_by
        .iter()
        .map(|member| member.name.as_str())
        .collect::<Vec<_>>()
        .join(", ");
    let split_count = expense.split_between.len();
    let symbol = expense.currency.symbol.clone();
    let description = expense.description.clone();
    let has_description = !description.is_empty();
    let edit_expense = expense.clone();

    view! {
        <article class="expense-row">
            <div class="expense-icon" aria-hidden="true">"↗"</div>
            <div class="expense-copy">
                <h3>{expense.title.clone()}</h3>
                <p>
                    {format!(
                        "{} · Paid by {} · Split between {}",
                        expense.created.formatted,
                        if payer_names.is_empty() { "Unknown" } else { &payer_names },
                        split_count,
                    )}
                </p>
                <Show when=move || has_description>
                    <p class="expense-description">{description.clone()}</p>
                </Show>
            </div>
            <div class="expense-actions">
                <strong class="expense-amount">{format!("{symbol}{:.2}", expense.amount)}</strong>
                <button
                    class="edit-expense-button"
                    type="button"
                    aria-label=format!("Edit {}", expense.title)
                    on:click=move |_| expense_dialog.set(Some(Some(edit_expense.clone())))
                >
                    "Edit"
                </button>
            </div>
        </article>
    }
}

fn current_balance(group: &GroupDto, user_name: &str) -> f64 {
    let Some(member) = group
        .members
        .iter()
        .find(|member| member.name.trim().eq_ignore_ascii_case(user_name.trim()))
    else {
        return 0.0;
    };

    group
        .payback_transactions
        .iter()
        .map(|transaction| {
            if transaction.creditor_uid == member.uid {
                transaction.amount
            } else if transaction.debtor_uid == member.uid {
                -transaction.amount
            } else {
                0.0
            }
        })
        .sum()
}
