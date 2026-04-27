package gcash.app;
import gcash.app.utils.security.ReferenceNumberGenerator;
import gcash.app.view.AuthMenuView;

import static gcash.app.view.In.scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("| ========== Mini DigiBank ========== |");
        System.out.println();
        AuthMenuView.mainMenu();

        //TODO: transaction history
        //TODO: Implement In class/method to read inputs safely. Use Separate scanner class to accept input from all parts of the program
        //TODO: Implement MVC
        //TODO: Implement SOLID, DRY, SoC principles
    }
}
