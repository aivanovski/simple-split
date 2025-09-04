package com.github.ai.split.api.response;

import com.github.ai.split.api.ExpenseDto;
public class PutExpenseResponse {

    public ExpenseDto expense = new ExpenseDto();

    public PutExpenseResponse() {}

    public PutExpenseResponse(ExpenseDto expense) {
        this.expense = expense != null ? expense : new ExpenseDto();
    }
}