import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.*;

public class Main {
    static Path path = Paths.get("").toAbsolutePath();

    public static void main(String[] args) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("$ ");
                String input = scanner.nextLine();

                if (isExitCommand(input)) {
                    break;
                } else {
                    handleRedirection(input);
                }
            }
        }
    }

    private static void handleRedirection(String input) throws Exception {
        if (input.contains(">")) {
            boolean isStderr = false;
            List<String> arguments = parseArgumentsList(input);
            int redirectionPosition = arguments.indexOf(">");
            String[] operators = {"1>>", "2>>", ">>", "1>", "2>"};
            for (String op : operators) {
                if (input.contains(op)) {
                    redirectionPosition = arguments.indexOf(op);
                    isStderr = op.startsWith("2");
                    break;
                }
            }
            String result = handleCommand(String.join(" ", arguments.subList(0, redirectionPosition)), isStderr);
            if (redirectionPosition < arguments.size() - 1) {
                Path outputPath = Paths.get(arguments.get(redirectionPosition + 1));

                if (result.endsWith("\n")) {
                    result = result.substring(0, result.length() - 1);
                }

                if (isStderr && (arguments.getFirst().equals("echo") || arguments.getFirst().equals("type") || arguments.getFirst().equals("cd"))) {
                    Files.writeString(outputPath, "");
                } else {
                    if (input.contains(">>")) {
                        if (Files.exists(outputPath) && Files.size(outputPath) > 0) {
                            Files.writeString(outputPath, "\n" + result, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } else {
                            Files.writeString(outputPath, result, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }
                    } else {
                        Files.writeString(outputPath, result);
                    }
                }
            }
        } else {
            if (!handleCommand(input).isEmpty()) {
                if (handleCommand(input).endsWith("\n")) {
                    System.out.print(handleCommand(input));
                } else {
                    System.out.println(handleCommand(input));
                }
            }
        }
    }

    private static String handleCommand(String input, boolean isStderr) throws Exception {
        String[] paths = System.getenv("PATH").split(File.pathSeparator);
        if (input.equals("pwd")) {
            return path.toString();
        } else if (input.split(" ")[0].equals("echo")) {
            return handleEcho(input, isStderr);
        } else if (input.split(" ")[0].equals("type")) {
            return handleType(input.substring(5), paths);
        } else if (input.split(" ")[0].equals("cd")) {
            if (!handleCd(input.substring(3))) {
                return "cd: " + input.substring(3) + ": No such file or directory";
            } else {
                return "";
            }
        } else {
            return handleExternalCommand(input, paths, isStderr);
        }
    }

    private static String handleCommand(String input) throws Exception {
        return handleCommand(input, false);
    }

    private static boolean isExitCommand(String input) {
        return input.equals("exit 0") || input.equals("exit");
    }

    private static String handleEcho(String input, boolean isStderr) {
        String s = input.substring(5);
        List<String> args = parseArgumentsList(s);
        if (isStderr) {
            System.out.println(String.join(" ", args));
        }
        return String.join(" ", args);
    }

    // returns if the cd worked or not
    private static boolean handleCd(String argument) {
        if (argument.equals("~")) {
            if (System.getenv("HOME") != null) {
                path = Paths.get(System.getenv("HOME"));
            } else {
                path = Paths.get(System.getenv("USERPROFILE"));
            }
            return true;
        } else {
            Path p = Paths.get(argument);
            if (!p.isAbsolute()) {
                p = path.resolve(argument);
            }
            File file = p.toFile();
            if (!file.isDirectory()) {
                return false;
            } else {
                path = p.normalize();
                return true;
            }
        }
    }

    private static String handleType(String arguments, String[] paths) {
        String[] allowedArguments = {"exit", "echo", "type", "pwd", "history"};

        if (Arrays.asList(allowedArguments).contains(arguments)) {
            return arguments + " is a shell builtin";
        } else {
            for (String path : paths) {
                File file = new File(path, arguments);
                if (file.exists() && file.canExecute()) {
                    return arguments + " is " + file.getAbsolutePath();
                }
            }
            return arguments + ": not found";
        }
    }

    private static String handleExternalCommand(String input, String[] paths, boolean isStderr) throws Exception {
        List<String> arguments = parseArgumentsList(input);
        for (String path : paths) {
            File file = new File(path, arguments.getFirst());
            if (file.exists() && file.canExecute()) {
                ProcessBuilder pb = new ProcessBuilder(arguments);
                Process process = pb.start();

                String stdout = new String(process.getInputStream().readAllBytes());

                String stderr = new String(process.getErrorStream().readAllBytes());

                if (!isStderr) {
                    if (!stderr.isEmpty()) {
                        System.out.print(stderr);
                    }
                    return stdout;
                } else {
                    if (!stdout.isEmpty()) {
                        System.out.print(stdout);
                    }
                    return stderr;
                }
            }
        }
        return input.split(" ")[0] + ": command not found";
    }

    private static List<String> parseArgumentsList(String input) {
        List<String> arguments = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"|'([^']*)'|(?:\\\\.|[^\\s\"'])+").matcher(input);

        StringBuilder currentArg = new StringBuilder();
        int prevEnd = -1;

        while (matcher.find()) {
            String word = matcher.group();
            if ((word.startsWith("'") && word.endsWith("'")) || (word.startsWith("\"") && word.endsWith("\""))) {
                word = word.substring(1, word.length() - 1);
            }

            // Inside double quotes: only process \\ and \"
            if (matcher.group(1) != null) {
                word = word.replace("\\\"", "\"").replace("\\\\", "\\");
            }
            // Outside quotes: process all backslash escapes
            else if (matcher.group(2) == null) {
                word = word.replaceAll("\\\\(.)", "$1");
            }

            // If there's space between tokens, start a new argument
            if (matcher.start() > prevEnd && prevEnd != -1) {
                if (!currentArg.isEmpty()) {
                    arguments.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            }

            currentArg.append(word);
            prevEnd = matcher.end();
        }

        // Add the last argument if any
        if (!currentArg.isEmpty()) {
            arguments.add(currentArg.toString());
        }

        return arguments;
    }
}