#!/usr/bin/env bash
# Compiles proto/**/*.proto; files with a service become swagger/<file>.swagger.yaml.
# Run from schemas/api; PROTO_INCLUDE lists directories with google/api, protoc-gen-openapiv2 and google/protobuf.
set -euo pipefail

PROTO_DIR=proto
OUT_DIR=swagger

include_flags=()
IFS=':' read -ra include_dirs <<< "${PROTO_INCLUDE:?PROTO_INCLUDE is not set}"
for dir in "${include_dirs[@]}"; do
  include_flags+=(-I "$dir")
done

mapfile -t protos < <(find "$PROTO_DIR" -name '*.proto' | sort)
if [[ ${#protos[@]} -eq 0 ]]; then
  echo "No .proto files under ${PROTO_DIR}" >&2
  exit 1
fi

# protoc-gen-openapiv2 requires a Go import path even for services written in other languages.
plugin_opts="output_format=yaml,omit_enum_default_value=true"
for proto in "${protos[@]}"; do
  rel="${proto#"$PROTO_DIR"/}"
  plugin_opts+=",M${rel}=warmhouse.example/gen/$(dirname "$rel")"
done

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

# Every contract must compile, including Kafka event messages that have no Swagger counterpart.
protoc -I "$PROTO_DIR" "${include_flags[@]}" \
  --descriptor_set_out="$tmp/all.pb" \
  "${protos[@]}"

mapfile -t service_protos < <(grep -l -E '^service ' "${protos[@]}")

protoc -I "$PROTO_DIR" "${include_flags[@]}" \
  --openapiv2_out="$tmp" \
  --openapiv2_opt="$plugin_opts" \
  "${service_protos[@]}"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"
find "$tmp" -name '*.swagger.yaml' -exec cp {} "$OUT_DIR"/ \;

echo "Compiled ${#protos[@]} proto file(s); generated $(find "$OUT_DIR" -name '*.swagger.yaml' | wc -l) swagger file(s) under ${OUT_DIR}/"
