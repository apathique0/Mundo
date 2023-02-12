package com.hawolt.core;

import com.hawolt.Main;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created: 10/02/2023 05:11
 * Author: Twitter @hawolt
 **/

public class Store implements IStore {
    private final Platform platform;
    private final Account account;
    private final Prepaid prepaid;
    private List<Transaction> transactions;
    private int refundCreditsRemaining;
    private Player player;

    public Store(Account account, IWalletUpdate wallet) throws IOException {
        this.platform = Platform.valueOf(account.get("cpid"));
        this.account = account;
        this.configure(wallet);
        this.prepaid = new Prepaid(account, platform.getEdge(), player);
    }

    public Platform getPlatform() {
        return platform;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getRefundCreditsRemaining() {
        return refundCreditsRemaining;
    }

    public List<Transaction> getRecommendedRefunds() {
        return transactions.stream().filter(transaction -> transaction.getRefundabilityMessage() == null).filter(transaction -> transaction.getCurrencyType().equals("RP")).sorted((t1, t2) -> Long.compare(t2.getAmountSpent(), t1.getAmountSpent())).collect(Collectors.toList());
    }

    public boolean redeemPrepaidCode(String code) {
        return prepaid.redeem(player, code);
    }

    public List<TransferItem> getAvailableTransferRegions() {
        Request request = new Request.Builder().url(String.format("https://%s.store.leagueoflegends.com/storefront/v3/view/misc?language=en_GB", platform.translateToWebRegion())).addHeader("Authorization", String.format("Bearer %s", account.get("access_token"))).build();
        Call call = Main.httpClient.newCall(request);
        try (Response response = call.execute()) {
            int status = response.code();
            try (ResponseBody body = response.body()) {
                if (body == null) return null;
                JSONObject object = new JSONObject(body.string());

                // Find item in catalog with inventoryType "TRANSFER"
                ArrayList<TransferItem> transferItems = new ArrayList<>();
                JSONArray catalog = object.getJSONArray("catalog");
                for (int i = 0; i < catalog.length(); i++) {
                    JSONObject item = catalog.getJSONObject(i);
                    if (item.getString("inventoryType").equals("TRANSFER")) {
                        transferItems.add(new TransferItem(item));
                    }
                }

                return transferItems;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int purchaseTransfer(TransferItem transferItem) {
        boolean isEligible = false;

        // Verify that the player is eligible for transfer
        JSONObject object1 = new JSONObject();
        object1.put("accountId", player.getAccountId());
        object1.put("destinationPlatform", transferItem.getDestinationPlatform());

        MediaType type = MediaType.parse("application/json");
        RequestBody verifyPost = RequestBody.create(type, object1.toString());
        Request verifyRequest = new Request.Builder()
                .url(String.format("https://%s.store.leagueoflegends.com/storefront/v3/transfer/verify/account", platform.translateToWebRegion()))
                .addHeader("Authorization", String.format("Bearer %s", account.get("access_token")))
                .post(verifyPost)
                .build();
        Call verifyCall = Main.httpClient.newCall(verifyRequest);
        try (Response verifyResponse = verifyCall.execute()) {
            int verifyStatus = verifyResponse.code();
            try (ResponseBody verifyBody = verifyResponse.body()) {
                if (verifyBody == null) return verifyStatus;
                if (verifyStatus == 200) {
                    JSONObject o = new JSONObject(verifyBody.string());
                    if (!o.getBoolean("errorOccurred")) {
                        isEligible = true;
                    }
                } else {
                    return verifyStatus;
                }
            }
        } catch (IOException e) {
            return 600;
        }

        // Do the actual transfer
        if (!isEligible) return 400;

        JSONObject object2 = new JSONObject();
        object2.put("accountId", player.getAccountId());
        object2.put("destinationPlatform", transferItem.getDestinationPlatform());
        object2.put("itemId", transferItem.getItemId());
        object2.put("ipCost", transferItem.getIp());
        object2.put("rpCost", 0);

        RequestBody transferPost = RequestBody.create(type, object2.toString());
        Request transferRequest = new Request.Builder()
                .url(String.format("https://%s.store.leagueoflegends.com/storefront/v3/transfer", platform.translateToWebRegion()))
                .addHeader("Authorization", String.format("Bearer %s", account.get("access_token")))
                .post(transferPost)
                .build();
        Call transferCall = Main.httpClient.newCall(transferRequest);
        try (Response transferResponse = transferCall.execute()) {
            int transferStatus = transferResponse.code();
            try (ResponseBody transferBody = transferResponse.body()) {
                if (transferBody == null) return transferStatus;
                if (transferStatus == 200) {
                    JSONObject o = new JSONObject(transferBody.string());
                    if (o.getBoolean("success")) {
                        return 200;
                    }
                }
                return transferStatus;
            }
        } catch (IOException e) {
            return 600;
        }
    }

    @Override
    public int purchaseSummonerNameChange(Currency currency, String name) {
        JSONObject object = new JSONObject();
        JSONObject item = new JSONObject();
        item.put("inventoryType", "SUMMONER_CUSTOMIZATION");
        item.put("itemId", 1);
        item.put("quantity", 1);
        if (currency == Currency.RP) item.put("rpCost", 1300);
        else item.put("ipCost", 13900);
        object.put("accountId", player.getAccountId());
        object.put("summonerName", name);
        JSONArray array = new JSONArray();
        array.put(item);
        object.put("items", array);
        MediaType type = MediaType.parse("application/json");
        RequestBody post = RequestBody.create(type, object.toString());
        Request request = new Request.Builder().url(String.format("https://%s.store.leagueoflegends.com/storefront/v3/summonerNameChange/purchase?language=en_US", platform.translateToWebRegion())).addHeader("Authorization", String.format("Bearer %s", account.get("access_token"))).post(post).build();
        Call call = Main.httpClient.newCall(request);
        try (Response response = call.execute()) {
            int status = response.code();
            try (ResponseBody body = response.body()) {
                if (body == null) return status;
                if (status == 200) {
                    if (currency == Currency.RP) {
                        player.withdrawRP(1300);
                    } else {
                        player.withdrawIP(13900);
                    }
                } else {
                    JSONObject o = new JSONObject(body.string());
                    JOptionPane.showMessageDialog(Frame.getFrames()[0], o.toString(5), "Error", JOptionPane.INFORMATION_MESSAGE);
                }
                return status;
            }
        } catch (IOException e) {
            return 600;
        }
    }

    private void configure(IWalletUpdate wallet) throws IOException {
        Request request = new Request.Builder().url(String.format("https://%s.store.leagueoflegends.com/storefront/v3/history/purchase?language=en_US", platform.translateToWebRegion())).addHeader("Authorization", String.format("Bearer %s", account.get("access_token"))).addHeader("Accept", "application/json").addHeader("Pragma", "no-cache").build();
        Call call = Main.httpClient.newCall(request);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                if (body == null) return;
                JSONObject object = new JSONObject(body.string());
                if (object.toString().contains("request.rateLimit")) throw new IOException(object.toString(5));
                this.player = new Player(wallet, object.getJSONObject("player"));
                this.refundCreditsRemaining = object.getInt("refundCreditsRemaining");
                this.transactions = object.getJSONArray("transactions").toList().stream().map(o -> (HashMap<?, ?>) o).map(JSONObject::new).map(Transaction::new).collect(Collectors.toList());
            }
        }
    }

    @Override
    public boolean refund(Transaction transaction) {
        JSONObject object = new JSONObject();
        object.put("accountId", player.getAccountId());
        object.put("transactionId", transaction.getTransactionId());
        object.put("inventoryType", transaction.getInventoryType());
        object.put("language", "en_GB");
        MediaType type = MediaType.parse("application/json");
        RequestBody post = RequestBody.create(type, object.toString());
        Request request = new Request.Builder().url(String.format("https://%s.store.leagueoflegends.com/storefront/v3/refund", platform.translateToWebRegion())).addHeader("Authorization", String.format("Bearer %s", account.get("access_token"))).post(post).build();
        Call call = Main.httpClient.newCall(request);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                if (body == null) return false;
                boolean status = response.code() == 200;
                if (status) {
                    if (transaction.getCurrencyType().equals("RP")) {
                        player.refundRP(transaction.getAmountSpent());
                    } else {
                        player.refundIP(transaction.getAmountSpent());
                    }
                } else {
                    JSONObject o = new JSONObject(body.string());
                    JOptionPane.showMessageDialog(Frame.getFrames()[0], o.toString(5), "Error", JOptionPane.INFORMATION_MESSAGE);
                }
                return status;
            }
        } catch (IOException e) {
            return false;
        }
    }
}
