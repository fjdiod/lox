package lox;

enum TokenType {
	// single-character
	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
	COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

	// comparison and logic
	BANG, BANG_EQUAL, EQUAL,
	EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESSER,
	LESS_EQUAL, QUESTION, COLON,

	// litterals
	IDENTIFIER, STRING, NUMBER,

	// keywords
	CLASS, FUN, IF, ELSE, OR, AND, FOR, WHILE,
	RETURN, NIL, PRINT, SUPER, TRUE, VAR, THIS, FALSE,

	EOF

}