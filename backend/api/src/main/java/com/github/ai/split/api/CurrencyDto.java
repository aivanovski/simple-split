package com.github.ai.split.api;

public class CurrencyDto {

    public String isoCode = "";
    public String name = "";
    public String symbol = "";

    public CurrencyDto() {}

    public CurrencyDto(String isoCode, String name, String symbol) {
        this.isoCode = isoCode;
        this.name = name;
        this.symbol = symbol;
    }
}
