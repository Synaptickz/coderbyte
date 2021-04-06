package com.luxon.assignment.dto;

import com.luxon.assignment.enums.Instrument;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ExchangeRequestDto {

    @NotNull
    private Integer accountId;

    @NotNull
    private ExchangeType exchangeType;

    //TODO - add more relevant fields here for some generic request

    @NotNull
    private Instrument baseInstrument;

    private Instrument toInstrument;

    @NotNull
    private Double qty;

    private String walletAddress;

    public enum ExchangeType {
        BUY, SELL, SEND
    }
}
