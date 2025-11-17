import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                System.out.print("$ ");
                String input = scanner.nextLine();
                String[] paths = System.getenv("PATH").split(File.pathSeparator);

                if (input.equals("exit 0")) break;

                else if (input.equals("pwd")) {
                    System.out.println(Paths.get("").toAbsolutePath().toString());
                }

                else if (input.split(" ")[0].equals("echo")) {
                    System.out.println(input.substring(5));

                } else if (input.split(" ")[0].equals("type")) {
                    String arguments = input.substring(5);
                    String[] allowedArguments = {"exit", "echo", "type"};

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

                } else {
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
        } finally {
            scanner.close();
        }
    }
}