FROM golang:1.26-alpine AS plugins

ARG GRPC_GATEWAY_VERSION=v2.33.0
ARG GOOGLEAPIS_COMMIT=3d42ca0ca56658a8ddae0571ebbe99faf044a440

RUN go install github.com/grpc-ecosystem/grpc-gateway/v2/protoc-gen-openapiv2@${GRPC_GATEWAY_VERSION} \
 && mkdir -p /include/protoc-gen-openapiv2/options \
 && cp "$(go env GOMODCACHE)/github.com/grpc-ecosystem/grpc-gateway/v2@${GRPC_GATEWAY_VERSION}/protoc-gen-openapiv2/options/"*.proto \
      /include/protoc-gen-openapiv2/options/

ADD https://raw.githubusercontent.com/googleapis/googleapis/${GOOGLEAPIS_COMMIT}/google/api/annotations.proto \
    https://raw.githubusercontent.com/googleapis/googleapis/${GOOGLEAPIS_COMMIT}/google/api/http.proto \
    https://raw.githubusercontent.com/googleapis/googleapis/${GOOGLEAPIS_COMMIT}/google/api/field_behavior.proto \
    /include/google/api/

# ADD from URL creates 0600 files; the generator runs as the host user.
RUN chmod -R a+rX /include

FROM alpine:3.22

RUN apk add --no-cache bash protobuf protobuf-dev

COPY --from=plugins /go/bin/protoc-gen-openapiv2 /usr/local/bin/
COPY --from=plugins /include /usr/local/include/third_party
COPY generate-swagger.sh /usr/local/bin/generate-swagger

ENV PROTO_INCLUDE=/usr/local/include/third_party:/usr/include

WORKDIR /work
ENTRYPOINT ["generate-swagger"]
