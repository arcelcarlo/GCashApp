package gcash.app.repository;

import gcash.app.config.DatabaseConnection;
import gcash.app.model.Transactions;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ViewTransactionsDAO {

    public void viewTransactions(UUID userID) throws SQLException {
        List<Transactions> transactionsList = new ArrayList<>();

        String sql = "SELECT * FROM transactions\n" +
                "WHERE from_user_id = ? \n" +
                "   OR to_user_id = ?\n" +
                "ORDER BY transaction_time DESC;";


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setQueryTimeout(5);
            stmt.setObject(1, userID);
            stmt.setObject(2, userID);

            try
                    (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n ========== Transaction History ==========");
                boolean hasTransactions = false;
                while (rs.next()) {
                    hasTransactions = true;
                    String refNum = rs.getString("transaction_id");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    String type = rs.getString("type");
                    Timestamp time = rs.getTimestamp("transaction_time");

                    UUID fromUser = (UUID) rs.getObject("from_user_id");
                    UUID toUser = (UUID) rs.getObject("to_user_id");

                    System.out.println("Date: " + time);
                    System.out.println("Ref. # " + refNum);

                    if ("cashin".equalsIgnoreCase(type)){
                        System.out.println("Type: Cash-In");
                        System.out.println("Amount: +₱" + amount);
                    } else if (userID.equals(fromUser)) {
                        String recipientPhone = findPhoneByUserId(conn, toUser);
                        System.out.println("Type: Sent");
                        System.out.println("To: " + recipientPhone);
                        System.out.println("Amount -₱" + amount);
                    }
                    else if (userID.equals(toUser)){
                        String senderPhone = findPhoneByUserId(conn, fromUser);
                        System.out.println("Type: Received");
                        System.out.println("From: " + senderPhone);
                        System.out.println("Amount: +₱" + amount);
                    }
                    System.out.println("-------------------------------------------");
                }
                if (!hasTransactions){
                    System.out.println("No transactions found.");
                }

            } catch (SQLException | RuntimeException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

            private String findPhoneByUserId(Connection conn, UUID uuid) throws SQLException {
        String sql = "SELECT phone_number FROM users WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("phone_number");
                }
                return null;
            }
        }
    }
}
