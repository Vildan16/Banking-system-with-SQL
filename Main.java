package banking;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static String luhn(String res) {
        int[] arr = new int[res.length()];
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(String.valueOf(res.charAt(i)));
        }
        for (int i = 0; i < arr.length; i += 2) {
            arr[i] *= 2;
        }
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 9 )
                arr[i] -= 9;
            sum += arr[i];
        }
        if (sum % 10 == 0)
            return "0";
        return String.valueOf(10 - (sum % 10));
    }

    public static String generateNumber() {
        Random random = new Random();
        String res = "";
        res = res.concat("400000");
        res = res.concat(String.valueOf(random.nextInt(1000000000 - 100000000) + 100000000));
        res = res.concat(luhn(res));
        return res;
    }

    public static void createAccount(String dbName) {
        Random random = new Random();
        System.out.println("Your card has been created\nYour card number:");
        String number = generateNumber();
        System.out.println(number);
        System.out.println("Your card PIN:");
        int pin = random.nextInt(10000 - 1000) + 1000;
        System.out.println(pin);
        System.out.println();
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            Statement statement = con.createStatement();
            statement.executeUpdate("INSERT INTO card (number, pin) VALUES " +
                    "('" + number + "', '" + pin + "');");
            statement.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void login(String dbName) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your card number:");
        String number = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String pin = scanner.nextLine();
        String cardNumber;
        ResultSet set2;
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            Statement statement = con.createStatement();
            Statement statement2 = con.createStatement();
            ResultSet set = statement.executeQuery("SELECT * FROM card WHERE number = '" + number + "' AND pin = '" + pin + "';");
            if (set.next()) {
                System.out.println("You have successfully logged in!");
                while (true) {
                    set = statement.executeQuery("SELECT * FROM card WHERE number = '" + number + "' AND pin = '" + pin + "';");
                    System.out.println("1. Balance\n" +
                            "2. Add income\n" +
                            "3. Do transfer\n" +
                            "4. Close account\n" +
                            "5. Log out\n" +
                            "0. Exit");
                    int input = scanner.nextInt();
                    scanner.nextLine();
                    if (input == 0) {
                        System.out.println("Bye!");
                        con.close();
                        set.close();
                        System.exit(0);
                    }
                    if (input == 1)
                        System.out.println("Balance: " + set.getInt("balance"));
                    if (input == 2) {
                        System.out.println("Enter income:");
                        int money = scanner.nextInt();
                        set = statement.executeQuery("SELECT * FROM card WHERE number = '" + number + "' AND pin = '" + pin + "';");
                        statement.executeUpdate("UPDATE card SET balance = balance + " + money +
                                " WHERE id = " + set.getInt("id") + ";");
                        System.out.println("Income was added!");
                    }
                    if (input == 3) {
                        System.out.println("Transfer\nEnter card number:");
                        cardNumber = scanner.nextLine();
                        if (cardNumber.equals(set.getString("number")))
                            System.out.println("You can't transfer money to the same account!");
                        else if (!luhn(cardNumber).equals("0"))
                            System.out.println("Probably you made a mistake in the card number. Please try again!");
                        else {
                            try {
                                set2 = statement2.executeQuery("SELECT * FROM card WHERE number = '" + cardNumber + "';");
                                set = statement.executeQuery("SELECT * FROM card WHERE number = '" + number + "' AND pin = '" + pin + "';");
                                if (set2.next()) {
                                    System.out.println(set2.getString("number"));
                                    System.out.println("Enter how much money you want to transfer:");
                                    int m = scanner.nextInt();
                                    scanner.nextLine();
                                    System.out.println();
                                    if (m > set.getInt("balance")) {
                                        System.out.println("Not enough money!");
                                    }
                                    else {
                                        set = statement.executeQuery("SELECT * FROM card WHERE number = '" + number + "' AND pin = '" + pin + "';");
                                        statement.executeUpdate("UPDATE card SET balance = balance - " + m +
                                                " WHERE id = " + set.getInt("id") + ";");
                                        set2 = statement.executeQuery("SELECT * FROM card WHERE number = '" +
                                                cardNumber + "';");
                                        statement.executeUpdate("UPDATE card SET balance = balance + " + m +
                                                " WHERE id = " + set2.getInt("id") + ";");
                                        System.out.println("Success!");
                                    }
                                } else {
                                    System.out.println("Such a card does not exist.");
                                }
                                set2.close();
                                statement2.close();
                                statement.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (input == 4) {
                        statement.executeUpdate("DELETE FROM card WHERE id = " + set.getInt("id") + ";");
                        System.out.println("The account has been closed!");
                        break;
                    }
                    if (input == 5)
                        break;
                }
            }
            else
                System.out.println("Wrong card number or PIN!");
            set.close();
            statement.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void createTable(String dbName) {
        Connection con;
        Statement statement;
        String url = "jdbc:sqlite:" + dbName;
        String sql = "CREATE TABLE IF NOT EXISTS card (" +
                    "id INTEGER PRIMARY KEY, " +
                    "number TEXT, " +
                    "pin TEXT, " +
                    "balance INTEGER DEFAULT 0);";
        try {
            con = DriverManager.getConnection(url);
            statement = con.createStatement();
            statement.execute(sql);
            statement.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String dbName = args[1];
        createTable(dbName);
        int input;
        while (true) {
            System.out.println("1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit");
            input = scanner.nextInt();
            System.out.println();
            if (input == 0) {
                System.out.println("Bye!");
                break;
            }
            if (input == 1)
                createAccount(dbName);
            if (input == 2)
                login(dbName);
        }
    }
}