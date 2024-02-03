grammar WhereStatement;

// Define tokens
WHERE       : 'WHERE';
AND         : 'AND';
OR          : 'OR';
LPAREN      : '(';
RPAREN      : ')';
LT          : '<';
GT          : '>';
LTE         : '<=';
GTE         : '>=';
IN          : 'IN';
EQUALS      : '=';
NOT_EQUALS  : '!=';
COLON       : ':';
IDENTIFIER  : [a-zA-Z]+;
PARAMETER   : COLON (IDENTIFIER);
COMMA       : ',';

// Define the entry point for parsing
start       : WHERE expression EOF        #WhereExpression
            ;

// Define the expression grammar
expression   : PARAMETER                               #ParameterExpression
            | LPAREN expression RPAREN                 #ParenExpession
            | expression AND expression                #AndPredicate
            | expression OR expression                 #OrPredicate
            | IDENTIFIER (LT|GT|LTE|GTE) PARAMETER     #ComparingPredicate
            | IDENTIFIER EQUALS PARAMETER              #EqualsPredicate
            | IDENTIFIER IN PARAMETER                  #InPredicate
            ;


// Skip whitespace
WS          : [ \t\r\n]+ -> skip;