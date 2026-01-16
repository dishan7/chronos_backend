package com.chronos.job_scheduler.config;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class CommandValidator {

    private static final Set<String> ALLOWED_BASE_COMMANDS = Set.of(
            "echo",
            "ls",
            "pwd",
            "date",
            "cat",
            "java",
            "javac",
            "python",
            "python3"
    );

    private static final List<String> FORBIDDEN_PATTERNS = List.of(
            "&&",
            "||",
            ";",
            "|",
            ">",
            "<",
            "`",
            "$(",
            "../",
            "sudo",
            "rm ",
            "shutdown",
            "reboot",
            ":(){",        // fork bomb
            "mkfs",
            "dd "
    );

    public void validate(String command){
        if(command == null || command.trim().isEmpty()){
            throw new IllegalArgumentException("Command is empty");
        }
        if(command.length() > 500){
            throw new IllegalArgumentException("Command too long");
        }
        String normalized = command.trim().toLowerCase();
        for(String forbidden: FORBIDDEN_PATTERNS){
            if(normalized.contains(forbidden)){
                throw new IllegalArgumentException(
                        "Forbidden command pattern detected: " + forbidden
                );
            }
        }
        String baseCommand = normalized.split("\\s+")[0];
        if (!ALLOWED_BASE_COMMANDS.contains(baseCommand)) {
            throw new IllegalArgumentException(
                    "Command not allowed: " + baseCommand
            );
        }
        validateRuntimeSpecificRules(baseCommand, command);
    }
    private void validateRuntimeSpecificRules(String baseCommand, String command) {

        if ((baseCommand.equals("python") || baseCommand.equals("python3"))
                && command.contains(" -c ")) {
            throw new IllegalArgumentException("Inline python execution (-c) not allowed");
        }

        if (baseCommand.equals("java")) {
            if (command.contains("-javaagent")
                    || command.contains("-agentlib")
                    || command.contains("-classpath")
                    || command.contains("-cp")) {
                throw new IllegalArgumentException("Unsafe java options not allowed");
            }
        }
    }
}
