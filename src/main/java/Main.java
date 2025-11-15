    import java.util.Scanner;

    public class Main {
        public static void main(String[] args) throws Exception {
            Scanner scanner = new Scanner(System.in);
            System.out.print("$ ");
            System.out.flush();

            String command = scanner.nextLine().trim();
            if (!command.isEmpty()) {
                System.out.println(command + ": command not found");
            }
        }
    }
