package net.coreprotect.utility;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.bukkit.command.ConsoleCommandSender;

import net.coreprotect.language.Phrase;

public class ChatUtils {

    private ChatUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getCoordinates(String command, int worldId, int x, int y, int z, boolean displayWorld, boolean italic) {
        StringBuilder message = new StringBuilder("<click:run_command:");

        StringBuilder worldDisplay = new StringBuilder();
        if (displayWorld) {
            worldDisplay.append("/").append(WorldUtils.getWorldName(worldId));
        }

        // command
        DecimalFormat decimalFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ROOT));
        message.append("'/" + command + " teleport wid:" + worldId + " " + decimalFormat.format(x + 0.50) + " " + y + " " + decimalFormat.format(z + 0.50) + "'>");

        // chat output
        message.append(Color.GREY + (italic ? Color.ITALIC : "") + "(x" + x + "/y" + y + "/z" + z + worldDisplay + ")");

        return message.append("</click>").toString();
    }

    public static String getPageNavigation(String command, int page, int totalPages) {
        StringBuilder message = new StringBuilder();

        // back arrow
        String backArrow = "";
        if (page > 1) {
            backArrow = "◀ ";
            backArrow = "<click:run_command:/" + command + " l " + (page - 1) + ">" + backArrow + "</click>";
        }

        // next arrow
        String nextArrow = " ";
        if (page < totalPages) {
            nextArrow = " ▶ ";
            nextArrow = "<click:run_command:/" + command + " l " + (page + 1) + ">" + nextArrow + "</click>";
        }

        StringBuilder pagination = new StringBuilder();
        if (totalPages > 1) {
            pagination.append(Color.GREY + "(");
            if (page > 3) {
                pagination.append(Color.WHITE + "<click:run_command:/" + command + " l " + 1 + ">1</click>");
                if (page > 4 && totalPages > 7) {
                    pagination.append(" <click:suggest_command:/" + command + " l >" + Color.GREY + "...</click> ");
                }
                else {
                    pagination.append(Color.GREY + "| ");
                }
            }

            int displayStart = (page - 2) < 1 ? 1 : (page - 2);
            int displayEnd = (page + 2) > totalPages ? totalPages : (page + 2);
            if (page > 999 || (page > 101 && totalPages > 99999)) { // limit to max 5 page numbers
                displayStart = (displayStart + 1) < displayEnd ? (displayStart + 1) : displayStart;
                displayEnd = (displayEnd - 1) > displayStart ? (displayEnd - 1) : displayEnd;
                if (displayStart > (totalPages - 3)) {
                    displayStart = (totalPages - 3) < 1 ? 1 : (totalPages - 3);
                }
            }
            else { // display at least 7 page numbers
                if (displayStart > (totalPages - 5)) {
                    displayStart = (totalPages - 5) < 1 ? 1 : (totalPages - 5);
                }
                if (displayEnd < 6) {
                    displayEnd = 6 > totalPages ? totalPages : 6;
                }
            }

            if (page > 99999) { // limit to max 3 page numbers
                displayStart = (displayStart + 1) < displayEnd ? (displayStart + 1) : displayStart;
                displayEnd = (displayEnd - 1) >= displayStart ? (displayEnd - 1) : displayEnd;
                if (page == (totalPages - 1)) {
                    displayEnd = totalPages - 1;
                }
                if (displayStart < displayEnd) {
                    displayStart = displayEnd;
                }
            }

            if (page > 3 && displayStart == 1) {
                displayStart = 2;
            }

            for (int displayPage = displayStart; displayPage <= displayEnd; displayPage++) {
                if (page != displayPage) {
                    pagination.append(Color.WHITE + "<click:run_command:/" + command + " l " + displayPage + ">" + displayPage + (displayPage < totalPages ? " " : "") + "</click>");
                }
                else {
                    pagination.append(Color.WHITE + Color.UNDERLINE + displayPage + Color.RESET + (displayPage < totalPages ? " " : ""));
                }
                if (displayPage < displayEnd) {
                    pagination.append(Color.GREY + "| ");
                }
            }

            if (displayEnd < totalPages) {
                if (displayEnd < (totalPages - 1)) {
                    pagination.append("<click:suggest_command:/" + command + " l >" + Color.GREY + "...</click> ");
                }
                else {
                    pagination.append(Color.GREY + "| ");
                }
                if (page != totalPages) {
                    pagination.append(Color.WHITE + "<click:run_command:/" + command + " l " + totalPages + ">" + totalPages + "</click>");
                }
                else {
                    pagination.append(Color.WHITE + Color.UNDERLINE + totalPages);
                }
            }

            pagination.append(Color.GREY + ")");
        }

        return message.append(Color.WHITE + backArrow + Color.DARK_AQUA + Phrase.build(Phrase.LOOKUP_PAGE, Color.WHITE + page + "/" + totalPages) + nextArrow + pagination).toString();
    }

    public static String getTimeSince(long resultTime, long currentTime, boolean component) {
        StringBuilder message = new StringBuilder();
        double timeSince = currentTime - (resultTime + 0.00);
        if (timeSince < 0.00) {
            timeSince = 0.00;
        }

        // minutes
        timeSince = timeSince / 60;
        if (timeSince < 60.0) {
            message.append(Phrase.build(Phrase.LOOKUP_TIME, new DecimalFormat("0.00").format(timeSince) + "/m"));
        }

        // hours
        if (message.length() == 0) {
            timeSince = timeSince / 60;
            if (timeSince < 24.0) {
                message.append(Phrase.build(Phrase.LOOKUP_TIME, new DecimalFormat("0.00").format(timeSince) + "/h"));
            }
        }

        // days
        if (message.length() == 0) {
            timeSince = timeSince / 24;
            message.append(Phrase.build(Phrase.LOOKUP_TIME, new DecimalFormat("0.00").format(timeSince) + "/d"));
        }

        if (component) {
            Date logDate = new Date(resultTime * 1000L);
            String formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(logDate);

            return "<hover:show_text:'<gray>" + formattedTimestamp + "'><gray>" + message + "</hover>";
        }

        return message.toString();
    }

    public static String createTooltip(String phrase, String tooltip) {
        if (tooltip.isEmpty()) {
            return phrase;
        }

        return "<hover:show_text:'" + tooltip + "'>" + phrase + "</hover>";
    }
} 