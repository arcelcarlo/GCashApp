package gcash.app.repository;

import gcash.app.config.DatabaseConnection;
import gcash.app.view.ProgressBar;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SendMoneyDAO {
    public void sendMoney(String senderPhone, String recipientPhone, BigDecimal amount)
            throws InterruptedException, SQLException {

        if (senderPhone == null || recipientPhone == null) {
            System.out.println("Invalid sender/recipient. Try again.");
            return;
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }

        if (senderPhone.equals(recipientPhone)) {
            System.out.println("You cannot send money to yourself.");
            return;
        }

        String selectBalanceSql = "SELECT amount FROM balances WHERE phone_number = ?";
        String updateBalanceSql = "UPDATE balances SET amount = ? WHERE phone_number = ?";

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            BigDecimal senderBalance;
            BigDecimal recipientBalance;

            try (PreparedStatement stmt = conn.prepareStatement(selectBalanceSql)) {
                stmt.setString(1, senderPhone);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        System.out.println("Sender balance not found.");
                        return;
                    }
                    senderBalance = rs.getBigDecimal("amount");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(selectBalanceSql)) {
                stmt.setString(1, recipientPhone);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        System.out.println("Recipient not found.");
                        return;
                    }
                    recipientBalance = rs.getBigDecimal("amount");
                }
            }

            if (senderBalance.compareTo(amount) < 0) {
                conn.rollback();
                System.out.println("Insufficient balance.");
                return;
            }

            BigDecimal newSenderBalance = senderBalance.subtract(amount);
            BigDecimal newRecipientBalance = recipientBalance.add(amount);

            int senderUpdate;
            try (PreparedStatement stmt = conn.prepareStatement(updateBalanceSql)) {
                stmt.setBigDecimal(1, newSenderBalance);
                stmt.setString(2, senderPhone);
                senderUpdate = stmt.executeUpdate();
            }

            int recipientUpdate;
            try (PreparedStatement stmt = conn.prepareStatement(updateBalanceSql)) {
                stmt.setBigDecimal(1, newRecipientBalance);
                stmt.setString(2, recipientPhone);
                recipientUpdate = stmt.executeUpdate();
            }

            if (senderUpdate == 1 && recipientUpdate == 1) {
                conn.commit();
                ProgressBar.progressBar();
                System.out.println("\nTransfer successful!");
                System.out.println("Sent: ₱" + amount);
                System.out.println("New balance: ₱" + newSenderBalance);

                TransactionHistoryDAO transactionHistoryDAO = new TransactionHistoryDAO();
                transactionHistoryDAO.sendMoneyTransactionHistory(senderPhone, recipientPhone, amount);
            } else {
                conn.rollback();
                System.out.println("Transfer failed. Try again later.");
            }

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}