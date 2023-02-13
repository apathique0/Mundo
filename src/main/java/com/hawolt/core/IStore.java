package com.hawolt.core;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created: 10/02/2023 06:22
 * Author: Twitter @hawolt
 **/

public interface IStore {
    boolean refund(Transaction transaction) throws IOException;

    int purchaseTransfer(TransferItem transferItem) throws IOException;

    int purchaseSummonerNameChange(Currency itemAt, String text);

    boolean redeemPrepaidCode(String code);
}
