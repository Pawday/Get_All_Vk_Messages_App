package per.pawday.getAllVkMessagesApp.json;

public class Formatter
{
    public String formatJSONStr(final String json_str, final int indent_width)
    {
        final char[] chars = json_str.toCharArray();
        final String newline = System.lineSeparator();

        StringBuilder ret = new StringBuilder();
        boolean begin_quotes = false;

        for (int i = 0, indent = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\"')
            {
                ret.append(c);
                begin_quotes = !begin_quotes;
                continue;
            }

            if (!begin_quotes)
            {
                switch (c)
                {
                    case '{':
                    case '[':
                        ret.append(c).append(newline).append(String.format(new StringBuilder().append("%").append(indent += indent_width).append("s").toString(), ""));
                        continue;
                    case '}':
                    case ']':
                        ret.append(newline).append((indent -= indent_width) > 0 ? String.format(new StringBuilder().append("%").append(indent).append("s").toString(), "") : "").append(c);
                        continue;
                    case ':':
                        ret.append(c).append(" ");
                        continue;
                    case ',':
                        ret.append(c).append(newline).append(indent > 0 ? String.format(new StringBuilder().append("%").append(indent).append("s").toString(), "") : "");
                        continue;
                    default:
                        if (Character.isWhitespace(c)) continue;
                }
            }

            ret.append(c).append(c == '\\' ? "" + chars[++i] : "");
        }

        return ret.toString();
    }
}
