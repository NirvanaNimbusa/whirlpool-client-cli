package com.samourai.whirlpool.client.run;

import com.samourai.wallet.hd.HD_Account;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.hd.HD_Chain;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.whirlpool.client.CliUtils;
import com.samourai.whirlpool.client.exception.NotifiableException;
import com.samourai.whirlpool.client.run.vpub.MultiAddrResponse;
import com.samourai.whirlpool.client.run.vpub.UnspentResponse;
import com.samourai.whirlpool.client.tx0.Tx0;
import com.samourai.whirlpool.client.tx0.Tx0Service;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.TransactionOutPoint;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

public class RunTx0VPub {
    private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private NetworkParameters params;
    private static final String XPUB_SAMOURAI_FEES = "vpub5YS8pQgZKVbrSn9wtrmydDWmWMjHrxL2mBCZ81BDp7Z2QyCgTLZCrnBprufuoUJaQu1ZeiRvUkvdQTNqV6hS96WbbVZgweFxYR1RXYkBcKt";
    private static final int TX0_SIZE = 5; // TODO
    private static final long SAMOURAI_FEES = 100; // TODO
    private static final long MINER_FEE_TX0 = 15000;

    public RunTx0VPub(NetworkParameters params) {
        this.params = params;
    }

    public void runTx0(List<UnspentResponse.UnspentOutput> utxos, MultiAddrResponse.Address address, HD_Wallet bip84w, long destinationValue) throws Exception {
        // find utxo to spend Tx0 from
        long spendFromBalanceMin = TX0_SIZE * (destinationValue + SAMOURAI_FEES);
        List<UnspentResponse.UnspentOutput> tx0SpendFroms = utxos.stream().filter(utxo -> utxo.value >= spendFromBalanceMin).collect(Collectors.toList());

        if (tx0SpendFroms.size() > 0) {
            log.info("Found " + tx0SpendFroms.size() + " utxos to use as Tx0 input");
            CliUtils.printUtxos(tx0SpendFroms);

            UnspentResponse.UnspentOutput tx0SpendFrom = tx0SpendFroms.get(0);
            Tx0 tx0 = runTx0(tx0SpendFrom, address, bip84w, destinationValue);

            final String tx0Hex = new String(Hex.encode(tx0.getTx().bitcoinSerialize()));
            throw new NotifiableException("Please broadcast TX0 and restart script:\ntx0Hash=" + tx0.getTx().getHashAsString() + "\ntx0Hex=" + tx0Hex);
        } else {
            throw new Exception("ERROR: No utxo available to spend Tx0 from");
        }
    }

    private Tx0 runTx0(UnspentResponse.UnspentOutput spendFrom, MultiAddrResponse.Address address, HD_Wallet bip84w, long destinationValue) throws Exception {
        /*
         * SPEND FROM: BIP84[ACCOUNT_0][CHAIN_DEPOSIT][spendFrom.xpub.address]
         */
        // utxo
        TransactionOutPoint spendFromOutpoint = spendFrom.computeOutpoint(params);

        // key
        HD_Account depositAccount = bip84w.getAccountAt(RunVPub.ACCOUNT_DEPOSIT);
        HD_Address depositAddress = depositAccount.getChain(RunVPub.CHAIN_DEPOSIT).getAddressAt(spendFrom.computePathAddressIndex());

        // change: BIP44[ACCOUNT_0][CHAIN_DEPOSIT][address.change_index]
        HD_Address changeAddress = depositAccount.getChain(RunVPub.CHAIN_DEPOSIT).getAddressAt(address.change_index);

        // destination: BIP84[ACCOUNT_PREMIX][CHAIN_PREMIX][address.account_index]
        HD_Chain destinationChain = bip84w.getAccountAt(RunVPub.ACCOUNT_PREMIX).getChain(RunVPub.CHAIN_PREMIX);

        // run tx0
        Tx0 tx0 = new Tx0Service(params).tx0(depositAddress, spendFromOutpoint,
            TX0_SIZE, destinationChain, destinationValue, address.account_index,
            changeAddress, MINER_FEE_TX0, XPUB_SAMOURAI_FEES, SAMOURAI_FEES);

        log.info("Tx0:");
        log.info(tx0.getTx().toString());
        return tx0;
    }
}
