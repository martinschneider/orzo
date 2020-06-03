package io.github.martinschneider.orzo.lexer;

import io.github.martinschneider.orzo.lexer.tokens.Location;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

public class LineAwareReader extends PushbackReader {
  public LineAwareReader(Reader in) {
    super(in);
    lineLengths = new int[128];
  }

  private int line = 1;
  private int[] lineLengths;
  private int column;

  @Override
  public int read() throws IOException {
    int c = super.read();
    if (c == 10) {
      if (line >= lineLengths.length) {
        resize();
      }
      lineLengths[line] = column;
      line++;
      column = 0;
    } else {
      column++;
    }
    return c;
  }

  private void resize() {
    int[] tmp = new int[lineLengths.length * 2];
    for (int i = 1; i < lineLengths.length; i++) {
      tmp[i] = lineLengths[i];
    }
    lineLengths = tmp;
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
