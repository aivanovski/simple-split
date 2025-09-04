package com.github.ai.split.api.response;

import com.github.ai.split.api.ExpenseDto;

public class PostExpenseResponse {

    public ExpenseDto expense = new ExpenseDto();

    public PostExpenseResponse() {}

    public PostExpenseResponse(ExpenseDto expense) {
        this.expense = expense != null ? expense : new ExpenseDto();
    }
}