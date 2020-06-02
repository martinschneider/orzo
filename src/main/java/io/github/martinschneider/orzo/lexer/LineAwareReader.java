package io.github.martinschneider.orzo.lexer;

import io.github.martinschneider.orzo.lexer.tokens.Location;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

public class LineAwareReader extends PushbackReader {
  public LineAwareReader(Reader in, int lines) {
    super(in);
    lineLengths = new int[lines];
  }

  private int line = 1;
  private int[] lineLengths;
  private int column;

  @Override
  public int read() throws IOException {
    int c = super.read();
    if (c == 10) {
      lineLengths[line] = column;
      line++;
      column = 0;
    } else {
      column++;
    }
    return c;
  }

  public void unread(char c) throws IOException {
    if (c == 10) {
      line--;
      column = lineLengths[line];
    } else {
      column--;
    }
    super.unread(c);
  }

  public Location getLoc() {
    return Location.of(line, column);
  }
}
