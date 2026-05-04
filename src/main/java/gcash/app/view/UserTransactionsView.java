package gcash.app.view;
import gcash.app.model.Balance;
import gcash.app.model.Transactions;
import gcash.app.model.Users;
import gcash.app.repository.*;

import javax.swing.text.View;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

import static gcash.app.view.In.scanner;


public class UserTransactionsView {


    public static void userCashInView(Users user) throws SQLException, InterruptedException {
        System.out.println("| ============= Cash-In ============= |\n");
        Balance balance = new Balance();
        CashInDAO cashInDAO = new CashInDAO();

        System.out.println("How much would you like to cash in?");
        BigDecimal amount = scanner.nextBigDecimal();
        cashInDAO.cashIn(user, balance, amount);

    }

    public static void userSendMoneyView(Users user) throws InterruptedException, SQLException {
        SendMoneyDAO sendMoneyDAO = new SendMoneyDAO();
        String senderNumber = user.getPhoneNumber();
        String recipientNumber;
        System.out.println("| =========== Send Money =========== |\n");
        System.out.print("How much would you like to send?: ");
        BigDecimal amount = scanner.nextBigDecimal();
        scanner.nextLine();
        System.out.print("Enter the number of the recipient: ");
        recipientNumber = scanner.nextLine();
        sendMoneyDAO.sendMoney(senderNumber, recipientNumber, amount);

    }

    public static void userBalanceView(Users user) throws InterruptedException, SQLException {
        Balance balance = new Balance();
        CheckBalanceDAO checkBalanceDAO = new CheckBalanceDAO();
        checkBalanceDAO.checkBalance(user, balance);
    }

    public static void userTransactionHistoryView(Users user) throws SQLException {
        ViewTransactionsDAO viewTransactionsDAO = new ViewTransactionsDAO();
        viewTransactionsDAO.viewTransactions(user.getUuid());
    }
}
