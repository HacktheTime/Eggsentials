package de.hype.eggsentials.client.common.chat;

import de.hype.eggsentials.client.common.api.Formatting;
import de.hype.eggsentials.client.common.client.BBsentials;
import de.hype.eggsentials.client.common.mclibraries.EnvironmentCore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {

    //{"strikethrough":false,"extra":[{"strikethrough":false,"clickEvent":{"action":"run_command","value":"/viewprofile 4fa1228c-8dd6-47c4-8fe3-b04b580311b8"},"hoverEvent":{"action":"show_text","contents":{"strikethrough":false,"text":"§eClick here to view §bHype_the_Time§e's profile"}},"text":"§9Party §8> §b[MVP§2+§b] Hype_the_Time§f: "},{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"text":"h:test"}],"text":""}// {"strikethrough":false,"extra":[{"strikethrough":false,"clickEvent":{"action":"run_command","value":"/viewprofile f772b2c7-bd2a-46e1-b1a2-41fa561157d6"},"hoverEvent":{"action":"show_text","contents":{"strikethrough":false,"text":"§eClick here to view §bShourtu§e's profile"}},"text":"§9Party §8> §b[MVP§c+§b] Shourtu§f: "},{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"text":"Hype_the_Time TEST"}],"text":""}
    //{"strikethrough":false,"extra":[{"strikethrough":false,"clickEvent":{"action":"run_command","value":"/viewprofile 4fa1228c-8dd6-47c4-8fe3-b04b580311b8"},"hoverEvent":{"action":"show_text","contents":{"strikethrough":false,"text":"§eClick here to view §bHype_the_Time§e's profile"}},"text":"§9Party §8> §b[MVP§2+§b] Hype_the_Time§f: "},{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"text":"h:test"}],"text":""}
    private final Map<String, Instant> partyDisbandedMap = new HashMap<>();
    private String lastPartyDisbandedUsername = null;

    public static String[] getVariableNames(String packageName, String className) {
        List<String> variableInfoList = new ArrayList<>();

        // Combine the class name with the package name
        String fullClassName = packageName + "." + className;

        // Load the class
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Extract fields of the class
        Field[] fields = clazz.getDeclaredFields();

        // Collect information for each field
        for (Field field : fields) {
            // Exclude transient fields
            if (java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            String variableName = field.getName();
            String variablePackageName = clazz.getPackage().getName();
            String variableClassName = clazz.getSimpleName();

            variableInfoList.add(variableName);
        }

        return variableInfoList.toArray(new String[variableInfoList.size()]);
    }

    public static void setVariableValue(String className, String variableName, String value) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        if (value == null) {
            sendPrivateMessageToSelfError("Invalid value: null");
            return;
        }

        String fullClassName = "de.hype.eggsentials.client.common.config" + "." + className;
        Object obj = null;
        Class<?> clazz = Class.forName(fullClassName);
        Field field = clazz.getDeclaredField(variableName);
        field.setAccessible(true);

        Class<?> fieldType = field.getType();
        Object convertedValue = parseValue(value, fieldType);

        if (Modifier.isStatic(field.getModifiers())) {
            field.set(null, convertedValue);
        }
        else {
            obj = clazz.getDeclaredConstructor().newInstance();
            field.set(obj, convertedValue);
        }

        sendPrivateMessageToSelfSuccess("The variable " + field.getName() + " is now: " + field.get(obj));
    }

    public static void getVariableValue(String className, String variableName) {
        String fullClassName = "de.hype.eggsentials.client.common.config" + "." + className;

        try {
            Class<?> clazz = Class.forName(fullClassName);
            Field field = clazz.getDeclaredField(variableName);
            field.setAccessible(true);

            Object obj = clazz.getDeclaredConstructor().newInstance();
            sendPrivateMessageToSelfSuccess("The variable " + field.getName() + " is: " + field.get(obj));
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | InstantiationException |
                 InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static Object parseValue(String value, Class<?> targetType) {
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        }
        else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        else {
            // For other types, return the original string value
            return value;
        }
    }

    public static void sendPrivateMessageToSelfError(String message) {
        sendPrivateMessageToSelfBase(message, Formatting.RED);
    }

    public static void sendPrivateMessageToSelfFatal(String message) {
        sendPrivateMessageToSelfBase(message, Formatting.DARK_RED);
    }

    public static void sendPrivateMessageToSelfSuccess(String message) {
        sendPrivateMessageToSelfBase(message, Formatting.GREEN);
    }

    public static void sendPrivateMessageToSelfInfo(String message) {
        sendPrivateMessageToSelfBase(message, Formatting.YELLOW);
    }

    public static void sendPrivateMessageToSelfImportantInfo(String message) {
        sendPrivateMessageToSelfBase(message, Formatting.GOLD);
    }

    public static void sendPrivateMessageToSelfDebug(String message) {
        sendPrivateMessageToSelfBase(message, Formatting.AQUA);
    }

    public static void sendPrivateMessageToSelfBase(String message, Formatting formatting) {
        sendPrivateMessageToSelfBase(message, formatting.toString());
    }

    public static void sendPrivateMessageToSelfBase(String message, String formatting) {
        EnvironmentCore.chat.sendClientSideMessage(Message.of(formatting.toString() + message.replace("§r", "§r" + formatting)), false);
    }

    public static void sendPrivateMessageToSelfText(Message message) {
        EnvironmentCore.chat.sendClientSideMessage(message);
    }

    public static void sendCommand(String s) {
        BBsentials.sender.addSendTask(s);
    }

    public Message onEvent(Message text, boolean actionbar) {
        if (!actionbar && !isSpam(text.getString())) {
            if (BBsentials.developerConfig.isDetailedDevModeEnabled()) {
                System.out.println("got a message: " + text.getJson());
            }
            BBsentials.executionService.execute(() -> processThreaded(text));
            return processNotThreaded(text, actionbar);
        }
        return text; // Return the original message if it is spam
    }

    //Handle in the messages which need to be modified here
    public Message processNotThreaded(Message message, boolean actionbar) {
        if (actionbar) return message;
        return message;
    }

    public void processThreaded(Message message) {
        if (message.getString() != null) {
            String messageUnformatted = message.getUnformattedString();
            String username = message.getPlayerName();
//            else if (!EnvironmentCore.utils.isWindowFocused()) {
//
//            }
//            else if (message.isServerMessage()) {
//            }
//            else if (message.isFromGuild()) {
//            }
//            else if (message.isFromParty()) {
//            }
//            else if (message.isMsg()) {
//            }

        }
    }

    public boolean isSpam(String message) {
        if (message == null) return true;
        if (message.isEmpty()) return true;
        if (message.contains("Achievement Points")) return true;
        return false;
    }

    public void sendNotification(String title, String text) {
        sendNotification(title, text, 1);
    }

    public void sendNotification(String title, String text, float volume) {
        BBsentials.executionService.execute(() -> {
            EnvironmentCore.utils.playCustomSound("/sounds/mixkit-sci-fi-confirmation-914.wav", 0);
        });
        List<String> argsList = new ArrayList<>();
        argsList.add("--title");
        argsList.add(title);
        argsList.add("--passivepopup");
        argsList.add(text);
        argsList.add("5");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("kdialog");
            processBuilder.command().addAll(argsList);

            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
