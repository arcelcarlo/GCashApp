package gcash.app.repository;

import gcash.app.config.DatabaseConnection;
import gcash.app.model.Balance;
import gcash.app.model.Users;
import gcash.app.view.ProgressBar;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static gcash.app.view.In.scanner;

public class CashInDAO {
    public void cashIn(Users user, Balance balance, BigDecimal amount) throws InterruptedException, SQLException {



        ProgressBar.progressBar();
        System.out.println();

        String getBalanceSql = "SELECT amount FROM balances WHERE user_id = ?";
        String updateBalanceSql = "UPDATE balances SET amount = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()){
            conn.setAutoCommit(false);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                conn.rollback();
                System.out.println("Amount must be greater than 0!");
                return;
            }

            if (amount.compareTo(new BigDecimal("50000")) > 0) {
                conn.rollback();
                System.out.println("Amount must be less than 50,000!");
                return;
            }


            BigDecimal currentBalance = BigDecimal.ZERO;

            try(PreparedStatement getStmt = conn.prepareStatement(getBalanceSql)){
                getStmt.setObject(1, user.getUuid());
                try (ResultSet rs = getStmt.executeQuery()){
                    if (rs.next()){
                        currentBalance = rs.getBigDecimal("amount");

                    }
                    else {
                        conn.rollback();
                        System.out.println("Cash in failed. Try again later!");
                        return;
                    }
                }
            }
            BigDecimal newBalance = currentBalance.add(amount);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceSql)){
                updateStmt.setBigDecimal(1, newBalance);
                updateStmt.setObject(2, user.getUuid());

                int rows = updateStmt.executeUpdate();

                if (rows > 0){
                    TransactionHistoryDAO transactionHistoryDAO = new TransactionHistoryDAO();
                    conn.commit();
                    System.out.println("Cash-in successful. New balance: ₱" + newBalance);
                    balance.setAmount(newBalance);
                    transactionHistoryDAO.cashInTransactionHistory(conn, user.getUuid(),amount);
                    scanner.nextLine();
                }
                else {
                    conn.rollback();
                    System.out.println("Cash-in failed. Try again later!");
                }
            }
        }
    }
}
