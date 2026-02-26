package com.compiladores.practica1.generated;

import java_cup.runtime.*;
import com.compiladores.practica1.reports.ErrorReport;
import java.util.List;
import java.util.ArrayList;

%%

%class Lexer
%unicode
%cup
%line
%column
%public

%{
    private List<ErrorReport> errors = new ArrayList<>();
    private int currentLine() { return yyline + 1; }
    private int currentCol()  { return yycolumn + 1; }

    public List<ErrorReport> getErrors() { return errors; }

    private Symbol sym(int type) {
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }
    private Symbol sym(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
%}

%state CONFIG

LineTerminator = \r|\n|\r\n
WhiteSpace     = [ \t\f] | {LineTerminator}
Comment        = "#" [^\r\n]*
IntLit         = 0 | [1-9][0-9]*
DecLit         = {IntLit}"."[0-9]+
Identifier     = [a-zA-Z_][a-zA-Z0-9_]*
StringLit      = \"[^\"]*\"
HexColor       = H[0-9A-Fa-f]{6}

%%

<YYINITIAL, CONFIG> {
    {Comment}       { /* ignore */ }
    {WhiteSpace}    { /* ignore */ }
}

<YYINITIAL> {
    "%%%%"          { yybegin(CONFIG); return sym(sym.SEPARATOR); }
}


<YYINITIAL> {
    "INICIO"        { return sym(sym.INICIO); }
    "FIN"           { return sym(sym.FIN); }
    "VAR"           { return sym(sym.VAR); }
    "SI"            { return sym(sym.SI); }
    "ENTONCES"      { return sym(sym.ENTONCES); }
    "FINSI"         { return sym(sym.FINSI); }
    "MIENTRAS"      { return sym(sym.MIENTRAS); }
    "HACER"         { return sym(sym.HACER); }
    "FINMIENTRAS"   { return sym(sym.FINMIENTRAS); }
    "MOSTRAR"       { return sym(sym.MOSTRAR); }
    "LEER"          { return sym(sym.LEER); }

    "+"             { return sym(sym.PLUS); }
    "-"             { return sym(sym.MINUS); }
    "*"             { return sym(sym.TIMES); }
    "/"             { return sym(sym.DIVIDE); }

    "=="            { return sym(sym.EQ); }
    "!="            { return sym(sym.NEQ); }
    ">="            { return sym(sym.GTE); }
    "<="            { return sym(sym.LTE); }
    ">"             { return sym(sym.GT); }
    "<"             { return sym(sym.LT); }

    "&&"            { return sym(sym.AND); }
    "||"            { return sym(sym.OR); }
    "!"             { return sym(sym.NOT); }

    "="             { return sym(sym.ASSIGN); }
    "("             { return sym(sym.LPAREN); }
    ")"             { return sym(sym.RPAREN); }

    {DecLit}        { return sym(sym.DECIMAL,  Double.parseDouble(yytext())); }
    {IntLit}        { return sym(sym.INTEGER,  Integer.parseInt(yytext())); }
    {StringLit}     { return sym(sym.STRING,   yytext().substring(1, yytext().length()-1)); }
    {Identifier}    { return sym(sym.ID,       yytext()); }

    [^]             {
                        errors.add(new ErrorReport(
                            yytext(), currentLine(), currentCol(),
                            "Léxico", "Símbolo no existe en el lenguaje"));
                    }
}


<CONFIG> {
    /* configuracion de colores segun instrucciones */
    "%COLOR_TEXTO_SI"           { return sym(sym.CFG_COLOR_TEXTO_SI); }
    "%COLOR_SI"                 { return sym(sym.CFG_COLOR_SI); }
    "%FIGURA_SI"                { return sym(sym.CFG_FIGURA_SI); }
    "%LETRA_SIZE_SI"            { return sym(sym.CFG_LETRA_SIZE_SI); }
    "%LETRA_SI"                 { return sym(sym.CFG_LETRA_SI); }
    "%COLOR_TEXTO_MIENTRAS"     { return sym(sym.CFG_COLOR_TEXTO_MIENTRAS); }
    "%COLOR_MIENTRAS"           { return sym(sym.CFG_COLOR_MIENTRAS); }
    "%FIGURA_MIENTRAS"          { return sym(sym.CFG_FIGURA_MIENTRAS); }
    "%LETRA_SIZE_MIENTRAS"      { return sym(sym.CFG_LETRA_SIZE_MIENTRAS); }
    "%LETRA_MIENTRAS"           { return sym(sym.CFG_LETRA_MIENTRAS); }
    "%COLOR_TEXTO_BLOQUE"       { return sym(sym.CFG_COLOR_TEXTO_BLOQUE); }
    "%COLOR_BLOQUE"             { return sym(sym.CFG_COLOR_BLOQUE); }
    "%FIGURA_BLOQUE"            { return sym(sym.CFG_FIGURA_BLOQUE); }
    "%LETRA_SIZE_BLOQUE"        { return sym(sym.CFG_LETRA_SIZE_BLOQUE); }
    "%LETRA_BLOQUE"             { return sym(sym.CFG_LETRA_BLOQUE); }
    "%DEFAULT"                  { return sym(sym.CFG_DEFAULT); }

    /* nombre de figuras */
    "RECTANGULO_REDONDEADO"     { return sym(sym.FIGURA_NAME, "RECTANGULO_REDONDEADO"); }
    "PARALELOGRAMO"             { return sym(sym.FIGURA_NAME, "PARALELOGRAMO"); }
    "RECTANGULO"                { return sym(sym.FIGURA_NAME, "RECTANGULO"); }
    "CIRCULO"                   { return sym(sym.FIGURA_NAME, "CIRCULO"); }
    "ELIPSE"                    { return sym(sym.FIGURA_NAME, "ELIPSE"); }
    "ROMBO"                     { return sym(sym.FIGURA_NAME, "ROMBO"); }

    "TIMES_NEW_ROMAN"           { return sym(sym.LETRA_NAME, "TIMES_NEW_ROMAN"); }
    "COMIC_SANS"                { return sym(sym.LETRA_NAME, "COMIC_SANS"); }
    "VERDANA"                   { return sym(sym.LETRA_NAME, "VERDANA"); }
    "ARIAL"                     { return sym(sym.LETRA_NAME, "ARIAL"); }

    /* Color / numero del color en ansii */
    {HexColor}      { return sym(sym.HEX_COLOR, yytext().substring(1)); }
    {DecLit}        { return sym(sym.DECIMAL,   Double.parseDouble(yytext())); }
    {IntLit}        { return sym(sym.INTEGER,   Integer.parseInt(yytext())); }

    /* Operadores / como lo llame */
    "+"             { return sym(sym.PLUS); }
    "-"             { return sym(sym.MINUS); }
    "*"             { return sym(sym.TIMES); }
    "/"             { return sym(sym.DIVIDE); }
    "="             { return sym(sym.ASSIGN); }
    "|"             { return sym(sym.PIPE); }
    ","             { return sym(sym.COMMA); }
    "("             { return sym(sym.LPAREN); }
    ")"             { return sym(sym.RPAREN); }

    [^]             {
                        errors.add(new ErrorReport(
                            yytext(), currentLine(), currentCol(),
                            "Léxico", "Símbolo no existe en el lenguaje"));
                    }
}
