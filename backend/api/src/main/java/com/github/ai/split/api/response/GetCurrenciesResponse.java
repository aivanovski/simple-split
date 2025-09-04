package com.github.ai.split.api.response;

import com.github.ai.split.api.CurrencyDto;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


public class GetCurrenciesResponse {

    public List<CurrencyDto> currencies = Collections.emptyList();

    public GetCurrenciesResponse() {
        this.currencies = new ArrayList<>();
    }

    public GetCurrenciesResponse(List<CurrencyDto> currencies) {
        this.currencies = currencies != null ? currencies : new ArrayList<>();
    }
}