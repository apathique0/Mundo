package com.hawolt.ui;

import com.hawolt.Main;
import com.hawolt.core.IStore;
import com.hawolt.core.TransferItem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class TransferPanel extends JPanel {
    private final JComboBox<TransferItem> transferJComboBox = new JComboBox<>();
    private IStore store;

    public TransferPanel() {
        this.setLayout(new BorderLayout(5, 0));
        this.setBorder(BorderFactory.createTitledBorder("Transfer"));
        this.add(transferJComboBox);
        JButton transfer = new JButton("Transfer");
        transfer.addActionListener(listener -> {
            transfer.setEnabled(false);
            Main.service.execute(() -> {
                TransferItem transferItem = transferJComboBox.getItemAt(transferJComboBox.getSelectedIndex());
                if (transferItem != null) {
                    try {
                        int purchaseTransfer = store.purchaseTransfer(transferItem);

                        if (purchaseTransfer == 200) {
                            JOptionPane.showMessageDialog(null, "Successfully transferred ");
                            transferJComboBox.removeItem(transferItem);
                        } else {
                            JOptionPane.showMessageDialog(null, "Failed to transfer - " + purchaseTransfer);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                transfer.setEnabled(true);
            });
        });
        add(transferJComboBox, BorderLayout.CENTER);
        add(transfer, BorderLayout.EAST);
    }

    public void populate(IStore store, List<TransferItem> list) {
        this.store = store;
        this.transferJComboBox.removeAllItems();
        for (TransferItem transferItem : list) {
            this.transferJComboBox.addItem(transferItem);
        }
        this.revalidate();
    }
}
