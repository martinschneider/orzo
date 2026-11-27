package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.QUESTION;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;

import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.EOF;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * "assert cond;" and "assert cond : message;" become (like javac does)
 *
 * <pre>
 * { if (!$assertionsDisabled && !(cond)) throw new AssertionError(message); }
 * </pre>
 *
 * (a block, so that a following "else" isn't this if's), where $assertionsDisabled is a static
 * field of the class (see ClassParser.addAssertionsField).
 */
public class AssertParser implements ProdParser<Statement> {
  public static final String FIELD = "$assertionsDisabled";
  private ParserContext ctx;

  public AssertParser(ParserContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Statement parse(TokenList tokens) {
    Token start = tokens.curr();
    if (!(start instanceof Identifier) || !"assert".equals(start.val.toString())) {
      return null;
    }
    // the ":" before the message and the ";" ending the statement (outside of brackets and
    // conditional expressions)
    int colon = -1;
    int end = -1;
    int depth = 0;
    int conditionals = 0;
    for (int i = tokens.idx() + 1; end < 0 && !(tokens.get(i) instanceof EOF); i++) {
      Token token = tokens.get(i);
      if (token.eq(sym(LPAREN)) || token.eq(sym(LBRACE)) || token.eq(sym(LBRAK))) {
        depth++;
      } else if (token.eq(sym(RPAREN)) || token.eq(sym(RBRACE)) || token.eq(sym(RBRAK))) {
        depth--;
      } else if (depth == 0 && token.eq(sym(QUESTION))) {
        conditionals++;
      } else if (depth == 0 && token.eq(sym(COLON))) {
        if (conditionals > 0) {
          conditionals--;
        } else if (colon < 0) {
          colon = i;
        }
      } else if (depth == 0 && token.eq(sym(SEMICOLON))) {
        end = i;
      }
    }
    if (end < 0 || end == tokens.idx() + 1 || colon == tokens.idx() + 1 || colon == end - 1) {
      return null;
    }
    List<Token> condition =
        new ArrayList<>(tokens.list().subList(tokens.idx() + 1, colon < 0 ? end : colon));
    List<Token> message =
        (colon < 0) ? List.of() : new ArrayList<>(tokens.list().subList(colon + 1, end));
    List<Token> rewritten = new ArrayList<>();
    rewritten.addAll(lex("{ if (!" + FIELD + " && !(", start));
    rewritten.addAll(condition);
    rewritten.addAll(lex(")) throw new AssertionError(", start));
    rewritten.addAll(message);
    rewritten.addAll(lex("); }", start));
    List<Token> list = tokens.list();
    list.subList(tokens.idx(), end + 1).clear();
    list.addAll(tokens.idx(), rewritten);
    ctx.usesAssert = true;
    return ctx.stmtParser.parse(tokens);
  }

  // (positioned at the assert keyword, for error messages)
  private static List<Token> lex(String code, Token at) {
    List<Token> tokens = new ArrayList<>(new Lexer().getTokens(code).list());
    tokens.removeIf(token -> token instanceof EOF);
    for (Token token : tokens) {
      token.line = at.line;
      token.col = at.col;
    }
    return tokens;
  }
}
