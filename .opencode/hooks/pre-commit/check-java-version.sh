#!/bin/bash
# Verifica se Java 19 está configurado no pom.xml
if grep -q "<java.version>19</java.version>" backend/pom.xml; then
    echo "✅ Java 19 OK"
    exit 0
else
    echo "❌ ERRO: Java version deve ser 19 no pom.xml"
    echo "Corrija: <java.version>19</java.version>"
    exit 2  # Bloqueia o commit
fi
