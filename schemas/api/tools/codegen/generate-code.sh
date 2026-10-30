#!/usr/bin/env bash
# Generates gRPC and Protobuf code for the services in apps/ from schemas/api/proto.
# Run from the repository root; PROTO_INCLUDE lists directories with google/api and protoc-gen-openapiv2.
set -euo pipefail

PROTO_DIR=schemas/api/proto

include_flags=(-I "$PROTO_DIR")
IFS=':' read -ra include_dirs <<< "${PROTO_INCLUDE:?PROTO_INCLUDE is not set}"
for dir in "${include_dirs[@]}"; do
  include_flags+=(-I "$dir")
done

run_protoc() {
  protoc "${include_flags[@]}" "$@"
}

run_python_protoc() {
  python -m grpc_tools.protoc "${include_flags[@]}" "$@"
}

reset_dir() {
  rm -rf "$1"
  mkdir -p "$1"
}

EVENTS=warmhouse/events/v1/device_events.proto
HEATING=(warmhouse/heating/v1/heating_service.proto warmhouse/heating/v1/heating_events.proto)
TELEMETRY=warmhouse/telemetry/v1/telemetry_service.proto
# The service contracts import them; Go and Java take google/api from their gRPC libraries
OPENAPI_OPTIONS=(protoc-gen-openapiv2/options/annotations.proto protoc-gen-openapiv2/options/openapiv2.proto)

# Go: smart_home (Kafka publisher, Heating and Telemetry clients); e2e imports the same packages
go_module=smarthome
go_out=apps/smart_home
go_protos=("$EVENTS" "${HEATING[@]}" "$TELEMETRY")
go_opts=()
for proto in "${go_protos[@]}"; do
  dir="$(dirname "$proto")"
  name="$(basename "$(dirname "$dir")")v1"
  go_opts+=("M${proto}=${go_module}/gen/${dir};${name}")
done
reset_dir "$go_out/gen"
run_protoc \
  --go_out="$go_out" --go_opt=module="$go_module" \
  --go-grpc_out="$go_out" --go-grpc_opt=module="$go_module" \
  $(printf -- '--go_opt=%s ' "${go_opts[@]}") \
  $(printf -- '--go-grpc_opt=%s ' "${go_opts[@]}") \
  "${go_protos[@]}"

# Java: heating-service
java_out=apps/heating-service/src/generated/java
reset_dir "$java_out"
run_protoc \
  --java_out="$java_out" \
  --grpc-java_out="$java_out" \
  "$EVENTS" "${HEATING[@]}" "${OPENAPI_OPTIONS[@]}"

# Python: telemetry-service; google.api comes from googleapis-common-protos
python_out=apps/telemetry-service/gen
reset_dir "$python_out"
run_python_protoc \
  --python_out="$python_out" \
  --pyi_out="$python_out" \
  --grpc_python_out="$python_out" \
  "$EVENTS" "$TELEMETRY" "${OPENAPI_OPTIONS[@]}"

echo "Generated Go code under ${go_out}/gen, Java under ${java_out}, Python under ${python_out}"
