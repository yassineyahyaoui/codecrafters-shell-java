import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static Path path = Paths.get("").toAbsolutePath();

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                System.out.print("$ ");
                String input = scanner.nextLine();
                String[] paths = System.getenv("PATH").split(File.pathSeparator);

                if (isExitCommand(input)) {
                    break;
                } else if (input.equals("pwd")) {
                    System.out.println(path.toString());
                } else if (input.split(" ")[0].equals("echo")) {
                    handleEcho(input);
                } else if (input.split(" ")[0].equals("type")) {
                    String arguments = input.substring(5);
                    handleType(arguments, paths);
                } else if (input.split(" ")[0].equals("cd")) {
                    handleCd(input.substring(3));
                } else {
                    handleExternalCommand(input, paths);
                }
            }
        } finally {
            scanner.close();
        }
    }

    private static boolean isExitCommand(String input) {
        return input.equals("exit 0");
    }

    private static void handleEcho(String input) {
        System.out.println(input.substring(5));
    }

    private static void handleCd(String argument) {
        Path p = Paths.get(argument);
        if (!p.isAbsolute()) {
            System.out.println("cd: " + argument + ": No such file or directory");
        } else {
            path = Paths.get(p.toString());
        }
    }

    private static void handleType(String arguments, String[] paths) {
        String[] allowedArguments = {"exit", "echo", "type", "pwd"};

        if (Arrays.asList(allowedArguments).contains(arguments)) {
            System.out.println(arguments + " is a shell builtin");
        } else {
            boolean found = false;
            for (String path : paths) {
                File file = new File(path, arguments);
                if (file.exists() && file.canExecute()) {
                    System.out.println(arguments + " is " + file.getAbsolutePath());
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println(arguments + ": not found");
            }
        }
    }

    private static void handleExternalCommand(String input, String[] paths) throws Exception {
        boolean found = false;
        List<String> arguments = new ArrayList<>();
        Collections.addAll(arguments, input.split(" "));
        for (String path : paths) {
            File file = new File(path, arguments.getFirst());
            if (file.exists() && file.canExecute()) {
                found = true;
                ProcessBuilder pb = new ProcessBuilder(arguments);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.getInputStream().transferTo(System.out);
                break;
            }
        }
        if (!found) {
            System.out.println(input + ": command not found");
        }
    }
}