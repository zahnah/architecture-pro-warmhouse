FROM node:22-alpine AS assets

ARG ASYNCAPI_REACT_VERSION=3.2.2

WORKDIR /assets
RUN npm pack -s "@asyncapi/react-component@${ASYNCAPI_REACT_VERSION}" \
 && tar xzf asyncapi-react-component-*.tgz \
 && mkdir out \
 && cp package/browser/standalone/index.js out/asyncapi-ui.js \
 && cp package/styles/default.min.css out/asyncapi-ui.css

FROM nginx:1.29-alpine

COPY --from=assets /assets/out/ /usr/share/nginx/html/
COPY viewer/asyncapi/index.html /usr/share/nginx/html/
COPY viewer/asyncapi/default.conf.template /etc/nginx/templates/
COPY asyncapi/ /usr/share/nginx/html/specs/
