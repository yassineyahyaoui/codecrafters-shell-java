import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class Main {
    static Path path = Paths.get("").toAbsolutePath();

    public static void main(String[] args) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
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
        }
    }

    private static boolean isExitCommand(String input) {
        return input.equals("exit 0");
    }

    private static void handleEcho(String input) {
        String s = input.substring(5);
        System.out.println(handleArgumentParsing(s));
    }

    private static void handleCd(String argument) {
        if (argument.equals("~")) {
            if (System.getenv("HOME") != null) {
                path = Paths.get(System.getenv("HOME"));
            } else {
                path = Paths.get(System.getenv("USERPROFILE"));
            }
        } else {
            Path p = Paths.get(argument);
            if (!p.isAbsolute()) {
                p = path.resolve(argument);
            }
            File file = p.toFile();
            if (!file.isDirectory()) {
                System.out.println("cd: " + argument + ": No such file or directory");
            } else {
                path = p.normalize();
            }
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
        input = handleArgumentParsing(input);
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

    private static String handleArgumentParsing(String input) {
        Matcher matcher = Pattern.compile("'([^']*)'|[^'\\s]+").matcher(input);
        StringBuilder res = new StringBuilder();

        int prevEnd = -1;
        while (matcher.find()) {
            String word = matcher.group();
            if (word.startsWith("'") && word.endsWith("'")) {
                word = word.substring(1, word.length() - 1);
            }

            if (!res.isEmpty()) {
                if (matcher.start() > prevEnd) {
                    res.append(" ");
                }
            }

            res.append(word);
            prevEnd = matcher.end();
        }

        return res.toString();
    }
}