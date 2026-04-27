package gcash.app.repository;

import gcash.app.config.DatabaseConnection;
import gcash.app.model.TransactionTypes;
import gcash.app.model.Transactions;
import gcash.app.model.Users;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import static gcash.app.view.In.scanner;


public class TransactionHistoryDAO {

    public void cashInTransactionHistory(Connection conn, UUID userId, BigDecimal amount) throws RuntimeException {
        Transactions transactions = new Transactions();
        String sql = "INSERT INTO transactions " +
                "(transaction_id, amount, type, to_user_id, transaction_time) " +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(sql);

            stmt.setObject(1, transactions.getRefNum());
            stmt.setBigDecimal(2, amount);
            stmt.setString(3, "cashin");
            stmt.setObject(4, userId);
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(transactions.getUpdatedAt()));

            int transactionRows = stmt.executeUpdate();

            if (transactionRows > 0) {
                conn.commit();
                System.out.println("Ref. #: " + transactions.getRefNum() + "\n\n");
            } else {
                conn.rollback();
                System.out.println("Cash-in transaction NOT recorded. Try again later!");
            }


        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }


    }

    public void sendMoneyTransactionHistory(String senderPhone, String recipientPhone, BigDecimal amount) {
        Transactions transactions = new Transactions();

        String sql = "INSERT INTO transactions " +
                "(transaction_id, amount, type, from_user_id, to_user_id, transaction_time) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            UUID senderUuid = findUserIdByPhone(conn, senderPhone);
            UUID recipientUuid = findUserIdByPhone(conn, recipientPhone);

            if (senderUuid == null || recipientUuid == null) {
                conn.rollback();
                System.out.println("Sender or recipient not found.");
                return;
            }

            stmt = conn.prepareStatement(sql);

            stmt.setObject(1, transactions.getRefNum());
            stmt.setBigDecimal(2, amount);
            stmt.setString(3, "transfer");
            stmt.setObject(4, senderUuid);
            stmt.setObject(5, recipientUuid);
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(transactions.getUpdatedAt()));

            int transactionRows = stmt.executeUpdate();

            if (transactionRows > 0) {
                conn.commit();
                System.out.println("Ref. #: " + transactions.getRefNum() + "\n\n");
            } else {
                conn.rollback();
                System.out.println("Cash-in transaction NOT recorded. Try again later!");
            }


        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

    private UUID findUserIdByPhone(Connection conn, String phoneNumber) throws SQLException {
        String sql = "SELECT id FROM users WHERE phone_number = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("id");
                }
                return null;
            }
        }
    }
}
