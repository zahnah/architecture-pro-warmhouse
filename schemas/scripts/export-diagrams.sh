#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DIAGRAMS="${REPO_ROOT}/schemas/diagrams"
JAVA="${PLANTUML_JAVA:-/usr/bin/java}"
JAR="${PLANTUML_JAR:-${REPO_ROOT}/tools/plantuml.jar}"

if [[ ! -f "$JAR" ]]; then
  JAR="$(find "${HOME}/.vscode-server/extensions" "${HOME}/.cursor-server/extensions" \
    -path '*/jebbs.plantuml-*/plantuml.jar' 2>/dev/null | sort -V | tail -1 || true)"
fi
if [[ -z "$JAR" || ! -f "$JAR" ]]; then
  echo "Could not find plantuml.jar. Install jebbs.plantuml or set PLANTUML_JAR." >&2
  exit 1
fi

SRC="${DIAGRAMS}/src"
OUT="${DIAGRAMS}/out"
mkdir -p "$OUT"
cd "${DIAGRAMS}"

mapfile -t FILES < <(find "${SRC}" -maxdepth 1 -type f -name '*.puml' | sort)
if [[ ${#FILES[@]} -eq 0 ]]; then
  echo "No .puml files in ${SRC}" >&2
  exit 1
fi

# paths relative to schemas/diagrams (cwd)
REL_FILES=()
for f in "${FILES[@]}"; do
  REL_FILES+=("src/$(basename "$f")")
done

"$JAVA" -DRELATIVE_INCLUDE=relative \
  -Dplantuml.include.path="${DIAGRAMS}/lib/C4:${DIAGRAMS}/lib:${DIAGRAMS}/src" \
  -jar "$JAR" -tsvg -o "$OUT" \
  "${REL_FILES[@]}"

echo "Exported ${#REL_FILES[@]} diagram(s) to ${OUT}/"
