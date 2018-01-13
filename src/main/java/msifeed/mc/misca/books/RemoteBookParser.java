package msifeed.mc.misca.books;

public class RemoteBookParser {
    public static RemoteBook parse(String raw) {
        final RemoteBook book = new RemoteBook();

        final int firstLineEnd = raw.indexOf('\n');
        final String firstLine = raw.substring(0, firstLineEnd).trim();

        final boolean hasHeader = firstLine.startsWith("#!");
        if (hasHeader) {
            final String header = firstLine.substring(2);
            try {
                book.style = RemoteBook.Style.valueOf(header.toUpperCase());
            } catch (Exception ignored) {
            }

            final int secondLineEnd = raw.indexOf('\n', firstLineEnd + 2);
            book.title = raw.substring(firstLineEnd, secondLineEnd).trim();
            book.text = raw.substring(secondLineEnd);
        } else {
            book.title = firstLine;
            book.text = raw.substring(firstLineEnd);
        }

        return book;
    }
}
