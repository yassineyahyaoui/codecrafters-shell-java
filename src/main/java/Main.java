import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if (input.equals("exit 0")) break;
            else if (input.split(" ")[0].equals("echo")) {
                System.out.println(input.substring(5));

            } else if (input.split(" ")[0].equals("type")) {
                String arguments = input.substring(5);
                String[] allowedArguments = {"exit", "echo", "type"};

                if (Arrays.asList(allowedArguments).contains(arguments)) {
                    System.out.println(arguments + " is a shell builtin");
                }

                String[] paths = System.getenv("PATH").split(File.pathSeparator);
                for (String path : paths) {
                    File file = new File(path, arguments);
                    if (file.exists()) {
                        System.out.println(arguments + " is " + file.getAbsolutePath());
                    }
                }

                System.out.println(arguments + ": not found");

            } else {
                System.out.println(input + ": command not found");
            }

            scanner.close();
        }
    }
}