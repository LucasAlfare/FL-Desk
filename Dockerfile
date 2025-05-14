FROM node:20-alpine
LABEL authors="Francisco Lucas"

# Instala Java 21 e Gradle
RUN apk add --no-cache openjdk21 gradle

# Define variáveis de ambiente úteis
ENV NODE_PATH=/usr/local/lib/node_modules
ENV PATH=$PATH:/usr/local/bin

# Copia todos os arquivos do projeto para o contêiner
COPY . /app
WORKDIR /app

# Roda o build completo (frontend + backend)
RUN gradle fullBuild --no-daemon

# Segunda etapa: imagem final só com o JDK e o .jar gerado
FROM eclipse-temurin:21-alpine

# Expõe a porta que sua aplicação Java escuta
EXPOSE 3000

# Cria diretório da aplicação
RUN mkdir /app

# Copia os arquivos gerados na etapa anterior
COPY --from=0 /app /app

# Comando de entrada para rodar o JAR
ENTRYPOINT ["java", "-jar", "/app/build/libs/FL-Desk-1.0.jar"]