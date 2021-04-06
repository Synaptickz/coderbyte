package com.luxon.assignment.service;

import com.luxon.assignment.dto.ExchangeRequestDto;
import com.luxon.assignment.entity.Account;
import com.luxon.assignment.entity.Balance;
import com.luxon.assignment.enums.Instrument;
import com.luxon.assignment.repository.AccountRepository;
import com.luxon.assignment.repository.BalanceRepository;
import com.luxon.assignment.repository.RateRepository;
import com.luxon.assignment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final AccountRepository accountRepository;

    private final RateRepository rateRepository;

    private final BalanceRepository balanceRepository;

    private final WalletRepository walletRepository;

    @Transactional
    public ResponseEntity<?> execute(ExchangeRequestDto exchangeRequestDto) {
        //TODO -- add logic here
        var exchangeType = exchangeRequestDto.getExchangeType();
        // TODO: The following methods could use some refactoring to reduce code duplication,
        switch (exchangeType) {
            case BUY:
                buy(exchangeRequestDto);
                break;
            case SEND:
                send(exchangeRequestDto);
                break;
            case SELL:
                sell(exchangeRequestDto);
                break;
            default:
                throw new RuntimeException("Unimplemented exchange type method");
        }

        return ResponseEntity.ok(200);
    }

    private void buy(ExchangeRequestDto exchangeRequestDto) {
        Integer accountId = exchangeRequestDto.getAccountId();
        Instrument fromInstrument = exchangeRequestDto.getBaseInstrument();
        Instrument toInstrument = exchangeRequestDto.getToInstrument();
        // TODO: maybe check if instruments are the same?
        double qty = exchangeRequestDto.getQty();

        // TODO: check for nulls
        Account account = accountRepository.findById(accountId).get();

        // TODO: do null checks
        var fromBalance = account.getBalances().stream().filter(f -> f.getInstrument().equals(fromInstrument)).findFirst().get();
        var toBalance = account.getBalances().stream().filter(f -> f.getInstrument().equals(toInstrument)).findFirst().get();

        // TODO: retrieve rate from RateRepository
        // Rate entity does not support pairs, so a rate cannot be measured against other instrument
        double rate = 1;

        double price = qty * rate;
        if (fromBalance.getQty() < price) {
            throw new RuntimeException("Not enough funds");
        }

        fromBalance.setQty(fromBalance.getQty() - price);
        toBalance.setQty(toBalance.getQty() + qty);

        balanceRepository.saveAll(Arrays.asList(fromBalance, toBalance));
    }

    private void send(ExchangeRequestDto exchangeRequestDto) {
        Integer baseAccountId = exchangeRequestDto.getAccountId();
        Instrument baseInstrument = exchangeRequestDto.getBaseInstrument();
        // TODO: check for null;
        String targetAddress = exchangeRequestDto.getWalletAddress();
        double qty = exchangeRequestDto.getQty();

        if (baseInstrument.isFiatInstrument()) {
            throw new RuntimeException("Operation not supported. Only crypto currencies are allowed to perform SEND operations");
        }

        // TODO: check for nulls
        Account account = accountRepository.findById(baseAccountId).get();
        var fromBalance = account.getBalances().stream().filter(f -> f.getInstrument().equals(baseInstrument)).findFirst().get();

        // Check if user has the balance
        if (fromBalance.getQty() < qty) {
            throw new RuntimeException("Insufficient balance");
        }

        // Find the target account
        var targetWallet = walletRepository.findAll().stream().filter(wallet -> wallet.getWalletAddress().equals(targetAddress)).findFirst().get();
        var targetBalance = targetWallet.getAccount().getBalances().stream().filter(balance -> balance.getInstrument().equals(baseInstrument)).findFirst().get();

        fromBalance.setQty(fromBalance.getQty() - qty);
        targetBalance.setQty(targetBalance.getQty() + qty);

        balanceRepository.saveAll(Arrays.asList(fromBalance, targetBalance));
    }

    public void sell(ExchangeRequestDto exchangeRequestDto) {
        Integer accountId = exchangeRequestDto.getAccountId();
        Instrument baseInstrument = exchangeRequestDto.getBaseInstrument(); // The instrument to be sold
        Instrument toInstrument = exchangeRequestDto.getToInstrument();
        // TODO: maybe check if instruments are the same?
        double qty = exchangeRequestDto.getQty();

        // TODO: check for nulls
        Account account = accountRepository.findById(accountId).get();

        // TODO: do null checks
        var sourceBalance = account.getBalances().stream().filter(f -> f.getInstrument().equals(baseInstrument)).findFirst().get();
        var targetBalance = account.getBalances().stream().filter(f -> f.getInstrument().equals(toInstrument)).findFirst().get();

        // TODO: retrieve rate from RateRepository
        double rate = 1;

        if (sourceBalance.getQty() < qty) {
            throw new RuntimeException("Not enough funds");
        }

        sourceBalance.setQty(sourceBalance.getQty() - qty);
        targetBalance.setQty(targetBalance.getQty() + qty * rate);

        balanceRepository.saveAll(Arrays.asList(sourceBalance, targetBalance));
    }

    @PostConstruct
    public void doSome() {
        Account account = accountRepository.save(Account.builder().id(1).name("Jack Black").build());
        List<Balance> userBalance = Collections.singletonList(Balance.builder().account(account).build());
        account.setBalances(userBalance);
    }
}
