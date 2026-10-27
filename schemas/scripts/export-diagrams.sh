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

mapfile -t FILES < <(find "${SRC}" -type f -name '*.puml' | sort)
if [[ ${#FILES[@]} -eq 0 ]]; then
  echo "No .puml files under ${SRC}" >&2
  exit 1
fi

EXPORTED=0
for f in "${FILES[@]}"; do
  rel="$(realpath --relative-to="${DIAGRAMS}" "$f")"
  path_after_src="${rel#src/}"
  out_dir="${OUT}/$(dirname "$path_after_src")"
  mkdir -p "${DIAGRAMS}/${out_dir}"

  "$JAVA" -DRELATIVE_INCLUDE=relative \
    -Dplantuml.include.path="${DIAGRAMS}/lib/C4:${DIAGRAMS}/lib:${DIAGRAMS}/src" \
    -jar "$JAR" -tsvg -o "$out_dir" \
    "$rel"
  EXPORTED=$((EXPORTED + 1))
done

echo "Exported ${EXPORTED} diagram(s) under ${OUT}/"
