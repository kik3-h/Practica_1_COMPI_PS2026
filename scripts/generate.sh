#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────
# generate.sh  –  Manually regenerate Lexer.java and parser.java
#                 (Gradle does this automatically; use only for debug)
# ─────────────────────────────────────────────────────────────────

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT="$SCRIPT_DIR/.."
LIBS="$PROJECT/libs"
OUT="$PROJECT/app/src/main/java/com/compiladores/practica1/generated"

mkdir -p "$OUT"

echo "▶  Running JFlex ..."
java -jar "$LIBS/jflex-full-1.9.1.jar" \
     --outdir "$OUT" \
     "$PROJECT/app/src/main/jflex/Lexer.flex"

echo "▶  Running CUP ..."
java -jar "$LIBS/java-cup-11b.jar" \
     -parser parser \
     -symbols sym \
     -destdir "$OUT" \
     "$PROJECT/app/src/main/cup/parser.cup"

echo "✅  Generated files written to $OUT"
ls "$OUT"
