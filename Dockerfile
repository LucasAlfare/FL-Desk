FROM node:20-alpine as node_stage
LABEL authors="Francisco Lucas"

FROM gradle:8.10.2-alpine as gradle_stage

# uses stuff from node_stage
COPY --from=node_stage /usr/local/bin/node /usr/local/bin/node
COPY --from=node_stage /usr/local/bin/npm /usr/local/bin/npm
COPY --from=node_stage /usr/local/lib/node_modules /usr/local/lib/node_modules
COPY --from=node_stage /opt /opt

# Variáveis de ambiente para npm funcionar bem
# node env
ENV NODE_PATH=/usr/local/lib/node_modules
ENV PATH=$PATH:/usr/local/bin

COPY . /app
WORKDIR /app
RUN cd /app
# gradle fullBuild is used to:
# - build without running tests;
# - build the vite/react frontend using node;
# also, daemons are not needed because gradle will be discarded
RUN gradle fullBuild --no-daemon

FROM eclipse-temurin:21-alpine as jdk_stage
EXPOSE 80
RUN mkdir /app
COPY --from=gradle_stage /app /app
ENTRYPOINT ["java", "-jar", "/app/build/libs/FL-Desk-1.0.jar"]