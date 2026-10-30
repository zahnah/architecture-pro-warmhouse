FROM golang:1.26-bookworm AS plugins

ARG PROTOC_GEN_GO_VERSION=v1.36.12
ARG PROTOC_GEN_GO_GRPC_VERSION=v1.6.2
ARG GRPC_GATEWAY_VERSION=v2.33.0
ARG GRPC_JAVA_VERSION=1.85.1
# Same protoc as bundled in grpcio-tools below, so all languages get the same gencode version
ARG PROTOC_VERSION=35.1
ARG GOOGLEAPIS_COMMIT=3d42ca0ca56658a8ddae0571ebbe99faf044a440
ARG TARGETARCH

RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/*

RUN go install google.golang.org/protobuf/cmd/protoc-gen-go@${PROTOC_GEN_GO_VERSION} \
 && go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@${PROTOC_GEN_GO_GRPC_VERSION} \
 && go install github.com/grpc-ecosystem/grpc-gateway/v2/protoc-gen-openapiv2@${GRPC_GATEWAY_VERSION} \
 && mkdir -p /include/protoc-gen-openapiv2/options \
 && cp "$(go env GOMODCACHE)/github.com/grpc-ecosystem/grpc-gateway/v2@${GRPC_GATEWAY_VERSION}/protoc-gen-openapiv2/options/"*.proto \
      /include/protoc-gen-openapiv2/options/

RUN case "${TARGETARCH:-amd64}" in \
      amd64) arch=x86_64 ;; \
      arm64) arch=aarch_64 ;; \
      *) echo "unsupported arch ${TARGETARCH}" >&2; exit 1 ;; \
    esac \
 && curl -fsSL -o /go/bin/protoc-gen-grpc-java \
      "https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/${GRPC_JAVA_VERSION}/protoc-gen-grpc-java-${GRPC_JAVA_VERSION}-linux-${arch}.exe" \
 && chmod +x /go/bin/protoc-gen-grpc-java \
 && curl -fsSL -o /tmp/protoc.zip \
      "https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOC_VERSION}/protoc-${PROTOC_VERSION}-linux-${arch}.zip" \
 && unzip -q /tmp/protoc.zip -d /protoc \
 && chmod -R a+rX /protoc

ADD https://raw.githubusercontent.com/googleapis/googleapis/${GOOGLEAPIS_COMMIT}/google/api/annotations.proto \
    https://raw.githubusercontent.com/googleapis/googleapis/${GOOGLEAPIS_COMMIT}/google/api/http.proto \
    https://raw.githubusercontent.com/googleapis/googleapis/${GOOGLEAPIS_COMMIT}/google/api/field_behavior.proto \
    /include/google/api/

# ADD from URL creates 0600 files; the generator runs as the host user.
RUN chmod -R a+rX /include

# protoc generates Go and Java; Python goes through grpcio-tools, the only source of the grpc_python plugin.
# Their protobuf version sets the minimal runtime version for the generated Java and Python code.
FROM python:3.12-slim-bookworm

ARG GRPCIO_TOOLS_VERSION=1.85.0

RUN pip install --no-cache-dir grpcio-tools==${GRPCIO_TOOLS_VERSION}

COPY --from=plugins /go/bin/protoc-gen-go /go/bin/protoc-gen-go-grpc /go/bin/protoc-gen-grpc-java /usr/local/bin/
COPY --from=plugins /protoc/bin/protoc /usr/local/bin/
COPY --from=plugins /protoc/include /usr/local/include/protoc
COPY --from=plugins /include /usr/local/include/third_party
COPY generate-code.sh /usr/local/bin/generate-code

ENV PROTO_INCLUDE=/usr/local/include/third_party:/usr/local/include/protoc

WORKDIR /work
ENTRYPOINT ["generate-code"]
